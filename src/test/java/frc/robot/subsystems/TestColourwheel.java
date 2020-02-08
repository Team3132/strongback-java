package frc.robot.subsystems;

import static org.junit.Assert.assertEquals;
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
        
        colourWheel.enable();
        colourWheel.execute(0);
        assertEquals(0, motor.get(), 0.01);
        assertEquals(new ColourAction(Type.NONE, Colour.UNKNOWN), colourWheel.getDesiredAction());

        colourWheel.setDesiredAction(new ColourAction(Type.ADJUST_WHEEL_CLOCKWISE, Colour.UNKNOWN));
        colourWheel.execute(0);
        assertEquals(-Constants.COLOUR_WHEEL_MOTOR_ADJUST, motor.get(), 0.01);

        colourWheel.disable();
        colourWheel.execute(0);
        assertEquals(0, motor.get(), 0.01);
        assertEquals(new ColourAction(Type.NONE, Colour.UNKNOWN), colourWheel.getDesiredAction());
    }

}