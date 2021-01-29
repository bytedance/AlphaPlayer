package com.ss.ugc.android.alpha_player.model

class DataInfo {

    var path: String = ""
    var scaleType: ScaleType = ScaleType.ScaleAspectFill
    var totalFrame: Int = 0
    var videoWidth: Int = 0
    var videoHeight: Int = 0
    var actualWidth: Int = 0
    var actualHeight: Int = 0
    var version: Int = 0
    var looping: Boolean = false
    var masks: Map<String, Map<String, Element>>? = null
    private var alphaArea: Area? = null
    private var rgbArea: Area? = null

    fun setScaleType(align: Int) {
        scaleType = ScaleType.convertFrom(align)
    }

    fun setAlphaArea(area: FloatArray?) {
        if (area?.size == 4) {
            alphaArea = Area(area)
        }
    }

    fun getAlphaArea(): Area? {
        return alphaArea
    }

    fun setRgbArea(area: FloatArray?) {
        if (area?.size == 4) {
            rgbArea = Area(area)
        }
    }

    fun getRgbArea(): Area? {
        return rgbArea
    }

    fun isSupportZip(): Boolean {
        return (version == 1) && (rgbArea != null) && (alphaArea != null)
    }

    fun isSupportMask(): Boolean {
        return (version == 1) && (masks?.isNotEmpty() == true)
    }

    class Area constructor(var left: Float, var top: Float, var right: Float, var bottom: Float) {

        constructor(area: FloatArray): this(area[0], area[1], area[0] + area[2], area[1] + area[3])

        fun normalize(
            startX: Float,
            startY: Float,
            endX: Float,
            endY: Float,
            width: Float,
            height: Float
        ): Area {
            if (width == 0f || height == 0f) {
                return this
            }
            left = startX / width
            top = startY / height
            right = endX / width
            bottom = endY / height
            return this
        }

        fun normalize(area: IntArray?, width: Float, height: Float): Area {
            if (width == 0f || height == 0f) {
                return this
            }
            return if (area != null && area.size == 4) {
                normalize(
                    area[0].toFloat(),
                    area[1].toFloat(),
                    (area[0] + area[2]).toFloat(),
                    (area[1] + area[3]).toFloat(),
                    width,
                    height
                )
            } else this
        }

        fun normalize(width: Float, height: Float): Area {
            return if (width == 0f || height == 0f) {
                this
            } else normalize(left, top, right, bottom, width, height)
        }

        fun width(): Float {
            return right - left
        }

        fun height(): Float {
            return bottom - top
        }

        fun offset(dx: Float, dy: Float) {
            left += dx
            top += dy
            right += dx
            bottom += dy
        }

        fun scale(scale: Float) {
            scaleX(scale)
            scaleY(scale)
        }

        fun scaleX(scale: Float) {
            if (scale != 1.0f) {
                left *= scale
                right *= scale
            }
        }

        fun scaleY(scale: Float) {
            if (scale != 1.0f) {
                top *= scale
                bottom *= scale
            }
        }

        fun flipY() {
            top = -top
            bottom = -bottom
        }

        fun flipX() {
            left = -left
            right = -right
        }

        fun reset(area: IntArray): Area {
            left = area[0].toFloat()
            top = area[1].toFloat()
            right = (area[0] + area[2]).toFloat()
            bottom = (area[1] + area[3]).toFloat()
            return this
        }

        fun isValid(): Boolean {
            return left <= right && top <= bottom
        }
    }

    class Element {
        /**
         * 0-文字，1-图片
         */
        var type = 0

        /**
         * 0-平铺，1-等比拉伸
         */
        var fitType = 0
        var sourceFrame: IntArray? = null
        var renderFrame: IntArray? = null

        val isValid: Boolean
            get() {
                if (type != 0 && type != 1) {
                    return false
                }
                if (fitType != 0 && fitType != 1) {
                    return false
                }
                if (sourceFrame?.size == 4) {
                    return false
                }
                return renderFrame?.size == 4
            }

        override fun toString(): String {
            return "Element{" +
                    "type=" + type +
                    ", fitType=" + fitType +
                    ", sourceFrame=" + sourceFrame +
                    ", renderFrame=" + renderFrame +
                    '}'
        }
    }
}