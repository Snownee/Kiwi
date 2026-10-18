#!/usr/bin/env python3
"""Run production Kiwi jars in an isolated client using an existing vanilla launch audit."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import time
import zipfile


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jar', required=True, type=Path)
    parser.add_argument('--mods', required=True, type=Path, help='Only production dependency jars; optionally Snow Real Magic')
    parser.add_argument('--launch-audit', required=True, type=Path, help='JSON containing a working vanilla 26.3 command array')
    parser.add_argument('--java', type=Path, default=Path('/tmp/sso-jdk25/bin/java'))
    parser.add_argument('--cache', type=Path, default=Path.home() / '.gradle/caches')
    parser.add_argument('--timeout', type=int, default=180)
    parser.add_argument('--server', help='Optional disposable localhost server with the SRM QA snow grid')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    run = root / 'build/qa-client' / (time.strftime('%Y%m%d-%H%M%S') + '-' + str(os.getpid()))
    for child in ['mods', 'classes', 'compile-libraries']:
        (run / child).mkdir(parents=True, exist_ok=False)
    command = json.loads(args.launch_audit.read_text())['command']
    command = [value for value in command if not value.startswith('-Datlas.')]
    if any(value.startswith('-javaagent') for value in command):
        raise ValueError('The vanilla launch audit must not contain a Java agent')
    command[0] = str(args.java)
    command.insert(1, '-XX:ActiveProcessorCount=4')
    command.insert(1, '-Duser.home=' + str(run / 'home'))
    (run / 'home').mkdir()
    for prefix, folder in [('java.library.path', 'java'), ('jna.tmpdir', 'jna'),
                           ('org.lwjgl.system.SharedLibraryExtractPath', 'lwjgl'), ('io.netty.native.workdir', 'netty')]:
        command = [('-D' + prefix + '=' + str(run / 'natives' / folder))
                   if value.startswith('-D' + prefix + '=') else value for value in command]
    for flag in ['--quickPlayMultiplayer', '--quickPlaySingleplayer', '--quickPlayRealms']:
        if flag in command:
            index = command.index(flag)
            del command[index:index + 2]
    if args.server:
        if not args.server.startswith('127.0.0.1:'):
            raise ValueError('--server must identify a disposable localhost server')
        command.extend(['--quickPlayMultiplayer', args.server])
        command.insert(1, '-Dkiwi.qa.world=true')
    for flag, value in [('--gameDir', str(run)), ('--username', 'KiwiClientQA')]:
        command[command.index(flag) + 1] = value
    if command[command.index('--version') + 1] != '26.3':
        raise ValueError('The launch audit must use Minecraft 26.3')
    command[command.index('net.minecraft.client.main.Main')] = 'net.fabricmc.loader.impl.launch.knot.KnotClient'
    modules = args.cache / 'modules-2/files-2.1'

    def artifact(group, name, version):
        paths = sorted((modules / group / name / version).glob('*/' + name + '-' + version + '.jar'))
        if not paths or len({digest(p) for p in paths}) != 1:
            raise RuntimeError('Missing or ambiguous artifact: ' + ':'.join([group, name, version]))
        return paths[0]

    loader = artifact('net.fabricmc', 'fabric-loader', '0.19.5')
    with zipfile.ZipFile(loader) as archive:
        metadata = json.loads(archive.read('fabric-installer.json'))
    libraries = [loader]
    for entry in metadata['libraries']['common'] + metadata['libraries']['client']:
        path = artifact(*entry['name'].split(':'))
        if hashlib.sha1(path.read_bytes()).hexdigest() != entry['sha1']:
            raise RuntimeError('Fabric dependency checksum mismatch: ' + str(path))
        libraries.append(path)
    classpath = list(dict.fromkeys(map(str, libraries))) + command[command.index('-cp') + 1].split(os.pathsep)
    for path in classpath:
        if not Path(path).is_file() or Path(path).suffix != '.jar':
            raise ValueError('Client classpath must contain only existing JARs: ' + path)
    command[command.index('-cp') + 1] = os.pathsep.join(classpath)
    mod_audit = []
    ids = set()
    compile_classpath = list(classpath)

    def add_compile_jar(path):
        compile_classpath.append(str(path.resolve()))
        with zipfile.ZipFile(path) as archive:
            for name in archive.namelist():
                if name.endswith('.jar'):
                    data = archive.read(name)
                    nested = run / 'compile-libraries' / (hashlib.sha256(data).hexdigest()[:16] + '-' + Path(name).name)
                    nested.write_bytes(data)
                    add_compile_jar(nested)

    for source in [args.jar, *sorted(args.mods.glob('*.jar'))]:
        with zipfile.ZipFile(source) as archive:
            metadata = json.loads(archive.read('fabric.mod.json'))
        if metadata['id'] in ids:
            raise ValueError('Duplicate mod ID: ' + metadata['id'])
        ids.add(metadata['id'])
        target = run / 'mods' / source.name
        shutil.copy2(source, target)
        mod_audit.append({'id': metadata['id'], 'version': metadata['version'], 'source': str(source.resolve()),
                          'file': target.name, 'sha256': digest(target)})
        add_compile_jar(target)
    fixture = root / 'qa/fixture/KiwiClientChecks.java'
    subprocess.run([str(args.java.with_name('javac')), '--release', '25', '-proc:none', '-cp',
                    os.pathsep.join(compile_classpath), '-d', str(run / 'classes'), str(fixture)], check=True)
    with zipfile.ZipFile(run / 'mods/kiwi-client-qa.jar', 'w', zipfile.ZIP_DEFLATED) as archive:
        archive.writestr('fabric.mod.json', json.dumps({'schemaVersion': 1, 'id': 'kiwi_client_qa',
            'version': '1', 'environment': 'client', 'entrypoints': {'client': ['kiwi.qa.KiwiClientChecks']},
            'depends': {'kiwi': '*', 'cloth-config': '*', 'fabric-api': '*'}}))
        for path in (run / 'classes').rglob('*.class'):
            archive.write(path, path.relative_to(run / 'classes'))
    (run / 'options.txt').write_text('graphicsMode:0\nrenderDistance:2\nmaxFps:30\nmaxFpsInactive:30\nsoundCategory_master:0.0\n')
    (run / 'launch-audit.json').write_text(json.dumps({'command': command, 'mods': mod_audit,
        'fixture_sha256': digest(run / 'mods/kiwi-client-qa.jar')}, indent=2) + '\n')
    env = os.environ.copy()
    env.update(SDL_VIDEODRIVER=env.get('SDL_VIDEODRIVER', 'wayland'), LP_NUM_THREADS='4')
    print('Run:', run, flush=True)
    timed_out = False
    with (run / 'console.log').open('w') as log:
        process = subprocess.Popen(command, cwd=run, env=env, stdout=log, stderr=subprocess.STDOUT)
        try:
            code = process.wait(timeout=args.timeout)
        except subprocess.TimeoutExpired:
            timed_out = True
            process.terminate()
            try:
                code = process.wait(timeout=15)
            except subprocess.TimeoutExpired:
                process.kill()
                code = process.wait()
    output = (run / 'console.log').read_text(errors='replace')
    allowed = ["Couldn't connect to realms", 'Failed to fetch Realms feature flags']
    if 'InvalidCredentialsException: Status: 401' in output:
        allowed.append('Failed to fetch user properties')
    if 'Narrator$InitializeException: Failed to load library flite' in output:
        allowed.append('Error while loading the narrator')
    error_lines = [line for line in output.splitlines() if '/ERROR]' in line or '/FATAL]' in line]
    environment_diagnostics = [line for line in error_lines if any(message in line for message in allowed)]
    errors = [line for line in error_lines if line not in environment_diagnostics]
    result = {'passed': code == 0 and 'KIWI_CLIENT_QA_PASS' in output and not errors and not timed_out,
              'exit_code': code, 'timed_out': timed_out, 'errors': errors,
              'environment_diagnostics': environment_diagnostics}
    (run / 'result.json').write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result, indent=2))
    return 0 if result['passed'] else 1


if __name__ == '__main__':
    raise SystemExit(main())
