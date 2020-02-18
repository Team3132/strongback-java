package frc.robot.subsystems;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import com.revrobotics.ColorSensorV3;

import org.strongback.Executor.Priority;
import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.Gyroscope;
import org.strongback.components.Motor;
import org.strongback.components.PneumaticsModule;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.ui.InputDevice;
import org.strongback.hardware.Hardware;
import org.strongback.hardware.HardwareSparkMAX;
import org.strongback.mock.Mock;

import edu.wpi.first.wpilibj.I2C;
import frc.robot.Constants;
import frc.robot.controller.Controller.TrajectoryGenerator;
import frc.robot.drive.routines.*;
import frc.robot.interfaces.*;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineType;
import frc.robot.interfaces.VisionInterface.TargetDetails;
import frc.robot.lib.*;
import frc.robot.mock.*;
import frc.robot.simulator.*;

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

	public LocationInterface location;
	public DrivebaseInterface drivebase;
	public IntakeInterface intake;
	public OverridableSubsystem<IntakeInterface> intakeOverride;
	public PassthroughInterface passthrough;
	public OverridableSubsystem<PassthroughInterface> passthroughOverride;
	public ClimberInterface climber;
	public ColourWheelInterface colourWheel;
	public OverridableSubsystem<ClimberInterface> climberOverride;
	public PneumaticsModule compressor;
	public VisionInterface vision;
	public JevoisInterface jevois;
	// Drivebase encoder values.
	public DoubleSupplier leftDriveDistance;
	public DoubleSupplier rightDriveDistance;
	public DoubleSupplier leftDriveSpeed;
	public DoubleSupplier rightDriveSpeed;

	private final I2C.Port i2cPort = I2C.Port.kOnboard;
	/**
	 * A Rev Color Sensor V3 object is constructed with an I2C port as a 
	 * parameter. The device will be automatically initialized with default 
	 * parameters.
	 */
	private final ColorSensorV3 colourSensor = new ColorSensorV3(i2cPort);

	public Subsystems(DashboardInterface dashboard, RobotConfiguration config, Clock clock, Log log) {
		this.dashboard = dashboard;
		this.config = config;
		this.clock = clock;
		this.log = log;
	}

	public void createOverrides() {
		createIntakeOverride();
		createPassthrougOverride();
		createClimberOverride();
	}

	public void enable() {
		log.info("Enabling subsystems");
		// location is always enabled.
		drivebase.enable();
		intake.enable();
		passthrough.enable();
		climber.enable();
	}

	public void disable() {
		log.info("Disabling Subsystems");
		drivebase.disable();
		intake.disable();
		passthrough.disable();
		climber.disable();
	}

	@Override
	public void updateDashboard() {
		drivebase.updateDashboard();
		intake.updateDashboard();
		climber.updateDashboard();
		location.updateDashboard();
		passthrough.updateDashboard();
		vision.updateDashboard();
		colourWheel.updateDashboard();
	}

	/**
	 * Create the drivebase and location subsystems.
	 * Creates the motors and gyro as needed by both.
	 * Registers all of the available drive routines that can be requested by the controller.
	 */
	public void createDrivebaseLocation(TrajectoryGenerator generator, InputDevice leftStick, InputDevice rightStick) {
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
				config.drivebaseCanIdsLeftWithoutEncoders, !config.drivebaseSwapLeftRight, config.drivebaseSensorPhase, config.drivebaseRampRate,
				config.drivebaseCurrentLimiting, config.drivebaseContCurrent, config.drivebasePeakCurrent, 
				config.drivebaseP, config.drivebaseI, config.drivebaseD, config.drivebaseF, clock, log);
		Motor rightMotor = MotorFactory.getDriveMotor(config.drivebaseMotorControllerType, config.drivebaseCanIdsRightWithEncoders,
				config.drivebaseCanIdsRightWithoutEncoders, config.drivebaseSwapLeftRight, config.drivebaseSensorPhase, config.drivebaseRampRate,
				config.drivebaseCurrentLimiting, config.drivebaseContCurrent, config.drivebasePeakCurrent, config.drivebaseP, config.drivebaseI, config.drivebaseD, config.drivebaseF, clock, log);
		leftDriveDistance = () -> leftMotor.getPosition();
		rightDriveDistance = () -> rightMotor.getPosition();
		leftDriveSpeed = () -> leftMotor.getVelocity();
		rightDriveSpeed = () -> rightMotor.getVelocity();

		Gyroscope gyro = new NavXGyroscope("NavX", config.navxIsPresent, log);
		gyro.zero();
		location = new Location(leftDriveDistance, rightDriveDistance, gyro, clock, dashboard, log); // Encoders must return inches.
		drivebase = new Drivebase(leftMotor, rightMotor, dashboard, log);
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
		drivebase.registerDriveRoutine(DriveRoutineType.WAYPOINTS,
				new SplineDrive(generator, leftDriveDistance, rightDriveDistance, location, clock, log));
		// Driving using the vision targets to help with alignment. Overrides the
		// steering but not the speed.
		drivebase.registerDriveRoutine(DriveRoutineType.VISION_ASSIST,
				new PositionalPIDDrive("vision",
				() -> getVisionDriveSpeed(10 /*maxSpeed*/, 40 /*(stopAtDistance*/),
				() -> getVisionTurnAdjustment(),
				Constants.VISION_SPEED_SCALE, Constants.VISION_ANGLE_SCALE,
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
		log.register(true, () -> getVisionTurnAdjustment(), "Drive/vision/turnAdj")
		   .register(true, () -> getVisionDriveSpeed(10 /*maxSpeed*/, 40 /*(stopAtDistance*/), "Drive/vision/distance")
		   .register(true, () -> getTurnToAngleTurnAdjustment(), "Drive/angle/turnAdj")
		   .register(true, () -> getVisionWaypoint().x, "Drive/vision/waypointX")
		   .register(true, () -> getVisionWaypoint().y, "Drive/vision/waypointY");

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
	public Position getVisionWaypoint() {
		if (!vision.isConnected()) return new Position(0,0);
		TargetDetails details = vision.getTargetDetails();
		Position current = location.getCurrentLocation();
		if (current.distanceTo(details.location) > Constants.VISION_SPLINE_MIN_DISTANCE) {
			return details.location.addVector(-current.distanceTo(details.location) * Constants.VISION_WAYPOINT_DISTANCE_SCALE, 0);
		} else {
			return details.location;
		}
	}

	public double getVisionTurnAdjustment() {
		if (!vision.isConnected()) return 0;
		TargetDetails details = vision.getTargetDetails();
		double now = clock.currentTime();
		if (!details.targetFound) return 0;
		if (now - details.seenAtSec > Constants.VISON_MAX_TARGET_AGE_SECS) return 0;
		// We have a recent target position relative to the robot starting position.
		Position current = location.getCurrentLocation();
		Position waypoint = getVisionWaypoint();
		//log.sub("curr pos=%s  waypoint = %s  target = %s", current, waypoint, details.location);
		//log.sub("bearingToWaypoint = %.1f  bearingToVision = %.1f", current.bearingTo(waypoint), current.bearingTo(details.location));
		return -current.bearingTo(waypoint);
	}

	public double getVisionDriveSpeed(double maxSpeed, double stopAtDistance) {
		if (vision == null) return 0;
		if (!vision.isConnected())
			return 0;
		TargetDetails details = vision.getTargetDetails();
		double now = clock.currentTime();
		if (!details.targetFound)
			return 0;
		if (now - details.seenAtSec > Constants.VISON_MAX_TARGET_AGE_SECS)
			return 0;
		// We have a recent target position relative to the robot starting position.
		Position current = location.getCurrentLocation();
		double distance = Math.max(0, current.distanceTo(details.location) - stopAtDistance);

		// Cap it so that the robot quickly gets to max speed.
		return Math.min(distance, maxSpeed);
	}

	public double getTapeTurnAdjustment() {
		// TODO: Implement
		return 0;
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
		Motor intakeMotor = MotorFactory.getIntakeMotor(config.intakeCanID, false, log);
		BooleanSupplier intakeSensor = () -> intakeMotor.isAtReverseLimit();
		intake = new Intake(intakeMotor, intakeSensor, intakeSolenoid, dashboard, log);
	}

	public void createIntakeOverride() {
		// Setup the diagBox so that it can take control.
		IntakeSimulator simulator = new IntakeSimulator();
		MockIntake mock = new MockIntake(log);
		intakeOverride = new OverridableSubsystem<IntakeInterface>("intake", IntakeInterface.class, intake, simulator, mock, log);
		// Plumb accessing the intake through the override.
		intake = intakeOverride.getNormalInterface();
	}

	public void createColourWheel() {
		if (!config.colourWheelIsPresent) {
			colourWheel = new MockColourWheel(log);
			log.sub("Colour Sensor not present, using a mock colour sensor instead");
			return;
		}
		Motor motor = MotorFactory.getColourWheelMotor(config.colourWheelCanID, true, log);
		colourWheel = new ColourWheel(motor, colourSensor, dashboard, log);
		Strongback.executor().register(colourWheel, Priority.HIGH);
	}


	public void createPassthrough() {
		if (!config.passthroughIsPresent) {
			passthrough = new MockPassthrough(log);
			log.sub("Created a mock passthrough!");
			return;
		}

		Motor passthroughMotor = MotorFactory.getPassthroughMotor(config.passthroughCanID, false, log);
		passthrough = new Passthrough(config.teamNumber, passthroughMotor, dashboard, log);
	}

	public void createPassthrougOverride() {
		// Setup the diagBox so that it can take control.
		MockPassthrough simulator = new MockPassthrough(log);  // Nothing to simulate, use the mock
		MockPassthrough mock = new MockPassthrough(log);
		passthroughOverride = new OverridableSubsystem<PassthroughInterface>("passthrough", PassthroughInterface.class, passthrough, simulator, mock, log);
		// Plumb accessing the lift through the override.
		passthrough = passthroughOverride.getNormalInterface();
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


	public void createClimber() {
		if (!config.climberIsPresent) {
			climber = new MockClimber(log);
			return;
		}
		Motor frontWinchMotor = MotorFactory.getClimberWinchMotor(config.climberFrontCanID, false, false, log);
		frontWinchMotor.setInverted(true);
		frontWinchMotor.setScale(Constants.CLIMBER_WINCH_FRONT_SCALE_FACTOR); // 18" ticks = 20208 ticks
		Motor rearWinchMotor = MotorFactory.getClimberWinchMotor(config.climberRearCanID, false, false, log);
		rearWinchMotor.setScale(Constants.CLIMBER_WINCH_REAR_SCALE_FACTOR); // 18" ticks = 20208 ticks
		Motor driveMotor = MotorFactory.getClimberDriveMotor(config.climberDriveMotorCanID, true, log);
		climber = new Climber(frontWinchMotor, rearWinchMotor, driveMotor, dashboard, log);
		Strongback.executor().register(climber, Priority.HIGH);
	}

	public void createClimberOverride() {
		// Setup the diagBox so that it can take control.
		MockClimber simulator = new MockClimber(log);
		MockClimber mock = new MockClimber(log);
		climberOverride = new OverridableSubsystem<ClimberInterface>("climber", ClimberInterface.class, climber, simulator, mock, log);
		// Plumb accessing the climber through the override.
		climber = climberOverride.getNormalInterface();
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
