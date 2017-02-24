#!/bin/bash
adb shell am broadcast -a "services.LocationDetectionService:SPOOF" --es "data" "$1" > /dev/null
