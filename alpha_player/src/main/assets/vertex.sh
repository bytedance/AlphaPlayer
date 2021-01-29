attribute vec4 aPosition;
attribute vec2 aTextureCoord;
attribute vec2 aAlphaTextureCoord;
varying vec2 vTextureCoord;
varying vec2 vAlphaTextureCoord;

void main() {
    gl_Position = aPosition;
    vTextureCoord = aTextureCoord;
    vAlphaTextureCoord = aAlphaTextureCoord;
}