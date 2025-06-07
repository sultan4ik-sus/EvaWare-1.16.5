package newcode.fun.utils.render.points;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.module.impl.render.Particles;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.render.animation.AnimationMath;
import newcode.fun.utils.world.WorldUtils;

import java.util.concurrent.ThreadLocalRandom;

public final class Point {
    private final Particles this$0;
    public Vector3d position;
    public Vector3d motion;
    public Vector3d animatedMotion;
    public long aliveTime;
    public float size;
    public long createdTime;
    public float rotation; // Угол вращения для конкретного объекта

    public Point(Particles var1, Vector3d position2) {
        this.this$0 = var1;
        this.createdTime = System.currentTimeMillis();
        this.position = new Vector3d(position2.x, position2.y, position2.z);
        this.motion = new Vector3d(ThreadLocalRandom.current().nextFloat(-0.01f, 0.01f), 0.0, ThreadLocalRandom.current().nextFloat(-0.01f, 0.01f));
        this.animatedMotion = new Vector3d(0.0, 0.0, 0.0);
        this.size = ThreadLocalRandom.current().nextFloat(4.0f, 5.0f);
        this.aliveTime = ThreadLocalRandom.current().nextLong(5000L, 6000L);
        this.rotation = (float) (Math.random() * 360.0);
    }

    public void update() {
        float physicsFactor = this.this$0.fizika.getValue().floatValue(); // Получаем значение слайдера "Физика"

        if (physicsFactor == 0) {

        } else {
            // Учитываем физику в движении
            if (this.isGround()) {
                this.motion.y = 2 * physicsFactor; // Чем выше "Физика", тем сильнее отскок
                Vector3d var10000 = this.motion;
                var10000.x *= 1.1 * physicsFactor;
                var10000 = this.motion;
                var10000.z *= 1.1 * physicsFactor;
            } else {
                this.motion.y = -0.01 * physicsFactor;
                Vector3d var10000 = this.motion;
                var10000.y *= 2.0 * physicsFactor;
            }
            this.animatedMotion.x = AnimationMath.fast((float)this.animatedMotion.x, (float)this.motion.x, 3.0f);
            this.animatedMotion.y = AnimationMath.fast((float)this.animatedMotion.y, (float)this.motion.y, 3.0f);
            this.animatedMotion.z = AnimationMath.fast((float)this.animatedMotion.z, (float)this.motion.z, 3.0f);
            this.position = this.position.add(this.animatedMotion);
        }
    }


    boolean isGround() {
        Vector3d position2 = this.position.add(this.animatedMotion);
        AxisAlignedBB bb = new AxisAlignedBB(position2.x - 0.1, position2.y - 0.1, position2.z - 0.1, position2.x + 0.1, position2.y + 0.1, position2.z + 0.1);
        return WorldUtils.TotemUtil.getSphere(new BlockPos(position2), 2.0f, 4, false, true, 0).stream().anyMatch(blockPos -> !IMinecraft.mc.world.getBlockState((BlockPos)blockPos).isAir() && bb.intersects(new AxisAlignedBB((BlockPos)blockPos)) && AxisAlignedBB.calcSideHit(new AxisAlignedBB(blockPos.add(0, 1, 0)), position2, new double[]{2.0}, (Direction)null, (double)0.1f, (double)0.1f, (double)0.1f) == Direction.DOWN);
    }
}
