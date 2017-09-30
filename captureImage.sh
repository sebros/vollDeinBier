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
#  preperations: lock devices screen orientation to vertical
#
################################

RED='\033[1;31m'
GREEN='\033[1;32m'
NC='\033[0m' # No Color

camera_img_loc="/sdcard/DCIM/Camera"
last_img=""

if [ "$1" == "" ]; then
	echo "need first parameter as api key"
	exit 1
fi

apiKey=$1


if [ "$ANDROID_HOME" == "" ]; then
	echo "env var ANDROID_HOME need to be set"
	exit 1
fi

export PATH=$PATH:$ANDROID_HOME/platform-tools

if ! type adb > /dev/null 2>&1; then
	echo "error: adb command not found. Pleas check ANDROID_HOME"
	exit 1
fi

if ! type jq > /dev/null 2>&1; then
	echo -e "error: jq command not found\nyou may try to: apt install jq"
	exit 1
fi


function cleanexit() {
	echo -e "\nexiting now"
	exit 1
}


function capture_image() {

	local tap_x=1200
	local tap_y=56

	echo -n "capturing picture from android device ... "
	adb shell "am start -a android.media.action.IMAGE_CAPTURE" > /dev/null 2>&1
	if [ $? -ne 0 ]; then
		echo "failed, starting the intent"
		cleanexit
	fi

	adb shell "input keyevent 'KEYCODE_FOCUS'" > /dev/null 2>&1
	if [ $? -ne 0 ]; then
		echo "failed, failed setting focus"
		cleanexit
	fi

	## KEYCODE_CAMERA -> int: 27
	adb shell "input keyevent 'KEYCODE_CAMERA'" > /dev/null 2>&1
	if [ $? -ne 0 ]; then
		echo "failed, sending keyevent"
		cleanexit
	fi

	sleep 3.5

	##TODO: calculate touch event coordinates by fetching view with 
	# adb shell uiautomator dump /sdcard/view.xml
	# adb shell cat /sdcard/view.xml
	adb shell "input tap $tap_x $tap_y" > /dev/null 2>&1
	if [ $? -ne 0 ]; then
		echo "failed, could not send touc event to finish capture"
		cleanexit
	fi

	echo "done"
	
	
	echo -n "downloading img to local storage ... "
	#TODO: sort this by creation date, not by file name
	last_img=$(adb shell "ls -l $camera_img_loc" | grep -P -o "[0-9_]*\.jpg" | sort -r | head -n1)
	
	if [ "$last_img" == "" ]; then
		echo "failed to get last img ($last_img) from storage location ($camera_img_loc)"
		exit 1
	fi
	
	adb pull "$camera_img_loc/$last_img" ./tmp/$last_img > /dev/null 2>&1
	if [ $? -ne 0 ]; then
		echo "failed, could not download captured picture"
		cleanexit
	fi
	
	echo "done"
	
	
	echo -n "resizing picture ... "
	convert ./tmp/$last_img -resize 1024x ./tmp/resized-$last_img > /dev/null 2>&1
	if [ $? -ne 0 ]; then
		echo -n "failed, to resize picture"
		cleanexit
	fi
	
	echo "done"
	
	mv ./tmp/$last_img ./archive/original/$last_img
	mv ./tmp/resized-$last_img ./archive/resized/resized-$last_img
}

function fetch_vis_prediction() {
	
	local img_loc=./archive/resized/resized-$last_img

	#apiKey=$(cat ../watson_apikey.txt);
	response=$(curl --silent -X POST --form "images_file=@$img_loc" --form "parameters=@classes.json" "https://gateway-a.watsonplatform.net/visual-recognition/api/v3/classify?api_key=$apiKey&version=2016-05-20" | tee out.log | jq '.images[].classifiers[].classes[].class' | cut -d"\"" -f2)
	
	echo ""

	if [ "$response" == "biervoll" ]; then
		printf "${GREEN}There is enought beer in the glass.${NC}\n\n"
	else
		printf "${RED}Glass seems to be emtpy.${NC}\n\n"
	fi
}



### start here
capture_image
fetch_vis_prediction

