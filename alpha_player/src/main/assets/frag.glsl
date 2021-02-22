#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform sampler2D uTexture;
uniform float switchColor;

void main() {
    vec4 colorout1 = vec4(1.0, 1.0, 1.0, 1.0);
    vec4 colorout2 = vec4(1.0, 1.0, 1.0, 1.0);
    vec4 color = texture2D(sTexture, vec2(vTextureCoord.x / 2.0, vTextureCoord.y));//左边视频
    vec4 color2Map = texture2D(sTexture, vec2(vTextureCoord.x/2.0 + 0.5, vTextureCoord.y));//右半边
    if (color2Map.g > 10.0/255.0) {//mask 透明
        colorout1 = vec4(color.rgb, 0.0);
    } else {
        colorout1 = vec4(color.rgb, color2Map.r);//red表示alpha
    }

    //mask position & color
    colorout2 = texture2D(uTexture, vec2(vTextureCoord.x, vTextureCoord.y));
    color2Map = texture2D(sTexture, vec2((vTextureCoord.x/2.0 - 0.25)*0.25 + 0.75, (vTextureCoord.y - 0.5)*0.15 + 0.5));

    if (color2Map.g < 10.0 / 255.0) {//遮罩处透明， 0是全透，1是完全不透
        colorout2.a = 0.0;
    }
    gl_FragColor = switchColor * colorout1 + (1.0 - switchColor) * colorout2;

}