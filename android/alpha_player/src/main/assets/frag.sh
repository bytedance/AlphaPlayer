#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
    vec4 color = texture2D(sTexture, vTextureCoord);
    vec4 color2Map = vec4(1.0, 1.0, 1.0, 1.0);
    if (vTextureCoord.x >= 0.5) {
        color2Map = texture2D(sTexture, vec2(vTextureCoord.x - 0.5, vTextureCoord.y));
        gl_FragColor = vec4(color.r, color.g, color.b, color2Map.g);
    } else {
        gl_FragColor = vec4(color.r, color.g, color.b, color.a);
    }
}