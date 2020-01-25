package frc.robot.subsystems;

import java.io.IOException;

import org.strongback.components.Clock;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.JevoisInterface;
import frc.robot.interfaces.LocationInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.VisionInterface;
import frc.robot.lib.MathUtil;
import frc.robot.lib.Position;
import frc.robot.lib.Subsystem;

public class Vision extends Subsystem implements VisionInterface, DashboardUpdater, Runnable {
	private JevoisInterface jevois;
	private LocationInterface location;
	private Clock clock;
	private double visionHMin, visionSMin, visionVMin;
	private double visionHMax, visionSMax, visionVMax;
	private TargetDetails lastSeenTarget = new TargetDetails();
	private boolean connected = false;

	private double prevSkew = 0;

	public Vision(JevoisInterface jevois, LocationInterface location, DashboardInterface dashboard, Clock clock,
		double visionHMin, double visionSMin, double visionVMin, double visionHMax, double visionSMax, double visionVMax, Log log) {
		super("Vision", dashboard, log);
		this.jevois = jevois;
		this.location = location;
		this.clock = clock;
		this.visionHMin = visionHMin;
		this.visionSMin = visionSMin;
		this.visionVMin = visionVMin;
		this.visionHMax = visionHMax;
		this.visionSMax = visionSMax;
		this.visionVMax = visionVMax;
	
		log.register(true, () -> isConnected(), "%s/connected", name)
				//.register(true, () -> lastSeenTarget.location.x, "%s/curX", name)
				//.register(true, () -> lastSeenTarget.location.y, "%s/curY", name)
				//.register(true, () -> lastSeenTarget.location.heading, "%s/heading", name)
				.register(true, () -> clock.currentTime() - lastSeenTarget.seenAtSec, "%s/seenAt", name)
				.register(true, () -> lastSeenTarget.targetFound, "%s/targetFound", name)
				.register(true, () -> lastSeenTarget.distance, "%s/distance", name)
				.register(true, () -> lastSeenTarget.xOffset, "%s/xOffset", name);
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
		log.sub("Vision waiting for the camera server to start up");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {}
		log.sub("Starting to read from Jevois camera\n");
		try {
			// Attempt to detect if there is a camera plugged in. It will throw an exception
			// if not.
			log.sub(jevois.issueCommand("info"));
			connected = true;
			// Update the HSV filter ranges from the config values.
			jevois.issueCommand(String.format("setHSVMin %.0f %.0f %.0f", visionHMin, visionSMin, visionVMin));
			jevois.issueCommand(String.format("setHSVMax %.0f %.0f %.0f", visionHMax, visionSMax, visionVMax));
			while (true) {
				processLine(jevois.readLine());
			}
		} catch (IOException e) {
			log.error("Failed to read from jevois, aborting vision processing\n");
			connected = false;
			e.printStackTrace();
		}
	}

	/**
	 * Parses a line from the vision and calculates the target position on the field
	 * based on where the robot was at that time.
	 * No line is read when a target isn't seen.
	 * 
	 * Example line:
	    D3 -0.3190665833627917 -0.16974943059054468 1.4929203902754815 0.28 0.175 1.0 0.994180288811222 0.0003884439751087179 0.09825181108672097 0.04418126377428119 FIRST

	 * Line format:
     *   D3 <x_pos,> <y pos> <dist> <target_width> <target_height> <1.0> <i> <j> <k> <l> FIRST
	 * 
	 * Where:
	 *   D3: static string to indicate that this is a found vision target.
	 *   x_pos: horizontal position of the middle of the vision target on the image.
	 *          -1 is the very left of the image, 1 is the very right hand side.
	 *   y_pos: vertical position of the middle of the vision target on the image.
	 *          -1 is the very top image, 1 is the bottom.
	 *   dist: the distance to the vision target in metres.
	 *   target_width: hard coded target width in metres.
	 *   target_height: hard coded target height in metres.
	 *   1.0: hard coded value.
	 *   i: Some pose measurement.
	 *   j: Some pose measurement.
	 *   k: The skew of the target (units unknown...)
	 *   l: Some pose measurement.
	 *   FIRST: static string.
	 */
	private void processLine(String line) {
		// Split the line on whitespace.
		// "D3 timestamp found distance x FIRST"
		String[] parts = line.split("\\s+");

		if (!parts[0].equals("D3")) {
			log.info("Ignoring non-vision target line: %s", line);
			return;
		}
		log.sub("Vision::processLine(%s)\n", line);

		// Specs for the Jevois camera: https://www.jevoisinc.com/pages/hardware
		final double horizontalFOV = 65;
		final double verticalFOV = horizontalFOV / (4.0/3.0);
		
		double seenAtSec = clock.currentTime() - 0.01; // when the image was taken
		// A target was seen, update the TargetDetails in case it's asked for.
		// Fill in a new TargetDetails so it can be returned if asked for and it won't
		// change as the caller uses it.
		TargetDetails latestTargetSeen = new TargetDetails();
		//log.sub("Vision old pos = %s", robotPosition);
		//log.sub("Vision: angle=%.1f, distance=%.1f", xAngle, distanceInches);

		// When the target is flat with the camera the skew is inaccurate. Can be as bad as +-20 degrees
		// Use a low pass filter to try and smooth these out
		// TODO: since this is an issue when the skew is zero try to squash small skew values
		
		latestTargetSeen.seenAtSec = seenAtSec;
		latestTargetSeen.targetFound = Boolean.parseBoolean(parts[2]);
		latestTargetSeen.distance = Double.parseDouble(parts[3]);
		latestTargetSeen.xOffset = Double.parseDouble(parts[4]);
		synchronized (this) {
			lastSeenTarget = latestTargetSeen;
		}
		//log.sub("Vision: Updated target %s", lastSeenTarget);
	}

	
	@Override
	public void updateDashboard() {
		boolean targetFound = lastSeenTarget.targetFound;
		double lockAgeSec = clock.currentTime() - lastSeenTarget.seenAtSec;
		if (lockAgeSec > 2)
			targetFound = false;
		double angle = 0, distance = 0;
		if (targetFound) {
			Position robotPos = location.getCurrentLocation();
			// Where is the target relative to the current robot position?
			Position relativePos = lastSeenTarget.location.getRelativeToPosition(robotPos);
			angle = relativePos.heading;
			distance = robotPos.distanceTo(lastSeenTarget.location);
		}
		dashboard.putBoolean("Vision camera found", connected);
		dashboard.putBoolean("Vision lock", targetFound);
		dashboard.putNumber("Vision lock age", lockAgeSec);
		dashboard.putNumber("Vision X", lastSeenTarget.location.x);
		dashboard.putNumber("Vision Y", lastSeenTarget.location.y);
		dashboard.putNumber("Vision Heading", lastSeenTarget.location.heading);
		dashboard.putNumber("Vision height", lastSeenTarget.height);
		dashboard.putNumber("Vision angle to target", angle);
		dashboard.putNumber("Vision distance to target", lastSeenTarget.distance);
		dashboard.putBoolean("Vision target found", lastSeenTarget.targetFound);
		dashboard.putNumber("Vision xOffset", lastSeenTarget.xOffset);
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
