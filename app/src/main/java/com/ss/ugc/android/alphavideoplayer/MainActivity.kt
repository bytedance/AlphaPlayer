package com.ss.ugc.android.alphavideoplayer

import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ss.ugc.android.alpha_player.IMonitor
import com.ss.ugc.android.alpha_player.IPlayerAction
import com.ss.ugc.android.alpha_player.model.ScaleType
import com.ss.ugc.android.alpha_player.utils.SystemUtil
import com.ss.ugc.android.alphavideoplayer.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

/**
 * created by dengzhuoyao on 2020/07/08
 */
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val basePath = Environment.getExternalStorageDirectory().absolutePath
    private var mIsLandScape = false
    private var mScreenWidth = 0
    private var mScreenHeigth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mScreenWidth = SystemUtil.getScreenWidth(this)
        mScreenHeigth = SystemUtil.getScreenHeight(this)
        PermissionUtils.verifyStoragePermissions(this)
        initVideoGiftView()
    }

    private fun initVideoGiftView() {
        video_gift_view.initPlayerController(this, this, playerAction, monitor)

        video_gift_view.post {
            video_gift_view.attachView()
            startPlayVideo()
        }
    }

    private val playerAction = object : IPlayerAction {
        override fun onVideoSizeChanged(videoWidth: Int, videoHeight: Int, scaleType: ScaleType) {
            Log.i(
                TAG,
                "call onVideoSizeChanged(), videoWidth = $videoWidth, videoHeight = $videoHeight, scaleType = $scaleType"
            )
            mIsLandScape = SystemUtil.isLandscape(this@MainActivity)
            if (mIsLandScape) {
                val layoutParams = video_gift_view.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = mScreenWidth * (videoWidth / videoHeight) / 2
                layoutParams.height = mScreenWidth
                video_gift_view.requestLayout()
            }
        }

        override fun startAction() {
            Log.i(TAG, "call startAction()")
        }

        override fun endAction() {
            Log.i(TAG, "call endAction")
        }
    }

    private val monitor = object : IMonitor {
        override fun monitor(
            state: Boolean,
            playType: String,
            what: Int,
            extra: Int,
            errorInfo: String
        ) {
            Log.i(
                TAG,
                "call monitor(), state: $state, playType = $playType, what = $what, extra = $extra, errorInfo = $errorInfo"
            )
        }
    }

    fun attachView(v: View) {
        video_gift_view.attachView()
    }

    fun detachView(v: View) {
        video_gift_view.detachView()
    }

    fun playGift(v: View) {
        startPlayVideo()
    }

    private fun startPlayVideo() {
        val testPath = getResourcePath()
        Log.i("dzy", "play gift file path : $testPath")
        if ("" == testPath) {
            Toast.makeText(
                this,
                "please run 'gift_install.sh gift/demoRes' for load alphaVideo resource.",
                Toast.LENGTH_SHORT
            )
                .show()
        }
        video_gift_view.startVideoGift(testPath)
    }

    private fun getResourcePath(): String {
        val dirPath = basePath + File.separator + "alphaVideoGift" + File.separator
        val dirFile = File(dirPath)
        if (dirFile.exists() && dirFile.listFiles() != null && dirFile.listFiles().isNotEmpty()) {
            return dirFile.listFiles()[0].absolutePath
        }
        return ""
    }

    override fun onDestroy() {
        super.onDestroy()
        video_gift_view.releasePlayerController()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mIsLandScape = newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.i("dq-alpha", "onConfigurationChanged")
        if (mIsLandScape) {
            val layoutParams = video_gift_view.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = 750 / 2
            layoutParams.height = 750
            video_gift_view.requestLayout()
        }
    }
}
