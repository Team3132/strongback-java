
package frc.robot.simulator;

import frc.robot.interfaces.ShooterInterface;
import frc.robot.lib.MovementSimulator;

/**
 * Very basic intake simulator used for unit testing.
 * Does not do gravity/friction etc.
 */
public class ShooterSimulator implements ShooterInterface {

	private final double kMaxSpeed = 180;  // degrees/sec
	private final double kMaxAccel = 200;   // degrees/sec/sec
	private final double kMinAngle = 0;
	private final double kMaxAngle = 45;
	private final double kMovementTolerance = 1;  // How close before it's classed as being in position.
    private double targetRPS = 0;
    private double shooterTime = 0;
	private MovementSimulator arm = new MovementSimulator("hood", kMaxSpeed, kMaxAccel, kMinAngle, kMaxAngle, kMovementTolerance);

    @Override
    public String getName() {
        return "ShooterSimulator";
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public void execute(long timeInMillis) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public ShooterInterface setTargetRPS(double rps) {
        this.targetRPS = rps;
        this.shooterTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public double getTargetRPS() {
        return targetRPS;
    }


    @Override
    public boolean isAtTargetSpeed() {
        if ((System.currentTimeMillis() - this.shooterTime) < 1000) {
            return true;
        }
        return false;
    }

	@Override
	public ShooterInterface setHoodExtended(boolean extend) {
		arm.setTargetPos(kMaxAngle);
		return this;
	}

	@Override
	public boolean isHoodExtended() {
		return arm.getTargetPos() == kMaxAngle && arm.isInPosition();
	}

	@Override
	public boolean isHoodRetracted() {
		return arm.getTargetPos() == kMinAngle && arm.isInPosition();
	}
}
