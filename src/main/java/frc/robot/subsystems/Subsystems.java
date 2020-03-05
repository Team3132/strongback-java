package frc.robot.subsystems;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import org.strongback.Executor.Priority;
import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.Gyroscope;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.PneumaticsModule;
import org.strongback.components.Solenoid;
import org.strongback.components.ui.InputDevice;
import org.strongback.hardware.Hardware;
import org.strongback.mock.Mock;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.I2C;

import frc.robot.Constants;
import frc.robot.drive.routines.ArcadeDrive;
import frc.robot.drive.routines.CheesyDpadDrive;
import frc.robot.drive.routines.ConstantDrive;
import frc.robot.drive.routines.PositionalPIDDrive;
import frc.robot.drive.routines.TrajectoryDrive;
import frc.robot.interfaces.BuddyClimbInterface;
import frc.robot.interfaces.ColourWheelInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.DrivebaseInterface;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineType;
import frc.robot.interfaces.IntakeInterface;
import frc.robot.interfaces.JevoisInterface;
import frc.robot.interfaces.LEDStripInterface;
import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.LocationInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ShooterInterface;
import frc.robot.interfaces.VisionInterface;
import frc.robot.interfaces.VisionInterface.TargetDetails;
import frc.robot.lib.GamepadButtonsX;
import frc.robot.lib.Jevois;
import frc.robot.lib.MathUtil;
import frc.robot.lib.MotorFactory;
import frc.robot.lib.NavXGyroscope;
import frc.robot.lib.NetworkTablesHelper;
import frc.robot.lib.Position;
import frc.robot.lib.RobotConfiguration;
import frc.robot.lib.WheelColour;
import frc.robot.mock.MockBuddyClimb;
import frc.robot.mock.MockColourWheel;
import frc.robot.mock.MockDrivebase;
import frc.robot.mock.MockIntake;
import frc.robot.mock.MockLEDStrip;
import frc.robot.mock.MockLoader;
import frc.robot.mock.MockLocation;
import frc.robot.mock.MockShooter;
import frc.robot.mock.MockVision;
import frc.robot.simulator.IntakeSimulator;

/**
 * Contains the subsystems for the robot.
 * 
 * Makes it easy to pass all subsystems around.
 */
public class Subsystems implements DashboardUpdater {
	// Not really a subsystem, but used by all subsystems.
	public DashboardInterface dashboard;
	public RobotConfiguration config;
	public Clock clock;
	public Log log;
	public LEDStripInterface ledStrip;
	public LocationInterface location;
	public DrivebaseInterface drivebase;
	public IntakeInterface intake;
	public BuddyClimbInterface buddyClimb;
	public OverridableSubsystem<IntakeInterface> intakeOverride;
	public LoaderInterface loader;
	public OverridableSubsystem<LoaderInterface> loaderOverride;
	public ShooterInterface shooter;
	public OverridableSubsystem<ShooterInterface> shooterOverride;
	public ColourWheelInterface colourWheel;
	public PneumaticsModule compressor;
	public VisionInterface vision;
	public JevoisInterface jevois;
	// Drivebase encoder values.
	public DoubleSupplier leftDriveDistance;
	public DoubleSupplier rightDriveDistance;
	public DoubleSupplier leftDriveSpeed;
	public DoubleSupplier rightDriveSpeed;

	private final I2C.Port i2cPort = I2C.Port.kOnboard;

	public Subsystems(DashboardInterface dashboard, RobotConfiguration config, Clock clock, Log log) {
		this.dashboard = dashboard;
		this.config = config;
		this.clock = clock;
		this.log = log;
	}

	public void createOverrides() {
		createIntakeOverride();
		createLoaderOverride();
		createShooterOverride();
	}

	public void enable() {
		log.info("Enabling subsystems");
		// location is always enabled.
		drivebase.enable();
		intake.enable();
		shooter.enable();
		loader.enable();
		colourWheel.enable();
	}

	public void disable() {
		log.info("Disabling Subsystems");
		drivebase.disable();
		intake.disable();
		shooter.disable();
		loader.disable();
		colourWheel.disable();
	}

	@Override
	public void updateDashboard() {
		drivebase.updateDashboard();
		intake.updateDashboard();
		location.updateDashboard();
		loader.updateDashboard();
		shooter.updateDashboard();
		vision.updateDashboard();
		colourWheel.updateDashboard();
	}

