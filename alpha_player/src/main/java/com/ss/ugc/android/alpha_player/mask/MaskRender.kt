package com.ss.ugc.android.alpha_player.mask

import android.content.Context
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20
import com.ss.ugc.android.alpha_player.Constant
import com.ss.ugc.android.alpha_player.model.DataInfo
import com.ss.ugc.android.alpha_player.model.MaskSrc
import com.ss.ugc.android.alpha_player.model.Ratio
import com.ss.ugc.android.alpha_player.utils.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import kotlin.collections.HashMap

class MaskRender(private val context: Context, maskSrcList: List<MaskSrc>?) {

    private val vertexData = FloatArray(8)
    private val srcTexData = FloatArray(8)
    private val maskTexData = FloatArray(8)

    private var vertexBuffer: FloatBuffer? = null
    private var srcTexBuffer: FloatBuffer? = null
    private var maskTexBuffer: FloatBuffer? = null
    var tmpArea: DataInfo.Area = DataInfo.Area(0f, 0f, 0f, 0f)
    private var program = 0
    private var aPositionHandle = 0
    private var aTextureCoordHandle = 0
    private var aMaskTextureCoordHandle = 0
    private var sTextureHandle = 0
    private var sMaskTextureHandle = 0
    private val elements: HashMap<String, MaskSrc?> = HashMap()
    private val elementsTex: HashMap<String, Int> = HashMap()
    private val maskSrcList: ArrayList<MaskSrc> = ArrayList()

    init {
        maskSrcList?.apply {
            this@MaskRender.maskSrcList.addAll(this)
        }
    }

    fun init() {
        initVertex()
        initProgram()
        initElements()
    }

    private fun initVertex() {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        vertexBuffer?.position(0)
        srcTexBuffer = ByteBuffer.allocateDirect(srcTexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(srcTexData)
        srcTexBuffer?.position(0)
        maskTexBuffer = ByteBuffer.allocateDirect(maskTexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(maskTexData)
        maskTexBuffer?.position(0)
    }

    private fun initProgram() {
        val vertexShader: String =
            ShaderUtil.loadFromAssetsFile("mask/mask_vertex.sh", context.resources)
        val fragShader: String =
            ShaderUtil.loadFromAssetsFile("mask/mask_frag.sh", context.resources)
        program = ShaderUtil.createProgram(vertexShader, fragShader)
        if (program == 0) {
            return
        }

        // 顶点坐标
        aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        ShaderUtil.checkGlError("glGetAttribLocation aPosition")
        if (aPositionHandle == -1) {
            throw RuntimeException("Could not get attrib location for aPosition")
        }

        // bitmap纹理坐标
        aTextureCoordHandle = GLES20.glGetAttribLocation(program, "aTextureCoord")
        ShaderUtil.checkGlError("glGetAttribLocation aTextureCoord")
        if (aTextureCoordHandle == -1) {
            throw RuntimeException("Could not get attrib location for aTextureCoord")
        }

        // Mask纹理坐标
        aMaskTextureCoordHandle = GLES20.glGetAttribLocation(program, "aMaskTextureCoord")
        ShaderUtil.checkGlError("glGetAttribLocation aMaskTextureCoord")
        if (aMaskTextureCoordHandle == -1) {
            throw RuntimeException("Could not get attrib location for aMaskTextureCoord")
        }

        // bitmap纹理
        sTextureHandle = GLES20.glGetUniformLocation(program, "sTexture")
        ShaderUtil.checkGlError("glGetUniformLocation sTexture")
        if (sTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for sTexture")
        }
        // Mask纹理
        sMaskTextureHandle = GLES20.glGetUniformLocation(program, "sMaskTexture")
        ShaderUtil.checkGlError("glGetUniformLocation sMaskTexture")
        if (sMaskTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for sMaskTexture")
        }
    }

    private fun initElements() {
        if (maskSrcList.isEmpty()) {
            return
        }
        elementsTex.clear()
        elements.clear()
        for (i in maskSrcList.indices) {
            maskSrcList[i].apply {
                elements[name ?: ""] = this
                elementsTex[name ?: ""] = loadTexture(bitmap)
            }
        }
    }

    fun release() {
        for ((_, value) in elementsTex) {
            deleteTextures(value)
        }
        elements.clear()
        elementsTex.clear()
        maskSrcList.clear()
    }

    fun drawFrame(
        videoTextureId: Int,
        videoWidth: Int, videoHeight: Int,
        actualWidth: Int, actualHeight: Int,
        scaleRatio: Ratio,
        name: String, element: DataInfo.Element
    ) {
        if (!element.isValid) {
            return
        }
        GLES20.glUseProgram(program)
        ShaderUtil.checkGlError("glUseProgram")
        val maskSrc: MaskSrc = elements[name] ?: return
        // 给对应的buffer创建当前帧的数据
        createVertex(
            videoWidth.toFloat(),
            videoHeight.toFloat(),
            actualWidth.toFloat(),
            actualHeight.toFloat(),
            maskSrc.width.toFloat(),
            maskSrc.height.toFloat(),
            scaleRatio,
            element
        )

        // 设置顶点坐标
        vertexBuffer?.position(0)
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        ShaderUtil.checkGlError("glVertexAttribPointer aPositionHandle")
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        ShaderUtil.checkGlError("glEnableVertexAttribArray aPositionHandle")

        // 设置bitmap纹理的坐标
        srcTexBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            aTextureCoordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            srcTexBuffer
        )
        ShaderUtil.checkGlError("glVertexAttribPointer aTextureCoordHandle")
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle)
        ShaderUtil.checkGlError("glEnableVertexAttribArray aTextureCoordHandle")
        // 绑定bitmap纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        val eleTextureId = elementsTex[name]
        if (eleTextureId == null || eleTextureId == 0) {
            return
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, eleTextureId)
        GLES20.glUniform1i(sTextureHandle, 0)

        // 设置mask纹理坐标
        maskTexBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            aMaskTextureCoordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            maskTexBuffer
        )
        ShaderUtil.checkGlError("glVertexAttribPointer aMaskTextureCoordHandle")
        GLES20.glEnableVertexAttribArray(aMaskTextureCoordHandle)
        ShaderUtil.checkGlError("glEnableVertexAttribArray aMaskTextureCoordHandle")
        // 绑定mask纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, videoTextureId)
        GLES20.glUniform1i(sMaskTextureHandle, 1)

