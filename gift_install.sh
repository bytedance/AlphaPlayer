#!/bin/bash
adb shell rm -r /sdcard/alphaVideoGift/
adb shell mkdir /sdcard/alphaVideoGift/
adb push gift/* /sdcard/alphaVideoGift/