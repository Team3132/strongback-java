package frc.robot.mock;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import frc.robot.Config;
import frc.robot.interfaces.Location;
import frc.robot.lib.MathUtil;
import frc.robot.lib.Position;

public class MockLocation implements Location {

	Position here = new Position(0, 0, 0);
	double heading = 0;
	
	@Override
	public void execute(long timeInMillis) {
	}

	@Override
	public void setCurrentLocation(Position current) {
		here = current;
	}

	@Override
	public void setCurrentLocation(Pose2d pose) {
		here = new Position(pose.getTranslation().getX(), pose.getTranslation().getY(), pose.getRotation().getDegrees());
	}

	@Override
	public Position getCurrentLocation() {
		return here;
	}

	@Override
	public void setDesiredLocation(Position location) {
	}

	@Override
	public Position getHistoricalLocation(double timeSec) {
		// Build the position based on the time.
		return new Position(10*timeSec, 100*timeSec, timeSec % 360);
	}

	/**
	 * Returns the currently-estimated pose of the robot.
	 *
	 * @return The pose.
	 */
	@Override
	public Pose2d getPose() {
		return new Pose2d();
	}
	
	@Override
	public void update() {
	}

	@Override
	public double getHeading() {
		return heading;
	}

	@Override
	public double getBearing() {
		return MathUtil.normalise(heading, Config.constants.fullCircle);
	}

	@Override
	public double getSpeed() {
		return 0;
	}

	@Override
	public void resetHeading() {
		heading = 0;
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}
	
	public void setHeading(double heading) {
		this.heading = heading;
	}

	
}