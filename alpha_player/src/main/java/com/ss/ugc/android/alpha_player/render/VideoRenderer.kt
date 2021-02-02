package com.ss.ugc.android.alpha_player.render

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Build
import android.util.Log
import android.view.Surface
import com.ss.ugc.android.alpha_player.mask.MaskRender
import com.ss.ugc.android.alpha_player.model.DataInfo
import com.ss.ugc.android.alpha_player.model.MaskSrc
import com.ss.ugc.android.alpha_player.model.Ratio
import com.ss.ugc.android.alpha_player.model.ScaleType
import com.ss.ugc.android.alpha_player.utils.ShaderUtil
import com.ss.ugc.android.alpha_player.utils.convertScreen2World
import com.ss.ugc.android.alpha_player.utils.writeData
import com.ss.ugc.android.alpha_player.widget.IAlphaVideoView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * created by dengzhuoyao on 2020/07/07
 */
class VideoRenderer(private val alphaVideoView: IAlphaVideoView) : IRender {

    companion object {
        private const val TAG = "VideoRender"
        private const val FLOAT_SIZE_BYTES = 4
        private const val FRAME_LOSE_THRESHOLD = 2
        private const val GL_TEXTURE_EXTERNAL_OES = 0x8D65
    }

    private var programID: Int = 0
    private var textureID: Int = 0
    private var aPositionHandle: Int = 0
    private var aTextureHandle: Int = 0
    private var aAlphaTextureHandle: Int = 0

    /**
     * After mediaPlayer call onCompletion, GLSurfaceView still will call
     * {@link GLSurfaceView#requestRender} in some special case, so cause
     * the media source last frame be drawn again. So we add this flag to
     * avoid this case.
     */
    private val canDraw = AtomicBoolean(false)
    private val updateSurface = AtomicBoolean(false)

    private lateinit var surfaceTexture: SurfaceTexture
    private var surfaceListener: IRender.SurfaceListener? = null
    private var scaleType = ScaleType.ScaleAspectFill

    private val scaleRatio = Ratio()
    private var masks: Map<String, Map<String, DataInfo.Element>>? = null
    private var maskRender: MaskRender? = null
    private var alphaArea: DataInfo.Area? = null
    private var rgbArea: DataInfo.Area? = null
    private var vertexArea: DataInfo.Area? = null
    private val curFrame = AtomicInteger(0)

    private var videoWidth = 0
    private var videoHeight = 0
    private var actualWidth = 0
    private var actualHeight = 0

    private val vertexData = FloatArray(8)
    private val rgbTextureData = FloatArray(8)
    private val alphaTextureData = FloatArray(8)

    private var vertexBuffer: FloatBuffer? = null
    private var rgbTextureBuffer: FloatBuffer? = null
    private var alphaTextureBuffer: FloatBuffer? = null

    override fun setScaleType(scaleType: ScaleType) {
        this.scaleType = scaleType
    }

