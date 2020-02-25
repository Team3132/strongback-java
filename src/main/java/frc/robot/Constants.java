package frc.robot;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.ctre.phoenix.CANifier;
import com.ctre.phoenix.CANifier.LEDChannel;
import com.revrobotics.ColorMatch;

import edu.wpi.first.wpilibj.util.Color;

import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;

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
	public static final String CONFIG_FILE_PATH= "/home/lvuser/config.txt";
	public static final long EXECUTOR_CYCLE_INTERVAL_MSEC = 20;  // 50Hz
	public static final double DASHBOARD_UPDATE_INTERVAL_SEC = 0.5;
	
	/*
	 * Current limits.
	 */
	public static final int DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT = 30;
	public static final int DEFAULT_TALON_PEAK_CURRENT_LIMIT = 40;

	/*
	 * Drivebase Constants
	 * 
	 * The robot has motors on each side. This is the information that defines these motors and their behaviour
	 */
	public static final double ROBOT_WIDTH_INCHES = 20;
	public static final int[] DRIVE_LEFT_TALON_WITH_ENCODERS_CAN_ID_LIST	 = {5,6};
	public static final int[] DRIVE_LEFT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST	 = {};
	public static final int[] DRIVE_RIGHT_TALON_WITH_ENCODERS_CAN_ID_LIST	 = {1,2}; 
	public static final int[] DRIVE_RIGHT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST = {};
	public static final boolean DRIVE_BRAKE_MODE			= true;
	public static final double DRIVE_WHEEL_DIAMETER         = 6; // inches 
	public static final int DRIVE_ENCODER_CODES_PER_REV		= 4 * 360;
	public static final double DRIVE_GEABOX_RATIO = 11;
	
	public static final String MOTOR_CONTROLLER_TYPE_TALONSRX 	= "TalonSRX";
	public static final String MOTOR_CONTROLLER_TYPE_SPARKMAX 	= "SparkMAX";
	public static final String DRIVE_DEFAULT_CONTROLLER_TYPE	= MOTOR_CONTROLLER_TYPE_TALONSRX;

	// distance the robot moves per revolution of the encoders. Gearing needs to be taken into account here.
	// at full speed in a static environment the encoders are producing 2000 count differences per 100ms
	public static final double DRIVE_DISTANCE_PER_REV = DRIVE_WHEEL_DIAMETER * Math.PI;
	//public static final double DRIVE_MOTOR_POSITION_SCALE = DRIVE_ENCODER_CODES_PER_REV / DRIVE_DISTANCE_PER_REV;
	public static final double DRIVE_MOTOR_POSITION_SCALE = DRIVE_DISTANCE_PER_REV / DRIVE_GEABOX_RATIO;
	
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
	
	/*
	 * LED channels for the canifier 
	 */
	public static final CANifier.LEDChannel RED_LED_STRIP_CHANNEL = LEDChannel.LEDChannelB;
	public static final CANifier.LEDChannel GREEN_LED_STRIP_CHANNEL = LEDChannel.LEDChannelA;
	public static final CANifier.LEDChannel BLUE_LED_STRIP_CHANNEL = LEDChannel.LEDChannelC;
	
	/*
	 *  Intake constants 
	 */
	public static final int INTAKE_MOTOR_TALON_CAN_ID = 10;
	
	public static final double INTAKE_MOTOR_CURRENT = 0.5;  
	public static final double INTAKE_POSITION_F = 0;
	public static final double INTAKE_POSITION_P = 0;
	public static final double INTAKE_POSITION_I = 0;
	public static final double INTAKE_POSITION_D = 0;

	public static final int INTAKE_SOLENOID_PORT = 0;

	public static final int[] TEST_SPARK_MOTOR_CAN_ID_LIST = {50, 51};

	public static final int[] OUTTAKE_MOTOR_TALON_CAN_ID_LIST = {15};

	
	/*
	* Loader
	*/
	public static final int LOADER_SPINNER_CAN_ID = 30; //TODO: find canID for Loader motor
	public static final int LOADER_PASSTHROUGH_MOTOR_CAN_ID = 31; //TODO: find canID for Loader Input motor
	public static final int LOADER_FEEDER_MOTOR_CAN_ID = 47; //TODO: find canID for Loader Output motor
	public static final double LOADER_MOTOR_CURRENT = 1.0;
	public static final int LOADER_SOLENOID_PORT = 2; 
	public static final int PADDLE_SOLENOID_PORT = 1;
	public static final double LOADER_IN_MOTOR_SCALE = 4096/10;//1024*10; //ticks per rotation
	public static final double LOADER_MAIN_MOTOR_SCALE = 4096/10; //ticks per rotation
	public static final double LOADER_SPINNER_P = 0.4; //TODO: assign values to PIDF
	public static final double LOADER_SPINNER_I = 0;
	public static final double LOADER_SPINNER_D = 20;
	public static final double LOADER_SPINNER_F = 0.225;
	public static final double LOADER_PASSTHROUGH_P = 0.2;
	public static final double LOADER_PASSTHROUGH_I = 0;
	public static final double LOADER_PASSTHROUGH_D = 2.0;
	public static final double LOADER_PASSTHROUGH_F = 0.1025;
	
	/*
	 * Canifier 
	 */
	public static final int LED_CANIFIER_CAN_ID = 21;

	// Power distribution Panel (PDP)
	public static final int PDP_CAN_ID = 62;
	
	// Pneumatic Control Modules (PCM)
	public static final int PCM_CAN_ID = 61;

	/*
	 * Camera server constants
	 * These are magic numbers to tell the Jevois which vision processor to load.
	 */
	public static final int CAMERA_RESOLUTION_WIDTH = 640;
	public static final int CAMERA_RESOULTION_HEIGHT = 252;
	public static final int CAMERA_FRAMES_PER_SECOND = 60;
	// Vision (all need tuning)
	public static final double VISON_MAX_TARGET_AGE_SECS = 2;
	public static final double VISION_MAX_VELOCITY_JERK = 40; // in/s/s
	public static final double VISION_SPEED_SCALE = 2.4;
	public static final double VISION_ASSIST_ANGLE_SCALE = 0.6;
	public static final double VISION_AIM_ANGLE_SCALE = 0.4;
	public static final double VISION_SPLINE_MIN_DISTANCE = 60; // inches
	public static final double VISION_WAYPOINT_DISTANCE_SCALE = 0.5; // percentage 0 to 1
	public static final double VISION_STOP_DISTANCE = 230; // inches 
	public static final double VISION_MAX_DRIVE_SPEED = 15;
	public static final double VISION_AIM_ANGLE_RANGE = 2; //degrees
	public static final double VISION_AIM_DISTANCE_RANGE = 5; //inches
	public static final double VISION_AIM_DISTANCE_SCALE = 0.4;



	// Vision filter parameters
	public static final double VISION_H_MIN = 40;
	public static final double VISION_H_MAX = 100;
	public static final double VISION_S_MIN = 20;
	public static final double VISION_S_MAX = 255;
	public static final double VISION_V_MIN = 40;
	public static final double VISION_V_MAX = 255;

	// Turn to angle (all need tuning)
	public static final double TURN_TO_ANGLE_MAX_VELOCITY_JERK = 50;
	public static final double TURN_TO_ANGLE_ANGLE_SCALE = 0.3;

	// Climber
	public static final int CLIMBER_PTOSOLENOID_CAN_ID = 11;
	public static final int CLIMBER_BRAKESOLENOID_CAN_ID = 10;
	public static final double NEO_TICKS_PER_TURN = 42;
	public static final double CLIMBER_GEAR_RATIO = 13.5;
	public static final double CLIMBER_DRUM_CIRCUMFRENCE_METRES = 0.039 * Math.PI;

	public static final double CLIMBER_TOLERANCE = 0.5;
	public static final double CLIMBER_MAX_DISTANCE_FROM_TOP = 0;
	public static final double CLIMBER_HEIGHT = 2; //Temporary, unused
	public static final double CLIMBER_F = 0;
	public static final double CLIMBER_P = 4;
	public static final double CLIMBER_I = 0;
	public static final double CLIMBER_D = 0;

	public static final double CLIMBER_DEPLOY_HEIGHT = 5;
	public static final double CLIMBER_CLIMB_HEIGHT = -5;

	public static final double CLIMBER_MAX_MOTOR_POWER = 0.1;

	public static final double 	CLIMBER_POWER_NOT_LEVEL_P = 0;
	
	public static final int CLIMBER_PEAK_CURRENT_LIMIT = 38;
	public static final double CLIMBER_WINCH_LEFT_SCALE_FACTOR = 9609/7.9529;     // 7.7953;	// 18" ticks = 20208 ticks
	public static final double CLIMBER_WINCH_RIGHT_SCALE_FACTOR = 9634/7.9528;

	// logging information constants
	public static final String WEB_BASE_PATH = "/media/sda1";		// where web server's data lives
	public static final String LOG_BASE_PATH = WEB_BASE_PATH;		// log files (has to be inside web server)
	public static final String LOG_DATA_EXTENSION = "data";
	public static final String LOG_DATE_EXTENSION = "date";
	public static final Path LOG_NUMBER_FILE = Paths.get(System.getProperty("user.home"), "lognumber.txt");
	public static final int	 WEB_PORT = 5800;			// first open port for graph/log web server
	public static final double LOG_GRAPH_PERIOD = 0.05;	// run the graph updater every 50ms
	
	// LocationHistory
	public static final int LOCATION_HISTORY_MEMORY_SECONDS = 5;
	public static final int LOCATION_HISTORY_CYCLE_SPEED = 100; // in hz
	
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
		public static final double ksVolts = 0.177;
		public static final double kvVoltSecondsPerMeter = 3.3;// Calculated value = 2.94;
		public static final double kaVoltSecondsSquaredPerMeter = 0.4;// Calculated value = 0.368;

		// Example value only - as above, this must be tuned for your drive!
		public static final double kPDriveVel = 0.01;// 3//13.3; // should be 13.3
		// kD should be 0

		public static final double kMaxSpeedMetersPerSecond = 6;// 3;
		public static final double kMaxAccelerationMetersPerSecondSquared = 2;// 1;

		// Reasonable baseline values for a RAMSETE follower in units of meters and
		// seconds
		public static final double kRamseteB = 2;
		public static final double kRamseteZeta = 0.7;
	}
	/*
	 * Colour Wheel
	 */
	public static final int COLOUR_WHEEL_CAN_ID = 7;
	// Values callibrated using vynl sticker for control panel.
	public static final Color COLOUR_WHEEL_BLUE_TARGET = ColorMatch.makeColor(0.147, 0.437, 0.416); //Values from the colour sensor used to match colours.
	//public static final Color COLOUR_WHEEL_GREEN_TARGET = ColorMatch.makeColor(0.189, 0.559, 0.250); //This is the real green value.
	public static final Color COLOUR_WHEEL_GREEN_TARGET = ColorMatch.makeColor(0.209, 0.608, 0.182);
	public static final Color COLOUR_WHEEL_RED_TARGET = ColorMatch.makeColor(0.484, 0.366, 0.150);
	public static final Color COLOUR_WHEEL_YELLOW_TARGET = ColorMatch.makeColor(0.322, 0.546, 0.131);
	public static final Color COLOUR_WHEEL_WHITE_TARGET = ColorMatch.makeColor(0.276, 0.587, 0.217);
	public static final double COLOUR_WHEEL_MOTOR_OFF = 0;
	public static final double COLOUR_WHEEL_MOTOR_ADJUST = 0.3;
	public static final double COLOUR_WHEEL_MOTOR_FULL = 1;
	public static final double COLOUR_WHEEL_MOTOR_HALF = 0.5;
	public static final int COLOUR_WHEEL_ROTATION_TARGET = 3*8 + 2; //Counting in eights aiming for 3.25 full rotations.
	public static final int COLOUR_WHEEL_DELAY = 15; //Time in miliseconds to wait before disabling motor when correct colour found.

	/*
	 * LED Strip
	 */
	public static final int LED_STRIP_PWM_PORT = 9;
	public static final int LED_STRIP_NUMBER_OF_LEDS = 30;
}