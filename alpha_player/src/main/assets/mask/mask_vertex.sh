attribute vec4 aPosition;
attribute vec2 aTextureCoord;
attribute vec2 aMaskTextureCoord;
varying vec2 vTextureCoord;
varying vec2 vMaskTextureCoord;

void main() {
    gl_Position = aPosition;
    vTextureCoord = aTextureCoord;
    vMaskTextureCoord = aMaskTextureCoord;
}