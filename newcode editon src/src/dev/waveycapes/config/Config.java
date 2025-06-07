package dev.waveycapes.config;

import dev.waveycapes.CapeMovement;
import dev.waveycapes.CapeStyle;

public class Config {
    public CapeStyle capeStyle = CapeStyle.SMOOTH;
    public CapeMovement capeMovement = CapeMovement.BASIC_SIMULATION;
    public int gravity = 25;
    public int heightMultiplier = 6;
    public int straveMultiplier = 2;
}
