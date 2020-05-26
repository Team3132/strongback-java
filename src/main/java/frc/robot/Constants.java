package frc.robot;

import java.nio.file.Paths;

import com.revrobotics.ColorMatch;

import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.wpilibj.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.wpilibj.trajectory.constraint.TrajectoryConstraint;
import edu.wpi.first.wpilibj.util.Color;

import static frc.robot.lib.PoseHelper.createPose2d;

/**
 * These are constants used by the robot. They define physical things about the world, or the robot.
 * 
 * We collate them here to have all these stored in one place.
 */
public class Constants {

    private Constants() {
		throw new IllegalStateException();
	}

	/*
	 * Global - These things are immutable
	 */
	public static final double FULL_CIRCLE = 360.0;		// size of a full circle in internal units (degrees)
	public static final double HALF_CIRCLE = 180.0;		// size of a half circle in internal units (degrees)
	public static final double QUARTER_CIRCLE = 90.0;	// size of a quarter circle in internal units (degrees)
	public static final double INCHES_TO_METRES = 0.0254;

	public static final double JOYSTICK_DEADBAND_MINIMUM_VALUE = 0.05;	// below this we deadband the value away
	/*
	 * Location on the roborio of the configuration file.
	 */
	public static final String HOME_DIRECTORY = System.getProperty("user.home");
	public static final String CONFIG_FILE_PATH = Paths.get(HOME_DIRECTORY, "config.txt").toString();
	public static final String ROBOT_NAME_FILE_PATH = Paths.get(HOME_DIRECTORY, "robotname.txt").toString();
	public static final long EXECUTOR_CYCLE_INTERVAL_MSEC = 20;  // 50Hz
	public static final double DASHBOARD_UPDATE_INTERVAL_SEC = 0.5;
	
	/*
	 * Current limits.
	 */
	public static final int DEFAULT_CONTINUOUS_CURRENT_LIMIT = 30;
	public static final int DEFAULT_PEAK_CURRENT_LIMIT = 40;

	/*
	 * Drivebase Constants
	 * 
	 * The robot has motors on each side. This is the information that defines these motors and their behaviour
	 */
	public static final String MOTOR_CONTROLLER_TYPE_TALONSRX 	= "TalonSRX";
	public static final String MOTOR_CONTROLLER_TYPE_SPARKMAX 	= "SparkMAX";
	public static final String DEFAULT_MOTOR_CONTROLLER_TYPE	= MOTOR_CONTROLLER_TYPE_TALONSRX;

	// Encoder values
	public static final double FALCON_ENCODER_TICKS = 2048;  // Falon inbuilt encoders.
	public static final double SPARKMAX_ENCODER_TICKS = 42; // SparkMAX inbuild encoders.
	public static final double S4T_ENCODER_TICKS = 1440; // ticks per rev.
	public static final double VERSA_INTEGRATED_ENCODER_TICKS = 4096; // ticks per rotation

	// Distance the robot moves per revolution of the wheels.
	public static final double DRIVE_WHEEL_DIAMETER_METRES  = 6 * INCHES_TO_METRES; // 6" wheels.
	public static final double DRIVE_METRES_PER_REV = DRIVE_WHEEL_DIAMETER_METRES * Math.PI;
	public static final double DRIVE_GEABOX_RATIO = 189.0/17.0;

	public static final String DRIVE_MODE_ARCADE = "arcade";
	public static final String DRIVE_MODE_CHEESY = "cheesy";	
	public static final double DRIVE_SLOW_POWER = 0.3;

	
	/**
	 * Field / Auto, see location.java for more info on the coordinate system. 
	 */

	public static final double ROBOT_LENGTH = 37.311 * INCHES_TO_METRES;
	public static final double HALF_ROBOT_LENGTH = ROBOT_LENGTH / 2;

	public static final double FIELD_LENGTH = 629.25 * INCHES_TO_METRES;
	public static final double HALF_FIELD_LENGTH = FIELD_LENGTH / 2;
	public static final double FIELD_WIDTH = 323.25 * INCHES_TO_METRES;
	public static final double HALF_FIELD_WIDTH = FIELD_WIDTH / 2;

	public static final double TRENCH_DIST_BETWEEN_BALLS = 36 * INCHES_TO_METRES;

	public static final double GOAL_YPOS = -HALF_FIELD_WIDTH + 94.66 * INCHES_TO_METRES;
	public static final double AUTO_LINE_XPOS = -HALF_FIELD_LENGTH + 508.875 * INCHES_TO_METRES;
	public static final double ALLIANCE_TRENCH_BALLS_YPOS = - HALF_FIELD_WIDTH + 27.75 * INCHES_TO_METRES;
	public static final double OPPOSING_TRENCH_BALLS_YPOS = -ALLIANCE_TRENCH_BALLS_YPOS;

