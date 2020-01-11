package frc.robot.mock;

import frc.robot.drive.routines.DriveRoutine;
import frc.robot.interfaces.DrivebaseInterface;
import frc.robot.interfaces.Log;

import com.ctre.phoenix.motorcontrol.ControlMode;

public class MockDrivebase implements DrivebaseInterface  {
	private DriveRoutineParameters parameters = new DriveRoutineParameters(DriveRoutineType.ARCADE);

	String name = "MockDrivebase";
	Log log;
	
	public MockDrivebase(Log log) {
		this.log = log;
	}

	@Override
	public void execute(long timeInMillis) {
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void setDriveRoutine(DriveRoutineParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public DriveRoutineParameters getDriveRoutine() {
		return parameters;
	}

	@Override
	public boolean hasFinished() {
		return true;
	}

	@Override
	public void registerDriveRoutine(DriveRoutineType mode, DriveRoutine routine, ControlMode controlMode) {
	}
}
