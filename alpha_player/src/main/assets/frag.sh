#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform sampler2D uTexture;
uniform float switchColor;

void main() {
    //vec4 color = texture2D(sTexture, vTextureCoord);
     vec4 color = switchColor * texture2D(sTexture, vTextureCoord) + (1.0 - switchColor) * texture2D(uTexture, vTextureCoord);

    vec4 color2 = texture2D(uTexture, vTextureCoord);
    vec4 color2Map = vec4(1.0, 1.0, 1.0, 1.0);
    if (vTextureCoord.x <= 0.5) {
        color2Map = texture2D(sTexture, vec2(vTextureCoord.x+0.5, vTextureCoord.y));
        if (color2Map.g > 10.0/255.0) {
           gl_FragColor = vec4(color2.rgb, color2Map.r);
        }else{
          gl_FragColor = vec4(color.rgb, color2Map.r);
        }
    } else {
        gl_FragColor = vec4(color.r, color.g, color.b, color.a);
    }
}