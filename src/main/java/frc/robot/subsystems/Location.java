package frc.robot.subsystems;

import java.util.function.DoubleSupplier;

import org.strongback.Executable;
import org.strongback.components.Clock;
import org.strongback.components.Gyroscope;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveOdometry;
import frc.robot.Constants;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.LocationInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.LocationHistory;
import frc.robot.lib.MathUtil;
import frc.robot.lib.NavXGyroscope;
import frc.robot.lib.Position;
import frc.robot.lib.Subsystem;

/**
 *	Location Subsystem.
 *
 * DANGER: This is in the process of being gutted and replaced with the DifferentialDriveOdometry
 * class which uses a different frame of reference.
 * There are two implementations of location tracking in this file. The old-style is used for
 * the vision processing. The new style is used for the RamseteController for auto driving.
 * In the 2020 offseason the plan is to remove the old style and update the vision driving to
 * use the style. This is not scheduled to happen during the 2020 build season, so both implementations
 * are left in this file to smooth the transition to new new Ramsete driving code.
 *
 * The location subsystem is responsible for tracking the location of the robot on the field.
 * It does this through reading the encoders and the gyro and plotting where the robot has moved.
 * This is guaranteed lossy, as we cannot update often enough to track every slight movement.
 * 
 * Coordinate system
 * -----------------
 * 
 * The field orientation has the Y axis across the driver's station and
 * the X axis between the alliance's ends.
 * Positive for Y is from right to left, and positive for X is towards the
 * opposite alliance's end from the driver's station.
 *  
 * X is along the "horizontal line", and Y is the "vertical" line with 
 * (0,0) at the bottom left of the diagram below.
 * DANGER: This is changing to having X away from the drivers' stations.
 *
 *          ^ X +ve 
 *          |
 *          | Other alliance driver stations
 *          +*******|*******|********
 *          |                       *
 *          |                       *
 *          |                       *
 *          |                       *
 *          |                       *
 *          |                       *
 *          |                       *
 *          +************************
 *          |                       *
 *          |                       *
 *          |                       *
 *          |                       *
 *          |                       *
 *          |                       *
 *          |                       *
 * Y +ve <--0-------|-------|-------+---> Y -ve
 *          | Our drivers' stations
 *          |
 *          v X -ve
 * 
 * Heading angles:
 *                        ^  0 degrees 
 *                        |
 *                        |
 *    90 degrees <--------+-------->   -90 degrees
 *                        |
 *                        |
 *                        v  180/-180 degrees
 * 
 * The location subsystem works in headings and bearings. When the robot starts we call the forward direction heading/bearing 0.
 * 
 * A heading is the angle that the robot has moved from the initial angle.
 * It can range from -infinity to infinity, so full circles change the heading by 360 degrees each time.
 * Increasing positive angles are the robot turning anticlockwise.
 * 
 * A bearing is the relative angle from the initial angle. In nautical terms it is the absolute bearing.
 * A bearing is constrained to the range -180 to 180 degrees.
 */
public class Location extends Subsystem implements LocationInterface, Executable, DashboardUpdater {
    private Gyroscope gyro;
    private DoubleDelta leftDistanceDelta, rightDistanceDelta;
    private Clock clock;            // Source of time.
    /**
     * current the current Position:
     * x: current left/right offset from 0 at start point (inches). +x is forward.
	 * y: current forward/backward offset from 0 at start point (inches). +y is left.
	 * h: current heading from facing directly down the field (degrees). +ve is anticlockwise.
	 * s: current speed (inches/sec)
	 * t: time of the last update. (seconds)
	 */
	private Position current;  // Where the robot actually is based on the encoders and gyro.
	private Position desired;  // Where the auto driving hopes the robot is at.
	private LocationHistory history; // history of points we have been on the field.
	private boolean debug = false;

	private Runnable resetEncoders;


	// Odometry class for tracking robot pose
	private final DifferentialDriveOdometry odometry;	
	private final DoubleSupplier leftDistance;
	private final DoubleSupplier rightDistance;
	
	/**
	 * Calculates the delta since last called.
	 */
	private class DoubleDelta implements DoubleSupplier {
		private final DoubleSupplier encoder;
		private double oldValue;
		
		public DoubleDelta(DoubleSupplier encoder) {
			this.encoder = encoder;
			oldValue = encoder.getAsDouble();
		}
		
