#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
varying vec2 vMaskTextureCoord;
uniform samplerExternalOES sMaskTexture;
uniform sampler2D sTexture;

void main() {
    vec4 color = texture2D(sTexture, vTextureCoord);
    vec4 alpha = texture2D(sMaskTexture, vMaskTextureCoord);
    gl_FragColor = vec4(color.rgb, color.a * alpha.r);
}