    override fun measureInternal(
        viewWidth: Float,
        viewHeight: Float,
        tmpVideoWidth: Float,
        tmpVideoHeight: Float
    ) {
        if (viewWidth <= 0 || viewHeight <= 0 || tmpVideoWidth <= 0 || tmpVideoHeight <= 0) {
            return
        }

        var videoWidth = tmpVideoWidth
        var videoHeight = tmpVideoHeight
        val currentRatio = viewWidth / viewHeight
        val videoRatio = videoWidth / videoHeight
        var ratioX: Float
        var ratioY: Float
        val leftRatio: Float
        val rightRatio: Float
        val topRatio: Float
        val bottomRatio: Float

        if (currentRatio > videoRatio) {
            // 宽度缩放到viewWidth，裁剪上下
            videoWidth = viewWidth
            videoHeight = videoWidth / videoRatio
            ratioX = 0f
            ratioY = (1 - viewHeight / videoHeight) / 2
        } else {
            // 高度缩放到viewHeight，裁剪左右
            videoHeight = viewHeight
            videoWidth = videoHeight * videoRatio
            ratioX = (1 - viewWidth / videoWidth) / 2
            ratioY = 0f
        }

        when (scaleType) {
            ScaleType.ScaleToFill -> {
                //  全屏拉伸铺满
                leftRatio = 0f
                rightRatio = 0f
                topRatio = 0f
                bottomRatio = 0f
                crop(leftRatio, topRatio, rightRatio, bottomRatio)
            }
            ScaleType.ScaleAspectFitCenter -> {
                //  等比例缩放对齐全屏，屏幕多余部分留空
                if (currentRatio > videoRatio) {
                    // 高度缩放到viewHeight，计算两边留空位置
                    videoHeight = viewHeight
                    videoWidth = videoHeight * videoRatio
                    ratioX = (1 - videoWidth / viewWidth) / 2
                    ratioY = 0f
                } else {
                    // 宽度缩放到viewWidth，计算上下留空位置
                    videoWidth = viewWidth
                    videoHeight = videoWidth / videoRatio
                    ratioX = 0f
                    ratioY = (1 - videoHeight / viewHeight) / 2
                }
                zoom(ratioX, ratioY, ratioX, ratioY)
            }
            ScaleType.ScaleAspectFill -> {
                //  等比例缩放铺满全屏，裁剪视频多余部分
                leftRatio = ratioX
                rightRatio = ratioX
                topRatio = ratioY
                bottomRatio = ratioY
                crop(leftRatio, topRatio, rightRatio, bottomRatio)
            }
            ScaleType.TopFill -> {
                leftRatio = ratioX
                rightRatio = ratioX
                topRatio = 0f
                bottomRatio = ratioY * 2
                crop(leftRatio, topRatio, rightRatio, bottomRatio)
            }
            ScaleType.BottomFill -> {
                leftRatio = ratioX
                rightRatio = ratioX
                topRatio = ratioY * 2
                bottomRatio = 0f
                crop(leftRatio, topRatio, rightRatio, bottomRatio)
            }
            ScaleType.LeftFill -> {
                leftRatio = 0f
                rightRatio = ratioX * 2
                topRatio = ratioY
                bottomRatio = ratioY
                crop(leftRatio, topRatio, rightRatio, bottomRatio)
            }
            ScaleType.RightFill -> {
                leftRatio = ratioX * 2
                rightRatio = 0f
                topRatio = ratioY
                bottomRatio = ratioY
                crop(leftRatio, topRatio, rightRatio, bottomRatio)
            }
            ScaleType.TopFit -> {
                // 宽度缩放到viewWidth，计算底部留空位置
                videoWidth = viewWidth
                videoHeight = videoWidth / videoRatio
                ratioY = (1 - videoHeight / viewHeight) / 2
                zoom(0f, 0f, 0f, ratioY * 2)
            }
            ScaleType.BottomFit -> {
                // 宽度缩放到viewWidth，计算顶部留空位置
                videoWidth = viewWidth
                videoHeight = videoWidth / videoRatio
                ratioY = (1 - videoHeight / viewHeight) / 2
                zoom(0f, ratioY * 2, 0f, 0f)
            }
            ScaleType.LeftFit -> {
                // 高度缩放到viewHeight，计算右边留空位置
                videoHeight = viewHeight
                videoWidth = videoHeight * videoRatio
                ratioX = (1 - videoWidth / viewWidth) / 2
                zoom(0f, 0f, ratioX * 2, 0f)
            }
            ScaleType.RightFit -> {
                // 高度缩放到viewHeight，计算左边留空位置
                videoHeight = viewHeight
                videoWidth = videoHeight * videoRatio
                ratioX = (1 - videoWidth / viewWidth) / 2
                zoom(ratioX * 2, 0f, 0f, 0f)
            }
        }
    }

    override fun setConfigParam(dataInfo: DataInfo) {
        scaleType = dataInfo.scaleType
        videoWidth = dataInfo.videoWidth
        videoHeight = dataInfo.videoHeight
        actualWidth = dataInfo.actualWidth
        actualHeight = dataInfo.actualHeight
        if (dataInfo.isSupportZip()) {
            rgbArea = dataInfo.getRgbArea()?.normalize(
                dataInfo.videoWidth.toFloat(),
                dataInfo.videoHeight.toFloat()
            )
            alphaArea = dataInfo.getAlphaArea()?.normalize(
                dataInfo.videoWidth.toFloat(),
                dataInfo.videoHeight.toFloat()
            )
        } else {
            rgbArea = DataInfo.Area(0.5f, 0f, 1f, 1f)
            alphaArea = DataInfo.Area(0f, 0f, 0.5f, 1f)
        }
        vertexArea = DataInfo.Area(0f, 0f, 1f, 1f)
        masks = if (dataInfo.isSupportMask()) {
            dataInfo.masks
        } else {
            null
        }
        initVertexData()
        resetMaskRender()
    }

