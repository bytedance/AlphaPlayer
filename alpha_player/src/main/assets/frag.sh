#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
varying vec2 vAlphaTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
    vec4 color = texture2D(sTexture, vTextureCoord);
    vec4 alpha = texture2D(sTexture, vAlphaTextureCoord);
    gl_FragColor = vec4(color.rgb, alpha.r);
}