package com.ss.ugc.android.alpha_player.utils

import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import com.ss.ugc.android.alpha_player.model.MaskSrc

fun createTextBitmap(maskSrc: MaskSrc): Bitmap? {
    if (TextUtils.isEmpty(maskSrc.text)) {
        return null
    }
    val textPaint = TextPaint()
    textPaint.isAntiAlias = true
    textPaint.textAlign = Paint.Align.CENTER
    textPaint.style = Paint.Style.FILL
    if (maskSrc.isBold) {
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    if (maskSrc.textSize <= 0) {
        // 默认24号
        maskSrc.textSize = 24
    }
    textPaint.textSize = maskSrc.textSize.toFloat()
    if (TextUtils.isEmpty(maskSrc.textColor)) {
        // 默认黑色
        maskSrc.textColor = "#FF000000"
    }
    textPaint.color = Color.parseColor(maskSrc.textColor)

    // 获取文字的宽高
    val rect = Rect()
    textPaint.getTextBounds(maskSrc.text, 0, maskSrc.text?.length ?: 0, rect)
    val width = rect.width()
    val height = rect.height()
    rect[0, 0, width] = height
    val dst = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(dst)
    dst.eraseColor(0)
    val fontMetrics = textPaint.fontMetricsInt
    val baseline = rect.centerY() + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    canvas.drawText(maskSrc.text ?: "", rect.centerX().toFloat(), baseline.toFloat(), textPaint)
    return dst
}