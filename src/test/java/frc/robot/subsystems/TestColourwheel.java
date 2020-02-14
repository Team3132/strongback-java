package frc.robot.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockMotor;

import frc.robot.Constants;
import frc.robot.interfaces.ColourWheelInterface;
import frc.robot.interfaces.ColourWheelInterface.Colour;
import frc.robot.interfaces.ColourWheelInterface.ColourAction;
import frc.robot.interfaces.ColourWheelInterface.ColourAction.Type;
import frc.robot.mock.MockDashboard;
import frc.robot.mock.MockLog;

public class TestColourwheel {


    Colour colour;
    MockMotor motor;
    ColourWheelInterface colourWheel;

    @Before
    public void setup() {
        colour = Colour.UNKNOWN;
        motor = Mock.stoppedMotor();
        colourWheel = new ColourWheel(motor, () -> colour, new MockDashboard(), new MockLog());
    }


    @Test
    public void testEnableDisable() {
        // Should start with no action and no output
        colourWheel.execute(0);
        assertEquals(0, motor.get(), 0.01);
        assertEquals(new ColourAction(Type.NONE, Colour.UNKNOWN), colourWheel.getDesiredAction());
        assertTrue(colourWheel.isFinished());
        
        colourWheel.enable();
        colourWheel.execute(0);
        assertEquals(0, motor.get(), 0.01);
        assertEquals(new ColourAction(Type.NONE, Colour.UNKNOWN), colourWheel.getDesiredAction());
        assertTrue(colourWheel.isFinished());

        colourWheel.setDesiredAction(new ColourAction(Type.ADJUST_WHEEL_CLOCKWISE, Colour.UNKNOWN));
        colourWheel.execute(0);
        assertEquals(-Constants.COLOUR_WHEEL_MOTOR_ADJUST, motor.get(), 0.01);
        assertFalse(colourWheel.isFinished());

        colourWheel.disable();
        colourWheel.execute(0);
        assertEquals(0, motor.get(), 0.01);
        assertEquals(new ColourAction(Type.NONE, Colour.UNKNOWN), colourWheel.getDesiredAction());
        assertTrue(colourWheel.isFinished());
    }

    @Test
    public void testAdjustClockwise() {
        colourWheel.enable();
        colourWheel.setDesiredAction(new ColourAction(Type.ADJUST_WHEEL_CLOCKWISE, Colour.UNKNOWN));
        colourWheel.execute(0);
        assertEquals(-Constants.COLOUR_WHEEL_MOTOR_ADJUST, motor.get(), 0.01);
        assertFalse(colourWheel.isFinished());
    }

    @Test
    public void testAdjustAnticlockwise() {
        colourWheel.enable();
        colourWheel.setDesiredAction(new ColourAction(Type.ADJUST_WHEEL_ANTICLOCKWISE, Colour.UNKNOWN));
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
    public void testPositionalGreenBlue() {
        doPositional(Colour.GREEN, Colour.BLUE);
    }

    public void doRotational(int x) {
        colourWheel.enable();
        colourWheel.setDesiredAction(new ColourAction(Type.ROTATION, Colour.UNKNOWN));
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

    public void doPositional(Colour desired, Colour start) {
        colourWheel.enable();
        colourWheel.setDesiredAction(new ColourAction(Type.POSITION, desired));
        colour = start;
        colourWheel.execute(0);
        if (desired.equals(start)) {
            assertEquals(Constants.COLOUR_WHEEL_MOTOR_OFF, motor.get(), 0.01);
            assertTrue(colourWheel.isFinished());
        } else {
            assertEquals(Constants.COLOUR_WHEEL_MOTOR_FULL, motor.get(), 0.01);
            if (Math.abs(motor.get()) > 0.3) {
                colour = Colour.of(colour.id + Colour.NUM_COLOURS + (int) Math.signum(motor.get()) % Colour.NUM_COLOURS);
            }
            assertEquals(Constants.COLOUR_WHEEL_MOTOR_FULL, motor.get(), 0.01);
        }
    }
}