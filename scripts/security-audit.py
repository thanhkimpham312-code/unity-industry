#!/usr/bin/env python3
"""Unity Industry source/JAR *triage* checks, not a malware certification.

Does not change/bypass browser or antivirus protection.
"""
from __future__ import annotations
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import sys
import zipfile

ROOT = Path(__file__).resolve().parent.parent
REPORT_DIR = ROOT / 'build' / 'security-review'
BLOCKED_EXT = {'.exe', '.dll', '.dylib', '.so', '.bat', '.cmd', '.ps1', '.vbs', '.scr'}
SOURCE_CHECKS = {
    'Execute system commands': re.compile(r'\b(?:new\s+ProcessBuilder\s*\(|Runtime\s*\.\s*getRuntime\s*\(\s*\)\s*\.\s*exec\s*\()', re.I),
    'Load native binaries': re.compile(r'\bSystem\s*\.\s*load(?:Library)?\s*\(', re.I),
    'Direct sockets / HTTP clients': re.compile(r'\b(?:java\.net\.(?:Socket|URL|HttpURLConnection)|java\.net\.http\.HttpClient)\b'),
    'Dynamic class loading': re.compile(r'\b(?:Class\s*\.\s*forName|java\.lang\.reflect)\b'),
}


def sha256_file(p):
    h = hashlib.sha256()
    with p.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def write_report(mode, lines, errors):
    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    path = REPORT_DIR / f'{mode}-security-audit.txt'
    final_lines = [
        'UNITY INDUSTRY — DIAGNOSTIC SECURITY REVIEW',
        f'Mode: {mode}',
        f'Status: {"FAIL / REVIEW REQUIRED" if errors else "NO ISSUES FOUND IN THESE LIMITED CHECKS"}',
        'WARNING: Not a malware scan, not proof of safety; does not suppress security warnings.',
        '',
    ] + lines + (['', 'Issues requiring investigation:'] + ['- '+e for e in errors] if errors else [])
    result = '\n'.join(final_lines) + '\n'
    path.write_text(result, encoding='utf-8')
    print(result)
    summary = os.getenv('GITHUB_STEP_SUMMARY')
    if summary:
        with open(summary, 'a', encoding='utf-8') as f:
            f.write(f'### Unity Industry {mode} security triage\n')
            f.write(f'- Result: **{"REVIEW REQUIRED" if errors else "No issues in limited checks"}**\n')
            f.write('- This is **not** an antivirus verdict; do not bypass browser/Defender blocks.\n')
            for e in errors:
                f.write(f'- Flag: `{e}`\n')
            f.write('\n')
    return bool(errors)


def audit_source():
    lines, errors = [], []
    roots = [ROOT / 'src', ROOT / 'tools', ROOT / 'scripts']
    sources = []
    for root in roots:
        if root.is_dir():
            for p in root.rglob('*'):
                if p.is_file() and not any(q in ('build', '.git') for q in p.relative_to(root).parts):
                    sources.append(p)
    for f in (ROOT / 'build.gradle', ROOT / 'settings.gradle', ROOT / 'gradle.properties'):
        if f.is_file():
            sources.append(f)
    java_files = 0
    for p in sorted(sources):
        rel = p.relative_to(ROOT).as_posix()
        if p.suffix.lower() in BLOCKED_EXT:
            errors.append(f'Unexpected executable/native file in source: {rel}')
        if p.suffix.lower() == '.jar':
            errors.append(f'Bundled JAR in project sources: {rel}')
        if p.suffix != '.java':
            continue
        java_files += 1
        text = p.read_text(encoding='utf-8', errors='replace')
        for label, pattern in SOURCE_CHECKS.items():
            for n, line in enumerate(text.splitlines(), 1):
                if pattern.search(line):
                    errors.append(f'{label}: {rel}:{n}')
    for name in ('build.gradle', 'settings.gradle', 'gradle.properties'):
        p = ROOT / name
        if not p.is_file():
            errors.append(f'Missing build config: {name}')
    lines.append(f'Files inspected: {len(sources)}; Java sources scanned: {java_files}')
    for name in ('build.gradle', 'settings.gradle', 'gradle.properties'):
        p = ROOT / name
        if p.is_file():
            lines.append(f'SHA256 {name}: {sha256_file(p)}')
    build = ROOT / 'build.gradle'
    if build.is_file():
        s = build.read_text(encoding='utf-8', errors='replace')
        deps = [line.strip() for line in s.splitlines() if re.search(r'\b(?:modImplementation|implementation|modApi|include)\b', line) and not line.strip().startswith('//')]
        lines.append('Declared dependency lines (review exact versions via gradle.properties):')
        lines.extend(f'  {x}' for x in deps[:40])
        if len(deps) > 40:
            lines.append('  ... further dependencies omitted')
    return write_report('source', lines, errors)


def audit_jar(filename):
    p = (ROOT / filename).resolve()
    lines, errors = [], []
    if not p.is_file():
        errors.append(f'JAR missing: {filename}')
        return write_report('jar', lines, errors)
    lines.extend([f'JAR filename: {p.name}', f'JAR size bytes: {p.stat().st_size}', f'JAR SHA256: {sha256_file(p)}'])
    try:
        with zipfile.ZipFile(p) as z:
            bad = z.testzip()
            if bad: errors.append(f'Corrupt ZIP member: {bad}')
            names = z.namelist()
            if 'fabric.mod.json' not in names:
                errors.append('Not a Fabric mod: fabric.mod.json missing')
            else:
                try:
                    manifest = json.loads(z.read('fabric.mod.json'))
                    lines.append(f"Fabric mod id: {manifest.get('id', '<missing>')}")
                    lines.append(f"Fabric mod version: {manifest.get('version', '<missing>')}")
                    if manifest.get('id') != 'unityindustry':
                        errors.append('Unexpected mod ID in fabric.mod.json')
                except (json.JSONDecodeError, UnicodeDecodeError) as exc:
                    errors.append(f'Invalid Fabric mod manifest: {exc}')
            counted = 0
            for item in z.infolist():
                name = item.filename
                parts = Path(name).parts
                if name.startswith('/') or '..' in parts or '\\' in name:
                    errors.append(f'Unsafe archive member path: {name}')
                if Path(name).suffix.lower() in BLOCKED_EXT:
                    errors.append(f'Unexpected executable or native member: {name}')
                if Path(name).suffix.lower() == '.jar':
                    errors.append(f'Nested JAR requires manual dependency audit: {name}')
                if item.file_size > 50 * 1024 * 1024:
                    errors.append(f'Unexpected large JAR entry: {name} ({item.file_size} bytes)')
                if name.endswith('.class'):
                    counted += 1
            lines.extend([f'Archive entries: {len(names)}', f'Compiled class files: {counted}'])
            lines.append('Top-level JAR directories: '+', '.join(sorted({x.split('/')[0] for x in names})[:20]))
    except (zipfile.BadZipFile, OSError) as exc:
        errors.append(f'Unreadable JAR: {exc}')
    return write_report('jar', lines, errors)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--source', action='store_true')
    parser.add_argument('--jar', type=str, help='relative or absolute JAR path')
    args = parser.parse_args()
    if args.source == bool(args.jar):
        parser.error('Supply exactly one of --source or --jar PATH')
    problem = audit_source() if args.source else audit_jar(args.jar)
    sys.exit(1 if problem else 0)
