package frc.robot.interfaces;

import frc.robot.Config;
import frc.robot.lib.Position;

/**
 * The Vision Subsystem - handles all interaction with the roboRio and the external processor for vision
 * 
 * The vision subsystem has listens for messages indicating when and where a vision target was last seen.
 */
public interface Vision extends DashboardUpdater {

	/**
	 * Information about the last target seen.
	 */
	public static class TargetDetails {
		public boolean targetFound = false;  // Was a target seen.
		public double imageTimestamp; // What time this target was seen at in seconds since boot.
		public Position location = new Position(0, 0);  // Co-ordinates relative to the location subsystem.
		public double distance;
		public double angle;
		public double skew;
		
		public boolean isValid(double currentTime) {
			double lockAgeSec = currentTime - imageTimestamp;
			return targetFound && lockAgeSec < Config.vision.maxTargetAgeSecs;
		}

		@Override
		public String toString() {
			return String.format("found: %s, at %f, location %s", targetFound, imageTimestamp, location);
		}
	}
	
	/**
	 * Returns a TargetDetails if the vision processor has seen a target in the last frame.
	 * 
	 * Note the robot has likely moved since this was seen, so it's important to
	 * use the calculated location.
	 * If there was more than one seen, then return the closest one.
	 * 
	 * @return A TargetDetails.
	 */
	public TargetDetails getTargetDetails();
	
	/**
	 * @return hasConnection returns the current status of the connection to the external vision processor
	 */
	public boolean isConnected();	
}