package com.ss.ugc.android.alpha_player.model

import android.content.res.Configuration
import android.text.TextUtils
import java.io.File

/**
 * created by dengzhuoyao on 2020/07/07
 */
class DataSource {

    lateinit var baseDir: String
    lateinit var portPath: String
    lateinit var landPath: String

    var portScaleType: ScaleType? = null
    var landScaleType: ScaleType? = null
    var isLooping: Boolean = false

    fun setBaseDir(baseDir: String): DataSource {
        this.baseDir = if (baseDir.endsWith(File.separator)) baseDir else (baseDir + File.separator)
        return this
    }

    fun setPortraitPath(portraitPath: String, portraitScaleType: Int): DataSource {
        this.portPath = portraitPath
        this.portScaleType = ScaleType.convertFrom(portraitScaleType)
        return this
    }

    fun setLandscapePath(landscapePath: String, landscapeScaleType: Int): DataSource {
        this.landPath = landscapePath
        this.landScaleType = ScaleType.convertFrom(landscapeScaleType)
        return this
    }

    fun setLooping(isLooping: Boolean): DataSource {
        this.isLooping = isLooping
        return this
    }

    fun getPath(orientation: Int): String {
        return baseDir + (if (Configuration.ORIENTATION_PORTRAIT == orientation) portPath else landPath)
    }

    fun getScaleType(orientation: Int): ScaleType? {
        return if (Configuration.ORIENTATION_PORTRAIT == orientation) portScaleType else landScaleType
    }

    fun isValid(): Boolean {
        return !TextUtils.isEmpty(portPath) && !TextUtils.isEmpty(landPath) && portScaleType != null && landScaleType != null
    }
}