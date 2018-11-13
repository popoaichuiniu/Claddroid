#!/bin/sh
# gnome-terminal -x bash -c "adb logcat | grep ZMSInstrument | tee ZMSInstrument.log"
# gnome-terminal -x bash -c "adb logcat | grep ZMSStart | tee ZMSStart.log"
# gnome-terminal -x bash -c "adb logcat *:E|tee error.log"
guake -e "adb logcat | grep ZMSInstrument | tee ZMSInstrument.log" &
guake -e "adb logcat | grep ZMSStart | tee ZMSStart.log" &
guake -e "adb logcat *:E|tee error.log" &