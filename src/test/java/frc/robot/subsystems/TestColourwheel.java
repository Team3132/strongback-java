package frc.robot.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockMotor;

import frc.robot.Constants;
import frc.robot.interfaces.ColourWheelInterface;
import frc.robot.interfaces.ColourWheelInterface.Colour;
import frc.robot.interfaces.ColourWheelInterface.ColourAction;
import frc.robot.interfaces.ColourWheelInterface.ColourAction.ColourWheelType;
import frc.robot.mock.MockDashboard;
import frc.robot.mock.MockLog;

public class TestColourwheel {


    Colour colour;
    MockMotor motor;
    MockClock clock;
    ColourWheelInterface colourWheel;

    @Before
    public void setup() {
        colour = Colour.UNKNOWN;
        motor = Mock.stoppedMotor();
        clock = Mock.clock();
        colourWheel = new ColourWheel(motor, () -> colour, clock, new MockDashboard(), new MockLog());
    }


    @Test
    public void testEnableDisable() {
        // Should start with no action and no output
        colourWheel.execute(0);
        assertEquals(0, motor.get(), 0.01);
        assertEquals(new ColourAction(ColourWheelType.NONE, Colour.UNKNOWN), colourWheel.getDesiredAction());
        assertTrue(colourWheel.isFinished());
        
        colourWheel.enable();
        colourWheel.execute(0);
        assertEquals(0, motor.get(), 0.01);
        assertEquals(new ColourAction(ColourWheelType.NONE, Colour.UNKNOWN), colourWheel.getDesiredAction());
        assertTrue(colourWheel.isFinished());

        colourWheel.setDesiredAction(new ColourAction(ColourWheelType.ADJUST_WHEEL_CLOCKWISE, Colour.UNKNOWN));
        colourWheel.execute(0);
        assertEquals(-Constants.COLOUR_WHEEL_MOTOR_ADJUST, motor.get(), 0.01);
        assertFalse(colourWheel.isFinished());

        colourWheel.disable();
        colourWheel.execute(0);
        assertEquals(0, motor.get(), 0.01);
        assertEquals(new ColourAction(ColourWheelType.NONE, Colour.UNKNOWN), colourWheel.getDesiredAction());
        assertTrue(colourWheel.isFinished());
    }

    @Test
    public void testAdjustClockwise() {
        colourWheel.enable();
        colourWheel.setDesiredAction(new ColourAction(ColourWheelType.ADJUST_WHEEL_CLOCKWISE, Colour.UNKNOWN));
        colourWheel.execute(0);
        assertEquals(-Constants.COLOUR_WHEEL_MOTOR_ADJUST, motor.get(), 0.01);
        assertFalse(colourWheel.isFinished());
    }

    @Test
    public void testAdjustAnticlockwise() {
        colourWheel.enable();
        colourWheel.setDesiredAction(new ColourAction(ColourWheelType.ADJUST_WHEEL_ANTICLOCKWISE, Colour.UNKNOWN));
        colourWheel.execute(0);
        assertEquals(Constants.COLOUR_WHEEL_MOTOR_ADJUST, motor.get(), 0.01);
        assertFalse(colourWheel.isFinished());
    }

    @Test
    public void testRotational() {
        for (int i = 0; i < 4; i++) {
            doRotational(i);
        }
    }

    @Test
    public void testPositional() {
        doPositional(Colour.GREEN, Colour.GREEN, 0);
        doPositional(Colour.GREEN, Colour.RED, 1);
        doPositional(Colour.GREEN, Colour.BLUE, 1);
        doPositional(Colour.GREEN, Colour.YELLOW, 2);
        doPositional(Colour.BLUE, Colour.BLUE, 0);
        doPositional(Colour.BLUE, Colour.GREEN, 1);
        doPositional(Colour.BLUE, Colour.YELLOW, 1);
        doPositional(Colour.BLUE, Colour.RED, 2);
        doPositional(Colour.YELLOW, Colour.YELLOW, 0);
        doPositional(Colour.YELLOW, Colour.BLUE, 1);
        doPositional(Colour.YELLOW, Colour.GREEN, 2);
        doPositional(Colour.YELLOW, Colour.RED, 1);
        doPositional(Colour.RED, Colour.RED, 0);
        doPositional(Colour.RED, Colour.YELLOW, 1);
        doPositional(Colour.RED, Colour.BLUE, 2);
        doPositional(Colour.RED, Colour.GREEN, 1);
    }

    public void doRotational(int x) {
        colourWheel.enable();
        colourWheel.setDesiredAction(new ColourAction(ColourWheelType.ROTATION, Colour.UNKNOWN));
        for (int i = 0; i < Constants.COLOUR_WHEEL_ROTATION_TARGET; i++) {
            colour = Colour.of(3-((i+x) % 4));
            colourWheel.execute(0);
            assertEquals(Constants.COLOUR_WHEEL_MOTOR_FULL, motor.get(), 0.01);
            assertFalse(colourWheel.isFinished());
        }
        colour = Colour.of(3-((Constants.COLOUR_WHEEL_ROTATION_TARGET + x) % 4));
        colourWheel.execute(0);
        assertEquals(Constants.COLOUR_WHEEL_MOTOR_OFF, motor.get(), 0.01);
        assertTrue(colourWheel.isFinished());
    }

    public void doPositional(Colour desired, Colour start, int amount) {
        colourWheel.enable();
        colourWheel.setDesiredAction(new ColourAction(ColourWheelType.POSITION, desired));
        colour = start;
        colourWheel.execute(0);
        if (!desired.equals(start)) {
            assertEquals(Math.signum(motor.get())*Constants.COLOUR_WHEEL_MOTOR_FULL, motor.get(), 0.01);
            assertFalse(colourWheel.isFinished());
            for (int i = 0; i < amount; i++) {
                colour = Colour.of((colour.id + Colour.NUM_COLOURS + -(int) Math.signum(motor.get())) % Colour.NUM_COLOURS);
                colourWheel.execute(0);
                assertEquals(Math.signum(motor.get())*Constants.COLOUR_WHEEL_MOTOR_FULL, motor.get(), 0.01);
                assertFalse(colourWheel.isFinished());
            }
        }
        clock.incrementByMilliseconds(50);
        colourWheel.execute(0);
        assertEquals(Constants.COLOUR_WHEEL_MOTOR_OFF, motor.get(), 0.01);
        assertTrue(colourWheel.isFinished());
    }
}