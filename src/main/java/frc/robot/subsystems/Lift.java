package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;

import org.strongback.components.Solenoid;
import org.strongback.components.TalonSRX;
import frc.robot.Constants;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.LiftInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.MathUtil;
import frc.robot.lib.Subsystem;

/*
 * The lift subsystem.
 * 
 * The lift is a two stage lift with two "soft stops" at either end.
 * There is a pot that is used to measure how far along the lift the carriage is currently.
 * A solenoid is used to 
 */

public class Lift extends Subsystem implements LiftInterface, DashboardUpdater {
	private final int UP_PID_SLOT = 0;
	private final int DOWN_PID_SLOT = 1;

	private double targetHeight = 0;
	private double maxHeight = Constants.LIFT_DEFAULT_MAX_HEIGHT;
	private double minHeight = 0.0;
	private TalonSRX liftMotor;
	private boolean deployed = false;
	private Solenoid deploySolenoid;
	
	public Lift(TalonSRX liftMotor, Solenoid deploy, DashboardInterface dashboard, Log log) {
		super("Lift", dashboard, log);
		this.liftMotor = liftMotor;
		this.deploySolenoid = deploy;
		
		log.register(false, this::getHeight, "%s/Actual", name)
		   .register(true, () -> minHeight, "%s/Min", name)
		   .register(true, () -> maxHeight, "%s/Max", name)
		   .register(true, () -> targetHeight, "%s/Desired", name)
		   .register(false, () -> deploy.isExtended() ? 1.0 : 0.0, "%s/Deployed", name)
		   .register(false, liftMotor::getOutputCurrent, "%s/Current", name)
		   .register(false, liftMotor::getMotorOutputVoltage, "%s/Voltage", name)
		   .register(false, liftMotor::getMotorOutputPercent, "%s/Percent", name);
		
		// Use slot 0 (up). PId values are configured in MotorFactory.
		liftMotor.selectProfileSlot(UP_PID_SLOT, 0);
		liftMotor.setSelectedSensorPosition(0, 0, 10);
		// Tell the lift that the current height is where we want to be.
		// The robot should start with the lift at the bottom, but in case it
		// doesn't leave the lift in the starting position for a sequence to change.
		setTargetHeight(getHeight());
	}
	
	/**
	 * Set the current height as the target height on enable.
	 */
	@Override
	public void enable() {
		super.enable();
		setTargetHeight(getHeight());
	}

	@Override
	public void disable() {
		super.disable();
		// Make so that it doesn't jump on re-enable.
		//liftMotor.set(ControlMode.PercentOutput, 0);
	}

	@Override
	public LiftInterface setTargetHeight(double height) {
		if (height == targetHeight) return this;
		
		targetHeight = height;
		
		double adjustedHeight = MathUtil.clamp(targetHeight, minHeight, maxHeight) + Constants.LIFT_DEFAULT_MIN_HEIGHT;
		liftMotor.set(ControlMode.MotionMagic, adjustedHeight);
		//liftMotor.set(ControlMode.Position, adjustedHeight);
		log.sub("%s: set lift target height %f (adjusted %f)", name, height, adjustedHeight);
		return this;
	}

	/**
	 * Change the PID values based on the direction that the lift has to move.
	 */
	protected void update() {

		// Use different PID values depending on where the lift is relative
		// to the target.
		if (targetHeight > getHeight()) {
			liftMotor.selectProfileSlot(UP_PID_SLOT, 0);
		} else {
			liftMotor.selectProfileSlot(DOWN_PID_SLOT, 0);
		}
	}

	@Override
	public double getTargetHeight() {
		return targetHeight;
	}

	@Override
	public double getHeight() {
		return liftMotor.getSelectedSensorPosition(0) - Constants.LIFT_DEFAULT_MIN_HEIGHT;
	}
	
	@Override
	public boolean isInPosition() {
		return Math.abs(getTargetHeight() - getHeight()) < Constants.LIFT_DEFAULT_TOLERANCE;
	}
	
	@Override
	public LiftInterface retract() {
		deployed = false;
		deploySolenoid.retract();
		return this;
	}

	@Override
	public LiftInterface deploy() {
		deployed = true;
		deploySolenoid.extend();
		return this;
	}
	
	@Override
	public boolean isDeployed() {
		return deploySolenoid.isExtended();
	}

	@Override
	public boolean shouldBeDeployed() {
		return deployed;
	}
	
	/**
	 * Function which determines if it is safe to shift.
	 * If we are in high gear we should only shift above the rung
	 * If we are in low gear we should only shift if we are not holding the weight
	 * of the robot (i.e. the motors aren't stalling)
	 */
	@Override
	public boolean isSafeToDeploy() {
		log.sub("is safe to deploy, lift height = %f\n", getHeight());
		return getHeight() > Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT;
	}

	@Override
	public void updateDashboard() {
		dashboard.putString("Lift height", formatHeight(getHeight()));		
		dashboard.putString("Lift target", formatHeight(getTargetHeight()));
		dashboard.putString("Lift status", isDeployed() ? "Extended" : "Retracted");
		dashboard.putString("Lift raw height", formatHeight(liftMotor.getSelectedSensorPosition(0)));
		dashboard.putString("Lift top sensor", liftMotor.getSensorCollection().isFwdLimitSwitchClosed() ? "detected" : "not detected");
		dashboard.putString("Lift bottom sensor", liftMotor.getSensorCollection().isRevLimitSwitchClosed() ? "detected" : "not detected");
	}

	public static String formatHeight(double height) {
		return String.format("%.1f", height);
	}
}