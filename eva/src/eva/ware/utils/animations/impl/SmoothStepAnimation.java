package eva.ware.utils.animations.impl;

import eva.ware.utils.animations.AnimationUtility;
import eva.ware.utils.animations.Direction;

public class SmoothStepAnimation extends AnimationUtility {

    public SmoothStepAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public SmoothStepAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        double x1 = x / (double) duration; //Used to force input to range from 0 - 1
        return -2 * Math.pow(x1, 3) + (3 * Math.pow(x1, 2));
    }

}
