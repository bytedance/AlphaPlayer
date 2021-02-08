package com.ss.ugc.android.alpha_player.render

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Build
import android.util.Log
import android.view.Surface
import com.ss.ugc.android.alpha_player.mask.MaskRender
import com.ss.ugc.android.alpha_player.model.ScaleType
import com.ss.ugc.android.alpha_player.utils.*
import com.ss.ugc.android.alpha_player.widget.IAlphaVideoView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * created by dengzhuoyao on 2020/07/07
 */
class VideoRenderer(val alphaVideoView: IAlphaVideoView) : IRender {

    companion object {
        private val TAG = VideoRenderer::class.java.simpleName
    }

    private val FLOAT_SIZE_BYTES = 4
    private val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
    private val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
    private val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3
    private val GL_TEXTURE_EXTERNAL_OES = 0x8D65
    private var triangleVertices: FloatBuffer? = null
    private val mVPMatrix = FloatArray(16)
    private val sTMatrix = FloatArray(16)
    private var programID: Int = 0
    private var textureID: Int = 0
    private var uMVPMatrixHandle: Int = 0
    private var uSTMatrixHandle: Int = 0
    private var aPositionHandle: Int = 0
    private var aTextureHandle: Int = 0


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

    /**
     * A float array that recorded the mapping relationship between texture
     * coordinates and window coordinates. It will changed for {@link ScaleType}
     * by {@link TextureCropUtil}.
     */
    private var videoVerticeData = floatArrayOf(
        // X, Y, Z, U, V
        -1.0f, -1.0f, 0f, 0.0f, 0f,
        1.0f, -1.0f, 0f, 1f, 0f,
        -1.0f, 1.0f, 0f, 0f, 1f,
        1.0f, 1.0f, 0f, 1f, 1f
    )

    private var maskVerticeData = floatArrayOf(
        // X, Y, Z, U, V
        -0.25f, -0.15f, 0f, 0f, 0f,
        0.25f, -0.15f, 0f, 1f, 0f,
        -0.25f, 0.15f, 0f, 0f, 1f,
        0.25f, 0.15f, 0f, 1f, 1f
    )

    private var uTextureHandle: Int = 0
    private var switchHandle: Int = 0
    private var maskTextureId = -1
    var maskBitmap: Bitmap? = null
    private var maskVertices: FloatBuffer? = null

    init {
        initVerticeInfo()
        Matrix.setIdentityM(sTMatrix, 0)
    }

    private fun initVerticeInfo() {
        triangleVertices = ByteBuffer.allocateDirect(videoVerticeData.size * FLOAT_SIZE_BYTES)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        triangleVertices?.put(videoVerticeData)?.position(0)

        maskVertices = ByteBuffer.allocateDirect(maskVerticeData.size * FLOAT_SIZE_BYTES)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        maskVertices?.put(maskVerticeData)?.position(0)
    }

    override fun setScaleType(scaleType: ScaleType) {
        this.scaleType = scaleType
    }

    override fun measureInternal(
        viewWidth: Float, viewHeight: Float,
        videoWidth: Float, videoHeight: Float
    ) {
        if (viewWidth <= 0 || viewHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) {
            return
        }
        // TODO-dq: 2021/2/8  处理横屏的视频显示，坐标要调整
        //videoVerticeData = TextureCropUtil.calculateHalfRightVerticeData(scaleType, viewWidth, viewHeight, videoWidth, videoHeight)
        initVerticeInfo()
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
            surfaceTexture.getTransformMatrix(sTMatrix)
        }

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f) //修改背景底色
        if (!canDraw.get()) {
            GLES20.glFinish()
            return
        }
        //半透明效果，alpha为0取目标色，alpha为1，则取源色，否则视源色的alpha大小各取一部分。
        GLES20.glEnable(GLES20.GL_BLEND)
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 基于源象素alpha通道值的半透明混合函数
        GLES20.glBlendFuncSeparate(
            GLES20.GL_SRC_ALPHA,
            GLES20.GL_ONE_MINUS_SRC_ALPHA,
            GLES20.GL_ONE,
            GLES20.GL_ONE_MINUS_SRC_ALPHA
        )
        GLES20.glUseProgram(programID)
        checkGlError("glUseProgram2")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureID)

        // 绑定mask纹理
        if (maskTextureId == -1) {
            maskTextureId = TextUtil.genBitmapTextureId(maskBitmap)
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, maskTextureId)
        GLES20.glUniform1i(uTextureHandle, 1)
        checkGlError("maskTextureId")
        maskVertices?.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            aPositionHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, maskVertices
        )

        GLES20.glEnableVertexAttribArray(aPositionHandle)
        checkGlError("aPositionHandle")

        triangleVertices?.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glVertexAttribPointer(
            aTextureHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices
        )
        GLES20.glEnableVertexAttribArray(aTextureHandle)
        checkGlError("aTextureHandle")

        Matrix.setIdentityM(mVPMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mVPMatrix, 0)
        GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, sTMatrix, 0)

        //switch
        GLES20.glUniform1f(switchHandle, 0.0f)
        checkGlError("switchHandle")
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //mask
        GLES20.glUniform1f(switchHandle, 1.0f)
        triangleVertices?.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            aPositionHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices
        )
        checkGlError("glVertexAttribPointer maPosition")
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        checkGlError("glEnableVertexAttribArray aPositionHandle")
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")

        //GLES20.glFinish()

        //todo-dq  任意位置添加文字 fix bug
        //mMaskRender?.renderFrame(textureID)
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

        uMVPMatrixHandle = GLES20.glGetUniformLocation(programID, "uMVPMatrix")
        checkGlError("glGetUniformLocation uMVPMatrix")
        if (uMVPMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uMVPMatrix")
        }

        uSTMatrixHandle = GLES20.glGetUniformLocation(programID, "uSTMatrix")
        checkGlError("glGetUniformLocation uSTMatrix")
        if (uSTMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uSTMatrix")
        }

        //new handle by dq
        uTextureHandle = GLES20.glGetUniformLocation(programID, "uTexture")
        checkGlError(" uTexture")
        if (uTextureHandle == -1) {
            throw RuntimeException("Could not get glGetUniformLocation uTexture")
        }

        switchHandle = GLES20.glGetUniformLocation(programID, "switchColor")
        checkGlError("glGetUniformLocation switchHandle")
        if (switchHandle == -1) {
            throw RuntimeException("Could not get switchHandle")
        }

        prepareSurface()

        mMaskRender = MaskRender()
        mMaskRender?.initMaskShader(true)
    }

    private var mMaskRender: MaskRender? = null

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
        canDraw.compareAndSet(false, true)
        Log.i(TAG, "onFirstFrame:    canDraw = " + canDraw.get())
        alphaVideoView.requestRender()
    }

    override fun onCompletion() {
        canDraw.compareAndSet(true, false)
        Log.i(TAG, "onCompletion:   canDraw = " + canDraw.get())
        alphaVideoView.requestRender()
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
     * create program with {@link vertex.glsl} and {@link frag.glsl}. If attach shader or link
     * program, it will return 0, else return program handle
     *
     * @return programID If link program success, it will return program handle, else return 0.
     */
    private fun createProgram(): Int {
        val vertexSource = ShaderUtils.loadFromAssetsFile(
            "vertex.glsl",
            alphaVideoView.getView().resources
        )
        val fragmentSource = ShaderUtils.loadFromAssetsFile(
            "frag.glsl",
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