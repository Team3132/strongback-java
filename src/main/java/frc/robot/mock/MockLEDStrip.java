package frc.robot.mock;

import frc.robot.interfaces.LEDStripInterface;
import frc.robot.lib.LEDColour;

// LED Strip Subsystem 2020

public class MockLEDStrip implements LEDStripInterface {

    @Override
    public void setColour(LEDColour c) {}

    @Override
    public void setProgressColour(LEDColour c1, LEDColour c2, int percent) {}

    @Override
    public void setIdle() {}
}