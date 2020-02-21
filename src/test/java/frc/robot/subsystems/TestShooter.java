package frc.robot.subsystems;

import frc.robot.interfaces.ShooterInterface;
import frc.robot.interfaces.Log;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockMotor;
import frc.robot.mock.MockDashboard;
import frc.robot.mock.MockLog;


public class TestShooter {
    MockMotor shooterMotor;
    ShooterInterface shooter;

	public static final int SHOOTER_TARGET_SPEED = 6500;
    private float shooterStartTime = 0;

    @Before
    public void setUp() {
        shooterMotor = Mock.stoppedMotor();
        shooter = new Shooter(shooterMotor, new MockDashboard(), new MockLog());
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
        shooterStartTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - shooterStartTime) < 3000) {
        }
        assertEquals(SHOOTER_TARGET_SPEED, shooterMotor.getSpeed(), 0.1);
    }
}
