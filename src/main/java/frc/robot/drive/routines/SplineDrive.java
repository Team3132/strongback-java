package frc.robot.drive.routines;

import java.util.function.DoubleSupplier;

import org.strongback.components.Clock;
import frc.robot.interfaces.LocationInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineParameters;
import frc.robot.lib.Position;
import frc.robot.lib.WaypointUtil;
import frc.robot.Constants;
import frc.robot.controller.Controller.TrajectoryGenerator;

import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.followers.EncoderFollower;

/**
 * Walks the drivebase through a supplied list of waypoints.
 */
public class SplineDrive implements DriveRoutine {
	static private double ENCODER_SCALING_FACTOR = 100;

	private final TrajectoryGenerator generator;
	private final DoubleSupplier leftDistance, rightDistance;
	private final LocationInterface location;
	private final Clock clock;
	private final Log log;
	private double scale = 1;
	private double nextUpdateSec = 0;
	private final double updatePeriodSec = 0.5;

	// These all change when the waypoints change.
	private EncoderFollower leftFollower, rightFollower;
	private Position initialPosition;
	private boolean enabled = true;
	private int numSegments = 0;
	private int segmentNum = 0;
	private Waypoint finalWaypoint;
	private boolean finalPositionLogged = false;


	// TODO: Move TrajectoryGenerator out of controller.
	public SplineDrive(TrajectoryGenerator generator, 
		DoubleSupplier leftDistance, DoubleSupplier rightDistance, LocationInterface location, Clock clock, Log log) {
		this.generator = generator;  // Converts waypoints to trajectories.
		this.leftDistance = leftDistance;   // Encoder left side.
		this.rightDistance = rightDistance;  // Encoder right side.
		this.location = location;  // Tracks where it is on the field.
		this.clock = clock;
		this.log = log;
	}

	/**
	 * This drive routine was requested by this action. Contains the waypoints to
	 * drive through.
	 * 
	 * @param parameters
	 */
	synchronized public void reset(DriveRoutineParameters parameters) {
		this.initialPosition = location.getCurrentLocation();
		log.info("Starting to drive trajectory");

		Waypoint[] waypoints = parameters.waypoints;
		// If going in reverse, change the apparent direction of the robot so the
		// motor powers can be reversed.
		if (!parameters.forward) initialPosition.heading += Constants.HALF_CIRCLE;
		if (!parameters.relative) {
			// Convert to relative waypoints.
			//waypoints = WaypointUtil.subtract(waypoints, WaypointUtil.toWaypoint(initialPos));
		}
		scale = parameters.forward ? 1 : -1;
		finalWaypoint = waypoints[waypoints.length-1];
		Trajectory[] trajectories = generator.generate(waypoints);
        // Run the trajectories, one per side, using the encoders as feedback.
        leftFollower = createFollower(trajectories[0], leftDistance.getAsDouble());
        rightFollower = createFollower(trajectories[1], rightDistance.getAsDouble());
		numSegments = trajectories[0].length();
		segmentNum = 0;
		nextUpdateSec = 0;
		finalPositionLogged = false;
        
        // Allow it to run.
        enabled = true;
	}

	private EncoderFollower createFollower(Trajectory traj, double initialPosition) {
		EncoderFollower follower = new EncoderFollower(traj);
		// The encoders have been configured to return inches, which doesn't play
		// well with the EncoderFollower. Convert the values to 1/100ths of an inch
		// so that they can be pretended to be inches.
		int initial_position = distanceToFakeTicks(initialPosition);
		int ticks_per_revolution = (int) ENCODER_SCALING_FACTOR;
		double wheel_diameter = 1 / Math.PI;  // One turn moves 1 inch.
		follower.configureEncoder((int)(scale) * initial_position, ticks_per_revolution, wheel_diameter);
		double kp = 0.13;  // Needs tuning, needs to be higher as of 22/1/2019 8:55 pm.
		double ki = 0;  // Unused.
		double kd = 0.015;
		double kv = 0.017; /*/ Constants.DRIVE_MAX_SPEED*/;
		double ka = 0.016;  // Needs tuning.
		follower.configurePIDVA(kp, ki, kd, kv, ka);
		return follower;
	}
	
	private static int distanceToFakeTicks(double distance) {
		return (int) (distance * ENCODER_SCALING_FACTOR);
	}
	
