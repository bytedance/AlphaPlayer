#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES sTexture;
varying highp vec2 l_TexCoordinate;
varying highp vec2 r_TexCoordinate;

void main() {
    vec4 color = texture2D(sTexture, r_TexCoordinate);
    vec4 alpha = texture2D(sTexture, vec2(l_TexCoordinate.x, l_TexCoordinate.y));
    gl_FragColor = vec4(color.rgb, alpha.r);
}