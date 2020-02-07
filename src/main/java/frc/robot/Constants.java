package frc.robot;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.ctre.phoenix.CANifier;
import com.ctre.phoenix.CANifier.LEDChannel;

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
	public static final int[] DRIVE_LEFT_TALON_WITH_ENCODERS_CAN_ID_LIST	 = {1};
	public static final int[] DRIVE_LEFT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST	 = {2};
	public static final int[] DRIVE_RIGHT_TALON_WITH_ENCODERS_CAN_ID_LIST	 = {5}; 
	public static final int[] DRIVE_RIGHT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST = {6};
	public static final boolean DRIVE_BRAKE_MODE			= true;
	public static final double DRIVE_WHEEL_DIAMETER         = 3.79;
	public static final int DRIVE_ENCODER_CODES_PER_REV		= 4 * 360;
	
	public static final String MOTOR_CONTROLLER_TYPE_TALONSRX 	= "TalonSRX";
	public static final String MOTOR_CONTROLLER_TYPE_SPARKMAX 	= "SparkMAX";
	public static final String DRIVE_DEFAULT_CONTROLLER_TYPE	= MOTOR_CONTROLLER_TYPE_TALONSRX;

	// distance the robot moves per revolution of the encoders. Gearing needs to be taken into account here.
	// at full speed in a static environment the encoders are producing 2000 count differences per 100ms
	public static final double DRIVE_DISTANCE_PER_REV = DRIVE_WHEEL_DIAMETER * Math.PI;
	public static final double DRIVE_MOTOR_POSITION_SCALE = DRIVE_ENCODER_CODES_PER_REV / DRIVE_DISTANCE_PER_REV;
	
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
	
	public static final double INTAKE_MOTOR_CURRENT = 0.5;  // Running at 0.6 with the PT and spitter can cause brownouts.s
	public static final double INTAKE_POSITION_F = 0;
	public static final double INTAKE_POSITION_P = 0;
	public static final double INTAKE_POSITION_I = 0;
	public static final double INTAKE_POSITION_D = 0;

	public static final int INTAKE_SOLENOID_PORT = 0;

	public static final int[] TEST_SPARK_MOTOR_CAN_ID_LIST = {50, 51};

	public static final int[] OUTTAKE_MOTOR_TALON_CAN_ID_LIST = {15};

	/*
	* Spitter Constant
	* TODO: add the actual can Id values to the constants.
	* TODO: Tune the Spitter F, P, I, D values.
	* TODO: Find the tolerance and spitter_speed value.
	*/
	public static final int SPITTER_LEFT_TALON_CAN_ID = 40; 
	public static final int SPITTER_RIGHT_TALON_CAN_ID = 41; 
	public static final double SPITTER_SPEED = 0.15;  // Power 0...1
	public static final double SPITTER_SCORE_SPEED = 1; //0.55; //was 0.75; 
	public static final double SPITTER_SPEED_TOLERANCE = 0;
	public static final double SPITTER_SPEED_F = 10.0/6;
	public static final double SPITTER_SPEED_P = 1;
	public static final double SPITTER_SPEED_I = 0;
	public static final double SPITTER_SPEED_D = 0;

	/**
	 * Tape Constants
	 */
	public static final double COLOUR_SENSOR_TOLERANCE = 0; // TODO: Find the tolerance for the colour sensor.
	public static final double GRADIENT_CONSTANT = 0;
	public static final double Y_AXIS_CONSTANT = 0;
	public static final double BAND_ON = 0;
	public static final double BAND_OFF = 0;
	public static final int COLOUR_SENSOR_LEFT = 0;
	public static final int COLOUR_SENSOR_RIGHT = 1;
	public static final int CENTER_OF_WHITE = 1450;
	public static final int OVER_THE_EDGE_VALUE = 2200;
	public static final int MIDDLE_OF_CARPET = 13500;

	/**
	 * Hatch constants.
	 */

	public static final double HATCH_POSITION_F = 0;
	public static final double HATCH_POSITION_P = 5;  // Not too high or the belt skips.
	public static final double HATCH_POSITION_I = 0;
	public static final double HATCH_POSITION_D = 0;
	public static final int HATCH_CONTINUOUS_CURRENT_LIMIT = 20;  // Likely to hit the end, so let's use a low current to begin with.
	public static final int HATCH_PEAK_CURRENT_LIMIT = 43;
	public static final int HATCH_CURRENT_TIMEOUT_MS = 100;
	public static final int HATCH_POSITION_MOTOR_CAN_ID = 25;
	public static final double HATCH_STOWED_POSITION = 0;  //0;  // 2 while tuning PID
	public static final double HATCH_READY_POSITION = 7;
	public static final double HATCH_INTAKE_HOLD_POSITION = 2.5;
	public static final double HATCH_POSITION_TOLERANCE = 0.5;
	public static final double HATCH_CALIBRATION_SPEED = -0.4; // Percentage of motor power needed for calibration.
	
	/*
	* Loader
	*/
	public static final int LOADER_MOTOR_TALON_CAN_ID = 20; //TODO: find canID for Loader motor
	public static final double LOADER_MOTOR_CURRENT = 1.0;
	public static final int LOADER_SOLENOID_PORT = 4;
	public static final int PADDLE_SOLENOID_PORT = 5;
	
	/*
	 * Canifier 
	 */
	public static final int LED_CANIFIER_CAN_ID = 21;

	/*
	 * Lift
	 */
	public static final int[] LIFT_MOTOR_TALON_CAN_ID_LIST = {30, 31};
	public static final int LIFT_SOLENOID_ID = 2;  // Listed as hatch mech in/out in the DDD.
	public static final double LIFT_SOLENOID_RETRACT_TIME = 0.1;
	public static  final double LEFT_SOLENOID_EXTEND_TIME = 0.1;

	public static final double LIFT_P_UP = 40.0;
	public static final double LIFT_I_UP = 0.0;
	public static final double LIFT_D_UP = 0.0;
	public static final double LIFT_F_UP = 0.0;

	public static final double LIFT_P_DOWN = 25.0;
	public static final double LIFT_I_DOWN = 0.0;
	public static final double LIFT_D_DOWN = 0.0;
	public static final double LIFT_F_DOWN = 0.0;

	public static final int LIFT_MOTION_MAX = 40;  // was 100
	public static final int LIFT_MOTION_ACCEL = 5 * LIFT_MOTION_MAX;

	// public static final double LIFT_SCALE = 6.313;	// 638 ticks to 59.44 inches
	public static final double LIFT_SCALE = ((638/59.44)*1.07);	// 638 ticks to 59.44 inches

	public static final double LIFT_DEFAULT_TOLERANCE = 1.0;
	public static final double LIFT_MICRO_ADJUST_HEIGHT = 0.5; // The smallest height by which the operator can raise the lift
	public static final int LIFT_CONTINUOUS_CURRENT_LIMIT = 38;
	public static final int LIFT_PEAK_CURRENT_LIMIT = 43;
	public static final int LIFT_CURRENT_TIMEOUT_MS = 100;
	public static final int LIFT_FWD_SOFT_LIMIT = 775;
	public static final int LIFT_REV_SOFT_LIMIT = -10;

	public enum LiftSetpoint {
		LIFT_BOTTOM_HEIGHT(0),
		LIFT_FEEDER_STATION_HEIGHT(0),
		LIFT_CARGO_SHIP_HATCH_HEIGHT(0),
		LIFT_CARGO_SHIP_CARGO_HEIGHT(16.0),
		LIFT_ROCKET_BOTTOM_HATCH_HEIGHT(0),
		LIFT_ROCKET_BOTTOM_CARGO_HEIGHT(7),
		LIFT_ROCKET_MIDDLE_HATCH_HEIGHT(17),
		LIFT_ROCKET_MIDDLE_CARGO_HEIGHT(22.5),
		LIFT_ROCKET_TOP_HATCH_HEIGHT(32.5),
		LIFT_ROCKET_TOP_CARGO_HEIGHT(38), // Was 38
		LIFT_MAX_HEIGHT(39); // 39.3 inches

		public final double height;
		private LiftSetpoint(double height) {
			this.height = height;
		}
	}
																	
	public static final List<LiftSetpoint> LIFT_SETPOINTS = Arrays.asList(
		LiftSetpoint.LIFT_BOTTOM_HEIGHT, 
		LiftSetpoint.LIFT_CARGO_SHIP_HATCH_HEIGHT, //Cargo ship, Lower Rocket and feeder station hatch heights are identical
		LiftSetpoint.LIFT_CARGO_SHIP_CARGO_HEIGHT, 
		LiftSetpoint.LIFT_ROCKET_BOTTOM_HATCH_HEIGHT,
		LiftSetpoint.LIFT_ROCKET_BOTTOM_CARGO_HEIGHT,
		LiftSetpoint.LIFT_ROCKET_MIDDLE_HATCH_HEIGHT,
		LiftSetpoint.LIFT_ROCKET_MIDDLE_CARGO_HEIGHT,
		LiftSetpoint.LIFT_ROCKET_TOP_HATCH_HEIGHT, 
		LiftSetpoint.LIFT_ROCKET_TOP_CARGO_HEIGHT,
		LiftSetpoint.LIFT_MAX_HEIGHT
	);


	// Position Constants
	public static final double LIFT_DEFAULT_MIN_HEIGHT = 0;
	public static final double LIFT_DEFAULT_MAX_HEIGHT = 39; // 42
	public static final double LIFT_DEPLOY_THRESHOLD_HEIGHT = 0; //18 / 25.4; // cannot deploy the spitter unless it is 18mm above the lift base height 

	// Power distribution Panel (PDP)
	public static final int PDP_CAN_ID = 62;
	
	// Pneumatic Control Modules (PCM)
	public static final int PCM_CAN_ID = 61;

	// Hatch Solenoids
	public static final int HATCH_HOLDER_PORT = 1;

	// Level two (step climb)
	public static final int LEVEL_TWO_SOLENOID_ID = 3;
	public static final double LEVEL_TWO_SOLENOID_RETRACT_TIME = 0.3;  // Faster to retract with the robot sitting on it.
	public static final double LEVEL_TWO_SOLENOID_EXTEND_TIME = 0.5;
	public static final double LEVEL_TWO_HEIGHT = 12; //Temporary

	// Level three (higher step climb)
	public static final int LEVEL_THREE_REAR_CAN_ID = 47;
	public static final int LEVEL_THREE_DRIVE_MOTOR_CAN_ID = 48;
	public static final double LEVEL_THREE_HEIGHT = 24; //Temporary
	public static final double L3_CLIMB_HEIGHT = 10;
	public static final double L3_DRIVE_POWER = 0.2;
	public static final double DRIVEBASE_L3_DRIVE_POWER = 0.2;
	public static final double DRIVEBASE_L3_DRIVE_SLOW_POWER = 0.25;
	public static final double L3_POSITION_F = 0;  // TODO: Needs tuning.
	public static final double L3_POSITION_P = 0.01;
	public static final double L3_POSITION_I = 0;
	public static final double L3_POSITION_D = 0;
	public static final int L3_CONTINUOUS_CURRENT_LIMIT = 20;  // Likely to hit the end, so let's use a low current to begin with.
	public static final int L3_PEAK_CURRENT_LIMIT = 43;
	public static final int L3_CURRENT_TIMEOUT_MS = 100;


	/*
	 * Camera server constants
	 * These are magic numbers to tell the Jevois which vision processor to load.
	 */
	public static final int CAMERA_RESOLUTION_WIDTH = 640;
	public static final int CAMERA_RESOULTION_HEIGHT = 252;
	public static final int CAMERA_FRAMES_PER_SECOND = 60;
	// Vision (all need tuning)
	public static final double VISON_MAX_TARGET_AGE_SECS = 2;
	public static final double VISION_MAX_VELOCITY_JERK = 10;
	public static final double VISION_SPEED_SCALE = 2.5;
	public static final double VISION_ANGLE_SCALE = 0.6;
	public static final double VISION_SPLINE_MIN_DISTANCE = 60; // inches
	public static final double VISION_WAYPOINT_DISTANCE_SCALE = 0.5; // percentage 0 to 1

	// Vision filter parameters
	public static final double VISION_H_MIN = 70;
	public static final double VISION_H_MAX = 90;
	public static final double VISION_S_MIN = 10;
	public static final double VISION_S_MAX = 255;
	public static final double VISION_V_MIN = 100;
	public static final double VISION_V_MAX = 255;

	// Tape (all need tuning)
	public static final double TAPE_MAX_VELOCITY_JERK = 10;
	public static final double TAPE_JOYSTICK_SCALE = 1;
	public static final double TAPE_ANGLE_SCALE = 0.2;

	// Turn to angle (all need tuning)
	public static final double TURN_TO_ANGLE_MAX_VELOCITY_JERK = 50;
	public static final double TURN_TO_ANGLE_ANGLE_SCALE = 0.3;

	// Climber (both level 2 and level 3). Replaces L3.
	public static final double CLIMBER_TOLERANCE = 0.5;
	public static final double MAX_WINCH_PAIR_OFFSET = 20./25.4;
	public static final int CLIMBER_FRONT_CAN_ID = 45;
	public static final int CLIMBER_REAR_CAN_ID = 47;
	public static final int CLIMBER_DRIVE_MOTOR_CAN_ID = 48;
	public static final double CLIMBER_HEIGHT = 2; //Temporary, unused
	public static final double CLIMBER_L2_CLIMB_HEIGHT = 7.5;
	public static final double CLIMBER_L3_CLIMB_HEIGHT = 21;
	public static final double DRIVEBASE_CLIMBER_DRIVE_SPEED = -2.5; // inches/sec
	//public static final double DRIVEBASE_CLIMBER_DRIVE_SLOW_POWER = -0.15;
	public static final double CLIMBER_DRIVE_POWER = 1;
	public static final double CLIMBER_POSITION_F = 0;
	public static final double CLIMBER_POSITION_P = 4;
	public static final double CLIMBER_POSITION_I = 0;
	public static final double CLIMBER_POSITION_D = 0;
	public static final int CLIMBER_CONTINUOUS_CURRENT_LIMIT = 20;  // Likely to hit the end, so let's use a low current to begin with.
	public static final int CLIMBER_PEAK_CURRENT_LIMIT = 43;
	public static final int CLIMBER_CURRENT_TIMEOUT_MS = 100;
	public static final double CLIMBER_WINCH_FRONT_SCALE_FACTOR = 9609/7.9529;     // 7.7953;	// 18" ticks = 20208 ticks
	public static final double CLIMBER_WINCH_REAR_SCALE_FACTOR = 9634/7.9528;

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
}