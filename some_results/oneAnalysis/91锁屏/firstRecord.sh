#!/bin/sh
adb shell screenrecord  --time-limit 15  --bit-rate 8000000 /sdcard/demo0.mp4 &
sleep 25s
adb pull /sdcard/demo0.mp4  .