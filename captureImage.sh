#!/bin/bash

################################
#
#
#  created by Sebastian Rosenberg <sebastian@rosenberg.click> (c) 2017
#
#  script to fetch picture from android camera
#
#  requires: adb tool out of android sdk
#
#
#  preparations: lock devices screen orientation to vertical
#
################################


if [ "$ANDROID_HOME" == "" ]; then
	echo "env var ANDROID_HOME need to be set"
	exit 1
fi

export PATH=$PATH:$ANDROID_HOME/platform-tools

if ! type adb > /dev/null 2>&1; then
	echo "error: adb command not found. Pleas check ANDROID_HOME"
	exit 1
fi


function cleanup() {
	echo -e "\nexiting now"
	exit 1
}



echo -n "capturing picture from android device ... "
adb shell "am start -a android.media.action.IMAGE_CAPTURE" > /dev/null 2>&1
if [ $? -ne 0 ]; then
	echo "failed, starting the intent"
	cleanup
fi

adb shell "input keyevent 'KEYCODE_FOCUS'"
if [ $? -ne 0 ]; then
	echo "failed, failed setting focus"
	cleanup
fi

## KEYCODE_CAMERA -> int: 27
adb shell "input keyevent 'KEYCODE_CAMERA'" > /dev/null 2>&1
if [ $? -ne 0 ]; then
	echo "failed, sending keyevent"
	cleanup
fi

##TODO: calculate touch event coords by fetching view with 
# adb shell uiautomator dump /sdcard/view.xml
# adb shell cat /sdcard/view.xml
adb shell "input tap 1200 56" > /dev/null 2>&1
if [ $? -ne 0 ]; then
	echo "failed, could not send touc event to finish capture"
	cleanup
fi

echo "done"








