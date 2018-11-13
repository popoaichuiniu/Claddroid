#!/bin/sh
guake -e "adb logcat | grep ZMSInstrument" &
guake -e "adb logcat | grep ZMSStart" &
guake -e "adb logcat *:E" &