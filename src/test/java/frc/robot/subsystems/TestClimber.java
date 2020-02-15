package frc.robot.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.strongback.Executable;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockMotor;
import org.strongback.components.Clock;

import frc.robot.Constants;
import frc.robot.subsystems.Climber;
import frc.robot.interfaces.ClimberInterface;
import frc.robot.interfaces.ClimberInterface.ClimberAction;
import frc.robot.interfaces.ClimberInterface.ClimberAction.ClimberType;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;
import frc.robot.mock.MockDashboard;
import frc.robot.mock.MockLog;
import frc.robot.lib.SimplePID;
import frc.robot.lib.MathUtil;

public class TestClimber {
    MockMotor leftWinchMotor;
    MockMotor rightWinchMotor;
    Climber climber;
    ClimberInterface climberinterface;
    MockClock clock;

    @Before
    public void setup() {
        leftWinchMotor = Mock.stoppedMotor();
        rightWinchMotor = Mock.stoppedMotor();
        clock = Mock.clock();
        climber = new Climber(leftWinchMotor, rightWinchMotor, new MockDashboard(), clock, new MockLog());

    }

    @Test
    public void testEnableDisable() {
        climber.execute(0);
        assertEquals(0.0, leftWinchMotor.get(), 0.01);
        assertEquals(0.0, rightWinchMotor.get(), 0.01);
        assertEquals(new ClimberAction(ClimberAction.ClimberType.STOP_CLIMBER, 0), climber.getDesiredAction());
        assertTrue(climber.isInPosition());

        climber.enable();
        climber.execute(0);
        assertEquals(0, leftWinchMotor.get(), 0.01);
        assertEquals(0, rightWinchMotor.get(), 0.01);
        assertEquals(new ClimberAction(ClimberAction.ClimberType.STOP_CLIMBER, 0), climber.getDesiredAction());
        assertTrue(climber.isInPosition());

        climber.setDesiredAction(new ClimberAction(ClimberType.SET_CLIMBER_POWER, Constants.CLIMBER_MAX_MOTOR_POWER));
        climber.execute(0);
        assertEquals(Constants.CLIMBER_MAX_MOTOR_POWER, leftWinchMotor.get(), 0.01);
        assertEquals(-Constants.CLIMBER_MAX_MOTOR_POWER, rightWinchMotor.get(), 0.01);
        assertTrue(climber.isInPosition());

        climber.disable();
        climber.execute(0);
        assertEquals(0, leftWinchMotor.get(), 0.01);
        assertEquals(0, rightWinchMotor.get(), 0.01);
        assertEquals(new ClimberAction(ClimberType.STOP_CLIMBER, 0), climber.getDesiredAction());
        assertTrue(climber.isInPosition());
    }

    @Test
    public void testAdjustUp() {
        climber.enable();
        climber.setDesiredAction(new ClimberAction(ClimberType.SET_CLIMBER_POWER, Constants.CLIMBER_MAX_MOTOR_POWER));
        climber.execute(0);
        assertEquals(Constants.CLIMBER_MAX_MOTOR_POWER, leftWinchMotor.get(), 0.01);
        assertEquals(-Constants.CLIMBER_MAX_MOTOR_POWER, rightWinchMotor.get(), 0.01);
        assertTrue(climber.isInPosition());
    }

    @Test
    public void testAdjustDown() {
        climber.enable();
        climber.setDesiredAction(new ClimberAction(ClimberType.SET_CLIMBER_POWER, -Constants.CLIMBER_MAX_MOTOR_POWER));
        climber.execute(0);
        assertEquals(-Constants.CLIMBER_MAX_MOTOR_POWER, leftWinchMotor.get(), 0.01);
        assertEquals(Constants.CLIMBER_MAX_MOTOR_POWER, rightWinchMotor.get(), 0.01);
        assertTrue(climber.isInPosition());
    }

    @Test
    public void testDeploy() {
        climber.enable();
        climber.setDesiredAction(new ClimberAction(ClimberType.SET_BOTH_HEIGHT, Constants.CLIMBER_DEPLOY_HEIGHT));
        climber.execute(0);
        assertEquals(Constants.CLIMBER_DEPLOY_HEIGHT, leftWinchMotor.getPosition(), 0.1);
        assertFalse(climber.isInPosition());

    }
}