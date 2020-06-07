package frc.robot.subsystems;

import frc.robot.interfaces.Shooter;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockMotor;
import org.strongback.mock.MockSolenoid;

import frc.robot.mock.MockDashboardImpl;


public class TestShooter {
    MockMotor shooterMotor;
    MockSolenoid shooterSolenoid;
    Shooter shooter;

	public static final int SHOOTER_TARGET_SPEED_RPS = 100;

    @Before
    public void setUp() {
        shooterMotor = Mock.stoppedMotor();
        shooterSolenoid = Mock.Solenoids.singleSolenoid(0);
        shooter = new FlywheelShooter(shooterMotor, shooterSolenoid, new MockDashboardImpl());
    }

    @Test
    public void testShooterEnable() {
        shooter.enable();
        assertEquals(0, shooterMotor.getSpeed(), 0.01);
    }

    @Test
    public void testShooterDisable() {
        shooter.disable();
        assertEquals(0, shooterMotor.getSpeed(), 0.01);
    }

    @Test
    public void testShooterSetTargetSpeed() {
        shooter.setTargetRPS(SHOOTER_TARGET_SPEED_RPS);
        assertEquals(SHOOTER_TARGET_SPEED_RPS, shooterMotor.getSpeed(), 0.1);
    }
    @Test
    public void testShooterSetTargetSpeedAndDisable() {
        shooter.setTargetRPS(SHOOTER_TARGET_SPEED_RPS);
        shooter.disable();
        assertEquals(0, shooterMotor.getSpeed(), 0.01);
    }
}
