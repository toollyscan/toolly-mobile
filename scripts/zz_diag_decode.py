import base64, os, sys
env_var = sys.argv[1]
path = sys.argv[2]
raw = ''.join(os.environ[env_var].split())
with open(path, 'wb') as f:
    f.write(base64.b64decode(raw))
