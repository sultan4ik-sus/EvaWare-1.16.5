package newcode.fun.utils.render.shader;

import newcode.fun.utils.render.shader.core.Shader;
import newcode.fun.utils.render.shader.impl.AlphaShader;

import java.util.ArrayList;
import java.util.List;

public class Shaders {

    public List<Shader> shaderList = new ArrayList<>();

    public Shaders() {

        shaderList.addAll(List.of(
           new AlphaShader("")
        ));

    }

}