	/**
	 * Create the drivebase and location subsystems.
	 * Creates the motors and gyro as needed by both.
	 * Registers all of the available drive routines that can be requested by the controller.
	 */
	public void createDrivebaseLocation(InputDevice leftStick, InputDevice rightStick) {
		if (!config.drivebaseIsPresent) {
			log.sub("Using mock drivebase");
			drivebase = new MockDrivebase(log);
			location = new MockLocation();
			log.sub("Created a mock drivebase and location");
			return;
		}
		// Redundant drive motors - automatic failover if the talon or the encoders
		// fail.
		Motor leftMotor = MotorFactory.getDriveMotor(config.drivebaseMotorControllerType, config.drivebaseCanIdsLeftWithEncoders,
				!config.drivebaseSwapLeftRight, config.drivebaseSensorPhase,config.drivebaseRampRate, config.drivebaseCurrentLimiting,
				config.drivebaseContCurrent, config.drivebasePeakCurrent,config.drivebaseP, config.drivebaseI, config.drivebaseD,
				config.drivebaseF, clock, log);
		Motor rightMotor = MotorFactory.getDriveMotor(config.drivebaseMotorControllerType, config.drivebaseCanIdsRightWithEncoders,
				config.drivebaseSwapLeftRight, config.drivebaseSensorPhase, config.drivebaseRampRate, config.drivebaseCurrentLimiting, 
				config.drivebaseContCurrent, config.drivebasePeakCurrent, config.drivebaseP, config.drivebaseI,
				 config.drivebaseD, config.drivebaseF, clock, log);
		Solenoid ptoSolenoid = Hardware.Solenoids.singleSolenoid(config.pcmCanId, Constants.CLIMBER_PTO_SOLENOID_PORT, 0.1, 0.1);
		Solenoid brakeSolenoid = Hardware.Solenoids.singleSolenoid(config.pcmCanId, Constants.CLIMBER_BRAKE_SOLENOID_PORT, 0.1, 0.1);

		leftDriveDistance = () -> leftMotor.getPosition();
		rightDriveDistance = () -> rightMotor.getPosition();
		leftDriveSpeed = () -> leftMotor.getVelocity();
		rightDriveSpeed = () -> rightMotor.getVelocity();

		leftMotor.setPosition(0);
		rightMotor.setPosition(0);

		// Save PID values into Network Tables
		NetworkTablesHelper driveHelper = new NetworkTablesHelper("drive");
		driveHelper.set("p", config.drivebaseP);
		driveHelper.set("i", config.drivebaseI);
		driveHelper.set("d", config.drivebaseD);
		driveHelper.set("f", config.drivebaseF);


		Gyroscope gyro = new NavXGyroscope("NavX", config.navxIsPresent, log);
		gyro.zero();
		location = new Location(() -> {	leftMotor.setPosition(0);
			rightMotor.setPosition(0); },
			leftDriveDistance, rightDriveDistance, gyro, clock, dashboard, log); // Encoders must return inches.
		drivebase = new Drivebase(leftMotor, rightMotor, ptoSolenoid, brakeSolenoid, driveHelper ,dashboard, log);
		Strongback.executor().register(drivebase, Priority.HIGH);
		Strongback.executor().register(location, Priority.HIGH);

		// Add the supported drive routines
		drivebase.registerDriveRoutine(DriveRoutineType.CONSTANT_POWER, new ConstantDrive(), ControlMode.PercentOutput);
		drivebase.registerDriveRoutine(DriveRoutineType.CONSTANT_SPEED, new ConstantDrive(), ControlMode.Velocity);
		// The old favourite arcade drive with throttling if a button is pressed.
		drivebase.registerDriveRoutine(DriveRoutineType.ARCADE, new ArcadeDrive("Arcade", 1.0, () -> { // Throttle.
			double scale = 1;
			if (leftStick.getButton(1).isTriggered()) { // Trigger
				scale = 0.6; // Root of 0.5 to half motion in accordance with SquaredInputs
			}
			return -leftStick.getAxis(1).read() * scale;
		}, () -> {
			double scale = 1;
			if (leftStick.getButton(1).isTriggered()) { // Trigger
				scale = 0.6; // Root of 0.5 to half motion in accordance with SquaredInputs
			}
			return -rightStick.getAxis(0).read() * scale; // Turn power.
		},
		true, log));

		// The old favourite arcade drive with throttling if a button is pressed but using velocity mode.
		drivebase.registerDriveRoutine(DriveRoutineType.ARCADE_VELOCITY, new ArcadeDrive("ArcadeVelocity", config.drivebaseMaxSpeed, () -> { // Throttle.
			double scale = 1;
			if (leftStick.getButton(1).isTriggered()) { // Trigger
				scale = 0.6; // Root of 0.5 to half motion in accordance with SquaredInputs
			}
			return -leftStick.getAxis(1).read() * scale;
		}, () -> {
			double scale = 1;
			if (leftStick.getButton(1).isTriggered()) { // Trigger
				scale = 0.6; // Root of 0.5 to half motion in accordance with SquaredInputs
			}
			return -rightStick.getAxis(0).read() * scale; // Turn power.
		},
		true, log), ControlMode.Velocity);
		// Cheesy drive.
		drivebase.registerDriveRoutine(DriveRoutineType.CHEESY, new CheesyDpadDrive(leftStick.getDPad(0), // DPad
				leftStick.getAxis(GamepadButtonsX.LEFT_Y_AXIS), // Throttle
				leftStick.getAxis(GamepadButtonsX.RIGHT_X_AXIS), // Wheel (turn?)
				leftStick.getButton(GamepadButtonsX.RIGHT_TRIGGER_AXIS), // Is quick turn
				log));
		// Drive through supplied waypoints using splines.
		drivebase.registerDriveRoutine(DriveRoutineType.TRAJECTORY,
				new TrajectoryDrive(location, clock, log), ControlMode.Voltage);
		// Driving using the vision targets to help with alignment. Overrides the
		// steering but not the speed.
		drivebase.registerDriveRoutine(DriveRoutineType.VISION_ASSIST,
				new PositionalPIDDrive("vision",
				() -> getVisionDriveSpeed(10 /*maxSpeed*/, 40 /*(stopAtDistance*/),
				() -> getVisionTurnWaypointAdjustment(),
				Constants.VISION_SPEED_SCALE, Constants.VISION_ASSIST_ANGLE_SCALE,
				Constants.VISION_MAX_VELOCITY_JERK, leftDriveDistance, leftDriveSpeed, rightDriveDistance,
				rightDriveSpeed, clock, log));
		// Vision aiming for shooter
		drivebase.registerDriveRoutine(DriveRoutineType.VISION_AIM,
				new PositionalPIDDrive("visionAim",
				() -> (Math.abs(getVisionTurnAdjustment())<Constants.VISION_AIM_ANGLE_RANGE) && (Math.abs(getVisionDistance()) < Constants.VISION_AIM_DISTANCE_RANGE), 
				() -> MathUtil.clamp(getVisionDistance()*Constants.VISION_AIM_DISTANCE_SCALE, -Constants.VISION_MAX_DRIVE_SPEED, Constants.VISION_MAX_DRIVE_SPEED),
				() -> getVisionTurnAdjustment(),
				Constants.VISION_SPEED_SCALE, Constants.VISION_AIM_ANGLE_SCALE,
				Constants.VISION_MAX_VELOCITY_JERK, leftDriveDistance, leftDriveSpeed, rightDriveDistance,
				rightDriveSpeed, clock, log));
		// Turns on the spot to a specified angle.
		drivebase.registerDriveRoutine(DriveRoutineType.TURN_TO_ANGLE,
				new PositionalPIDDrive("angle", () -> 0, () -> getTurnToAngleTurnAdjustment(), 0,
						Constants.TURN_TO_ANGLE_ANGLE_SCALE, Constants.TURN_TO_ANGLE_MAX_VELOCITY_JERK,
						leftDriveDistance, leftDriveSpeed, rightDriveDistance, rightDriveSpeed, clock, log));
		// Map joysticks in arcade mode for testing/tuning
		drivebase.registerDriveRoutine(DriveRoutineType.POSITION_PID_ARCADE,
				new PositionalPIDDrive("posArcade", () -> -leftStick.getAxis(1).read(),
						() -> rightStick.getAxis(0).read(), 50 /* joystick scale */, 50 /* turn scale */, 50 /* jerk */,
						leftDriveDistance, leftDriveSpeed, rightDriveDistance, rightDriveSpeed, clock, log));

		// Log some useful values for debugging.
		log.register(true, () -> getVisionTurnWaypointAdjustment(), "Drive/vision/turnAdj")
		   .register(true, () -> getVisionDriveSpeed(10 /*maxSpeed*/, 40 /*(stopAtDistance*/), "Drive/vision/distance")
		   .register(true, () -> getTurnToAngleTurnAdjustment(), "Drive/angle/turnAdj")
		   .register(true, () -> getVisionWaypoint().x, "Drive/vision/waypointX")
		   .register(true, () -> getVisionWaypoint().y, "Drive/vision/waypointY")
		   .register(true, () -> getVisionTurnAdjustment(), "Drive/vision/visionAim")
		   .register(true, () -> getVisionDistance(), "Drive/vision/visionAimDistance");	

	}

