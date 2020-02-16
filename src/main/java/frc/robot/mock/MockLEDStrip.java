package frc.robot.mock;

import frc.robot.interfaces.LEDStripInterface;

// LED Strip Subsystem 2020

public class MockLEDStrip implements LEDStripInterface {

    @Override
    public void setColour(Colour c) {}

    @Override
    public void setProgressColour(Colour c1, Colour c2, int percent) {}

    @Override
    public void setIdle() {}

    @Override
    public void setData() {}
}