package frc.robot.subsystems;

import frc.robot.interfaces.ShooterInterface;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockMotor;
import org.strongback.mock.MockSolenoid;

import frc.robot.mock.MockDashboard;
import frc.robot.mock.MockLog;


public class TestShooter {
    MockMotor shooterMotor;
    MockSolenoid shooterSolenoid;
    ShooterInterface shooter;

	public static final int SHOOTER_TARGET_SPEED_RPM = 6500;

    @Before
    public void setUp() {
        shooterMotor = Mock.stoppedMotor();
        shooterSolenoid = Mock.Solenoids.singleSolenoid(0);
        shooter = new Shooter(shooterMotor, shooterSolenoid, new MockDashboard(), new MockLog());
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
        shooter.setTargetRPM(SHOOTER_TARGET_SPEED_RPM);
        assertEquals(SHOOTER_TARGET_SPEED_RPM, shooterMotor.getSpeed(), 0.1);
    }
    @Test
    public void testShooterSetTargetSpeedAndDisable() {
        shooter.setTargetRPM(SHOOTER_TARGET_SPEED_RPM);
        shooter.disable();
        assertEquals(0, shooterMotor.getSpeed(), 0.01);
    }
}