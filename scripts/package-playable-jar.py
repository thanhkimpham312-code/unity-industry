#!/usr/bin/env python3
"""Package ONE verified-by-structure runtime JAR for GitHub Actions.

This makes no assertion that the JAR is free of malware. It checks the filename,
ZIP integrity, mod manifest and common unexpected executable payloads, and
provides a SHA-256 digest for post-download integrity checks.
"""

from __future__ import annotations

import hashlib
import os
from pathlib import Path
import shutil
import sys
import zipfile

ROOT = Path(__file__).resolve().parent.parent
PROPS = ROOT / 'gradle.properties'
LIBS = ROOT / 'build' / 'libs'
OUTPUT = ROOT / 'build' / 'checked-release'


def props(path: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    for line in path.read_text(encoding='utf-8').splitlines():
        line = line.strip()
        if not line or line.startswith('#') or '=' not in line:
            continue
        key, value = line.split('=', 1)
        result[key.strip()] = value.strip()
    return result


def fail(reason: str) -> None:
    raise SystemExit('FAILED RELEASE CHECK: ' + reason)


p = props(PROPS)
for key in ('archives_base_name', 'mod_version'):
    if not p.get(key):
        fail(f'gradle.properties missing {key}')

# Exact name avoids accidentally choosing a -sources.jar, -dev classifier,
# or another dependency. A version such as 0.4.8-dev is legitimate.
expected = f"{p['archives_base_name']}-{p['mod_version']}.jar"
jar_path = LIBS / expected
if not jar_path.is_file():
    candidates = ', '.join(x.name for x in LIBS.glob('*.jar')) if LIBS.exists() else '<none>'
    fail(f'expected playable JAR {expected!r} not found; available: {candidates}')

prohibited_suffixes = ('.exe', '.dll', '.dylib', '.so', '.bat', '.cmd', '.ps1')
with zipfile.ZipFile(jar_path, 'r') as bundle:
    bad_member = bundle.testzip()
    if bad_member is not None:
        fail(f'corrupted JAR entry: {bad_member}')
    names = bundle.namelist()
    if 'fabric.mod.json' not in names:
        fail('missing fabric.mod.json; not a Fabric mod JAR')
    for member in names:
        path = Path(member)
        if path.is_absolute() or '..' in path.parts:
            fail(f'unsafe path inside JAR: {member}')
        lowered = member.lower()
        if lowered.endswith(prohibited_suffixes):
            fail(f'unexpected native/executable file inside JAR: {member}')

OUTPUT.mkdir(parents=True, exist_ok=True)
for old_file in OUTPUT.iterdir():
    if old_file.is_file():
        old_file.unlink()
release_jar = OUTPUT / expected
shutil.copyfile(jar_path, release_jar)
digest = hashlib.sha256(release_jar.read_bytes()).hexdigest()
(OUTPUT / 'SHA256SUMS.txt').write_text(f'{digest}  {expected}\n', encoding='utf-8')
(OUTPUT / 'BUILD_INFO.txt').write_text(
    'Unity Industry build from source on GitHub Actions\n'
    f"Mod file: {expected}\n"
    f"Commit: {os.environ.get('BUILD_COMMIT', 'unknown')}\n"
    f"Branch: {os.environ.get('BUILD_REF', 'unknown')}\n"
    f"SHA-256: {digest}\n"
    'This is NOT a malware scan or security certification.\n'
    'If your browser or antivirus calls this dangerous, do not run it.\n',
    encoding='utf-8',
)
print(f'Playable JAR: {expected}')
print(f'SHA-256: {digest}')
print(f'Output: {OUTPUT}')
