package com.ss.ugc.android.alpha_player.utils

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics

import android.view.WindowManager

/**
 * Description:
 * @author 杜小菜,Created on 2/20/21 - 7:42 PM.
 * E-mail:duqian2010@gmail.com
 */
object SystemUtil {
    // 根据手机的分辨率从 dp 的单位 转成为 px(像素)
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    // 根据手机的分辨率从 px(像素) 的单位 转成为 dp
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt() // 四舍五入取整
    }

    // 获得屏幕的宽度
    fun getScreenWidth(ctx: Context): Int {
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }

    // 获得屏幕的高度
    fun getScreenHeight(ctx: Context): Int {
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        return dm.heightPixels
    }

    // 获得屏幕的像素密度
    fun getScreenDensity(ctx: Context): Float {
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        return dm.density
    }

    // 获取手机横竖屏的方法
    fun isLandscape(context: Context): Boolean {
        val mConfiguration: Configuration = context.resources.configuration // 获取设置的配置信息
        val ori: Int = mConfiguration.orientation // 获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            return true
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            return false
        }
        return false
    }
}