	/**
	 * Calculate a waypoint to drive to on the way to the vision target.
	 * The waypoint is located directly infront of the vision target at a distance
	 * of half the distance between the robot and the target
	 * 
	 * If the robot is within Constants.VISION_SPLINE_MIN_DISTANCE of the target we return the
	 * location of the target
	 * 
	 * This effectively makes the robot drive on a spline.
	 * @return a Position to drive to which leads the robot to the vision target on a spline
	 */

	public double getVisionTurnAdjustment() {
		if (vision == null || !vision.isConnected()) return 0;
		//log.sub("Vision is connected");
		TargetDetails details = vision.getTargetDetails();
		if (!details.isValid(clock.currentTime())) return 0;
		//log.sub("Target is valid");
		// We have a recent target position relative to the robot starting position.
		Position current = location.getCurrentLocation();
		//log.sub("curr pos=%s target = %s", current, details.location);
		//log.sub("VISION: bearingToVision = %.1f", current.bearingTo(details.location));
		
		// Scale turnadjustment depending on distance from goal
		double turnAdjustment = Math.max(0, Constants.VISION_MAX_DRIVE_SPEED - Math.abs(getVisionDistance())*2.5);
		turnAdjustment = MathUtil.scale(turnAdjustment, 0, Constants.VISION_MAX_DRIVE_SPEED, 0.1, 1);
		return turnAdjustment * -current.bearingTo(details.location);
	}

