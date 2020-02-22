package frc.robot.subsystems;

import org.strongback.components.Clock;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.SubsystemInterface;
import frc.robot.interfaces.VisionInterface;
import frc.robot.lib.Position;
import frc.robot.lib.Subsystem;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry;

public class LimeVision extends Subsystem implements VisionInterface, SubsystemInterface, DashboardUpdater {

    private Location location;
    private NetworkTable limeTable;
    private Clock clock;

    private NetworkTableEntry txEntry;
    private NetworkTableEntry tyEntry;
    private NetworkTableEntry tvEntry;

    private NetworkTableEntry thorEntry;

    private final double kDefaultTableValue = 0.0;

    private boolean enabled = false;

    private double lastXPos = kDefaultTableValue;
    private double lastYPos = kDefaultTableValue;
    private double lastSeen = kDefaultTableValue;
    private boolean isTargetFound = false;

    private double targetWidth = kDefaultTableValue;

    /**
     * A class for interfacing with the limelight camera.
     * Refrences to networktables can be found at this link:
     * http://docs.limelightvision.io/en/latest/getting_started.html#basic-programming 
     */
    public LimeVision(Location location, Clock clock, DashboardInterface dashboard, Log log) {
        super("limevision", dashboard, log);
        this.location = location;
        this.clock = clock;

        log.register(true, () -> isTargetFound, "%s/targetFound", name)
           .register(true, () -> isConnected(), "%s/connected", name)
		   .register(true, () -> lastXPos, "%s/xPos", name)
		   .register(true, () -> lastYPos, "%s/yPos", name)
		   .register(true, () -> lastSeen, "%s/lastSeen", name);

        limeTable = NetworkTableInstance.getDefault().getTable("limelight");
        txEntry = limeTable.getEntry("tx"); // X coords
        tyEntry = limeTable.getEntry("ty"); // Y coords
        tvEntry = limeTable.getEntry("tv"); // Valid target
        thorEntry = limeTable.getEntry("thor"); // Bounding box horizontal width.
   }

    /**
     * Gets the width of the target if it is found.
     * @return
     */
    private double getTargetWidth() {
        return targetWidth;
    }

    /**
     * Returns the distance of the vision target to the camera lens +- 1/4" in error.
     * 
     * Math & tuning spreadsheet:
     * https://docs.google.com/spreadsheets/d/1r8RORDwgjK-4-EeHcZ-2Sm4jVR-0COhqCdDgMzGgZAg/edit?usp=sharing
     */
    public double getDistanceFromTarget() {
        final double c = 3.4;
        final double d = 0.0015;
        final double e = 3.4;

        return ((1 / ((getTargetWidth() / (e * 1000)) - d)) + c);
    }
    
    /**
     * checks if the robot is connected to the limelight
     */
    @Override
    public boolean isConnected() {
        return limeTable.getInstance().isConnected();
    }

    /**
     * Retuns a class called TargetDetails containing information about the target that the limelight is locked on.
     */
    @Override
    public TargetDetails getTargetDetails() {
        TargetDetails details = new TargetDetails();   
        details.location = new Position(lastXPos, lastYPos, 0);     
        details.imageTimestamp = lastSeen;
        details.targetFound = isTargetFound;
        return details;
    }
    
    @Override
    public void execute(long timeInMillis) {
        // Update and check if the limelight has found a target.
        isTargetFound = tvEntry.getDouble(kDefaultTableValue) == 1.0;

        // Check if the limelight saw a target recently and store the values if it did
        if (isTargetFound) {
            lastXPos = txEntry.getDouble(kDefaultTableValue);
            lastYPos = tyEntry.getDouble(kDefaultTableValue);
            targetWidth = thorEntry.getDouble(-1.0);
            lastSeen = clock.currentTime();
        }
    }

    @Override
    public String getName() {
        return "LimeVision";
    }

    @Override
    public void enable() {
        enabled = true;
    }

    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void cleanup() {
    }

    /**
     * Update the operator console with the current status.
     */
	@Override
	public void updateDashboard() {
        double howLongAgo = clock.currentTime() - lastSeen;
        String timeStr = String.format("%.1f sec", howLongAgo);
        if (howLongAgo > 120) {
            timeStr = String.format("%.1f min", howLongAgo / 60);
        }
        dashboard.putString("Vision target age", isTargetFound ? timeStr : "Not seen");
        double angle = 0;
        double distance = 0;
		if (isTargetFound) {
            Position robotPos = location.getCurrentLocation();
            Position lastSeenPos = new Position(lastXPos, lastYPos, 0);
			// Where is the target relative to the current robot position?
			Position relativePos = robotPos.getRelativeToPosition(lastSeenPos);
			angle = relativePos.heading;
			distance = robotPos.distanceTo(lastSeenPos);
		}
		dashboard.putNumber("Vision angle to target", angle);
		dashboard.putNumber("Vision distance to target", distance);
	}
}