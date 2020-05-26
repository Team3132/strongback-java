package frc.robot.subsystems;

import java.io.IOException;

import org.strongback.components.Clock;

import frc.robot.Config;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.JevoisInterface;
import frc.robot.interfaces.LocationInterface;
import frc.robot.interfaces.VisionInterface;
import frc.robot.lib.Position;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;

public class Vision extends Subsystem implements VisionInterface, Runnable {
	private JevoisInterface jevois;
	private LocationInterface location;
	private Clock clock;
	private double visionHMin, visionSMin, visionVMin;
	private double visionHMax, visionSMax, visionVMax;
	private TargetDetails lastSeenTarget = new TargetDetails();
	private boolean connected = false;

	public Vision(JevoisInterface jevois, LocationInterface location, DashboardInterface dashboard, Clock clock,
			double visionHMin, double visionSMin, double visionVMin, double visionHMax, double visionSMax,
			double visionVMax) {
		super("Vision", dashboard);
		this.jevois = jevois;
		this.location = location;
		this.clock = clock;
		this.visionHMin = visionHMin;
		this.visionSMin = visionSMin;
		this.visionVMin = visionVMin;
		this.visionHMax = visionHMax;
		this.visionSMax = visionSMax;
		this.visionVMax = visionVMax;

		Chart.register(() -> isConnected(), "%s/connected", name);
		Chart.register(() -> lastSeenTarget.location.x, "%s/curX", name);
		Chart.register(() -> lastSeenTarget.location.y, "%s/curY", name);
		Chart.register(() -> lastSeenTarget.location.heading, "%s/heading", name);
		// Chart.register(() -> clock.currentTime() - lastSeenTarget.seenAtSec, "%s/seenAt", name)
		Chart.register(() -> lastSeenTarget.imageTimestamp, "%s/seenAtSec", name);
		Chart.register(() -> lastSeenTarget.targetFound, "%s/targetFound", name);
		Chart.register(() -> lastSeenTarget.distance, "%s/distance", name);
		Chart.register(() -> lastSeenTarget.angle, "%s/angle", name);
		// Start reading from the Jevois camera.
		(new Thread(this)).start();
	}

	/**
	 * Return the details of the last target seen. Let the caller decide if the data
	 * is too old/stale.
	 */
	@Override
	public synchronized TargetDetails getTargetDetails() {
		return lastSeenTarget;
	}

	/**
	 * Main loop. Runs in its own thread so it can block.
	 */
	@Override
	public void run() {
		while (true) {
			Log.debug("Vision waiting for the camera server to start up");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
			}
			Log.debug("Starting to read from Jevois camera\n");
			try {
				// Attempt to detect if there is a camera plugged in. It will throw an exception
				// if not.
				Log.debug(jevois.issueCommand("info"));
				connected = true;

				// Update the HSV filter ranges from the config values.

				jevois.issueCommand(String.format("setHSVMin %.0f %.0f %.0f", visionHMin,
				visionSMin, visionVMin));
				jevois.issueCommand(String.format("setHSVMax %.0f %.0f %.0f", visionHMax,
				visionSMax, visionVMax));

				while (true) {
					processLine(jevois.readLine());
				}
			} catch (IOException e) {
				Log.error("Failed to read from jevois, aborting vision processing\n");
				connected = false;
				e.printStackTrace();
			}
		}
	}

	/**
	 * Parses a line from the vision and calculates the target position on the field
	 * based on where the robot was at that time. No line is read when a target
	 * isn't seen.
	 * 
	 * Example line: D3 -0.3190665833627917 -0.16974943059054468 1.4929203902754815
	 * 0.28 0.175 1.0 0.994180288811222 0.0003884439751087179 0.09825181108672097
	 * 0.04418126377428119 FIRST
	 * 
	 * Line format: D3 <imageAge> <found> <distance> <angle> <skew> FIRST
	 * 
	 * Where: D3: static string to indicate that this is a found vision target.
	 * imageAge: time since image taken
	 * found: boolean for if goal was detected
	 * distance: horizontal distance from goal in inches
	 * angle: degrees
	 * skew: degrees
	 * FIRST: static string.
	 */
	private void processLine(String line) {
		// Split the line on whitespace.
		// "D3 timestamp found distance angle FIRST"
		String[] parts = line.split("\\s+");

		if (!parts[0].equals("D3")) {
			Log.info("Ignoring non-vision target line: %s", line);
			return;
		}
		//Logger.debug("Vision::processLine(%s)\n", line);
		TargetDetails newTarget = new TargetDetails();
		newTarget.targetFound = Boolean.parseBoolean(parts[2]);

		if (Boolean.parseBoolean(parts[2])) {
			// A target was seen, update the TargetDetails in case it's asked for.
			// Fill in a new TargetDetails so it can be returned if asked for and it won't
			// change as the caller uses it.

			newTarget.imageTimestamp = clock.currentTime() - Double.parseDouble(parts[1]);
			newTarget.distance = Double.parseDouble(parts[3])*Config.constants.inchesToMetres;
			newTarget.angle = -Double.parseDouble(parts[4]);
			newTarget.skew = Double.parseDouble(parts[5]);

			Position robotPosition = location.getHistoricalLocation(newTarget.imageTimestamp);
			newTarget.location = robotPosition.addVector(newTarget.distance, newTarget.angle);
		
			newTarget.location.heading += newTarget.angle - newTarget.skew;

			// Logger.debug("Location set.");
			// newTarget.location.heading += newTarget.angle


			// Low pass filter 
			// We reject the last seen target if it is older than <timeOffset> seconds
			double alpha = Config.vision.goalLowPassAlpha; 
			if (lastSeenTarget.isValid(clock.currentTime())) {
				newTarget.location.x = alpha * newTarget.location.x + (1-alpha) * lastSeenTarget.location.x;
				newTarget.location.y = alpha * newTarget.location.y + (1-alpha) * lastSeenTarget.location.y;
				newTarget.location.heading = alpha * newTarget.location.heading + (1-alpha) * lastSeenTarget.location.heading;
			}

			synchronized (this) {
				lastSeenTarget = newTarget;
			}
			// Logger.debug("Vision: Updated target %s", lastSeenTarget);

		}

	}

	@Override
	public void updateDashboard() {
		double lockAgeSec = (clock.currentTime() - lastSeenTarget.imageTimestamp);
		double angle = 0, distance = 0;
		if (lastSeenTarget.isValid(clock.currentTime())) {
			Position robotPos = location.getCurrentLocation();
			angle = -robotPos.bearingTo(lastSeenTarget.location);
			distance = robotPos.distanceTo(lastSeenTarget.location);
		}
		dashboard.putBoolean("Vision camera connected", connected);
		dashboard.putNumber("Vision distance to target", distance);
		dashboard.putNumber("Vision lockAgeSec", lockAgeSec);
		dashboard.putNumber("Vision angle", angle);
		dashboard.putBoolean("Vision targetFound", lastSeenTarget.targetFound);
		dashboard.putBoolean("Vision is Valid", lastSeenTarget.isValid(clock.currentTime()));
		dashboard.putNumber("Vision Skew", lastSeenTarget.location.heading);

	}

	/**
	 * @return hasConnection returns the current status of the connection to the
	 *         external vision processor
	 */
	@Override
	public boolean isConnected() {
		return connected;
	}
}
