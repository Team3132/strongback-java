package frc.robot.mock;

import frc.robot.drive.routines.DriveRoutine;
import frc.robot.interfaces.DrivebaseInterface;

public class MockDrivebase implements DrivebaseInterface  {
	private DriveRoutineParameters parameters = new DriveRoutineParameters(DriveRoutineType.ARCADE_DUTY_CYCLE);
	private boolean ClimbModeEnabled = false;
	private boolean BrakeApplied = false;
	String name = "MockDrivebase";
	;
	public MockDrivebase() {
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
	public DriveRoutineParameters getDriveRoutineParameters() {
		return parameters;
	}

	@Override
	public boolean hasFinished() {
		return true;
	}

	@Override
	public void registerDriveRoutine(DriveRoutineType mode, DriveRoutine routine) {
	}
	
	@Override
	public boolean isClimbModeEnabled() {
		return ClimbModeEnabled;
	}

	@Override
	public boolean isBrakeApplied() {
		return BrakeApplied;
	}

	@Override
	public boolean isBrakeReleased() {
		return !BrakeApplied;
	}

	@Override
	public boolean isDriveModeEnabled() {
		return !ClimbModeEnabled;
	}

	@Override
	public DrivebaseInterface activateClimbMode(boolean enabled) {
		ClimbModeEnabled = enabled;
		return this;
	}

	@Override
	public DrivebaseInterface applyBrake(boolean enabled) {
		BrakeApplied = enabled;
		return this;
	}
}
