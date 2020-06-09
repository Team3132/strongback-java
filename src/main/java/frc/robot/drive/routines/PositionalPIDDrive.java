package frc.robot.drive.routines;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import org.strongback.components.Clock;
import org.strongback.components.Motor.ControlMode;

import frc.robot.interfaces.Drivebase.DriveMotion;
import frc.robot.interfaces.Drivebase.DriveRoutineParameters;
import frc.robot.lib.log.Log;
import frc.robot.Config;
import frc.robot.drive.util.LowPassFilter;
import frc.robot.drive.util.PositionPID;

/**
 * Takes a pair of speeds, one for each side of the robot and attempts to
 * enforce that they keep to those speed.
 * 
 * Useful for when the robot is hard to turn (high traction wheels etc) and it
 * needs a bit more kick if it's falling behind.
 * 
 * Used by driver assist routines that take control of the steering while the
 * driver takes care of the overall forward-reverse motion.
 */
public class PositionalPIDDrive extends DriveRoutine {
	private PositionPID leftPID, rightPID;
	private BooleanSupplier finished;
	private Clock clock;
	private double timestamp = 0;

	public PositionalPIDDrive(String name, BooleanSupplier finished, DoubleSupplier targetSpeed,
			DoubleSupplier targetTurn, double speedScale, double turnScale, double maxJerk, DoubleSupplier leftDistance,
			DoubleSupplier leftSpeed, DoubleSupplier rightDistance, DoubleSupplier rightSpeed, Clock clock) {
		super(name, ControlMode.DutyCycle);
		this.finished = finished;
		this.clock = clock;
		Log.info("Starting to drive positional PID");

		/*
		 * There is an issue here. If the targetSpeed increases too rapidly, then even
		 * with the amount of turn subtracted off, it may still exceed the maximum jerk,
		 * making the drivebase go straight. It could somehow add on the turn factor
		 * after capping the change in acceleration, but that is getting ugly. Instead,
		 * apply a low pass filter to the targetSpeed so that it can't change too
		 * quickly and then set a high jerk so the jerk doesn't limit the acceleration.
		 */
		LowPassFilter filteredSpeed = new LowPassFilter(targetSpeed, 0.2);
		leftPID = createPID("Drive/" + name + "/left", () -> {
			return speedScale * filteredSpeed.getAsDouble() + turnScale * targetTurn.getAsDouble();
		}, maxJerk, leftDistance, leftSpeed, clock);
		rightPID = createPID("Drive/" + name + "/right", () -> {
			return speedScale * filteredSpeed.getAsDouble() - turnScale * targetTurn.getAsDouble();
		}, maxJerk, rightDistance, rightSpeed, clock);
	}

	public PositionalPIDDrive(String name, DoubleSupplier targetSpeed, DoubleSupplier targetTurn, double speedScale,
			double turnScale, double maxJerk, DoubleSupplier leftDistance, DoubleSupplier leftSpeed,
			DoubleSupplier rightDistance, DoubleSupplier rightSpeed, Clock clock) {
		this(name, () -> true, targetSpeed, targetTurn, speedScale, turnScale, maxJerk, leftDistance, leftSpeed,
				rightDistance, rightSpeed, clock);
	}

	private PositionPID createPID(String name, DoubleSupplier targetSpeed, double maxJerk, DoubleSupplier distance,
			DoubleSupplier speed, Clock clock) {
		PositionPID pid = new PositionPID(name, targetSpeed, maxJerk, distance, speed, clock);
		double kV = 0.3;
		double kA = 0, kI = 0, kD = 0;// 0.5;
		double kP = 0.64;
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
		Log.debug("%s: left=%f right=%f", name, leftPower, rightPower);
		return new DriveMotion(leftPower, rightPower);
	}

	@Override
	public boolean hasFinished() {
		// Check that aiming has stayed within tolerance before next state
		return clock.currentTime() - timestamp > Config.vision.aimTimeCompleteSecs;
	}
}
