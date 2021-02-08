package com.ss.ugc.android.alpha_player.model

import android.content.res.Configuration
import android.text.TextUtils
import java.io.File

/**
 * created by dengzhuoyao on 2020/07/07
 */
class DataSource {

    private val portraitDataInfo: DataInfo = DataInfo()
    private val landscapeDataInfo: DataInfo = DataInfo()
    private var isPortraitValid = false
    private var isLandscapeValid = false
    private var baseDir: String = ""
    var messageId: Long = 0L
    var errorInfo: String = ""

    fun getDataInfo(orientation: Int): DataInfo {
        return if (Configuration.ORIENTATION_PORTRAIT == orientation) {
            portraitDataInfo
        } else {
            landscapeDataInfo
        }
    }

    fun isValid(orientation: Int): Boolean {
        return if (Configuration.ORIENTATION_PORTRAIT == orientation) isPortraitValid else isLandscapeValid
    }

    fun setPortraitDataInfo(config: DataInfo.() -> Unit) {
        portraitDataInfo.apply {
            config()
            isPortraitValid = checkValid()
        }
    }

    fun setLandscapeDataInfo(config: DataInfo.() -> Unit) {
        landscapeDataInfo.apply {
            config()
            isLandscapeValid = checkValid()
        }
    }

    private fun DataInfo.checkValid(): Boolean {
        // check resource file exist
        if (TextUtils.isEmpty(path)) {
            errorInfo = "dataPath is empty."
            return false
        } else if (!File(path).exists()) {
            errorInfo = "dataPath is not exist, path: $path."
            return false
        }

        // check new version resource
        if (version > 0) {
            if (getAlphaArea() == null || getRgbArea() == null) {
                errorInfo = "area is empty."
                return false
            } else if (getAlphaArea()?.isValid() == false || getRgbArea()?.isValid() == false) {
                errorInfo = "area is invalid."
                return false
            } else if (videoWidth <= 0 || videoHeight <= 0) {
                errorInfo = "video size is wrong."
                return false
            } else if (actualWidth <= 0 || actualHeight <= 0) {
                errorInfo = "actual size is wrong."
                return false
            } else if (getRgbArea()?.width()?.toInt() != actualWidth || getRgbArea()?.height()?.toInt() != actualHeight) {
                errorInfo = "rgb area is not equal to actual size."
                return false
            }
        }
        return true
    }

    fun setBaseDir(baseDir: String): DataSource {
        this.baseDir = if (baseDir.endsWith(File.separator)) baseDir else (baseDir + File.separator)
        return this
    }

    fun setPortraitPath(portraitPath: String, portraitScaleType: Int): DataSource {
        setPortraitDataInfo {
            path = baseDir + portraitPath
            setScaleType(portraitScaleType)
        }
        return this
    }

    fun setLandscapePath(landscapePath: String, landscapeScaleType: Int): DataSource {
        setLandscapeDataInfo {
            path = baseDir + landscapePath
            setScaleType(landscapeScaleType)
        }
        return this
    }

    fun setLooping(isLooping: Boolean): DataSource {
        portraitDataInfo.looping = isLooping
        landscapeDataInfo.looping = isLooping
        return this
    }
}