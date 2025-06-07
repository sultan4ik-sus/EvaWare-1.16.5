package baritone.utils;

import baritone.api.utils.input.Input;
import net.minecraft.util.MovementInput;

public class PlayerMovementInput extends MovementInput {

    private final InputOverrideHandler handler;

    PlayerMovementInput(InputOverrideHandler handler) {
        this.handler = handler;
    }

    @Override
    public void tickMovement(boolean p_225607_1_) {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        this.jump = handler.isInputForcedDown(Input.JUMP);

        if (this.forwardKeyDown = handler.isInputForcedDown(Input.MOVE_FORWARD)) {
            this.moveForward++;
        }

        if (this.backKeyDown = handler.isInputForcedDown(Input.MOVE_BACK)) {
            this.moveForward--;
        }

        if (this.leftKeyDown = handler.isInputForcedDown(Input.MOVE_LEFT)) {
            this.moveStrafe++;
        }

        if (this.rightKeyDown = handler.isInputForcedDown(Input.MOVE_RIGHT)) {
            this.moveStrafe--;
        }

        if (this.sneaking = handler.isInputForcedDown(Input.SNEAK)) {
            this.moveStrafe *= 0.3D;
            this.moveForward *= 0.3D;
        }
    }
}
