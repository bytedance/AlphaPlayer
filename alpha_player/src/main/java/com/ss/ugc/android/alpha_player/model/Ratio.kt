package com.ss.ugc.android.alpha_player.model

class Ratio {

    var leftRatio: Float = 0f
    var topRatio: Float = 0f
    var rightRatio: Float = 0f
    var bottomRatio: Float = 0f

    fun set(leftRadio: Float, topRadio: Float, rightRadio: Float, bottomRadio: Float) {
        this.leftRatio = leftRadio
        this.topRatio = topRadio
        this.rightRatio = rightRadio
        this.bottomRatio = bottomRadio
    }

    fun isClear(): Boolean {
        return leftRatio != 0f && topRatio != 0f && rightRatio != 0f && bottomRatio != 0f
    }

    fun clear() {
        leftRatio = 0f
        topRatio = 0f
        rightRatio = 0f
        bottomRatio = 0f
    }
}