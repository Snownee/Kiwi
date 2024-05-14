import os
import shutil
import sys
import yaml
import tempfile

def main():
    if len(sys.argv) < 2:
        pack('kswitch')
        pack('ksit')
        pack('kiwicustomization')
        pack('persistentcreativeinventory')
        pack('fastscroll')
        return
    pack(sys.argv[1])

def pack(dir):
    curPath = os.getcwd()
    os.chdir(dir)
    config = yaml.safe_load(open('config.yaml', 'r'))
    tempdir = tempfile.mkdtemp()

    MANIFEST_MF = \
        'Manifest-Version: 1.0\r\n' \
        '\r\n'
    os.makedirs(os.path.join(tempdir, 'META-INF'), exist_ok=True)
    with open(os.path.join(tempdir, 'META-INF', 'MANIFEST.MF'), 'w') as f:
        f.write(MANIFEST_MF)

    for root, dirs, files in os.walk('.'):
        for file in files:
            if file == 'config.yaml':
                continue
            filepath = os.path.relpath(os.path.join(root, file), '.').replace('\\', '/')
            print('Adding %s' % filepath)
            os.makedirs(os.path.join(tempdir, os.path.dirname(filepath)), exist_ok=True)
            if filepath == 'META-INF/mods.toml' or filepath == 'META-INF/neoforge.mods.toml' or filepath == 'fabric.mod.json':
                text = open(os.path.join(root, file), 'r').read().replace('${version}', config['version'])
                with open(os.path.join(tempdir, filepath), 'w') as f:
                    f.write(text)
            else:
                shutil.copyfile(os.path.join(root, file), os.path.join(tempdir, filepath))
    os.chdir(tempdir)
    path = os.path.join(curPath, '%s-%s.jar' % (config['archiveName'], config['version']))
    os.system('jar -c -f %s .' % path)
    os.chdir(curPath)
    shutil.rmtree(tempdir)

if __name__ == '__main__':
    main()