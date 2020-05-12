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
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

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
	private final Log log;
	private DriveMotion currentMotion;

	public Drivebase(Motor left, Motor right, Solenoid ptoSolenoid, Solenoid brakeSolenoid, 
			DashboardInterface dashboard, Log log) {
		super("Drive", dashboard, log);
		this.left = left;
		this.right = right;
		this.ptoSolenoid = ptoSolenoid;
		this.brakeSolenoid = brakeSolenoid;
		this.log = log;

		currentMotion = new DriveMotion(0, 0);
		routine = new ConstantDrive("Constant Drive", ControlMode.DutyCycle, log);
		disable(); // disable until we are ready to use it.
		log.register(true, () -> currentMotion.left, "%s/setpoint/Left", name)
				.register(true, () -> currentMotion.right, "%s/setpoint/Right", name)
				.register(false, () -> left.getPosition(), "%s/position/Left", name)
				.register(false, () -> right.getPosition(), "%s/position/Right", name)
				.register(false, () -> left.getSpeed(), "%s/speed/Left", name)
				.register(false, () -> right.getSpeed(), "%s/speed/Right", name)
				.register(false, () -> left.getOutputVoltage(), "%s/outputVoltage/Left", name)
				.register(false, () -> right.getOutputVoltage(), "%s/outputVoltage/Right", name)
				.register(false, () -> left.getOutputPercent(), "%s/outputPercentage/Left", name)
				.register(false, () -> right.getOutputPercent(), "%s/outputPercentage/Right", name)
				.register(false, () -> left.getOutputCurrent(), "%s/outputCurrent/Left", name)
				.register(false, () -> right.getOutputCurrent(), "%s/outputCurrent/Right", name)
				.register(true, () -> isClimbModeEnabled(), "%s/extended", name)
				.register(true, () -> isDriveModeEnabled(), "%s/retracted", name)
				.register(true, () -> isBrakeApplied(), "%s/extended", name)
          		.register(true, () -> isBrakeReleased(), "%s/retracted", name);
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
			log.sub("%s: Parameters are identical not setting these", name);
			return;
		}
		// Drive routine has changed.
		this.parameters = parameters;  // Remember it for next time.
		// Find a routine to handle it
		DriveRoutine newRoutine = driveModes.getOrDefault(parameters.type, null);
		if (newRoutine == null) {
			log.error("%s: Bad drive mode %s", name, parameters.type);
			return;
		}
		// Tell the drive routine to change what it is doing.
		newRoutine.reset(parameters);
		log.sub("%s: Switching to %s drive routine using ControlMode %s", name, newRoutine.getName(), newRoutine.getControlMode());
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
		//log.debug("drive subsystem motion = %.1f, %.1f", motion.left, motion.right);
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
		log.sub("%s: Registered %s drive routine", name, routine.getName());
		driveModes.put(mode, routine);
	}

	@Override
	public boolean isClimbModeEnabled() {
		// log.sub("Is intake extended: " + solenoid.isExtended());
		return ptoSolenoid.isExtended();
	}

	@Override
	public boolean isDriveModeEnabled() {
		// log.sub("Is intake extended: " + solenoid.isExtended());
		return ptoSolenoid.isRetracted();
	}

	@Override
	public boolean isBrakeApplied() {
		// log.sub("Is intake extended: " + solenoid.isExtended());
		return brakeSolenoid.isExtended();
	}

	@Override
	public boolean isBrakeReleased() {
		// log.sub("Is intake extended: " + solenoid.isExtended());
		return brakeSolenoid.isRetracted();
	}

	
}