        // 混合颜色
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        ShaderUtil.checkGlError("glDrawArrays")
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun createVertex(
        videoWidth: Float, videoHeight: Float,
        actualWidth: Float, actualHeight: Float,
        srcWidth: Float, srcHeight: Float,
        scaleRatio: Ratio,
        element: DataInfo.Element
    ) {
        // 顶点坐标
        tmpArea.normalize(element.renderFrame, actualWidth, actualHeight)
        // 1、先根据video的缩放比例，转换顶点坐标，计算顶点对应的位置
        convertByRatio(tmpArea, scaleRatio)
        // 2、再将屏幕坐标系转化为世界坐标系
        convertScreen2World(tmpArea)
        // 3、写入数据
        writeData(
            vertexData,
            tmpArea.left,
            tmpArea.top,
            tmpArea.right,
            tmpArea.bottom
        )
        vertexBuffer?.position(0)
        vertexBuffer?.put(vertexData)

        // bitmap纹理坐标
        fitSrc(element, tmpArea, srcWidth, srcHeight)
        writeData(
            srcTexData,
            tmpArea.left,
            tmpArea.top,
            tmpArea.right,
            tmpArea.bottom
        )
        srcTexBuffer?.position(0)
        srcTexBuffer?.put(srcTexData)

        // mask纹理坐标
        tmpArea.normalize(element.sourceFrame, videoWidth, videoHeight)
        writeData(
            maskTexData,
            tmpArea.left,
            tmpArea.top,
            tmpArea.right,
            tmpArea.bottom
        )
        maskTexBuffer?.position(0)
        maskTexBuffer?.put(maskTexData)
    }

    private fun fitSrc(
        element: DataInfo.Element,
        tmpArea: DataInfo.Area,
        srcWidth: Float,
        srcHeight: Float
    ) {
        when (element.fitType) {
            Constant.FIT_TYPE_CENTER_CROP -> {
                // 获取需要绘制的区域
                element.renderFrame?.apply {
                    tmpArea.reset(this)
                }
                val renderWidth: Float = tmpArea.width()
                val renderHeight: Float = tmpArea.height()
                // 根据中心裁剪
                centerCrop(renderWidth, renderHeight, srcWidth, srcHeight, tmpArea)
            }
            Constant.FIT_TYPE_FIT_XY -> {
                // 默认拉伸铺满render区域
                element.renderFrame?.apply {
                    val renderArea: DataInfo.Area = tmpArea.reset(this)
                    tmpArea.normalize(
                        0f,
                        0f,
                        renderArea.width(),
                        renderArea.height(),
                        renderArea.width(),
                        renderArea.height()
                    )
                }
            }
            else -> {
                element.renderFrame?.apply {
                    val renderArea: DataInfo.Area = tmpArea.reset(this)
                    tmpArea.normalize(
                        0f,
                        0f,
                        renderArea.width(),
                        renderArea.height(),
                        renderArea.width(),
                        renderArea.height()
                    )
                }
            }
        }
    }

    private fun centerCrop(
        renderWidth: Float, renderHeight: Float,
        srcWidth: Float, srcHeight: Float,
        tmpArea: DataInfo.Area
    ) {
        if (renderWidth <= srcWidth && renderHeight <= srcHeight) {
            // 裁剪四周，对齐render
            val startX = (srcWidth - renderWidth) / 2
            val startY = (srcHeight - renderHeight) / 2
            tmpArea.normalize(
                startX,
                startY,
                startX + renderWidth,
                startY + renderHeight,
                srcWidth,
                srcHeight
            )
        } else {
            val renderRadio = renderWidth / renderHeight
            val sourceRadio = srcWidth / srcHeight
            if (renderRadio > sourceRadio) {
                // 宽度缩放到render，裁剪高度
                val height = srcWidth / renderRadio
                val startY = (srcHeight - height) / 2
                tmpArea.normalize(0f, startY, srcWidth, startY + height, srcWidth, srcHeight)
            } else {
                // 高度缩放到render，裁剪宽度
                val width = srcHeight * renderRadio
                val startX = (srcWidth - width) / 2
                tmpArea.normalize(startX, 0f, startX + width, srcHeight, srcWidth, srcHeight)
            }
        }
    }
}