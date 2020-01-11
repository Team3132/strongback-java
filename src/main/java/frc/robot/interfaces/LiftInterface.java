package frc.robot.interfaces;
// Leaving this file here as there might be a lift on 2019 robot

import java.util.List;
import java.util.ListIterator;

/*
 * The 2018 robot has a dual purpose lift.
 * In Lift mode its purpose is to raise and lower the intake/outtake device to allow us to pick up and deliver cubes.
 * in climb mode it allows us to raise the robot up the scale.
 * 
 * There are various sensors on the lift mechanism, a top and bottom stop indicator,
 * as well there is a sensor that will tell us how high the lift is currently.
 * 
 * The lift subsystems runs a state machine to raise and lower the lift autonomously.
 */
import org.strongback.Executable;
import frc.robot.Constants;
import frc.robot.Constants.LiftSetpoint;

public interface LiftInterface extends SubsystemInterface, Executable, DashboardUpdater {

	public class LiftAction {
        public final Type type;
        public final double value;

        public LiftAction(Type type, double value) {
            this.type = type;
            this.value = value;
        }

        public enum Type{
            SET_HEIGHT, 
			ADJUST_HEIGHT,
			SETPOINT_UP,
			SETPOINT_DOWN
		}

        @Override
        public String toString() {
			return String.format("%s: %f", type.toString().toLowerCase(), value);
		}

		/**
		 * Calculates the height the controller will need to set the lift to
		 * @param currentHeight current height of the lift
		 * @param currentTarget current target of the lift
		 * @return double to which we should set the lift height to
		 */
		public double calculateHeight(double currentHeight, double currentTarget) {
			switch (this.type) {
				case SET_HEIGHT:
					return this.value;
				case ADJUST_HEIGHT:
					return currentTarget + this.value;
				case SETPOINT_UP:
					return getNextLiftSetpoint(currentHeight, true);
				case SETPOINT_DOWN:
					return getNextLiftSetpoint(currentHeight, false);
				default:
					System.out.printf("UNSUPPORTED LIFT ACTION %s", this.type.toString());
					return this.value;
			}
		}

		/**
		 * Jump between pre-configured lift setpoints.
		 * Depending on the current height and the requested action, move to one of the other
		 * setpoints.
		 * @param up What direction to move.
		 * @return New target height for the lift.
		 */
		public double getNextLiftSetpoint(double currentHeight, boolean up) {
			// Sort our setpoints and get an iterator
			List<LiftSetpoint> l = Constants.LIFT_SETPOINTS;
			l.sort((a,b) -> (int) (a.height - b.height));
			ListIterator<LiftSetpoint> it = l.listIterator();

			while (it.hasNext()) {
				double setpoint = it.next().height;
				if (Math.abs(setpoint - currentHeight) <= Constants.LIFT_DEFAULT_TOLERANCE) {
					// we're within tolerance of this setpoint
					// return the setpoint above or below (dependent on up)
					return up ? above(it, setpoint) : below(it, setpoint);
				}
				;

				if (currentHeight > setpoint) {
					// setpoint is above the currentHeight and we were not within
					// tolerance of this setpoint

					// return the setpoint above or below where the lift is right now
					// dependent on up
					return up ? above(it, setpoint) : setpoint;
				}
			}
			// Must be higher than the highest setpoint, return current height.
			return currentHeight;
		}

		/**
		 * Safely ask for the setpoint above the current one.
		 * @param it the list iterator
		 * @param otherwise the value to return if there is no next element
		 */
		private double above(ListIterator<LiftSetpoint> it, double otherwise) {
			if (it.hasNext()) return it.next().height;
			return otherwise;
		}

		/**
		 * Safely ask for the setpoint below the current one.
		 * @param it the list iterator
		 * @param otherwise the value to return if there is no previous element
		 */
		private double below(ListIterator<LiftSetpoint> it, double otherwise) {
			if (it.hasPrevious()) return it.previous().height;
			return otherwise;
		}
		
    }

	
	/**
	 * Moves the lift to a height
	 * @param height The requested height for the lift (inches)
	 */
	public LiftInterface setTargetHeight(double height);
	
	/**
	 * Return the lift's desired height in inches
	 * @return desired height of the carriage from the base in inches
	 */
	public double getTargetHeight();
	
	/**
	 * Returns the lift's current height in inches
	 * @return current height of the carriage from the base in inches
	 */
	public double getHeight();
	
	/**
	 * @return true if the lift is within tolerance of its setpoint
	 */
	public boolean isInPosition();
	
	/**
	 * Tells the lift to shift for low gear if safe to do so
	 */
	public LiftInterface retract();

	/**
	 * Tells the lift to shift for high gear if safe to do so
	 */
	public LiftInterface deploy();
	
	/**
	 * @return true if has been told to be deployed.
	 */
	public boolean shouldBeDeployed();
	
	/**
	 * @return true if in really deployed.
	 */
	public boolean isDeployed();
	
	/**
	 * @return true if the lift is above height of the rung
	 */
	public boolean isSafeToDeploy();
}
