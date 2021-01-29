package com.ss.ugc.android.alpha_player.model

class Ratio {

    var leftRadio: Float = 0f
    var topRadio: Float = 0f
    var rightRadio: Float = 0f
    var bottomRadio: Float = 0f

    fun set(leftRadio: Float, topRadio: Float, rightRadio: Float, bottomRadio: Float) {
        this.leftRadio = leftRadio
        this.topRadio = topRadio
        this.rightRadio = rightRadio
        this.bottomRadio = bottomRadio
    }

    fun isClear(): Boolean {
        return leftRadio != 0f && topRadio != 0f && rightRadio != 0f && bottomRadio != 0f
    }

    fun clear() {
        leftRadio = 0f
        topRadio = 0f
        rightRadio = 0f
        bottomRadio = 0f
    }
}