#!/usr/bin/env python3
"""Promote only authentic, unmodified Android emulator captures into the Play bundle."""
from pathlib import Path
from PIL import Image
import shutil

SOURCE=Path('qa/screenshots')
TARGET=Path('brending/google-play/real-screenshots')
ROUTES=[
 ('05-dashboard','01-dashboard'),
 ('06-hardware-explorer','02-hardware'),
 ('10-memory-details','03-memory'),
 ('12-battery-details','04-battery'),
 ('21-live-monitor','05-monitor'),
 ('26-diagnostics-center','06-diagnostics'),
 ('38-recording-library','07-recordings'),
 ('39-session-insights','08-insights'),
]
validated=[]
for source, name in ROUTES:
    file=SOURCE / f'{source}.png'
    if not file.is_file(): raise SystemExit(f'MISSING real app capture: {file}')
    with Image.open(file) as im:
        if im.size != (1080,1920) or im.mode not in ('RGB','RGBA'):
            raise SystemExit(f'WRONG device capture: {file}, {im.size}. No synthetic upscaling allowed.')
    validated.append((file,name))
TARGET.mkdir(parents=True,exist_ok=True)
for file,name in validated:
    shutil.copyfile(file,TARGET / f'{name}-1080x1920.png')
print(f'Promoted {len(validated)} authentic emulator captures; manual UI review still required.')
