package com.ss.ugc.android.alpha_player

import com.ss.ugc.android.alpha_player.model.ScaleType

/**
 * created by dengzhuoyao on 2020/07/08
 *
 * Interface definition for a callback to be invoked when a player on some condition.
 */
interface IPlayerAction {

    /**
     * Called when the media source is prepared.
     *
     * @param videoWidth    the media source width information.
     * @param videoHeight   the media source height information.
     * @param scaleType     the scale type be defined.
     */
    fun onVideoSizeChanged(videoWidth: Int, videoHeight: Int, scaleType: ScaleType)

    /**
     * Called when the media source is ready to start.
     */
    fun startAction()

    /**
     * Called when the end of a media source is reached during playback.
     */
    fun endAction()
}