	@Override
	public void enable() {
		// Do nothing on resume. Wait for the next reset().
	}

	@Override
	synchronized public void disable() {
		enabled = false;
	}

	/**
	 * Query the followers to see what power level to give the motors
	 * based on the trajectory and how closely the robot is following it.
	 */
	@Override
	synchronized public DriveMotion getMotion() {
		segmentNum++;
		updateLocationSubsystem();
		// WARNING: The follower routine in use doesn't measure how much time has passed
		// between steps - it assumes that everything it running perfectly to time.
		// This will cause inaccuracies and weird errors.
		// It would ideally be re-written to check the time between calls.
		if (!enabled || leftFollower.isFinished()) {
			// It's done, return zero.
			//log.sub("auto driving done, enabled = %s, isFinished = %s", enabled, leftFollower.isFinished());
			logProgress();
			maybeLogFinalPosition();
			return new DriveMotion(0, 0);
		}
		maybeLogProgress();
		// Calculate the new speeds for both left and right motors.
		double leftPower = leftFollower.calculate(distanceToFakeTicks((int)(scale) * leftDistance.getAsDouble()));
		double rightPower = rightFollower.calculate(distanceToFakeTicks((int)(scale) * rightDistance.getAsDouble()));
		if (leftFollower.isFinished()) {
			log.info("Finished driving trajectory");
		}
		//log.sub("drive power = %.1f %.1f", leftPower, rightPower);
		return new DriveMotion(scale * leftPower,scale * rightPower);
	}
	
	@Override
	public boolean hasFinished() {
		return leftFollower.isFinished();
	}
	
	/**
	 * Tell the location subsystem where we should be so it can be recorded
	 * for plotting against the actual position.
	 */
	private void updateLocationSubsystem() {
		final Position desired = WaypointUtil.toPosition(leftFollower.getSegment(), rightFollower.getSegment());
		//location.setDesiredLocation(initialPosition.add(desired));
		//log.sub("desired postion %.1f,%.1f - %.1f,%.1f", leftFollower.getSegment().x, leftFollower.getSegment().y, rightFollower.getSegment().x, rightFollower.getSegment().y);
		location.setDesiredLocation(desired);
		log.sub("desired position (left/right): %.1f, %.1f and %.1f, %.1f", leftFollower.getSegment().x, leftFollower.getSegment().y, rightFollower.getSegment().x, rightFollower.getSegment().y);
		log.sub("desired velocity (left/right): %.1f and %.1f", leftFollower.getSegment().velocity, rightFollower.getSegment().velocity);
	}
	
	/**
	 * Periodically log the progress on the console.
	 */
	private void maybeLogProgress() {
		double now = clock.currentTime();
		if (now < nextUpdateSec) return;
		nextUpdateSec = now + updatePeriodSec;
		logProgress();
	}

	private void logProgress() {
		final int PROGRESS_LENGTH = 50;
		// Print a progress indicator like:
		// |=======================>      |
		int progress = (PROGRESS_LENGTH - 2) * segmentNum / numSegments;
		StringBuilder b = new StringBuilder(PROGRESS_LENGTH);
		b.append('|');
		while (progress-- > 0) b.append('=');
		if (b.length() < PROGRESS_LENGTH -1) b.append('>');
		while (b.length() < PROGRESS_LENGTH - 1) b.append(' ');
		b.append('|');
		log.info(b.toString());
	}

	/**
	 * Print out the final position and the difference to where it is expected to be.
	 */
	private void maybeLogFinalPosition() {
		if (finalPositionLogged) return;  // Already done.
		finalPositionLogged = true;
		Position finalPos = location.getCurrentLocation();
		Position expectedDiff = WaypointUtil.toPosition(finalWaypoint);
		Position actualDiff = finalPos.getRelativeToPosition(initialPosition);
		Position zeroPos = new Position(0, 0, 0);
		double expectedDist = expectedDiff.distanceTo(zeroPos);
		double actualDist = finalPos.distanceTo(initialPosition);
		double percentage = 100;
		if (expectedDist != 0) percentage = 100 * actualDist / expectedDist;
		log.info("Finished driving, expected to be at %s but got to %s", expectedDiff, actualDiff);
		log.info("Travelled %.1f%% of the expected %.1f inches", percentage, expectedDist);
	}

	@Override
	public String getName() {
		return "Spline";
	}
}