	// Field Positions 
	public static final Pose2d ALLIANCE_TRENCH_FIRST_BALL = createPose2d(TRENCH_DIST_BETWEEN_BALLS * 2,ALLIANCE_TRENCH_BALLS_YPOS,0);
	public static final Pose2d ALLIANCE_TRENCH_SECOND_BALL = createPose2d(TRENCH_DIST_BETWEEN_BALLS,ALLIANCE_TRENCH_BALLS_YPOS,0);
	public static final Pose2d ALLIANCE_TRENCH_THIRD_BALL = createPose2d(0 ,ALLIANCE_TRENCH_BALLS_YPOS,0);
	public static final Pose2d ALLIANCE_TRENCH_FIFTH_BALL = createPose2d(-65.53 * INCHES_TO_METRES,ALLIANCE_TRENCH_BALLS_YPOS,0);

	// Auto Starting Positions
	public static final Pose2d AUTO_LINE_ALLIANCE_TRENCH = createPose2d(AUTO_LINE_XPOS - HALF_ROBOT_LENGTH,ALLIANCE_TRENCH_BALLS_YPOS,0); 
	public static final Pose2d AUTO_LINE_GOAL = createPose2d(AUTO_LINE_XPOS - HALF_ROBOT_LENGTH,GOAL_YPOS,0); 
	public static final Pose2d AUTO_LINE_OPPOSING_TRENCH = createPose2d(AUTO_LINE_XPOS - HALF_ROBOT_LENGTH,OPPOSING_TRENCH_BALLS_YPOS,0); 

	/*
	 *  Intake constants 
	 */
	public static final double INTAKE_ENCODER_GEARBOX_RATIO = 3;
	public static final double INTAKE_TARGET_RPS = 30;

	/*
	* Shooter constants
	*/
	public static final double SHOOTER_SPEED_TOLERANCE_RPS = 3.33;
	public static final double SHOOTER_CLOSE_TARGET_SPEED_RPS = 62;
	public static final double SHOOTER_AUTO_LINE_TARGET_SPEED_RPS = 90;
	public static final double SHOOTER_FAR_TARGET_SPEED_RPS = 95;
	public static final double SHOOTER_GEARBOX_RATIO = 1; // Encoder is on output shaft.
	
	/*
	* Loader
	*/ 
	public static final double LOADER_MOTOR_INTAKING_RPS = 18;
	public static final double LOADER_MOTOR_SHOOTING_RPS = 8;
	public static final double PASSTHROUGH_MOTOR_CURRENT = 0.8;
	public static final double LOADER_MAIN_MOTOR_GEARBOX_RATIO = 1; // Encoder is on output shaft.

	/*
	 * Camera server constants
	 * These are magic numbers to tell the Jevois which vision processor to load.
	 */
	public static final int CAMERA_RESOLUTION_WIDTH = 640;
	public static final int CAMERA_RESOULTION_HEIGHT = 252;
	public static final int CAMERA_FRAMES_PER_SECOND = 60;
	// Vision (all need tuning)
	public static final double VISON_MAX_TARGET_AGE_SECS = 2;
	public static final double VISION_MAX_VELOCITY_JERK = 32; // m/s/s
	public static final double VISION_SPEED_SCALE = 2.4 * INCHES_TO_METRES;
	public static final double VISION_ASSIST_ANGLE_SCALE = 0.6;
	public static final double VISION_AIM_ANGLE_SCALE = 0.02;
	public static final double VISION_SPLINE_MIN_DISTANCE = 60 * INCHES_TO_METRES;
	public static final double VISION_WAYPOINT_DISTANCE_SCALE = 0.5; // percentage 0 to 1
	public static final double VISION_STOP_DISTANCE = 6; // metres 
	public static final double VISION_MAX_DRIVE_SPEED = 0.4; // metres per second
	public static final double VISION_AIM_ANGLE_TOLERANCE = 2; //degrees
	public static final double VISION_AIM_DISTANCE_TOLERANCE = 0.1; //metres
	public static final double VISION_AIM_DISTANCE_SCALE = 0.4;
	public static final double VISION_AIM_TIME_COMPLETE = 0.5; // seconds


	// Vision low pass filter (needs tuning)
	public static final double GOAL_LOWPASS_ALPHA = 0.2;

	// Turn to angle (all need tuning)
	public static final double TURN_TO_ANGLE_MAX_VELOCITY_JERK = 50;
	public static final double TURN_TO_ANGLE_ANGLE_SCALE = 0.3;
	
