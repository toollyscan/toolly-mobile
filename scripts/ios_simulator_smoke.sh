#!/bin/bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
derived_data="${repository_root}/build/ios-host"
app_path="${derived_data}/Build/Products/Debug-iphonesimulator/ToollyApp.app"
bundle_id="com.toollyscan.mobile"

read -r phone_udid tablet_udid <<< "$(
    xcrun simctl list devices available -j |
        /usr/bin/python3 -c '
import json
import sys

devices = json.load(sys.stdin).get("devices", {})
def latest_device(prefix):
    candidates = [
        (runtime, device["udid"])
        for runtime, runtime_devices in devices.items()
        if ".iOS-" in runtime
        for device in runtime_devices
        if device.get("isAvailable") and device.get("name", "").startswith(prefix)
    ]
    candidates.sort(reverse=True)
    if not candidates:
        raise SystemExit(f"No available {prefix} simulator")
    return candidates[0][1]

print(latest_device("iPhone"), latest_device("iPad"))
'
)"

cleanup() {
    for simulator_udid in "${phone_udid}" "${tablet_udid}"; do
        xcrun simctl terminate "${simulator_udid}" "${bundle_id}" >/dev/null 2>&1 || true
        xcrun simctl shutdown "${simulator_udid}" >/dev/null 2>&1 || true
    done
}
trap cleanup EXIT

xcrun simctl boot "${phone_udid}" >/dev/null 2>&1 || true
xcrun simctl bootstatus "${phone_udid}" -b

xcodebuild \
    -project "${repository_root}/iosApp/ToollyApp.xcodeproj" \
    -scheme ToollyApp \
    -configuration Debug \
    -destination "id=${phone_udid}" \
    -derivedDataPath "${derived_data}" \
    CODE_SIGNING_ALLOWED=NO \
    build

test -d "${app_path}"

for simulator_udid in "${phone_udid}" "${tablet_udid}"; do
    xcrun simctl boot "${simulator_udid}" >/dev/null 2>&1 || true
    xcrun simctl bootstatus "${simulator_udid}" -b
    xcrun simctl install "${simulator_udid}" "${app_path}"
    xcrun simctl launch --terminate-running-process "${simulator_udid}" "${bundle_id}"
done
