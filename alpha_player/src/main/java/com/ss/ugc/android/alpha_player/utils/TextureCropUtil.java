package com.ss.ugc.android.alpha_player.utils;


import com.ss.ugc.android.alpha_player.model.ScaleType;

/**
 * created by dengzhuoyao on 2020/07/07
 */
public class TextureCropUtil {

    public static float[] calculateHalfRightVerticeData(ScaleType scaleType,
                                                        float viewWidth, float viewHeight,
                                                        float videoWidth, float videoHeight) {
        float[] result;
        float currentRatio = viewWidth / viewHeight;
        float videoRatio = videoWidth / videoHeight;
        float ratioX, ratioY;
        float leftRatio, rightRatio, topRatio, bottomRatio;

        if (currentRatio > videoRatio) {
            // 宽度缩放到viewWidth，裁剪上下
            videoWidth = viewWidth;
            videoHeight = videoWidth / videoRatio;
            ratioX = 0;
            ratioY = (1 - (viewHeight / videoHeight)) / 2;

        } else {
            // 高度缩放到viewHeight，裁剪左右
            videoHeight = viewHeight;
            videoWidth = videoHeight * videoRatio;
            ratioX = (1 - (viewWidth / videoWidth)) / 2;
            ratioY = 0;
        }

        switch (scaleType) {
            case ScaleAspectFitCenter:
                //  等比例缩放对齐全屏，屏幕多余部分留空
                if (currentRatio > videoRatio) {
                    // 高度缩放到viewHeight，计算两边留空位置
                    videoHeight = viewHeight;
                    videoWidth = videoHeight * videoRatio;
                    ratioX = (1 - (videoWidth / viewWidth)) / 2;
                    ratioY = 0;
                } else {
                    // 宽度缩放到viewWidth，计算上下留空位置
                    videoWidth = viewWidth;
                    videoHeight = videoWidth / videoRatio;
                    ratioX = 0;
                    ratioY = (1 - (videoHeight / viewHeight)) / 2;
                }
                result = getZoomData(ratioX, ratioY, ratioX, ratioY);
                break;
            case ScaleAspectFill:
                //  等比例缩放铺满全屏，裁剪视频多余部分
                leftRatio = ratioX;
                rightRatio = ratioX;
                topRatio = ratioY;
                bottomRatio = ratioY;
                result = getCropData(leftRatio, topRatio, rightRatio, bottomRatio);
                break;
            case TopFill:
                leftRatio = ratioX;
                rightRatio = ratioX;
                topRatio = 0;
                bottomRatio = ratioY * 2;
                result = getCropData(leftRatio, topRatio, rightRatio, bottomRatio);
                break;
            case BottomFill:
                leftRatio = ratioX;
                rightRatio = ratioX;
                topRatio = ratioY * 2;
                bottomRatio = 0;
                result = getCropData(leftRatio, topRatio, rightRatio, bottomRatio);
                break;
            case LeftFill:
                leftRatio = 0;
                rightRatio = ratioX * 2;
                topRatio = ratioY;
                bottomRatio = ratioY;
                result = getCropData(leftRatio, topRatio, rightRatio, bottomRatio);
                break;
            case RightFill:
                leftRatio = ratioX * 2;
                rightRatio = 0;
                topRatio = ratioY;
                bottomRatio = ratioY;
                result = getCropData(leftRatio, topRatio, rightRatio, bottomRatio);
                break;
            case TopFit:
                // 宽度缩放到viewWidth，计算底部留空位置
                videoWidth = viewWidth;
                videoHeight = videoWidth / videoRatio;
                ratioY = (1 - (videoHeight / viewHeight)) / 2;
                result = getZoomData(0, 0, 0, ratioY * 2);
                break;
            case BottomFit:
                // 宽度缩放到viewWidth，计算顶部留空位置
                videoWidth = viewWidth;
                videoHeight = videoWidth / videoRatio;
                ratioY = (1 - (videoHeight / viewHeight)) / 2;
                result = getZoomData(0, ratioY * 2, 0, 0);
                break;
            case LeftFit:
                // 高度缩放到viewHeight，计算右边留空位置
                videoHeight = viewHeight;
                videoWidth = videoHeight * videoRatio;
                ratioX = (1 - (videoWidth / viewWidth)) / 2;
                result = getZoomData(0, 0, ratioX * 2, 0);
                break;
            case RightFit:
                // 高度缩放到viewHeight，计算左边留空位置
                videoHeight = viewHeight;
                videoWidth = videoHeight * videoRatio;
                ratioX = (1 - (videoWidth / viewWidth)) / 2;
                result = getZoomData(ratioX * 2, 0, 0, 0);
                break;
            default:
                //  全屏拉伸铺满
                leftRatio = 0;
                rightRatio = 0;
                topRatio = 0;
                bottomRatio = 0;
                result = getCropData(leftRatio, topRatio, rightRatio, bottomRatio);
                break;
        }

        return result;
    }

    private static float[] getZoomData(float leftZoomRatio, float topZoomRatio, float rightZoomRatio, float bottomZoomRatio) {
        return new float[] {
                // X, Y, Z, U, V
                -1.0f + leftZoomRatio * 2, -1.0f + bottomZoomRatio * 2, 0, 0.5f, 0.f,
                1.0f - rightZoomRatio * 2, -1.0f + bottomZoomRatio * 2, 0, 1.f, 0.f,
                -1.0f + leftZoomRatio * 2, 1.0f - topZoomRatio * 2, 0, 0.5f, 1.f,
                1.0f - rightZoomRatio * 2, 1.0f - topZoomRatio * 2, 0, 1.f, 1.f,
        };
    }

    private static float[] getCropData(float leftCropRatio, float topCropRatio, float rightCropRatio, float bottomCropRatio) {
        return new float[] {
                // X, Y, Z, U, V
                -1.0f,  -1.0f,  0, 0.5f + leftCropRatio / 2,    0.f + bottomCropRatio,
                1.0f,   -1.0f,  0, 1.0f - rightCropRatio / 2,   0.f + bottomCropRatio,
                -1.0f,  1.0f,   0, 0.5f + leftCropRatio / 2,    1.f - topCropRatio,
                1.0f,   1.0f,   0, 1.0f - rightCropRatio / 2,   1.f - topCropRatio,
        };
    }
}
