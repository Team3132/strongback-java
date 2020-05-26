package frc.robot.subsystems;

import java.util.Map;
import java.util.TreeMap;

import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Solenoid;

import frc.robot.drive.routines.ConstantDrive;
import frc.robot.drive.routines.DriveRoutine;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DrivebaseInterface;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;

/**
 * Subsystem responsible for the drivetrain
 *
 * Normally there are multiple motors per side, but these have been set to
 * follow one motor per side and these are passed to the drivebase.
 *
 * It periodically queries the joysticks (or the auto routine) for the
 * speed/power for each side of the drivebase.
 */
public class Drivebase extends Subsystem implements DrivebaseInterface {
	private DriveRoutineParameters parameters = DriveRoutineParameters.getConstantPower(0);
	private DriveRoutine routine = null;
	private ControlMode controlMode = ControlMode.DutyCycle;  // The mode the talon should be in.
	private final Motor left;
	private final Motor right;
	private Solenoid ptoSolenoid;
	private Solenoid brakeSolenoid;
	private DriveMotion currentMotion;

	public Drivebase(Motor left, Motor right, Solenoid ptoSolenoid, Solenoid brakeSolenoid, 
			DashboardInterface dashboard) {
		super("Drive", dashboard);
		this.left = left;
		this.right = right;
		this.ptoSolenoid = ptoSolenoid;
		this.brakeSolenoid = brakeSolenoid;

		currentMotion = new DriveMotion(0, 0);
		routine = new ConstantDrive("Constant Drive", ControlMode.DutyCycle);
		disable(); // disable until we are ready to use it.
		Chart.register(() -> currentMotion.left, "%s/setpoint/Left", name);
		Chart.register(() -> currentMotion.right, "%s/setpoint/Right", name);
		Chart.register(() -> left.getPosition(), "%s/position/Left", name);
		Chart.register(() -> right.getPosition(), "%s/position/Right", name);
		Chart.register(() -> left.getSpeed(), "%s/speed/Left", name);
		Chart.register(() -> right.getSpeed(), "%s/speed/Right", name);
		Chart.register(() -> left.getOutputVoltage(), "%s/outputVoltage/Left", name);
		Chart.register(() -> right.getOutputVoltage(), "%s/outputVoltage/Right", name);
		Chart.register(() -> left.getOutputPercent(), "%s/outputPercentage/Left", name);
		Chart.register(() -> right.getOutputPercent(), "%s/outputPercentage/Right", name);
		Chart.register(() -> left.getOutputCurrent(), "%s/outputCurrent/Left", name);
		Chart.register(() -> right.getOutputCurrent(), "%s/outputCurrent/Right", name);
		Chart.register(() -> isClimbModeEnabled(), "%s/extended", name);
		Chart.register(() -> isDriveModeEnabled(), "%s/retracted", name);
		Chart.register(() -> isBrakeApplied(), "%s/extended", name);
        Chart.register(() -> isBrakeReleased(), "%s/retracted", name);
	}

	@Override
	public DrivebaseInterface activateClimbMode(boolean extend) {
		if (extend) {
			ptoSolenoid.extend();
		} else {
			ptoSolenoid.retract();
		}

		return this;
	}

	@Override
	public DrivebaseInterface applyBrake(boolean extend) {
		if (extend) {
			brakeSolenoid.extend();
		} else {
			brakeSolenoid.retract();
		}

		return this;
	}

	@Override
	public void setDriveRoutine(DriveRoutineParameters parameters) {
		if (this.parameters != null && parameters.equals(this.parameters)) {
			Log.debug("%s: Parameters are identical not setting these", name);
			return;
		}
		// Drive routine has changed.
		this.parameters = parameters;  // Remember it for next time.
		// Find a routine to handle it
		DriveRoutine newRoutine = driveModes.getOrDefault(parameters.type, null);
		if (newRoutine == null) {
			Log.error("%s: Bad drive mode %s", name, parameters.type);
			return;
		}
		// Tell the drive routine to change what it is doing.
		newRoutine.reset(parameters);
		Log.debug("%s: Switching to %s drive routine using ControlMode %s", name, newRoutine.getName(), newRoutine.getControlMode());
		if (newRoutine != null) newRoutine.disable();
		newRoutine.enable();
		routine = newRoutine;
		controlMode = newRoutine.getControlMode();
	}

	@Override
	public DriveRoutineParameters getDriveRoutineParameters() {
		return parameters;
	}

	@Override
	synchronized public void update() {
		// Query the drive routine for the desired wheel speed/power.
		if (routine == null) return;  // No drive routine set yet.
		// Ask for the power to supply to each side. Pass in the current wheel speeds.
		DriveMotion motion = routine.getMotion(left.getSpeed(), right.getSpeed());
		//Logger.debug("drive subsystem motion = %.1f, %.1f", motion.left, motion.right);
		if (motion.equals(currentMotion)) {
			return; // No change.
		}
		// The TalonSRX doesn't have a watchdog (unlike the WPI_ version), so no need to
		// updated it often.
		currentMotion = motion; // Save it for logging.
		left.set(controlMode, motion.left);
		right.set(controlMode, motion.right);
	}

	@Override
	public void enable() {

		super.enable();

		if (routine != null) routine.enable();
	}

	public void disable() {
		super.disable();
		if (routine != null) routine.disable();
		left.set(ControlMode.DutyCycle, 0.0);
		right.set(ControlMode.DutyCycle, 0.0);
		currentMotion.left = 0;
		currentMotion.right = 0;
	}

	@Override
	public void updateDashboard() {
		dashboard.putNumber("Left drive motor duty cycle", currentMotion.left);
		dashboard.putNumber("Right drive motor duty cycle", currentMotion.right);
		dashboard.putNumber("Left drive pos", left.getPosition());
		dashboard.putNumber("Right drive pos", right.getPosition());
		dashboard.putString("Drive control", routine.getName());
	}

	/**
	 * Will return false if the current drive routine wants to keep control.
	 * For example, spline driving will want to keep driving until it's done.
	 */
	@Override
	public boolean hasFinished() {
		return routine.hasFinished();
	}

	private Map<DriveRoutineType, DriveRoutine> driveModes = new TreeMap<DriveRoutineType, DriveRoutine>();

	@Override
	public void registerDriveRoutine(DriveRoutineType mode, DriveRoutine routine) {
		Log.debug("%s: Registered %s drive routine", name, routine.getName());
		driveModes.put(mode, routine);
	}

	@Override
	public boolean isClimbModeEnabled() {
		// Logger.debug("Is intake extended: " + solenoid.isExtended());
		return ptoSolenoid.isExtended();
	}

	@Override
	public boolean isDriveModeEnabled() {
		// Logger.debug("Is intake extended: " + solenoid.isExtended());
		return ptoSolenoid.isRetracted();
	}

	@Override
	public boolean isBrakeApplied() {
		// Logger.debug("Is intake extended: " + solenoid.isExtended());
		return brakeSolenoid.isExtended();
	}

	@Override
	public boolean isBrakeReleased() {
		// Logger.debug("Is intake extended: " + solenoid.isExtended());
		return brakeSolenoid.isRetracted();
	}

	
}
