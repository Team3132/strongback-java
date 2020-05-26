package frc.robot;

import frc.robot.lib.ConfigReader;
import frc.robot.lib.PIDF;
import static frc.robot.Constants.*;

/**
 * Class responsible for updating values which are dependent on robot hardware.
 * (e.g. if subsystems are present or not) It reads from a text file Currently
 * the supported types are String, int, double, boolean and int array.
 * 
 * Example lines:
 *   drivebase/present = true
 *   drivebase/rampRate = 0.13125
*    drivebase/right/canIDs/withEncoders = 7,6
 *   drivebase/right/canIDs/withoutEncoders = 5
 * 
 * The configuration can be overridden on each robot by changing a text file
 * stored on the robot allowing different robots to have different
 * configuration preventing having to modify the code each time it's pushed
 * to a different bit of hardware.
 * 
 * This is very useful for testing when parts of the hardware are not
 * attached, delivered or even broken.
 */
public class Config {

	/*
	 * Drivebase parameters
	 * 
	 * The robot has motors on each side. This is the information that defines these
	 * motors and their behaviour
	 */
	public static class drivebase {
		public static final boolean present = getBoolean("drivebase/present", false);
		public static final String motorControllerType = getMotorControllerType("drivebase/motorControllerType", DEFAULT_MOTOR_CONTROLLER_TYPE);
		public static final int[] canIdsLeftWithEncoders = getIntArray("drivebase/left/canIDs/withEncoders", new int[] { 4, 5 });
		public static final int[] canIdsLeftWithoutEncoders = getIntArray("drivebase/left/canIDs/withoutEncoders", new int[] {});
		public static final int[] canIdsRightWithEncoders = getIntArray("drivebase/right/canIDs/withEncoders", new int[] { 1, 2 });
		public static final int[] canIdsRightWithoutEncoders = getIntArray("drivebase/right/canIDs/withoutEncoders", new int[] {});
		public static final boolean currentLimiting = getBoolean("drivebase/currentLimiting", true);
		public static final int contCurrent = getInt("drivebase/contCurrent", 38);
		public static final int peakCurrent = getInt("drivebase/peakCurrent", 80);
		public static final double rampRate = getDouble("drivebase/rampRate", 0.01);
		public static final PIDF pidf = getPIDF("drivebase", new PIDF(0, 0, 0, 0.7));
		public static final String mode = getString("drivebase/mode", DRIVE_MODE_ARCADE);
		public static final double maxSpeed = getDouble("drivebase/maxSpeed", 4.0);
		public static final boolean swapLeftRight = getBoolean("drivebase/swapLeftRight", false);
		public static final boolean sensorPhase = getBoolean("drivebase/sensor/phase", false);
	}

	/**
	 * NavX
	 * 
	 * Using the gyro for autonomous routines.
	 */
	public static class navx {
		public static final boolean present = getBoolean("navx/present", true);
	}

	/*
	 * Climber parameters.
	 * 
	 * The climber is a PTO from the drivebase. There is a solenoid to release the
	 * brake.
	 */
	public static class climber {
		public static final int ptoPort = 6;
		public static final int brakePort = 0;
	}

	/*
	 * Intake parameters.
	 * 
	 * Uses a pneumatic to deploy and a motor to run mecanum wheels.
	 */
	public static class intake {
		public static final boolean present = getBoolean("intake/present", false);
		public static final int canID = getInt("intake/canID", 10);
		public static final PIDF pidf = getPIDF("intake", new PIDF(0.015, 0.0, 15.0, 0.019));
		public static final int solenoidPort = getInt("intake/solenoidPort", 1);
	}

	/**
	 * Colour wheel parameters.
	 * 
	 * Unknown deployment method. Single motor to spin wheel.
	 */
	public static class colourWheel {
		public static final boolean present = getBoolean("colourWheel/present", false);
		public static final int canID = getInt("colourWheel/canID", 7);
		public static final PIDF pidf = getPIDF("colourWheel", new PIDF(0, 0, 0, 0));
		public static final int solenoidPort = getInt("colourWheel/solenoidPort", 5);
	}

