package eva.ware.utils.render.shader.shaders;

import eva.ware.utils.render.shader.IShader;

public class GradientGlsl implements IShader {
    @Override
    public String glsl() {
        return """
                        #version 120
                                                
                        uniform vec2 location, rectSize;
                        uniform sampler2D tex;
                        uniform vec4 color1, color2, color3, color4;
                                                
                        #define NOISE .5/255.0
                                                
                        vec3 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){
                            vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
                            //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/
                            color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));
                            return color;
                        }
                        void main() {
                            vec2 coords = (gl_FragCoord.xy - location) / rectSize;
                            float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a;
                            gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4).rgb, texColorAlpha);
                        }
                        """;
    }
}
