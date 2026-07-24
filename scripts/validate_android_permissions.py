#!/usr/bin/env python3
"""Reject permissions from the TLY-006B manifest; this ML Kit spike needs none."""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from pathlib import Path


ANDROID_NAME = "{http://schemas.android.com/apk/res/android}name"
MANIFEST = Path("spike-capture/src/main/AndroidManifest.xml")


def main() -> int:
    root = ET.parse(MANIFEST).getroot()
    permissions = [
        element.attrib.get(ANDROID_NAME, "<missing>")
        for element in root.findall("uses-permission")
    ]
    if permissions:
        print("TLY-006B must request no Android permissions:")
        for permission in sorted(permissions):
            print(f"- {permission}")
        return 1
    print("Android permission policy passed: TLY-006B requests 0 permissions.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
