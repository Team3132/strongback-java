package frc.robot.drive.routines;

public class Deadband implements DriveRoutine {
	private DriveRoutine child;
	
	public Deadband(DriveRoutine child) {
		this.child = child;
	}
	
	@Override
	public DriveMotion getMotion() {
		DriveMotion dm = child.getMotion();
		
		if (Math.abs(dm.left) < 0.02) {
			dm.left = 0;
		}
		if (Math.abs(dm.right) < 0.02) {
			dm.right = 0;
		}
		return dm;
	}

	@Override
	public String getName() {
		return child.getName();
	}

	@Override
	public boolean hasFinished() {
		return child.hasFinished();
	}
}