    override fun addMaskSrcList(maskSrcList: ArrayList<MaskSrc>) {
        if (masks != null && maskSrcList.isNotEmpty()) {
            maskRender = MaskRender(alphaVideoView.getContext(), maskSrcList)
            if (alphaVideoView.isSurfaceCreated()) {
                maskRender?.init()
            }
        }
    }

    private fun initVertexData() {
        convertScreen2World(vertexArea)
        vertexArea?.apply {
            writeData(vertexData, left, top, right, bottom)
            vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(vertexData)
            vertexBuffer?.position(0)
        }

        rgbArea?.apply {
            writeData(rgbTextureData, left, top, right, bottom)
            rgbTextureBuffer = ByteBuffer.allocateDirect(rgbTextureData.size * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(rgbTextureData)
            rgbTextureBuffer?.position(0)
        }

        alphaArea?.apply {
            writeData(alphaTextureData, left, top, right, bottom)
            alphaTextureBuffer = ByteBuffer.allocateDirect(alphaTextureData.size * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(alphaTextureData)
            alphaTextureBuffer?.position(0)
        }

        scaleRatio.clear()
    }

    private fun resetMaskRender() {
        maskRender?.release()
        maskRender = null
    }

    private fun zoom(
        leftZoomRatio: Float,
        topZoomRatio: Float,
        rightZoomRatio: Float,
        bottomZoomRatio: Float
    ) {
        vertexBuffer?.apply {
            scaleRatio.set(leftZoomRatio, topZoomRatio, rightZoomRatio, bottomZoomRatio)
            vertexArea?.let {
                writeData(
                    vertexData, it.left + leftZoomRatio * 2, it.top - topZoomRatio * 2,
                    it.right - rightZoomRatio * 2, it.bottom + bottomZoomRatio * 2
                )
            }
            position(0)
            put(vertexData)
        }
    }

    private fun crop(
        leftCropRatio: Float,
        topCropRatio: Float,
        rightCropRatio: Float,
        bottomCropRatio: Float
    ) {
        if (rgbTextureBuffer == null || rgbArea == null || alphaTextureBuffer == null || alphaArea == null) {
            return
        }

        scaleRatio.set(
            -leftCropRatio / (1 - rightCropRatio - leftCropRatio),
            -topCropRatio / (1 - topCropRatio - bottomCropRatio),
            -rightCropRatio / (1 - rightCropRatio - leftCropRatio),
            -bottomCropRatio / (1 - topCropRatio - bottomCropRatio)
        )

        rgbArea?.apply {
            writeData(
                rgbTextureData, left + leftCropRatio * width(), top + topCropRatio * height(),
                right - rightCropRatio * width(), bottom - bottomCropRatio * height()
            )
        }
        rgbTextureBuffer?.position(0)
        rgbTextureBuffer?.put(rgbTextureData)

        alphaArea?.apply {
            writeData(
                alphaTextureData, left + leftCropRatio * width(), top + topCropRatio * height(),
                right - rightCropRatio * width(), bottom - bottomCropRatio * height()
            )
        }
        alphaTextureBuffer?.position(0)
        alphaTextureBuffer?.put(alphaTextureData)
    }

    override fun setSurfaceListener(surfaceListener: IRender.SurfaceListener) {
        this.surfaceListener = surfaceListener
    }

    override fun onDrawFrame(glUnused: GL10) {
        if (updateSurface.compareAndSet(true, false)) {
            try {
                surfaceTexture.updateTexImage()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        if (!canDraw.get()) {
            GLES20.glFinish()
            return
        }

        curFrame.addAndGet(1)
        drawFrame()
        maskRender?.apply {
            drawMask()
        }
        GLES20.glFinish()
    }

    private fun drawFrame() {
        if (vertexBuffer == null || rgbTextureBuffer == null || alphaTextureBuffer == null) {
            Log.d(TAG, "setConfigParams not called")
            return
        }
        GLES20.glUseProgram(programID)
        checkGlError("glUseProgram")

        vertexBuffer?.position(0)
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        checkGlError("glVertexAttribPointer maPosition")
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        checkGlError("glEnableVertexAttribArray aPositionHandle")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureID)

        rgbTextureBuffer?.position(0)
        GLES20.glVertexAttribPointer(aTextureHandle, 2, GLES20.GL_FLOAT, false, 0, rgbTextureBuffer)
        checkGlError("glVertexAttribPointer aTextureHandle")
        GLES20.glEnableVertexAttribArray(aTextureHandle)
        checkGlError("glEnableVertexAttribArray aTextureHandle")

        alphaTextureBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            aAlphaTextureHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            alphaTextureBuffer
        )
        checkGlError("glVertexAttribPointer aAlphaTextureHandle")
        GLES20.glEnableVertexAttribArray(aAlphaTextureHandle)
        checkGlError("glEnableVertexAttribArray aAlphaTextureHandle")

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")
    }

    private fun drawMask() {
        var frame = curFrame.get()
        val currentFrame: Int = surfaceListener?.getCurrentFrame() ?: 0
        if (currentFrame > frame + FRAME_LOSE_THRESHOLD) {
            frame = currentFrame
            curFrame.set(frame)
        }
        if (masks == null) {
            return
        }
        val elementMap: Map<String, DataInfo.Element>? = masks?.get(frame.toString())
        if (elementMap == null) {
            Log.i(TAG, "no current frame")
            return
        }
        for ((key, value) in elementMap) {
            maskRender!!.drawFrame(
                textureID,
                videoWidth, videoHeight,
                actualWidth, actualHeight,
                scaleRatio, key, value
            )
        }
    }

    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        programID = createProgram()
        if (programID == 0) {
            return
        }

        aPositionHandle = GLES20.glGetAttribLocation(programID, "aPosition")
        checkGlError("glGetAttribLocation aPosition")
        if (aPositionHandle == -1) {
            throw RuntimeException("Could not get attrib location for aPosition")
        }

        aTextureHandle = GLES20.glGetAttribLocation(programID, "aTextureCoord")
        checkGlError("glGetAttribLocation aTextureCoord")
        if (aTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for aTextureCoord")
        }

        aAlphaTextureHandle = GLES20.glGetAttribLocation(programID, "aAlphaTextureCoord")
        checkGlError("glGetAttribLocation aAlphaTextureCoord")
        if (aAlphaTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for aAlphaTextureCoord")
        }

        prepareSurface()
    }

    override fun onSurfaceDestroyed(gl: GL10?) {
        surfaceListener?.onSurfaceDestroyed()
    }

    private fun prepareSurface() {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)

        textureID = textures[0]
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureID)
        checkGlError("glBindTexture textureID")

