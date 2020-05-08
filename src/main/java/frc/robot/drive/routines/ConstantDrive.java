package frc.robot.drive.routines;

import frc.robot.interfaces.Log;
import frc.robot.interfaces.DrivebaseInterface.DriveMotion;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineParameters;
import org.strongback.components.Motor.ControlMode;

/**
 * Tells the drive wheels to drive forward at a constant power level.
 */
public class ConstantDrive extends DriveRoutine {
	private DriveMotion motion = new DriveMotion(0, 0);

	public ConstantDrive(String name, ControlMode mode, Log log) {
		super(name, mode, log);
	}

	@Override
	public void reset(DriveRoutineParameters parameters) {
		motion = new DriveMotion(parameters.value, parameters.value);
	}

	@Override
	public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
		return motion;
	}
}
