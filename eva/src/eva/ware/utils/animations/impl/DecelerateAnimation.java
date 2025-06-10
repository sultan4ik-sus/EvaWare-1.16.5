package eva.ware.utils.animations.impl;


import eva.ware.utils.animations.AnimationUtility;
import eva.ware.utils.animations.Direction;

public class DecelerateAnimation extends AnimationUtility {

    public DecelerateAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public DecelerateAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        double x1 = x / duration;
        return 1 - ((x1 - 1) * (x1 - 1));
    }
}
