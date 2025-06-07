/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package newcode.fun.utils.anim.animations;

public enum Direction {
    FORWARDS,
    BACKWARDS;


    public Direction opposite() {
        if (this == FORWARDS) {
            return BACKWARDS;
        }
        return FORWARDS;
    }
}

