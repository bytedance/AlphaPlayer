uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;

attribute vec4 aPosition;
attribute vec4 aTextureCoord;

varying highp vec2 l_TexCoordinate;
varying highp vec2 r_TexCoordinate;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    r_TexCoordinate = (uSTMatrix * aTextureCoord).xy;
    float midX = (uSTMatrix * vec4(0.5, 0.0, 0.0, 1.0)).x;
    l_TexCoordinate = vec2(r_TexCoordinate.x - midX, r_TexCoordinate.y);
}