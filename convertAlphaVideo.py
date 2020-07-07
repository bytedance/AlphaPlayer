#!/usr/bin/env python
# -*- coding: UTF-8 -*-
import os
import subprocess
import sys
import shutil
import argparse
import cv2
import numpy as np

isDebug = False
needZip = False
outputVideoPath = ""
imageDir = ""
srcPath = ""
maskPath = ""
outputPath = ""
oVideoFilePath = ""
fps = 25
bitrate = 2000


def main():
	parser = argparse.ArgumentParser(description='manual to this script')
	parser.add_argument('--file', type=str, default = None)
	parser.add_argument('--dir', type=str, default = None)
	parser.add_argument('--zip', type=str2bool, nargs='?', const=True, default = False, help="Activate zip mode.")
	parser.add_argument('--fps', type=int, default = 25)
	parser.add_argument('--bitrate', type=int, default = 2000)
	args = parser.parse_args()

	print("convertAlphaVideo.py running")
	global needZip
	needZip = args.zip
	fps = args.fps
	bitrate = args.bitrate
	print "args.zip: ", args.zip
	if not args.file is None:
		parseVideoFile(args.file)
	elif not args.dir is None:
		parseImageDir(args.dir)
	else:
		print("params is None!")
		return
	print("finish")


def str2bool(v):
    if isinstance(v, bool):
       return v
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected.')


def help():
	print("help ~")


def parseVideoFile(path):
	print(">>>>>>> paraseVideoFile, file is %s" % path)

	parentDir = os.path.basename(path)
	parentDir = parentDir.split('.')[0] + "/"

	initDir(parentDir)
	videoToImage(path, imageDir)
	parseImageList(imageDir)
	imagesToVideo(outputPath, oVideoFilePath)

	shutil.rmtree(parentDir + "temp/")
	print(">>>>>> convert alpha video finish, video file path is : %s" % oVideoFilePath)


def parseImageDir(path):
	parentDir = os.path.abspath(path) + "/"
	print(">>>>>>> paraseImageDir, dirName is %s" % parentDir)

	initDir(parentDir)
	parseImageList(parentDir)
	imagesToVideo(outputPath, oVideoFilePath)

	shutil.rmtree(parentDir + "temp/")
	print(">>>>>> convert alpha video finish, video file path is : %s" % oVideoFilePath)


def initDir(parentDir):
	global imageDir
	imageDir = parentDir + "temp/imageDir/"
	mkdir(imageDir)
	global srcPath
	srcPath = parentDir + "temp/source/"
	mkdir(srcPath)
	global maskPath
	maskPath = parentDir + "temp/mask/"
	mkdir(maskPath)
	global outputPath
	outputPath = parentDir + "temp/output/"
	mkdir(outputPath)
	global outputVideoPath
	outputVideoPath = parentDir + "output/"
	mkdir(outputVideoPath)

	global oVideoFilePath
	oVideoFilePath = outputVideoPath + "video.mp4"


def parseImageList(inputPath):
	fileList = os.listdir(inputPath)
	totalLength = len(fileList)
	progress = 0
	for fileName in fileList:
		if os.path.splitext(fileName)[1] == ".png":
			inputImageFile = inputPath + fileName
			srcImageFile = srcPath + os.path.splitext(fileName)[0] + ".jpg"
			tempMaskImageFile = maskPath + os.path.splitext(fileName)[0] + "_temp.jpg"
			maskImageFile = maskPath + os.path.splitext(fileName)[0] + ".jpg"
			outputImageFile = outputPath + os.path.splitext(fileName)[0] + ".jpg"

			removeAlpha(inputImageFile, srcImageFile)
			if needZip:
				separateAlphaChannel(inputImageFile, tempMaskImageFile)
				zipAlphaChannelPro(tempMaskImageFile, maskImageFile)
			else:
				separateAlphaChannel(inputImageFile, maskImageFile)
			appendImageLand(srcImageFile, maskImageFile, outputImageFile)

			deleteTempFile(srcImageFile)
			deleteTempFile(maskImageFile)
			deleteTempFile(tempMaskImageFile)

			progress += 1
			updateProgress(progress, totalLength)


def videoToImage(videoPath, imageDir):
	command = "ffmpeg -i {} -r {} {}%05d.png".format(videoPath, fps, imageDir)
	if isDebug:
		print (command)
	ret = subprocess.Popen(command, shell = True)
	ret.communicate()


def removeAlpha(imageSrc, imageDst):
	command = "convert {} -background black -alpha remove {}".format(imageSrc, imageDst)
	if isDebug:
		print (command)
	ret = subprocess.Popen(command, shell = True)
	ret.communicate()


def separateAlphaChannel(imageFileOne, imageFileTwo):
	command = "convert {} -channel A -separate {}".format(imageFileOne, imageFileTwo)
	if isDebug:
		print (command)
	ret = subprocess.Popen(command, shell = True)
	ret.communicate()


def zipAlphaChannel(imageSrc, imageDst):
	srcImage = cv2.imread(imageSrc)
	shape = srcImage.shape
	dstImage = np.zeros((int(shape[0]), int(shape[1])/3, int(shape[2])), np.uint8)
	dstShape = dstImage.shape

	height 		= dstShape[0]
	width 		= dstShape[1]
	channels 	= dstShape[2]

	for row in range(height):
		for col in range(width):
			for channel in range(channels):
				dstImage[row][col][channel] = srcImage[row][col * 3 + channel][0]
	cv2.imwrite(imageDst, dstImage)


def zipAlphaChannelPro(imageSrc, imageDst):
	srcImage = cv2.imread(imageSrc)
	shape = srcImage.shape
	dstImage = np.zeros((int(shape[0]), int(shape[1])/3, int(shape[2])), np.uint8)
	dstShape = dstImage.shape

	height 		= dstShape[0]
	width 		= dstShape[1]
	channels 	= dstShape[2]

	for row in range(height):
		for col in range(width):
			for channel in range(channels):
				dstImage[row][col][channel] = srcImage[row][col + channel * width][0]
	cv2.imwrite(imageDst, dstImage)


def appendImageLand(imageFileOne, imageFileTwo, imageFileAppend):
	command = "convert +append {} {} {}".format(imageFileTwo, imageFileOne, imageFileAppend)
	if isDebug:
		print (command)
	ret = subprocess.Popen(command, shell = True)
	ret.communicate()


def deleteTempFile(filePath):
	if os.path.exists(filePath):
		os.remove(filePath)


def imagesToVideo(imagesPath, videoFile):
	command = "ffmpeg -r {} -i {}%05d.jpg -vcodec libx264 -pix_fmt yuv420p -b {}k {}".format(fps, imagesPath, bitrate, videoFile)
	if isDebug:
		print (command)
	ret = subprocess.Popen(command, shell = True)
	ret.communicate()


def updateProgress(progress, total):
	percent = round(1.0 * progress / total * 100,2)
	sys.stdout.write('\rprogress : %s [%d/%d]'%(str(percent)+'%', progress, total))
	sys.stdout.flush()


def mkdir(path):
	folder = os.path.exists(path)
	if not folder:
		os.makedirs(path)


if __name__ == '__main__':
	main()