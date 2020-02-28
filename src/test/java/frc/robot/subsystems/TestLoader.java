package frc.robot.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.BooleanSupplier;

import org.junit.Before;
import org.junit.Test;
import org.strongback.components.Motor.ControlMode;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockMotor;
import org.strongback.mock.MockSolenoid;

import frc.robot.Constants;
import frc.robot.subsystems.Loader;
import frc.robot.interfaces.LEDStripInterface;
import frc.robot.interfaces.LoaderInterface;
import frc.robot.mock.MockDashboard;
import frc.robot.mock.MockLog;

public class TestLoader {
    MockMotor spinner, passthrough;
    MockSolenoid paddle;
    LoaderInterface loader;
    BooleanSupplier inSensor, outSensor;
    LEDStripInterface led;
    MockDashboard dashboard;
    MockLog log;
    MockClock clock;

    @Before
    public void setup() {
        spinner = Mock.stoppedMotor();
        passthrough = Mock.stoppedMotor();
        led = Mock.ledStrip(); 
        clock = Mock.clock();
        loader = new Loader(spinner, passthrough, paddle, inSensor, outSensor, led, dashboard, log);
    }

    @Test 
    public void spinnerVelocity() {
        spinner.enable();
        spinner.set(ControlMode.Velocity, 0);
        assertEquals(0, spinner.getVelocity(), 0.01);
    }
    
    @Test 
    public void passthroughVelocity() {
        passthrough.enable();
        passthrough.set(ControlMode.Velocity, 0);
        
        passthrough.set(ControlMode.Velocity, 600);
        assertEquals(600, spinner.getVelocity(), 0.01);
    }
}