		// Difference from last time called.
		@Override
		public double getAsDouble() {
			double angle = encoder.getAsDouble();
			double newAngle = angle - oldValue;
			oldValue = angle;			
			return newAngle;
		}
	}
	
	/**
	 * Constructor. The location subsystem obtains inputs from the drivebase and from the gyro.
	 *
	 * It performs inverse kinematics on these to determine where the robot is currently on the field.
	 * We can override this with the setCurrent method which allows some other subsystem to determine
	 * where we are on the field, and then to inform the location subsystem of that fact.
	 * 
	 * @param name The name to be used in the logs
	 * @param leftDistance The distance travelled by the left wheel (in inches)
	 * @param gyro The gyro to get angles
	 * @param log The log to store debug and other logging messages
	 */
    public Location(Runnable resetEncoders, DoubleSupplier leftDistance, DoubleSupplier rightDistance, Gyroscope gyro, Clock clock, DashboardInterface dashboard, Log log) {
		super("Location", dashboard, log);	// always present!
		this.resetEncoders = resetEncoders;
		this.leftDistance = leftDistance;
		this.rightDistance = rightDistance;
		odometry = new DifferentialDriveOdometry(Rotation2d.fromDegrees(gyro.getAngle()));

		leftDistanceDelta = new DoubleDelta(leftDistance);
		rightDistanceDelta = new DoubleDelta(rightDistance);
		this.gyro = gyro;
		this.clock = clock;
		this.history = new LocationHistory(clock);
		current = new Position(0, 0, 0, 0, clock.currentTime());
		desired = new Position(0, 0, 0, 0, clock.currentTime());

		log.register(true, () -> current.x, "%s/actual/x", name)
           .register(true, () -> current.y, "%s/actual/y", name)
		   .register(true, () -> current.heading, "%s/actual/a", name)
		   .register(true, () -> current.speed, "%s/s/acutal/speed", name) 
		   .register(true, () -> current.timeSec, "%s/actual/time", name)
		   .register(true, () -> desired.x, "%s/desired/x", name)
           .register(true, () -> desired.y, "%s/desired/y", name)
		   .register(true, () -> desired.heading, "%s/desired/a", name)
		   .register(true, () -> desired.speed, "%s/desired/speed", name) 
		   .register(true, () -> desired.timeSec, "%s/desired/time", name);
		
		// Enable this subsystem by default.
		enable();
	}
    
    /**
     * Set the current location. This allows a subsystem to override the location and force the location to a particular point.
     * In particular the start location should be set as accurately as possible, so the robot knows where it starts on the field
     * @param location The current location.
     */
    @Override
    public void setCurrentLocation(Position location) {
		log.sub("%s: resetting to: %s", name, location.toString());
    	((NavXGyroscope) gyro).setAngle(location.heading);
    	current.speed = 0;
    	current.timeSec = clock.currentTime();  // time of last update
		history.setInitial(current);
		resetEncoders.run();
		odometry.resetPosition(new Pose2d(location.x, location.y, Rotation2d.fromDegrees(location.heading)), Rotation2d.fromDegrees(0));
	}
	
	/**
	 * Resets the odometry to the specified pose.
	 *
	 * @param pose The pose to which to set the odometry.
	 */
	/*
	public void resetOdometry(Pose2d pose) {
		resetEncoders();
		odometry.resetPosition(pose, Rotation2d.fromDegrees(getHeading()));
	}
	*/
    
    /**
     * Return the location on the field at the current time.
     * @return the current location
     */
    @Override
    public Position getCurrentLocation() {
    	return current;
    }
    
    /**
     * Set the desired location.
     * Usually used for the automatic driving to log where the robot should be.
     * @param position The current location.
     */
    @Override
    public void setDesiredLocation(Position location) {
    	desired = location;
    }

    /**
     * Return the location on the field at the specified time
     * @param timeSec The time (in seconds) for which we wish to obtain the location
     * @return The location at the specified time
     */
	public Position getHistoricalLocation(double timeSec) {
		return history.getLocation(timeSec);
	}

	/**
	 * Returns the currently-estimated pose of the robot.
	 *
	 * @return The pose.
	 */
	public Pose2d getPose() {
		return odometry.getPoseMeters();
	}

	@Override
	public void execute(long timeInMillis) {
		update();
	}

