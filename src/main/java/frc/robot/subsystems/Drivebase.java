package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import frc.robot.Constants;
import frc.robot.drive.routines.ConstantDrive;
import frc.robot.drive.routines.DriveRoutine;
import frc.robot.drive.routines.DriveRoutine.DriveMotion;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.DrivebaseInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.NetworkTablesHelper;
import frc.robot.lib.Subsystem;

import java.util.Map;
import java.util.TreeMap;

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
	private final Motor left;
	private final Motor right;
	private final Log log;
	private DriveMotion currentMotion;

	public Drivebase(Motor left, Motor right,	DashboardInterface dashboard, Log log) {
		super("Drive", dashboard, log);
		this.left = left;
		this.right = right;
		this.log = log;

		currentMotion = new DriveMotion(0, 0);
		routine = new ConstantDrive();
		disable(); // disable until we are ready to use it.
		log.register(true, () -> currentMotion.left, "%s/setpoint/Left", name) // talons work in units/100ms
				.register(true, () -> currentMotion.right, "%s/setpoint/Right", name) // talons work in units/100ms
				.register(false, () -> left.getPosition(), "%s/position/Left", name)
				.register(false, () -> right.getPosition(), "%s/position/Right", name)
				.register(false, () -> left.getVelocity(), "%s/speed/Left", name) // talons work in units/100ms
				.register(false, () -> right.getVelocity(), "%s/speed/Right", name) // talons work in units/100ms
				.register(false, () -> left.getOutputVoltage(), "%s/outputVoltage/Left", name)
				.register(false, () -> right.getOutputVoltage(), "%s/outputVoltage/Right", name)
				.register(false, () -> left.getOutputPercent(), "%s/outputPercentage/Left", name)
				.register(false, () -> right.getOutputPercent(), "%s/outputPercentage/Right", name)
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
		// Ask for the power to supply to each side. Pass in the current wheel speeds.
		// TODO: Ensure that this is in meters/sec (not inches/100ms). See comment above.
		DriveMotion motion = routine.getMotion(left.getVelocity(), right.getVelocity());
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
		NetworkTablesHelper helper = new NetworkTablesHelper("drive");
		double leftP = helper.get("p", Constants.DRIVE_P);
		double leftI = helper.get("i", Constants.DRIVE_I);
		double leftD = helper.get("d", Constants.DRIVE_D);
		double leftF = helper.get("f", Constants.DRIVE_F);
		double rightP = helper.get("p", Constants.DRIVE_P);
		double rightI = helper.get("i", Constants.DRIVE_I);
		double rightD = helper.get("d", Constants.DRIVE_F);
		double rightF = helper.get("f", Constants.DRIVE_P);
		left.setPIDF(0, helper.get("p", Constants.DRIVE_P), helper.get("i", Constants.DRIVE_I),
				helper.get("d", Constants.DRIVE_D), helper.get("f", Constants.DRIVE_F));
		right.setPIDF(0, helper.get("p", Constants.DRIVE_P), helper.get("i", Constants.DRIVE_I),
				helper.get("d", Constants.DRIVE_D), helper.get("f", Constants.DRIVE_F));
		super.enable();
		log.info("Drivebase LEFT PID values: %f %f %f %f", leftP, leftI, leftD, leftF);
		log.info("Drivebase RIGHT PID values: %f %f %f %f", rightP, rightI, rightD, rightF);
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
