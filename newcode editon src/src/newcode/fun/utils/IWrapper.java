package newcode.fun.utils;

import com.mojang.blaze3d.systems.IRenderCall;
import newcode.fun.utils.render.KawaseBlur;

import java.util.LinkedList;
import java.util.Queue;

public interface IWrapper extends IMinecraft {

    Queue<IRenderCall> blurQueue = new LinkedList<>();
    static void executeQueue(boolean blur) {

        KawaseBlur.applyBlur(() -> {
            while (!blurQueue.isEmpty()) {
                blurQueue.poll().execute();
            }
        }, 2, 3);
    }
}