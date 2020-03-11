package frc.robot.drive.routines;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import org.strongback.components.Clock;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineParameters;
import frc.robot.drive.util.LowPassFilter;
import frc.robot.drive.util.PositionPID;

/**
 * Takes a pair of speeds, one for each side of the robot and attempts
 * to enforce that they keep to those speed.
 * 
 * Useful for when the robot is hard to turn (high traction wheels etc)
 * and it needs a bit more kick if it's falling behind.
 * 
 * Used by driver assist routines that take control of the steering while the
 * driver takes care of the overall forward-reverse motion.
 */
public class PositionalPIDDrive implements DriveRoutine {

	private String name;
	private final Log log;
	private PositionPID leftPID, rightPID;
	private BooleanSupplier finished;
	private Clock clock;
	private double timestamp = 0;
	    
	public PositionalPIDDrive(
		String name, BooleanSupplier finished, DoubleSupplier targetSpeed, DoubleSupplier targetTurn,
		double speedScale, double turnScale,
 	 	double maxJerk,
	  	DoubleSupplier leftDistance, DoubleSupplier leftSpeed,
	  	DoubleSupplier rightDistance, DoubleSupplier rightSpeed,
		Clock clock, Log log) {
		this.name = name;
		this.log = log;
		this.finished = finished;
		this.clock = clock;
		log.info("Starting to drive positional PID");
		// There is an issue here. If the targetSpeed increases too rapidly, then even with
		// the amount of turn subtracted off, it may still exceed the maximum jerk, making
		// the drivebase go straight. It could somehow add on the turn factor after capping
		// the change in acceleration, but that is getting ugly.
		// Instead, apply a low pass filter to the targetSpeed so that it can't change too
		// quickly and then set a high jerk so the jerk doesn't limit the acceleration.
		LowPassFilter filteredSpeed = new LowPassFilter(targetSpeed, 0.2);
		leftPID = createPID("Drive/"+name+"/left", () -> {
			 	return speedScale * filteredSpeed.getAsDouble() + turnScale * targetTurn.getAsDouble();
			},
			maxJerk, leftDistance, leftSpeed, clock, log);
		rightPID = createPID("Drive/"+name+"/right", () -> {
			 	return speedScale * filteredSpeed.getAsDouble() - turnScale * targetTurn.getAsDouble();
	   		},
		maxJerk, rightDistance, rightSpeed, clock, log);
	}

	public PositionalPIDDrive(
		String name, DoubleSupplier targetSpeed, DoubleSupplier targetTurn,
		double speedScale, double turnScale,
 	 	double maxJerk,
	  	DoubleSupplier leftDistance, DoubleSupplier leftSpeed,
	  	DoubleSupplier rightDistance, DoubleSupplier rightSpeed,
		Clock clock, Log log){
			this(name,() -> true,targetSpeed,targetTurn,speedScale,turnScale,maxJerk,leftDistance,leftSpeed,rightDistance,rightSpeed,clock,log);
	}
	
	
	private PositionPID createPID(String name, DoubleSupplier targetSpeed, double maxJerk,
		 DoubleSupplier distance, DoubleSupplier speed, Clock clock, Log log) {
		PositionPID pid = new PositionPID(name, targetSpeed, maxJerk, distance, speed, clock, log);
		double kV = 0.018;
		double kA = 0, kI = 0, kD = 0.5;//1.05;
		double kP = 0.0125;
		// double kA = 0, kP = 0.03, kI = 0, kD = 0; // from offseason 
		pid.setVAPID(kV, kA, kP, kI, kD);
		return pid;
	}

	@Override
	public void reset(DriveRoutineParameters parameters) {
		timestamp = clock.currentTime();
		leftPID.reset();
		rightPID.reset();
	}

	@Override
	public void enable() {
		// Don't do anything on enable, wait for the next reset().
	}

	@Override
	public void disable() {
		leftPID.disable();
		rightPID.disable();
	}

	

	@Override
	public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
		// Calculate the new speeds for both left and right motors.
		double leftPower = leftPID.getMotorPower();
		double rightPower = rightPID.getMotorPower();
		if (!finished.getAsBoolean()) {
			timestamp = clock.currentTime();
		}
		log.sub("%s: left=%f right=%f", name, leftPower, rightPower);
		return new DriveMotion(leftPower, rightPower);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean hasFinished() {
		return clock.currentTime() - timestamp > 1;  // Always ready for the next state.
	}

}