	public double getVisionDistance(){
		if (vision == null || !vision.isConnected()) return 0;
		TargetDetails details = vision.getTargetDetails();
		if (!details.isValid(clock.currentTime())) return 0;
		
		// We have a recent target position relative to the robot starting position.
		Position current = location.getCurrentLocation();
		double distance = current.distanceTo(details.location) - Constants.VISION_STOP_DISTANCE;
		return distance; 
	}
	
	public Position getVisionWaypoint() {
		if (vision == null || !vision.isConnected()) return new Position(0,0);
		TargetDetails details = vision.getTargetDetails();
		Position current = location.getCurrentLocation();
		if (current.distanceTo(details.location) > Constants.VISION_SPLINE_MIN_DISTANCE) {
			return details.location.addVector(-current.distanceTo(details.location) * Constants.VISION_WAYPOINT_DISTANCE_SCALE, 0);
		} else {
			return details.location;
		}
	}

	public double getVisionTurnWaypointAdjustment() {
		if (vision == null || !vision.isConnected()) return 0;
		TargetDetails details = vision.getTargetDetails();
		if (!details.isValid(clock.currentTime())) return 0;
		// We have a recent target position relative to the robot starting position.
		Position current = location.getCurrentLocation();
		Position waypoint = getVisionWaypoint();
		//log.sub("curr pos=%s  waypoint = %s  target = %s", current, waypoint, details.location);
		//log.sub("bearingToWaypoint = %.1f  bearingToVision = %.1f", current.bearingTo(waypoint), current.bearingTo(details.location));
		return -current.bearingTo(waypoint);
	}

	public double getVisionDriveSpeed(double maxSpeed, double stopAtDistance) {
		if (vision == null || !vision.isConnected())
			return 0;
		TargetDetails details = vision.getTargetDetails();
		
		if (!details.isValid(clock.currentTime()))
			return 0;
		// We have a recent target position relative to the robot starting position.
		Position current = location.getCurrentLocation();
		double distance = Math.max(0, current.distanceTo(details.location) - stopAtDistance);

		// Cap it so that the robot quickly gets to max speed.
		return Math.min(distance, maxSpeed);
	}

