package frc.robot.mock;
import frc.robot.lib.Subsystem;

import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.LEDStripInterface;
import frc.robot.interfaces.Log;

// LED Strip Subsystem 2020

public class MockLEDStrip extends Subsystem implements LEDStripInterface, DashboardUpdater {

    public MockLEDStrip(int PWM_Port, int numberOfLEDs, DashboardInterface dashboard, Log log) {
        super("LED Strip", dashboard, log);   

    }

    public LEDStripInterface setColour(int index, int r, int g, int b) {
        return this;
    }

    public LEDStripInterface setColour(int indexS, int indexE, int r, int g, int b) {
        return this;
    }

    public LEDStripInterface setProgressColour(int current, int total) {
        return this;
    }

    public LEDStripInterface setDefault() {
        return this;
    }

    public LEDStripInterface setData() {
        return this;
    }

    public void updateDashboard() {
		// do nothing by default
    }
    
    @Override
    public LEDStripInterface setDesiredAction(LEDAction action) {
        //this.action = action;
        return this;
    }
    
}