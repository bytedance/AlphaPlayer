package com.ss.ugc.android.alpha_player.model

/**
 * created by dengzhuoyao on 2020/07/07
 *
 * A enum class that describe the alpha-video crop type. The specific
 * relationship can be viewd at {@link TextureCropUtil}
 */
enum class ScaleType(index: Int) {

    ScaleToFill(0),             //  拉伸铺满全屏
    ScaleAspectFitCenter(1),    //  等比例缩放对齐全屏，居中，屏幕多余留空
    ScaleAspectFill(2),         //  等比例缩放铺满全屏，裁剪视频多余部分
    TopFill(3),                 //  等比例缩放铺满全屏，顶部对齐
    BottomFill(4),              //  等比例缩放铺满全屏，底部对齐
    LeftFill(5),                //  等比例缩放铺满全屏，左边对齐
    RightFill(6),               //  等比例缩放铺满全屏，右边对齐
    TopFit(7),                  //  等比例缩放至屏幕宽度，顶部对齐，底部留空
    BottomFit(8),               //  等比例缩放至屏幕宽度，底部对齐，顶部留空
    LeftFit(9),                 //  等比例缩放至屏幕高度，左边对齐，右边留空
    RightFit(10);               //  等比例缩放至屏幕高度，右边对齐，左边留空

    companion object {
        fun convertFrom(index: Int) : ScaleType{
            val result: ScaleType = when(index) {
                1 -> ScaleAspectFitCenter
                2 -> ScaleAspectFill
                3 -> TopFill
                4 -> BottomFill
                5 -> LeftFill
                6 -> RightFill
                7 -> TopFit
                8 -> BottomFit
                9 -> LeftFit
                10 -> RightFit
                else -> ScaleToFill
            }
            return result
        }
    }
}