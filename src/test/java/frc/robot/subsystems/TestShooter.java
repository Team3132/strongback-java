package frc.robot.subsystems;

import frc.robot.interfaces.ShooterInterface;
import frc.robot.interfaces.Log;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockMotor;
import org.strongback.mock.MockSolenoid;

import frc.robot.mock.MockDashboard;
import frc.robot.mock.MockLog;
import frc.robot.mock.MockShooter;
import frc.robot.mock.MockNetworkTableHelper;


public class TestShooter {
    MockMotor shooterMotor;
    MockSolenoid shooterSolenoid;
    ShooterInterface shooter;

	public static final int SHOOTER_TARGET_SPEED = 6500;

    @Before
    public void setUp() {
        shooterMotor = Mock.stoppedMotor();
        shooterSolenoid = Mock.Solenoids.singleSolenoid(0);
        shooter = new Shooter(shooterMotor, shooterSolenoid, new MockNetworkTableHelper("Shooter"), new MockDashboard(), new MockLog());
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
        shooter.setTargetSpeed(SHOOTER_TARGET_SPEED);
        assertEquals(SHOOTER_TARGET_SPEED, shooterMotor.getSpeed(), 0.1);
    }
    @Test
    public void testShooterSetTargetSpeedAndDisable() {
        shooter.setTargetSpeed(SHOOTER_TARGET_SPEED);
        shooter.disable();
        assertEquals(0, shooterMotor.getSpeed(), 0.01);
    }
}
