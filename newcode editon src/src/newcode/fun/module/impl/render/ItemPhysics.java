package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;

import static newcode.fun.module.TypeList.*;

@Annotation(name = "ItemPhysics", type = Render, desc = "��������� ������ ��������� ��� ������������")
public class ItemPhysics extends Module {

    @Override
    public boolean onEvent(Event event) {

        return false;
    }
}

