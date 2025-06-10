package eva.ware.utils.render.shader;

public interface IShader {

    String glsl();

    default String getName() {
        return "SHADERNONAME";
    }

}