	/**
	 * Loader parameters.
	 * 
	 * A hooper containing a spinner motor for pushing the balls into the shooter.
	 */
	public static class loader {
		public static final boolean present = getBoolean("loader/present", false);
		public static final int spinnerCanID = getInt("loader/spinner/canID", 12);
		public static final PIDF spinnderPIDF = getPIDF("loader/spinner/", new PIDF(0.3, 0.0, 30.0, 0.1));
		public static final int passthroughCanID = getInt("loader/passthrough/canID", 11);
		public static final PIDF passthroughPIDF = getPIDF("loader/passthrough/", new PIDF(0, 0, 0, 0));
		public static final int ballInDetectorPort = getInt("loader/ballInDetectorPort", 0);
		public static final int ballOutDetectorPort = getInt("loader/ballOutDetectorPort", 1);
		public static final int solenoidPort = getInt("loader/solenoidPort", 2);
	}

	/**
	 * Shooter parameters.
	 * 
	 * A single shooter wheel powered by three motors.
	 */
	public static class shooter {
		public static final boolean present = getBoolean("shooter/present", false);
		public static final int[] canIds = getIntArray("shooter/shooterCanIDs", new int[] { 30, 31, 32 });;
		public static final PIDF pidf = getPIDF("shooter/", new PIDF(0.7, 0.0, 0.0, 0.08));
		public static final int solenoidPort = getInt("shooter/solenoidPort", 3);
	}

	/**
	 * PDP parameters
	 * 
	 * The motor controller wrappers also monitor current, so this is normally off.
	 */
	public static class pdp {
		public static final boolean present = getBoolean("pdp/present", false);
		public static final int canId = getInt("pdp/canID", 62);
		public static final boolean monitor = getBoolean("pdp/monitor", false); // by default we do NOT monitor the PDP
		public static final int[] channelsToMonitor = getIntArray("pdp/channels", new int[0]); // by default we do NOT monitor any channels
	}

	/**
	 * PCM parameters
	 * 
	 * Pneumatic control module controls intake, the climber brake, buddy climb and
	 * the shooter hood.
	 */
	public static class pcm {
		public static final boolean present = getBoolean("pcm/present", false);
		public static final int canId = getInt("pcm/canID", 61);
	}

	/**
	 * Vision parameters
	 * 
	 * A jevois camera connected via USB for detecting the vision target on the
	 * goal.
	 */
	public static class vision {
		public static final boolean present = getBoolean("vision/present", false);
		public static final double hMin = getDouble("vision/hsvFilter/h/min", 40.0);
		public static final double hMax = getDouble("vision/hsvFilter/h/max", 100.0);
		public static final double sMin = getDouble("vision/hsvFilter/s/min", 20.0);
		public static final double sMax = getDouble("vision/hsvFilter/s/max", 225.0);
		public static final double vMin = getDouble("vision/hsvFilter/v/min", 40.0);
		public static final double vMax = getDouble("vision/hsvFilter/v/max", 255.0);
	}

	/**
	 * Buddy climb
	 * 
	 * Not currently used or read from the config file.
	 */
	public static class buddyClimb {
		public static final boolean present = getBoolean("buddyClimb/present", false);
		public static final int solenoidPort = getInt("buddyClimb/solenoidPort", 7);
	}

	/**
	 * LED strip
	 * 
	 * Used to indicate the state of the robot (balls count, shoot count, issues).
	 */
	public static class ledStrip {
		public static final boolean present = getBoolean("ledStrip/present", false);
		public static final int pwmPort = getInt("ledStrip/pwmPort", 0);
		public static final int numLEDs = getInt("ledStrip/numLEDs", 30);
	}

	public static final boolean doCharting = getBoolean("charting/enabled", true);



	// Only implementation from here onwards.

	private final static ConfigReader reader = new ConfigReader();

	/**
	 * Needs to be called after the config is loaded to write out an example config
	 * file and to print out details about the config file.
	 */
	public static void finishLoadingConfig() {
		reader.finishLoadingConfig();
	}

	protected static String getMotorControllerType(final String parameterName, final String defaultValue) {
		return reader.getMotorControllerType(parameterName, defaultValue);
	}

	protected static int getInt(final String key, final int defaultValue) {
		return reader.getInt(key, defaultValue);
	}

	protected static double getDouble(final String key, final double defaultValue) {
		return reader.getDouble(key, defaultValue);
	}

	protected static PIDF getPIDF(final String prefix, final PIDF pidf) {
		return reader.getPIDF(prefix, pidf);
	}

	protected static boolean getBoolean(final String key, final boolean defaultValue) {
		return reader.getBoolean(key, defaultValue);
	}
	
	protected static String getString(final String key, final String defaultValue) {
		return reader.getString(key, defaultValue);
	}

	protected static int[] getIntArray(final String key, final int[] defaultValue) {
		return reader.getIntArray(key, defaultValue);
	}
}