	/**
	 * Update the robots location on the field.
	 *
	 * Gets the movement of the robot since the last update and the heading of
	 * that movement and calculate the new position of the robot.
	 * 
	 * It also adds this new value to the LocationHistory class which holds
	 * the history of where the robot has been on the field.
	 *
	 * y += distance * sin(heading)
	 * x += distance * cos(heading)
	 * 
	 * If the encoder deltas are different then we have been moving in an arc.
	 * Assume that the arc is smooth and has straight entry and exit segments to calculate the new X and Y locations.
	 * The gyro is assumed correct, and is used to update the Heading.
	 * 
	 * First cut. assume we have moved the average distance of both left and right movements at
	 * the average Heading between the start and the end Heading.
	 * This is not exact, but since we sample fast enough it is close enough for tracking how the robot is moving.
	 * 
	 * One problem is that very fast sampling means we will have zero movements for some samples.
	 * This causes problems with the current speed value.
	 * TODO: Add a low pass filter.
	 */
	@Override
	public void update() {
		if (!enabled) return;			// The location subsystem should never be disabled.

		// odometry expects degrees and metres
		odometry.update(Rotation2d.fromDegrees(gyro.getAngle()), leftDistance.getAsDouble() * Constants.INCHES_TO_METRES,
				rightDistance.getAsDouble() * Constants.INCHES_TO_METRES);

		double newLeft = leftDistanceDelta.getAsDouble();  // The change in inches since last call.
		double newRight = rightDistanceDelta.getAsDouble();
		double newHeading = gyro.getAngle();
		double newTime = clock.currentTime();  // Time of last update
		double averageHeading = (current.heading + newHeading) / 2.0;
		// Average of the distance - inches.
		double averageDistance = (newLeft + newRight) / 2.0;
		
		/*log.sub("%s: leftSupplier: %f, leftDelta: %f, rightSupplier: %f, rightDelta: %f", name,
				leftDistanceSupplier.getAsDouble(), newLeft,
				rightDistanceSupplier.getAsDouble(), newRight);
		*/
		// Calculate the true X and Y movement for this heading.
		// the heading is defined as 0 = movement along the Y axis in a positive direction.
		// 90 = movement along the X axis in a positive direction
		/*
		 * 
		 * y += distance * sin(heading)
		 * x += distance * cos(heading)
		 */
		current.y += MathUtil.sin(averageHeading) * averageDistance;
		current.x += MathUtil.cos(averageHeading) * averageDistance;
		current.heading = newHeading;
		current.speed = averageDistance/(newTime - current.timeSec);	// speed = distance/time
		current.timeSec = newTime;								// time of last update
    	history.addLocation(current);
    	
    	if (debug) {
    		log.debug("%s: %s", name, current.toString());
    	}
	}

	/**
	 * Return the heading of the robot
	 * @return Returns the cumulative heading of the robot. This is a continuous heading,
	 * so it moves from 360 to 361 degrees.
	 */
	@Override
	public double getHeading() {
	    return gyro.getAngle();
	} 
	
	/**
	 * Return the robot heading restricted to -180 to 180 degrees. This is discontinuous,
	 * so 180 leads to -179.9999 and -180 leads to 179.9999
	 * @return the current bearing of the robot
	 */
	@Override
	public double getBearing() {
		return (MathUtil.normalise(getHeading(), Constants.FULL_CIRCLE));
	}
	
	/**
	 * Return the distance traveled over the last interval/time of the last interval.
	 * This has problems if we are sampling more often than the talon is updating, as the
	 * distance drops to zero (and so the speed drops to zero).
	 * 
	 * REX: This should be a decaying moving average over the last few samples, with instantaneous zeros ignored to
	 * solve the sampling problem.
	 * @return speed for the last interval.
	 */
	@Override
	public double getSpeed() {
		return current.speed;
	}
    
	/**
	 * Return the robot heading restricted from 0 to 1 in fractions of a circle.
	 * @return the Heading from 0 straight ahead to .5 backwards to 1 straight ahead.
	 */
	public double getUnitHeading() {
		double bearing = getBearing();
		if (bearing < 0.0) {
			bearing += Constants.FULL_CIRCLE;
		}
		return bearing / Constants.FULL_CIRCLE;
	}
	
	@Override
	public void resetHeading() {
		gyro.zero();
		// Update the saved state.
		current.heading = gyro.getAngle();
	}

	@Override
	public void updateDashboard() {
		dashboard.putNumber("Location X", current.x);
		dashboard.putNumber("Location Y", current.y);
		dashboard.putNumber("Location A", current.heading);
	}

}

