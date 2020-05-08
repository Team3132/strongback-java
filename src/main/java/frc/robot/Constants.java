package frc.robot;

import java.nio.file.Paths;

import com.revrobotics.ColorMatch;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.wpilibj.util.Color;

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
	public static final int NUM_JOYSTICK_BUTTONS = 32;	// maximum number of push buttons on a joystick
	public static final int NUM_JOYSTICK_DIRECTIONS = 10;
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
	public static final boolean DRIVE_PRESENT_DEFAULT = false;
	public static final int[] DRIVE_LEFT_TALON_WITH_ENCODERS_CAN_ID_LIST	 = {4,5};
	public static final int[] DRIVE_LEFT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST	 = {};
	public static final int[] DRIVE_RIGHT_TALON_WITH_ENCODERS_CAN_ID_LIST	 = {1,2}; 
	public static final int[] DRIVE_RIGHT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST = {};
	public static final boolean DRIVE_BRAKE_MODE			= true;
	
	public static final String MOTOR_CONTROLLER_TYPE_TALONSRX 	= "TalonSRX";
	public static final String MOTOR_CONTROLLER_TYPE_SPARKMAX 	= "SparkMAX";
	public static final String DRIVE_DEFAULT_CONTROLLER_TYPE	= MOTOR_CONTROLLER_TYPE_TALONSRX;

	// Encoder values
	public static final double FALCON_ENCODER_TICKS = 2048;  // Falon inbuilt encoders.
	public static final double SPARKMAX_ENCODER_TICKS = 42; // SparkMAX inbuild encoders.
	public static final double S4T_ENCODER_TICKS = 1440; // ticks per rev.
	public static final double VERSA_INTEGRATED_ENCODER_TICKS = 4096; // ticks per rotation

	// Distance the robot moves per revolution of the wheels.
	public static final double DRIVE_WHEEL_DIAMETER_METRES  = 6 * INCHES_TO_METRES; // 6" wheels.
	public static final double DRIVE_METRES_PER_REV = DRIVE_WHEEL_DIAMETER_METRES * Math.PI;
	public static final double DRIVE_GEABOX_RATIO = 189.0/17.0;
	
	// This magic number is the "fastest" we want the motor to go. It is calculated
	// by running the motor at full speed and observing what the quad encoder
	// velocity returns.
	// This number is very suspect.
	public static final double DRIVE_COUNT_100ms = 13.0;
	// A more sensible number.
	public static final double DRIVE_MAX_SPEED = 4;
	public static final double DRIVE_MAX_ACCELERATION = 2; // Inches/sec/sec
	public static final double DRIVE_MAX_JERK = 1; // Inches/sec/sec/sec.
	public static final double DRIVE_P = 0.0;//5;//1.0;
	public static final double DRIVE_I = 0.0;
	public static final double DRIVE_D = 0.0;//0.01;
	public static final double DRIVE_F = 0.7;//0.665;
	public static final double DRIVE_DEADBAND = 0.05;
	public static final double DRIVE_RAMP_RATE = 0.01; //0.175; sluggish but smooth //0.1375; jittered	// seconds from neutral to full
	public static final String DRIVE_MODE_ARCADE = "arcade";
	public static final String DRIVE_MODE_CHEESY = "cheesy";	
	public static final String DRIVE_DEFAULT_MODE = DRIVE_MODE_ARCADE;  // Joystick teleop mode.
	public static final int DRIVE_CONT_CURRENT = 38;	// current limit to this value if...
	public static final int DRIVE_PEAK_CURRENT = 80;	// the current exceeds this value for 100ms
	public static final int DRIVE_SCALE_FACTOR = 128;
	public static final double DRIVE_OFF_LEVEL_TWO_POWER = 0.3;

	
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
	public static final Pose2d ALLIANCE_TRENCH_FIRST_BALL = new Pose2d(TRENCH_DIST_BETWEEN_BALLS * 2, ALLIANCE_TRENCH_BALLS_YPOS, new Rotation2d(Math.toRadians(0)));
	public static final Pose2d ALLIANCE_TRENCH_SECOND_BALL = new Pose2d(TRENCH_DIST_BETWEEN_BALLS, ALLIANCE_TRENCH_BALLS_YPOS, new Rotation2d(Math.toRadians(0)));
	public static final Pose2d ALLIANCE_TRENCH_THIRD_BALL = new Pose2d(0 , ALLIANCE_TRENCH_BALLS_YPOS, new Rotation2d(Math.toRadians(0)));
	public static final Pose2d ALLIANCE_TRENCH_FIFTH_BALL = new Pose2d(-65.53 * INCHES_TO_METRES, ALLIANCE_TRENCH_BALLS_YPOS, new Rotation2d(Math.toRadians(0)));

	// Auto Starting Positions
	public static final Pose2d AUTO_LINE_ALLIANCE_TRENCH = new Pose2d(AUTO_LINE_XPOS - HALF_ROBOT_LENGTH, ALLIANCE_TRENCH_BALLS_YPOS, new Rotation2d(Math.toRadians(0))); 
	public static final Pose2d AUTO_LINE_GOAL = new Pose2d(AUTO_LINE_XPOS - HALF_ROBOT_LENGTH, GOAL_YPOS, new Rotation2d(Math.toRadians(0))); 
	public static final Pose2d AUTO_LINE_OPPOSING_TRENCH = new Pose2d(AUTO_LINE_XPOS - HALF_ROBOT_LENGTH, OPPOSING_TRENCH_BALLS_YPOS, new Rotation2d(Math.toRadians(0))); 

	/*
	 * LED constants
	 */
	// brightness
	public static final double LED_PERCENTAGE = 0.2;

	/*
	 *  Intake constants 
	 */
	public static final boolean INTAKE_PRESENT_DEFAULT = false;
	public static final int INTAKE_CAN_ID = 10;
	public static final double INTAKE_ENCODER_GEARBOX_RATIO = 3;
	public static final double INTAKE_TARGET_RPS = 30;
	public static final double INTAKE_F = 0.019;
	public static final double INTAKE_P = 0.015;
	public static final double INTAKE_I = 0;
	public static final double INTAKE_D = 15.0;



	public static final int INTAKE_SOLENOID_PORT = 1;

	public static final int[] TEST_SPARK_MOTOR_CAN_ID_LIST = {50, 51};

	public static final int[] OUTTAKE_MOTOR_TALON_CAN_ID_LIST = {15};

	/*
	* Shooter constants
	*/
	public static final boolean SHOOTER_PRESENT_DEFAULT = false;
	public static final int[] SHOOTER_TALON_CAN_ID_LIST	 = {30, 31, 32};
	public static final int SHOOTER_HOOD_SOLENOID_PORT = 3;
	public static final double SHOOTER_SPEED_TOLERANCE_RPS = 3.33;
	public static final double SHOOTER_F = 0.08;//0.075;
	public static final double SHOOTER_P = 0.7;
	public static final double SHOOTER_I = 0;
	public static final double SHOOTER_D = 0;
	public static final double SHOOTER_CLOSE_TARGET_SPEED_RPS = 62;
	public static final double SHOOTER_AUTO_LINE_TARGET_SPEED_RPS = 90;
	public static final double SHOOTER_FAR_TARGET_SPEED_RPS = 95;
	public static final double SHOOTER_GEARBOX_RATIO = 1; // Encoder is on output shaft.
	
	/*
	* Loader
	*/
	public static final boolean LOADER_PRESENT_DEFAULT = false;
	public static final int LOADER_SPINNER_MOTOR_CAN_ID = 12;
	public static final int LOADER_PASSTHROUGH_MOTOR_CAN_ID = 11;
	public static final int IN_BALL_DETECTOR_DIO_PORT = 0;
	public static final int OUT_BALL_DETECTOR_DIO_PORT = 1;
	public static final int PADDLE_SOLENOID_PORT = 2; 
	public static final double LOADER_MOTOR_INTAKING_RPS = 18;
	public static final double LOADER_MOTOR_SHOOTING_RPS = 8;
	public static final double PASSTHROUGH_MOTOR_CURRENT = 0.8;
	public static final double LOADER_MAIN_MOTOR_GEARBOX_RATIO = 1; // Encoder is on output shaft.
	public static final double LOADER_SPINNER_P = 0.3;
	public static final double LOADER_SPINNER_I = 0;
	public static final double LOADER_SPINNER_D = 30;
	public static final double LOADER_SPINNER_F = 0.1;

	// Power distribution Panel (PDP)
	public static final boolean PDP_PRESENT_DEFAULT = false;
	public static final int PDP_CAN_ID = 62;
	
	// Pneumatic Control Modules (PCM)
	public static final boolean PCM_PRESENT_DEFAULT = false;
	public static final int PCM_CAN_ID = 61;

	/*
	 * Camera server constants
	 * These are magic numbers to tell the Jevois which vision processor to load.
	 */
	public static final int CAMERA_RESOLUTION_WIDTH = 640;
	public static final int CAMERA_RESOULTION_HEIGHT = 252;
	public static final int CAMERA_FRAMES_PER_SECOND = 60;
	// Vision (all need tuning)
	public static final boolean VISION_PRESENT_DEFAULT = false;
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



	// Vision filter parameters
	public static final double VISION_H_MIN = 40;
	public static final double VISION_H_MAX = 100;
	public static final double VISION_S_MIN = 20;
	public static final double VISION_S_MAX = 255;
	public static final double VISION_V_MIN = 40;
	public static final double VISION_V_MAX = 255;

	// Vision low pass filter (needs tuning)
	public static final double GOAL_LOWPASS_ALPHA = 0.2;

	// Turn to angle (all need tuning)
	public static final double TURN_TO_ANGLE_MAX_VELOCITY_JERK = 50;
	public static final double TURN_TO_ANGLE_ANGLE_SCALE = 0.3;

	// Climber
	public static final int CLIMBER_PTO_SOLENOID_PORT = 6;
	public static final int CLIMBER_BRAKE_SOLENOID_PORT = 0; 
	
	// Buddy climb
	public static final boolean BUDDYCLIMB_PRESENT_DEFAULT = false;
	public static final int BUDDYCLIMB_SOLENOID_PORT = 7;

	// logging information constants
	public static final String USB_FLASH_DRIVE = "/media/sda1/";
	public static final String WEB_BASE_PATH = USB_FLASH_DRIVE;		// where web server's data lives
	public static final String LOG_BASE_PATH = USB_FLASH_DRIVE;		// log files (has to be inside web server)
	public static final String LOG_DATA_EXTENSION = "data";
	public static final String LOG_DATE_EXTENSION = "date";
	public static final String LOG_LATEST_EXTENSION = "latest";
	public static final String LOG_EVENT_EXTENSION = "event";
	public static final int	 WEB_PORT = 5800;// first open port for graph/log web server
	public static final double LOG_GRAPH_PERIOD = 0.05;	// run the graph updater every 50ms

	// Config WebServer
	public static final String CONFIG_WEB_ROOT = "/home/lvuser/www";
	public static final int CONFIG_WEB_PORT = 5801;
	
	// LocationHistory
	public static final int LOCATION_HISTORY_MEMORY_SECONDS = 5;
	public static final int LOCATION_HISTORY_CYCLE_SPEED = 100; // in hz

	// NavX (gyro)
	public static final boolean NAVX_PRESENT_DEFAULT = DRIVE_PRESENT_DEFAULT;
	
	/*
	 * Command timings
	 */
	public static final double TIME_COMMAND_RUN_PERIOD = (1.0/50.0);		// run the commands 50 times a second
	public static final double TIME_LOCATION_PERIOD = (1.0/(double)LOCATION_HISTORY_CYCLE_SPEED);	// update the location subsystem 100 times a second
	public static final double TIME_DRIVEBASE_PERIOD = (1.0/40.0);	// update the drivebase 40 times a second

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
	}
	/*
	 * Colour Wheel
	 */
	public static final boolean COLOUR_WHEEL_PRESENT_DEFAULT = false;
	public static final int COLOUR_WHEEL_CAN_ID = 7;
	public static final int COLOUR_WHEEL_SOLENOID_PORT = 5;
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
	 * LED Strip
	 */
	public static final boolean LED_STRIP_PRESENT_DEFAULT = false;
	public static final int LED_STRIP_PWM_PORT = 0;
	public static final int LED_STRIP_NUMBER_OF_LEDS = 30;
	public static final int LED_STRIP_COUNTDOWN = 15;

	/*
	 * Log Syncing
	 */
	public static final int RSYNC_PORT = 5802; // External port to forward to port 22 for transfering robot logs to pc over rsync.
	public static final String RSYNC_HOSTNAME = "roborio-3132-FRC.local"; //Hostname of the robot.
}