        GLES20.glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )

        surfaceTexture = SurfaceTexture(textureID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            surfaceTexture.setDefaultBufferSize(
                alphaVideoView.getMeasuredWidth(),
                alphaVideoView.getMeasuredHeight()
            )
        }
        surfaceTexture.setOnFrameAvailableListener(this)

        val surface = Surface(this.surfaceTexture)
        surfaceListener?.onSurfacePrepared(surface)
        updateSurface.compareAndSet(true, false)
    }

    override fun onFrameAvailable(surface: SurfaceTexture) {
        updateSurface.compareAndSet(false, true)
        alphaVideoView.requestRender()
    }

    override fun onFirstFrame() {
        curFrame.set(0)
        canDraw.compareAndSet(false, true)
        Log.i(TAG, "onFirstFrame:    canDraw = " + canDraw.get())
        alphaVideoView.requestRender()
    }

    override fun onCompletion() {
        canDraw.compareAndSet(true, false)
        Log.i(TAG, "onCompletion:   canDraw = " + canDraw.get())
        alphaVideoView.requestRender()
        resetMaskRender()
    }

    /**
     * load shader by OpenGL ES, if compile shader success, it will return shader handle,
     * else return 0.
     *
     * @param shaderType shader type, {@link GLES20.GL_VERTEX_SHADER} and
     * {@link GLES20.GL_FRAGMENT_SHADER}
     * @param source   shader source
     *
     * @return shaderID If compile shader success, it will return shader handle, else return 0.
     */
    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader $shaderType:")
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    /**
     * create program with {@link vertex.sh} and {@link frag.sh}. If attach shader or link
     * program, it will return 0, else return program handle
     *
     * @return programID If link program success, it will return program handle, else return 0.
     */
    private fun createProgram(): Int {
        val vertexSource = ShaderUtil.loadFromAssetsFile(
            "vertex.sh",
            alphaVideoView.getView().resources
        )
        val fragmentSource = ShaderUtil.loadFromAssetsFile(
            "frag.sh",
            alphaVideoView.getView().resources
        )

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            GLES20.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link programID: ")
                Log.e(TAG, GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    private fun checkGlError(op: String) {
        val error: Int = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "$op: glError $error")
            // TODO: 2018/4/25 端监控 用于监控礼物播放成功状态
        }
    }
}