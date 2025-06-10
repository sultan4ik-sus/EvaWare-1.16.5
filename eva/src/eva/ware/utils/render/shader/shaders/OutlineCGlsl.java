package eva.ware.utils.render.shader.shaders;

import eva.ware.utils.render.shader.IShader;

public class OutlineCGlsl implements IShader {
    @Override
    public String glsl() {
        return """
            #version 120
            
            uniform vec4 color;
            uniform sampler2D textureIn, textureToCheck;
            uniform vec2 texelSize, direction;
            uniform float size;
            
            #define offset direction * texelSize
            
            void main() {
                if (direction.y == 1) {
                    if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;
                }
            
                vec4 innerAlpha = texture2D(textureIn, gl_TexCoord[0].st);
                innerAlpha *= innerAlpha.a;
                for (float r = 1.0; r <= size; r ++) {
                    vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);
                    vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);
                    colorCurrent1.rgb *= colorCurrent1.a;
                    colorCurrent2.rgb *= colorCurrent2.a;
                    innerAlpha += (colorCurrent1 + colorCurrent2) * r;
                }
                gl_FragColor = vec4(innerAlpha.rgb / innerAlpha.a, mix(innerAlpha.a, 1.0 - exp(-innerAlpha.a), step(0.0, direction.y)));
            }""";
    }
}
