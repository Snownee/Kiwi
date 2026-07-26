import os
import shutil

def copytree(source, target, symlinks=False, ignore=None):
    if os.path.exists(target):
        shutil.rmtree(target)
        print(f'Deleted: {target}')

    shutil.copytree(source, target)
    print(f'Copied: {source} -> {target}')

def main():
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    target = os.path.join('kswitch', 'assets', 'kswitch', 'kiwi')
    source = os.path.join('..', 'src', 'main', 'resources', 'assets', 'kswitch', 'kiwi')
    copytree(source, target)
    target = os.path.join('kswitch', 'data', 'kswitch')
    source = os.path.join('..', 'src', 'main', 'resources', 'data', 'kswitch')
    copytree(source, target)

if __name__ == '__main__':
    main()
