package frc.robot.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

import com.ctre.phoenix.GadgeteerUartClient.GadgeteerConnection;

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
    BooleanSupplier inSensor, outSensor = () -> false;
    ArrayList<Boolean> inSensorCounts = new ArrayList<Boolean>(10);
    int outSensorCounts = 0;
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
        log = new MockLog();
        inSensor = () -> {
            if (inSensorCounts.size() == 0) return false;
            return inSensorCounts.get(0);
        };
        loader = new Loader(spinner, passthrough, paddle, inSensor, outSensor, led, dashboard, log);
    }

    @Test
    public void testSpinnerMotor() {
        // spinner.set(ControlMode.Velocity, 0);
        loader.setTargetSpinnerMotorRPM(600);
        assertEquals(600, loader.getTargetSpinnerMotorRPM(), 0.01);
    }

    @Test
    public void testPassthroughMotor() {
        // spinner.set(ControlMode.Velocity, 0);
        loader.setTargetPassthroughMotorOutput(0.5);
        assertEquals(0.5, loader.getTargetPassthroughMotorOutput(),0.01);
    }


    @Test
    public void testInitialBalls() {
        loader.setInitBallCount(3);
        assertEquals(3, loader.getCurrentBallCount());
    }

    @Test
    public void testCounting() {
        // inSensorCounts.addAll(Arrays.asList({true,false,true,false}));
    }
}