	// logging information constants
	public static final String USB_FLASH_DRIVE = "/media/sda1/";
	public static final String WEB_BASE_PATH = USB_FLASH_DRIVE;		// where web server's data lives
	public static final String LOG_BASE_PATH = USB_FLASH_DRIVE;		// log files (has to be inside web server)
	public static final String LOG_DATA_EXTENSION = "data";
	public static final String LOG_DATE_EXTENSION = "date";
	public static final String LOG_LATEST_EXTENSION = "latest";
	public static final String LOG_EVENT_EXTENSION = "event";
	public static final int	 WEB_PORT = 5800;// first open port for graph/log web server

	// Config WebServer
	public static final String CONFIG_WEB_ROOT = "/home/lvuser/www";
	public static final int CONFIG_WEB_PORT = 5801;
	
	// LocationHistory
	public static final int LOCATION_HISTORY_MEMORY_SECONDS = 5;
	public static final int LOCATION_HISTORY_CYCLE_SPEED = 100; // in hz

	/*
	 * Command timings
	 */

	public static final class DriveConstants {
		public static final double kTrackwidthMeters = 0.71;

		public static final DifferentialDriveKinematics kDriveKinematics = new DifferentialDriveKinematics(
				kTrackwidthMeters);

		public static final double kWheelDiameterMeters = 0.15; // 6" wheels
		public static final double kGearboxRatio = 11;
		public static final double kEncoderDistancePerRev =
				// Encoders are mounted on the motors. Wheels are by 11:1 gearbox
				(kWheelDiameterMeters * Math.PI) / kGearboxRatio;

		// The Robot Characterization Toolsuite provides a convenient tool for obtaining
		// these values for your robot.
		public static final double ksVolts = 0.283;
		public static final double kvVoltSecondsPerMeter = 2.49;
		public static final double kaVoltSecondsSquaredPerMeter = 0.316;

		// PID values.
		public static final double kPDriveVel = 0.01; // should be 12.1
		// kD should be 0

		public static final double kMaxSpeedMetersPerSecond = 4;
		public static final double kMaxAccelerationMetersPerSecondSquared = 2;

		// Reasonable baseline values for a RAMSETE follower in units of meters and
		// seconds
		public static final double kRamseteB = 2;
		public static final double kRamseteZeta = 0.7;

		public static final double kMaxVoltage = 10;

		// Create a voltage constraint to ensure we don't accelerate too fast
		public static final TrajectoryConstraint kAutoVoltageConstraint = new DifferentialDriveVoltageConstraint(
			new SimpleMotorFeedforward(Constants.DriveConstants.ksVolts,
					Constants.DriveConstants.kvVoltSecondsPerMeter,
					Constants.DriveConstants.kaVoltSecondsSquaredPerMeter),
			Constants.DriveConstants.kDriveKinematics, 
			Constants.DriveConstants.kMaxVoltage);
	}
	/*
	 * Colour Wheel
	 */
	// Values callibrated using vynl sticker for control panel.
	public static final Color COLOUR_WHEEL_BLUE_TARGET = ColorMatch.makeColor(0.147, 0.437, 0.416); // Values from the colour sensor used to match colours.
	// public static final Color COLOUR_WHEEL_GREEN_TARGET = ColorMatch.makeColor(0.189, 0.559, 0.250); // This is the real green value.
	public static final Color COLOUR_WHEEL_GREEN_TARGET = ColorMatch.makeColor(0.209, 0.608, 0.182);
	public static final Color COLOUR_WHEEL_RED_TARGET = ColorMatch.makeColor(0.484, 0.366, 0.150);
	public static final Color COLOUR_WHEEL_YELLOW_TARGET = ColorMatch.makeColor(0.322, 0.546, 0.131);
	public static final Color COLOUR_WHEEL_WHITE_TARGET = ColorMatch.makeColor(0.276, 0.587, 0.217);
	public static final double COLOUR_WHEEL_MOTOR_OFF = 0;
	public static final double COLOUR_WHEEL_MOTOR_ADJUST = 0.3;
	public static final double COLOUR_WHEEL_MOTOR_FULL = 1;
	public static final double COLOUR_WHEEL_MOTOR_HALF = 0.5;
	public static final int COLOUR_WHEEL_ROTATION_TARGET = 3*8 + 2; // Counting in eights aiming for 3.25 full rotations.
	public static final int COLOUR_WHEEL_DELAY = 15; // Time in miliseconds to wait before disabling motor when correct colour found.

	/*
	 * LED constants
	 */
	public static final int LED_STRIP_COUNTDOWN = 15;
	public static final double LED_BRIGHTNESS_PERCENTAGE = 0.2;

	/*
	 * Log Syncing
	 */
	public static final int RSYNC_PORT = 5802; // External port to forward to port 22 for transfering robot logs to pc over rsync.
	public static final String RSYNC_HOSTNAME = "roborio-3132-FRC.local"; //Hostname of the robot.
}
