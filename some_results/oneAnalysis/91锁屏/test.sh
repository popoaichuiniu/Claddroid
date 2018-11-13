#!/bin/sh
killall guake
adb shell screenrecord  --time-limit 15  --bit-rate 8000000 /sdcard/demo1.mp4 &
./startLogger.sh &
intentInfo=$1 
echo $intentInfo
adb push $intentInfo /data/data/jacy.popoaichuiniu.com.testpermissionleakge/files/intentInfo.txt
adb install -r /media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/android_project/Camera/TestPermissionleakge/app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n jacy.popoaichuiniu.com.testpermissionleakge/jacy.popoaichuiniu.com.testpermissionleakge.MainActivity
sleep 25s
adb pull /sdcard/demo1.mp4  .