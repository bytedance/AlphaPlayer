package com.ss.ugc.android.alpha_player.player

import android.content.Context

/**
 * created by dengzhuoyao on 2020/07/07
 */
abstract class AbsPlayer(context: Context? = null) : IMediaPlayer {

    var completionListener: IMediaPlayer.OnCompletionListener? = null
    var preparedListener: IMediaPlayer.OnPreparedListener? = null
    var errorListener: IMediaPlayer.OnErrorListener? = null
    var firstFrameListener: IMediaPlayer.OnFirstFrameListener? = null

    override fun setOnCompletionListener(completionListener: IMediaPlayer.OnCompletionListener) {
        this.completionListener = completionListener
    }

    override fun setOnPreparedListener(preparedListener: IMediaPlayer.OnPreparedListener) {
        this.preparedListener = preparedListener
    }

    override fun setOnErrorListener(errorListener: IMediaPlayer.OnErrorListener) {
        this.errorListener = errorListener
    }

    override fun setOnFirstFrameListener(firstFrameListener: IMediaPlayer.OnFirstFrameListener) {
        this.firstFrameListener = firstFrameListener
    }
}