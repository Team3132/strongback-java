package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.TalonSRX;
import frc.robot.drive.routines.ConstantDrive;
import frc.robot.drive.routines.DriveRoutine;
import frc.robot.drive.routines.DriveRoutine.DriveMotion;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.DrivebaseInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

import java.util.Map;
import java.util.TreeMap;

import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Subsystem responsible for the drivetrain
 *
 * Normally there are multiple motors per side, but these have been set to
 * follow one motor per side and these are passed to the drivebase.
 *
 * It periodically queries the joysticks (or the auto routine) for the
 * speed/power for each side of the drivebase.
 */
public class Drivebase extends Subsystem implements DrivebaseInterface, Executable, DashboardUpdater {
	private DriveRoutineParameters parameters = DriveRoutineParameters.getConstantPower(0);
	private DriveRoutine routine = null;
	private ControlMode controlMode = ControlMode.PercentOutput;  // The mode the talon should be in.
	private final TalonSRX left;
	private final TalonSRX right;
	private DriveMotion currentMotion;

	public Drivebase(TalonSRX left, TalonSRX right,	DashboardInterface dashboard, Log log) {
		super("Drive", dashboard, log);
		this.left = left;
		this.right = right;

		currentMotion = new DriveMotion(0, 0);
		routine = new ConstantDrive();
		disable(); // disable until we are ready to use it.
		log.register(true, () -> currentMotion.left, "%s/setpoint/Left", name) // talons work in units/100ms
				.register(true, () -> currentMotion.right, "%s/setpoint/Right", name) // talons work in units/100ms
				.register(false, () -> left.getSelectedSensorPosition(0), "%s/position/Left", name)
				.register(false, () -> right.getSelectedSensorPosition(0), "%s/position/Right", name)
				.register(false, () -> left.getSelectedSensorVelocity(0), "%s/speed/Left", name) // talons work in units/100ms
				.register(false, () -> right.getSelectedSensorVelocity(0), "%s/speed/Right", name) // talons work in units/100ms
				.register(false, () -> left.getMotorOutputVoltage(), "%s/outputVoltage/Left", name)
				.register(false, () -> right.getMotorOutputVoltage(), "%s/outputVoltage/Right", name)
				.register(false, () -> left.getOutputCurrent(), "%s/outputCurrent/Left", name)
				.register(false, () -> right.getOutputCurrent(), "%s/outputCurrent/Right", name)
				.register(false, () -> left.getMotorOutputPercent(), "%s/outputPercentage/Left", name)
				.register(false, () -> right.getMotorOutputPercent(), "%s/outputPercentage/Right", name)
				.register(false, () -> left.getOutputCurrent(), "%s/outputCurrent/Left", name)
				.register(false, () -> right.getOutputCurrent(), "%s/outputCurrent/Right", name);
	}

	@Override
	public void setDriveRoutine(DriveRoutineParameters parameters) {
		if (this.parameters != null && parameters.equals(this.parameters)) {
			return;
		}
		// Drive routine has changed.
		this.parameters = parameters;  // Remember it for next time.
		// Find a routine to handle it
		DriveMode mode = driveModes.getOrDefault(parameters.type, null);
		if (mode == null) {
			log.error("%s: Bad drive mode %s", name, parameters.type);
			return;
		}
		// Tell the drive routine to change what it is doing.
		mode.routine.reset(parameters);
		log.sub("%s: Switching to %s drive routine", name, mode.routine.getName());
		if (routine != null) routine.disable();
		mode.routine.enable();
		routine = mode.routine;
		if (mode.controlMode == ControlMode.PercentOutput) {
			log.sub("%s: PercentOutput Control Mode", name);
		} else {
			log.sub("%s: Other Control Mode", name);
		}
		this.controlMode = mode.controlMode;
	}

	@Override
	public DriveRoutineParameters getDriveRoutine() {
		return parameters;
	}

	@Override
	synchronized public void update() {
		// Query the drive routine for the desired wheel speed/power.
		if (routine == null) return;  // No drive routine set yet.
		DriveMotion motion = routine.getMotion();
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
		left.getSelectedSensorPosition(0);
		right.getSelectedSensorPosition(0);
		super.enable();
		if (routine != null) routine.enable();
	}

	public void disable() {
		super.disable();
		if (routine != null) routine.disable();
		left.set(ControlMode.PercentOutput, 0.0);
		right.set(ControlMode.PercentOutput, 0.0);
		currentMotion.left = 0;
		currentMotion.right = 0;
	}

	@Override
	public void updateDashboard() {
		dashboard.putNumber("Left drive motor", currentMotion.left);
		dashboard.putNumber("Right drive motor", currentMotion.right);
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

	/**
	 * Stores the routine and the control mode for each DriveRoutineType
	 */
	private class DriveMode {
		public DriveMode(DriveRoutine routine, ControlMode controlMode) {
			this.routine = routine;
			this.controlMode = controlMode;
		}
		DriveRoutine routine;
		ControlMode controlMode;
	}
	private Map<DriveRoutineType, DriveMode> driveModes = new TreeMap<DriveRoutineType, DriveMode>();

	@Override
	public void registerDriveRoutine(DriveRoutineType mode, DriveRoutine routine, ControlMode controlMode) {
		log.sub("%s: Registered %s drive routine", name, routine.getName());
		driveModes.put(mode, new DriveMode(routine, controlMode));
	}
}
