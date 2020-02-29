package frc.robot.drive.routines;

import frc.robot.interfaces.DrivebaseInterface.DriveRoutineParameters;
import frc.robot.lib.MathUtil;

/*
 * This interface class defines the interface to the drive controls.
 * 
 * Each drive control system can operate independently.
 */
public abstract interface DriveRoutine {

	/**
	 * The values to give to the motors on each side of the robot.
	 */
	public class DriveMotion {
		public double left;
		public double right;
		
		public DriveMotion(double left, double right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public String toString() {
			return "Left: " + left + ", Right: " + right;
		}
		
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof DriveMotion)) {
				return false;
			}
			DriveMotion m = (DriveMotion) o;
			return m.left == left && m.right == right;
		}

		public int hashCode() {
			return (int) (1000 * left + right);
		}
	}
	
	/*
	 * Whether to drive both fieldConfig, or just the left or right side for our Arcade to Tank conversion.
	 */
	public enum DriveSide {
		BOTH, LEFT, RIGHT
	};
	
	/**
	 * This drive routine was requested by this action.
	 * Get any necessary details from it (eg waypoints)
	 * @param parameters
	 */
	public default void reset(DriveRoutineParameters parameters) {
	}

	/**
	 * DriveMotion determines the power that should be applied to the left and right
	 * hand fieldConfig of the robot by the drivebase.
	 * @param leftSpeed The current speed of the robot on the left side.
	 * @param rightSpeed The current speed of the robot on the right side.
	 * @return The power to apply to each side of the robot by the drivebase.
	 */
	public DriveMotion getMotion(double leftSpeed, double rightSpeed);

	/**
	 * Returns true if there is nothing more to do.
	 * Used by spline driving to indicate if there is more to drive.
	 * Most routines will return true.
	 * @return if no more time is needed to finish the current driving.
	 */
	public boolean hasFinished();
	
	/**
	 * Return the name of the Drive Control
	 * @return name of the drive control
	 */
	public String getName();
	
	/**
	 * Activate this driveControl. Perform any initialisation needed with the assumption
	 * that the robot is currently in the correct position
	 */
	public default void enable() {
	}
	
	/**
	 * Prepare the drive control for deactivation. Stop all independent tasks and safe all controls.
	 * Deactivate can be called before activate.
	 */
	public default void disable() {
	}
	
	public default double limit(double value) {
		if (value < -1.0) value = -1.0;
		if (value > 1.0) value = 1.0;
		return value;
	}
	
	public default double square(double value) {
		if (value < 0.0) {
			return -(value * value);
		}
		return value * value;
	}
	
	public default DriveMotion arcadeToTank(double moveValue, double turnValue, double scale, boolean squaredInputs) {
		return arcadeToTank(moveValue, turnValue, scale, squaredInputs, DriveSide.BOTH);
	}
	
	public default DriveMotion arcadeToTank(double moveValue, double turnValue, double scale, boolean squaredInputs, DriveSide driveSide) {
//		double im = moveValue;
//		double it = turnValue;
		moveValue = limit(moveValue);
		turnValue = limit(turnValue);
	    double leftMotorSpeed = 0;
	    double rightMotorSpeed = 0;

	    if (squaredInputs) {
	    	moveValue = square(moveValue);
	    	turnValue = square(turnValue);
	    }

	    if (moveValue > 0.0) {
	      if (turnValue > 0.0) {
	        leftMotorSpeed = moveValue - turnValue;
	        rightMotorSpeed = Math.max(moveValue, turnValue);
	      } else {
	        leftMotorSpeed = Math.max(moveValue, -turnValue);
	        rightMotorSpeed = moveValue + turnValue;
	      }
	    } else {
	      if (turnValue > 0.0) {
	        leftMotorSpeed = -Math.max(-moveValue, turnValue);
	        rightMotorSpeed = moveValue + turnValue;
	      } else {
	        leftMotorSpeed = moveValue - turnValue;
	        rightMotorSpeed = -Math.max(-moveValue, -turnValue);
	      }
	    }
	    /*
	     * Adjust here for left and right only changes if necessary.
	     */
		switch (driveSide) {
		default:
		case BOTH:
			// all is good. Do nothing
			break;
		case LEFT:
			leftMotorSpeed = leftMotorSpeed - rightMotorSpeed;
			rightMotorSpeed = 0.0;
			break;
		case RIGHT:
			leftMotorSpeed = 0.0;
			rightMotorSpeed = rightMotorSpeed - leftMotorSpeed;
			break;
		}
		leftMotorSpeed = scale * MathUtil.clamp(leftMotorSpeed, -1.0, 1.0);
		rightMotorSpeed = scale * MathUtil.clamp(rightMotorSpeed, -1.0, 1.0);
//		System.out.printf("A2T(%f, %f) -> %f,%f\n", im, it, leftMotorSpeed, rightMotorSpeed);
		return new DriveMotion(leftMotorSpeed, rightMotorSpeed);
	}
}