	public double getTurnToAngleTurnAdjustment() {
		double target = drivebase.getDriveRoutine().value;
		double actual = location.getBearing();
		//log.sub("angle diff = %f\n", MathUtil.getAngleDiff(actual, target));
		return MathUtil.clamp(MathUtil.getAngleDiff(actual, target), -100, 100);
	}

	public void createIntake() {
		if (!config.intakeIsPresent) {
			intake = new MockIntake(log);
			log.sub("Intake not present, using a mock intake instead");
			return;
		}
		
		Solenoid intakeSolenoid = Hardware.Solenoids.singleSolenoid(config.pcmCanId, Constants.INTAKE_SOLENOID_PORT, 0.1, 0.1);
		// TODO: replace 0 with appropriate subsystem PIDF values
		Motor intakeMotor = MotorFactory.getIntakeMotor(config.intakeCanID, true, 0, 0, 0, 0, log);
		intake = new Intake(intakeMotor, intakeSolenoid, dashboard, log); 
	}

	public void createIntakeOverride() {
		// Setup the diagBox so that it can take control.
		IntakeSimulator simulator = new IntakeSimulator();
		MockIntake mock = new MockIntake(log);
		intakeOverride = new OverridableSubsystem<IntakeInterface>("intake", IntakeInterface.class, intake, simulator, mock, log);
		// Plumb accessing the intake through the override.
		intake = intakeOverride.getNormalInterface();
	}

	public void createBuddyClimb() {
		if (!config.buddyClimbIsPresent) {
			buddyClimb = new MockBuddyClimb(log);
			log.sub("Buddy climb not present, using a mock buddy climb instead");
			return;
		}

		Solenoid buddyClimbSolenoid = Hardware.Solenoids.singleSolenoid(config.pcmCanId, Constants.BUDDYCLIMB_SOLENOID_PORT, 0.1, 0.1);
		buddyClimb = new BuddyClimb(buddyClimbSolenoid, dashboard, log);
	}

