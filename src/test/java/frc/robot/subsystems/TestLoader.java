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

import frc.robot.interfaces.LEDStrip;
import frc.robot.mock.MockDashboardImpl;

public class TestLoader {
    MockMotor spinner, passthrough;
    MockSolenoid paddle;
    LoaderImpl loader;
    BooleanSupplier inSensor, outSensor = () -> false;
    ArrayList<Boolean> inSensorCounts = new ArrayList<Boolean>(10);
    int outSensorCounts = 0;
    LEDStrip led;
    MockDashboardImpl dashboard;
    MockClock clock;

    @Before
    public void setup() {
        spinner = Mock.stoppedMotor();
        passthrough = Mock.stoppedMotor();
        led = Mock.ledStrip(); 
        clock = Mock.clock();
        inSensor = () -> {
            if (inSensorCounts.size() == 0) return false;
            return inSensorCounts.get(0);
        };
        loader = new LoaderImpl(spinner, passthrough, paddle, inSensor, outSensor, led, dashboard);
    }

    @Test
    public void testSpinnerMotor() {
        loader.setTargetSpinnerRPS(10);
        assertEquals(10, loader.getTargetSpinnerRPS(), 0.01);
    }

    @Test
    public void testPassthroughMotor() {
        loader.setTargetPassthroughDutyCycle(0.5);
        assertEquals(0.5, loader.getTargetPassthroughDutyCycle(),0.01);
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

