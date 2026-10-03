#!/usr/bin/env python3
"""Generate the exact updater contract for an immutable tagged release."""
import hashlib,json,os,pathlib,re,sys
apk=pathlib.Path(sys.argv[1]);out=pathlib.Path(sys.argv[2])
repo=os.environ['REPOSITORY'];tag=os.environ['RELEASE_TAG'];code=int(os.environ['VERSION_CODE'])
if not re.fullmatch(r'[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+',repo) or not re.fullmatch(r'v\d+\.\d+\.\d+',tag) or code<1:raise SystemExit('Invalid repository, tag or version code')
manifest={'packageName':'ca.loonandhearth.staff','versionCode':code,'versionName':tag,'size':apk.stat().st_size,'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'url':f'https://github.com/{repo}/releases/download/{tag}/loon-hearth-staff.apk'}
out.write_text(json.dumps(manifest,indent=2)+'\n')