	public void createColourWheel() {
		if (!config.colourWheelIsPresent) {
			colourWheel = new MockColourWheel(log);
			log.sub("Colour Sensor not present, using a mock colour sensor instead");
			return;
		}
		 // TODO: replace 0 with appropriate subsystem PIDF values
		Motor motor = MotorFactory.getColourWheelMotor(config.colourWheelCanID, true, 0, 0, 0, 0, log);
		Solenoid colourWheelSolenoid = Hardware.Solenoids.singleSolenoid(config.pcmCanId, Constants.COLOUR_WHEEL_SOLENOID_PORT, 0.1, 0.1); // TODO: Test and work out correct timings.

		ColorSensorV3 colourSensor = new ColorSensorV3(i2cPort);
		ColorMatch colourMatcher = new ColorMatch();
		colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_BLUE_TARGET); //Adding colours to the colourMatcher
    	colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_GREEN_TARGET);
    	colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_RED_TARGET);
    	colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_YELLOW_TARGET);
		colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_WHITE_TARGET);

		colourWheel = new ColourWheel(motor, colourWheelSolenoid, new Supplier<WheelColour>() {
			@Override
			public WheelColour get() {
				ColorMatchResult match = colourMatcher.matchClosestColor(colourSensor.getColor());
				WheelColour sensedColour = WheelColour.UNKNOWN;
				if (match.color == Constants.COLOUR_WHEEL_BLUE_TARGET) {
					sensedColour = WheelColour.BLUE;
				} else if (match.color == Constants.COLOUR_WHEEL_RED_TARGET) {
					sensedColour = WheelColour.RED;
				} else if (match.color == Constants.COLOUR_WHEEL_GREEN_TARGET) {
					sensedColour = WheelColour.GREEN;
				} else if (match.color == Constants.COLOUR_WHEEL_YELLOW_TARGET) {
					sensedColour = WheelColour.YELLOW;
				}
				return sensedColour;
			}
		}, ledStrip, clock, dashboard, log);
		Strongback.executor().register(colourWheel, Priority.HIGH);
	}

	public void createLEDStrip() {
		if (!config.ledStripIsPresent) {
			ledStrip = new MockLEDStrip();
			log.sub("LED Strip not present, using a mock LED Strip instead.");
			return;
		}
		ledStrip = new LEDStrip(Constants.LED_STRIP_PWM_PORT, Constants.LED_STRIP_NUMBER_OF_LEDS, log);
	}

	public void updateIdleLED() {
		ledStrip.setIdle();
	}

	public void createLoader() {
		if (!config.loaderIsPresent) {
			loader = new MockLoader(log);
			log.sub("Created a mock loader!");
			return;
		}

		Motor spinnerMotor = MotorFactory.getLoaderSpinnerMotor(config.loaderSpinnerCanID, false, Constants.LOADER_SPINNER_P, Constants.LOADER_SPINNER_I, Constants.LOADER_SPINNER_D, Constants.LOADER_SPINNER_F, log);
		Motor loaderPassthroughMotor = MotorFactory.getLoaderPassthroughMotor(config.loaderPassthroughCanID, true, Constants.LOADER_SPINNER_P, Constants.LOADER_SPINNER_I, Constants.LOADER_SPINNER_D, Constants.LOADER_SPINNER_F, log); 
		Solenoid paddleSolenoid = Hardware.Solenoids.singleSolenoid(config.pcmCanId, Constants.PADDLE_SOLENOID_PORT, 0.1, 0.1);
		// The ball sensors are connected to the DIO ports on the rio.
		DigitalInput inBallSensor = new DigitalInput(Constants.IN_BALL_DETECTOR_DIO_PORT);
		DigitalInput outBallSensor = new DigitalInput(Constants.OUT_BALL_DETECTOR_DIO_PORT);
		BooleanSupplier loaderInSensor = () -> !inBallSensor.get();
		BooleanSupplier loaderOutSensor = () -> !outBallSensor.get(); 
		loader = new Loader(spinnerMotor, loaderPassthroughMotor, paddleSolenoid, loaderInSensor, loaderOutSensor, ledStrip, dashboard, log);
		Strongback.executor().register(loader, Priority.LOW);

	}

	public void createLoaderOverride() {
		// Setup the diagBox so that it can take control.
		MockLoader simulator = new MockLoader(log);  // Nothing to simulate, use the mock
		MockLoader mock = new MockLoader(log);
		loaderOverride = new OverridableSubsystem<LoaderInterface>("loader", LoaderInterface.class, loader, simulator, mock, log);
		// Plumb accessing the lift through the override.
		loader = loaderOverride.getNormalInterface();
	}

	public void createShooter() {
		if (!config.shooterIsPresent) {
			shooter = new MockShooter(log);
			log.sub("Created a mock shooter!");
			return;
		}

		Solenoid hoodSolenoid = Hardware.Solenoids.singleSolenoid(config.pcmCanId, Constants.SHOOTER_HOOD_SOLENOID_PORT, 0.1, 0.1);
		Motor shooterMotor = MotorFactory.getShooterMotor(config.shooterCanIds, false, config.shooterP, config.shooterI,
				config.shooterD, config.shooterF, clock, log);

		shooter = new Shooter(shooterMotor, hoodSolenoid, dashboard, log);
	}

	public void createShooterOverride() {
		// Setup the diagBox so that it can take control.
		MockShooter simulator = new MockShooter(log);  // Nothing to simulate, use a mock instead.
		MockShooter mock = new MockShooter(log);
		shooterOverride = new OverridableSubsystem<ShooterInterface>("shooter", ShooterInterface.class, shooter, simulator, mock, log);
		// Plumb accessing the shooter through the override.
		shooter = shooterOverride.getNormalInterface();
	}

	/**
	 * Create the Pneumatics Control Module (PCM) subsystem.
	 */
	public void createPneumatics() {
		if (!config.pcmIsPresent) {
			compressor = Mock.pneumaticsModule(config.pcmCanId);
			log.sub("Created a mock compressor");
			return;
		}
		compressor = Hardware.pneumaticsModule(config.pcmCanId);
	}

	public void createVision() {
		if (!config.visionIsPresent) {
			vision = new MockVision();
			log.sub("Created a mock vision subsystem");
			return;
		}
		try {
			jevois = new Jevois(log);
			vision = new Vision(jevois, location, dashboard, clock, config.visionHMin, config.visionSMin, config.visionVMin, config.visionHMax, config.visionSMax, config.visionVMax, log);
		} catch (IOException e) {
			log.exception("Unable to create an instance of the jevois camera", e);
			e.printStackTrace();
			vision = new MockVision();
		}
	}
}
