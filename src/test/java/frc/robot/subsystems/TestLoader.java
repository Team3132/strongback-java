package frc.robot.subsystems;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockMotor;
import org.strongback.mock.MockSolenoid;

import frc.robot.interfaces.LEDStripInterface;
import frc.robot.mock.MockDashboard;
import frc.robot.mock.MockLog;

public class TestLoader {
    MockMotor spinner, passthrough;
    MockSolenoid paddle;
    Loader loader;
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
        loader.setTargetSpinnerMotorRPS(10);
        assertEquals(10, loader.getTargetSpinnerMotorRPS(), 0.01);
    }

    @Test
    public void testPassthroughMotor() {
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
        // TODO: Finishing Implementing functionality
    }
}

