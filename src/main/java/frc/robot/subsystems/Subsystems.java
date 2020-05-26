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
import frc.robot.Config;
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
import frc.robot.interfaces.ShooterInterface;
import frc.robot.interfaces.VisionInterface;
import frc.robot.interfaces.VisionInterface.TargetDetails;
import frc.robot.lib.GamepadButtonsX;
import frc.robot.lib.Jevois;
import frc.robot.lib.LEDColour;
import frc.robot.lib.MathUtil;
import frc.robot.lib.MotorFactory;
import frc.robot.lib.NavXGyroscope;
import frc.robot.lib.Position;
import frc.robot.lib.WheelColour;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;
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
	public Clock clock;
	public LEDStripInterface ledStrip;
	public LocationInterface location;
	public DrivebaseInterface drivebase;
	public IntakeInterface intake;
	public IntakeInterface hwIntake;
	public BuddyClimbInterface buddyClimb;
	public OverridableSubsystem<IntakeInterface> intakeOverride;
	public LoaderInterface loader;
	public LoaderInterface hwLoader; // Keep track of the real hardware for dashboard update
	public OverridableSubsystem<LoaderInterface> loaderOverride;
	public ShooterInterface shooter;
	public ShooterInterface hwShooter;
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

	public Subsystems(DashboardInterface dashboard, Clock clock) {
		this.dashboard = dashboard;
		this.clock = clock;
	}

	public void createOverrides() {
		createIntakeOverride();
		createLoaderOverride();
		createShooterOverride();
	}

	public void enable() {
		Log.info("Enabling subsystems");
		// location is always enabled.
		drivebase.enable();
		intake.enable();
		shooter.enable();
		loader.enable();
		colourWheel.enable();
	}

	public void disable() {
		Log.info("Disabling Subsystems");
		drivebase.disable();
		intake.disable();
		shooter.disable();
		loader.disable();
		colourWheel.disable();
	}

	@Override
	public void updateDashboard() {
		drivebase.updateDashboard();
		hwIntake.updateDashboard();
		location.updateDashboard();
		hwLoader.updateDashboard();
		hwShooter.updateDashboard();
		vision.updateDashboard();
		colourWheel.updateDashboard();
	}

	/**
	 * Create the drivebase and location subsystems. Creates the motors and gyro as
	 * needed by both. Registers all of the available drive routines that can be
	 * requested by the controller.
	 */
	public void createDrivebaseLocation(InputDevice leftStick, InputDevice rightStick) {
		if (! Config.drivebase.present) {
			Log.debug("Using mock drivebase");
			drivebase = new MockDrivebase();
			location = new MockLocation();
			Log.debug("Created a mock drivebase and location");
			return;
		}
		// Redundant drive motors - automatic failover if the talon or the encoders
		// fail.
		Motor leftMotor = MotorFactory.getDriveMotor(true, clock);
		Motor rightMotor = MotorFactory.getDriveMotor(false, clock);
		Solenoid ptoSolenoid = Hardware.Solenoids.singleSolenoid(Config.pcm.canId, Config.climber.ptoPort,
				0.1, 0.1); // TODO: Test and work out correct timings.
		Solenoid brakeSolenoid = Hardware.Solenoids.singleSolenoid(Config.pcm.canId,
				Config.climber.brakePort, 0.1, 0.1); // TODO: Test and work out correct timings.

		leftDriveDistance = () -> leftMotor.getPosition();
		rightDriveDistance = () -> rightMotor.getPosition();
		leftDriveSpeed = () -> leftMotor.getSpeed();
		rightDriveSpeed = () -> rightMotor.getSpeed();

		leftMotor.setPosition(0);
		rightMotor.setPosition(0);
		try {
			// Let the encoders get the message and have time to send it back to us.
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		Log.error("Reset drive encoders to zero, currently are: %f, %f", leftMotor.getPosition(),
				rightMotor.getPosition());

		Gyroscope gyro = new NavXGyroscope("NavX", Config.navx.present);
		gyro.zero();
		location = new Location(() -> {
			leftMotor.setPosition(0);
			rightMotor.setPosition(0);
		}, leftDriveDistance, rightDriveDistance, gyro, clock, dashboard); // Encoders must return metres.
		drivebase = new Drivebase(leftMotor, rightMotor, ptoSolenoid, brakeSolenoid, dashboard);
		Strongback.executor().register(drivebase, Priority.HIGH);
		Strongback.executor().register(location, Priority.HIGH);

		// Add the supported drive routines
		drivebase.registerDriveRoutine(DriveRoutineType.CONSTANT_POWER,
				new ConstantDrive("Constant Power", ControlMode.DutyCycle));
		drivebase.registerDriveRoutine(DriveRoutineType.CONSTANT_SPEED,
				new ConstantDrive("Constant Speed", ControlMode.Speed));

		// The old favourite arcade drive with throttling if a button is pressed.
		drivebase.registerDriveRoutine(DriveRoutineType.ARCADE_DUTY_CYCLE,
				new ArcadeDrive("ArcadeDutyCycle", ControlMode.DutyCycle, 1.0,
						leftStick.getAxis(1).invert().deadband(Config.ui.joystick.deadbandMinValue)
								.scale(leftStick.getButton(1).isTriggered() ? 1 : 0.6), // Throttle.
						rightStick.getAxis(0).invert().deadband(Config.ui.joystick.deadbandMinValue)
								.scale(leftStick.getButton(1).isTriggered() ? 1 : 0.6), // Turn power.
						true));

		// The old favourite arcade drive with throttling if a button is pressed but
		// using velocity mode.
		drivebase.registerDriveRoutine(DriveRoutineType.ARCADE_VELOCITY,
				new ArcadeDrive("ArcadeVelocity", ControlMode.Speed,  Config.drivebase.maxSpeed,
						leftStick.getAxis(1).invert().deadband(Config.ui.joystick.deadbandMinValue)
								.scale(leftStick.getButton(1).isTriggered() ? 1 : 0.6), // Throttle
						rightStick.getAxis(0).invert().deadband(Config.ui.joystick.deadbandMinValue)
								.scale(leftStick.getButton(1).isTriggered() ? 1 : 0.6), // Turn power.
						true));

		// Cheesy drive.
		drivebase.registerDriveRoutine(DriveRoutineType.CHEESY,
				new CheesyDpadDrive("CheesyDPad", leftStick.getDPad(0), // DPad
						leftStick.getAxis(GamepadButtonsX.LEFT_Y_AXIS), // Throttle
						leftStick.getAxis(GamepadButtonsX.RIGHT_X_AXIS), // Wheel (turn?)
						leftStick.getButton(GamepadButtonsX.RIGHT_TRIGGER_AXIS))); // Is quick turn

		// Drive through supplied waypoints using splines.
		drivebase.registerDriveRoutine(DriveRoutineType.TRAJECTORY, new TrajectoryDrive(location, clock));

		// Driving using the vision targets to help with alignment. Overrides the
		// steering but not the speed.
		drivebase.registerDriveRoutine(DriveRoutineType.VISION_ASSIST,
				new PositionalPIDDrive("visionAssist",
						() -> getVisionDriveSpeed(10 /* maxSpeed */, 40 /* (stopAtDistance */),
						() -> getVisionTurnWaypointAdjustment(), Config.vision.speedScale,
						Config.vision.assistAngleScale, Config.vision.maxVelocityJerk, leftDriveDistance,
						leftDriveSpeed, rightDriveDistance, rightDriveSpeed, clock));

		// Vision aiming for shooter
		drivebase.registerDriveRoutine(DriveRoutineType.VISION_AIM,
				new PositionalPIDDrive("visionAim",
						() -> (Math.abs(getVisionTurnAdjustment()) < Config.vision.aimAngleToleranceDegrees)
								&& (Math.abs(getVisionDistance()) < Config.vision.aimDistanceToleranceMetres),
						() -> MathUtil.clamp(getVisionDistance() * Config.vision.aimDistanceScale,
								-Config.vision.maxDriveSpeedMPerSec, Config.vision.maxDriveSpeedMPerSec),
						() -> getVisionTurnAdjustment(), Config.vision.speedScale, Config.vision.aimAngleScale,
						Config.vision.maxVelocityJerk, leftDriveDistance, leftDriveSpeed, rightDriveDistance,
						rightDriveSpeed, clock));

		// Turns on the spot to a specified angle.
		drivebase.registerDriveRoutine(DriveRoutineType.TURN_TO_ANGLE,
				new PositionalPIDDrive("angle", () -> 0, () -> getTurnToAngleTurnAdjustment(), 0,
						Config.drivebase.turnToAngle.angleScale, Config.drivebase.turnToAngle.maxVelocityJerk,
						leftDriveDistance, leftDriveSpeed, rightDriveDistance, rightDriveSpeed, clock));
						
		// Map joysticks in arcade mode for testing/tuning
		drivebase.registerDriveRoutine(DriveRoutineType.POSITION_PID_ARCADE,
				new PositionalPIDDrive("posArcade", () -> -leftStick.getAxis(1).read(),
						() -> rightStick.getAxis(0).read(), 50 /* joystick scale */, 50 /* turn scale */, 50 /* jerk */,
						leftDriveDistance, leftDriveSpeed, rightDriveDistance, rightDriveSpeed, clock));

		// Log some useful values for debugging.
		Chart.register(() -> getVisionTurnWaypointAdjustment(), "Drive/vision/turnAdj");
		Chart.register(() -> getVisionDriveSpeed(10 /* maxSpeed */, 40 /* (stopAtDistance */),
						"Drive/vision/distance");
		Chart.register(() -> getTurnToAngleTurnAdjustment(), "Drive/angle/turnAdj");
		Chart.register(() -> getVisionWaypoint().x, "Drive/vision/waypointX");
		Chart.register(() -> getVisionWaypoint().y, "Drive/vision/waypointY");
		Chart.register(() -> getVisionTurnAdjustment(), "Drive/vision/visionAim");
		Chart.register(() -> getVisionDistance(), "Drive/vision/visionAimDistance");

	}

	/**
	 * Calculate a waypoint to drive to on the way to the vision target. The
	 * waypoint is located directly infront of the vision target at a distance of
	 * half the distance between the robot and the target
	 * 
	 * If the robot is within Config.VISION_SPLINE_MIN_DISTANCE of the target we
	 * return the location of the target
	 * 
	 * This effectively makes the robot drive on a spline.
	 * 
	 * @return a Position to drive to which leads the robot to the vision target on
	 *         a spline
	 */
	public double getVisionTurnAdjustment() {
		if (vision == null || !vision.isConnected())
			return 0;
		// Logger.debug("Vision is connected");
		TargetDetails details = vision.getTargetDetails();
		if (!details.isValid(clock.currentTime()))
			return 0;
		// Logger.debug("Target is valid");
		// We have a recent target position relative to the robot starting position.
		Position current = location.getCurrentLocation();
		// Logger.debug("curr pos=%s target = %s", current, details.location);
		// Logger.debug("VISION: bearingToVision = %.1f",
		// current.bearingTo(details.location));

		// Scale turnadjustment depending on distance from goal
		double turnAdjustment = Math.max(0, Config.vision.maxDriveSpeedMPerSec - Math.abs(getVisionDistance()) * 2.5);
		turnAdjustment = MathUtil.scale(turnAdjustment, 0, Config.vision.maxDriveSpeedMPerSec, 0.1, 1);
		return turnAdjustment * -current.bearingTo(details.location);
	}

	public double getVisionDistance() {
		// returning zero for now so visionAim just aims the robot (doesn't drive to a
		// distance)
		// TODO: reincorporate the distance measurment into how we shoot
		/*
		if (vision == null || !vision.isConnected()) return 0;
		TargetDetails details = vision.getTargetDetails();
		if (!details.isValid(clock.currentTime())) return 0;
		
		// We have a recent target position relative to the robot starting position.
		Position current = location.getCurrentLocation();
		double distance = current.distanceTo(details.location) - Config.VISION_STOP_DISTANCE;
		return distance; 
		*/
		return 0;
	}

	public Position getVisionWaypoint() {
		if (vision == null || !vision.isConnected())
			return new Position(0, 0);
		TargetDetails details = vision.getTargetDetails();
		Position current = location.getCurrentLocation();
		if (current.distanceTo(details.location) > Config.vision.splineMinDistanceMetres) {
			return details.location
					.addVector(-current.distanceTo(details.location) * Config.vision.waypointDistanceScale, 0);
		} else {
			return details.location;
		}
	}

	public double getVisionTurnWaypointAdjustment() {
		if (vision == null || !vision.isConnected())
			return 0;
		TargetDetails details = vision.getTargetDetails();
		if (!details.isValid(clock.currentTime()))
			return 0;
		// We have a recent target position relative to the robot starting position.
		Position current = location.getCurrentLocation();
		Position waypoint = getVisionWaypoint();
		// Logger.debug("curr pos=%s waypoint = %s target = %s", current, waypoint,
		// details.location);
		// Logger.debug("bearingToWaypoint = %.1f bearingToVision = %.1f",
		// current.bearingTo(waypoint), current.bearingTo(details.location));
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
		double target = drivebase.getDriveRoutineParameters().value;
		double actual = location.getBearing();
		// Logger.debug("angle diff = %f\n", MathUtil.getAngleDiff(actual, target));
		return MathUtil.clamp(MathUtil.getAngleDiff(actual, target), -100, 100);
	}

	public void createIntake() {
		if (!Config.intake.present) {
			intake = new MockIntake();
			Log.debug("Intake not present, using a mock intake instead");
			return;
		}

		Solenoid intakeSolenoid = Hardware.Solenoids.singleSolenoid(Config.pcm.canId, Config.intake.solenoidPort,
				0.2, 0.2); // TODO: Test and work out correct timings.
		Motor intakeMotor = MotorFactory.getIntakeMotor();
		intake = hwIntake = new Intake(intakeMotor, intakeSolenoid, dashboard);
	}

	public void createIntakeOverride() {
		// Setup the diagBox so that it can take control.
		IntakeSimulator simulator = new IntakeSimulator();
		MockIntake mock = new MockIntake();
		intakeOverride = new OverridableSubsystem<IntakeInterface>("intake", IntakeInterface.class, intake, simulator,
				mock);
		// Plumb accessing the intake through the override.
		intake = intakeOverride.getNormalInterface();
		Strongback.executor().register(simulator, Priority.HIGH);
	}

	public void createBuddyClimb() {
		if (!Config.buddyClimb.present) {
			buddyClimb = new MockBuddyClimb();
			Log.debug("Buddy climb not present, using a mock buddy climb instead");
			return;
		}

		Solenoid buddyClimbSolenoid = Hardware.Solenoids.singleSolenoid(Config.pcm.canId,
				Config.buddyClimb.solenoidPort, 0.1, 0.1); // TODO: Test and work out correct timings.
		buddyClimb = new BuddyClimb(buddyClimbSolenoid, dashboard);
	}

	public void createColourWheel() {
		if (!Config.colourWheel.present) {
			colourWheel = new MockColourWheel();
			Log.debug("Colour Sensor not present, using a mock colour sensor instead");
			return;
		}
		Motor motor = MotorFactory.getColourWheelMotor();
		Solenoid colourWheelSolenoid = Hardware.Solenoids.singleSolenoid(Config.pcm.canId,
				Config.colourWheel.solenoidPort, 0.1, 0.1); // TODO: Test and work out correct timings.

		ColorSensorV3 colourSensor = new ColorSensorV3(i2cPort);
		ColorMatch colourMatcher = new ColorMatch();
		colourMatcher.addColorMatch(Config.colourWheel.target.blue); // Adding colours to the colourMatcher
		colourMatcher.addColorMatch(Config.colourWheel.target.green);
		colourMatcher.addColorMatch(Config.colourWheel.target.red);
		colourMatcher.addColorMatch(Config.colourWheel.target.yellow);
		colourMatcher.addColorMatch(Config.colourWheel.target.white);

		colourWheel = new ColourWheel(motor, colourWheelSolenoid, new Supplier<WheelColour>() {
			@Override
			public WheelColour get() {
				ColorMatchResult match = colourMatcher.matchClosestColor(colourSensor.getColor());
				WheelColour sensedColour = WheelColour.UNKNOWN;
				if (match.color == Config.colourWheel.target.blue) {
					sensedColour = WheelColour.BLUE;
				} else if (match.color == Config.colourWheel.target.red) {
					sensedColour = WheelColour.RED;
				} else if (match.color == Config.colourWheel.target.green) {
					sensedColour = WheelColour.GREEN;
				} else if (match.color == Config.colourWheel.target.yellow) {
					sensedColour = WheelColour.YELLOW;
				}
				return sensedColour;
			}
		}, ledStrip, clock, dashboard);
		Strongback.executor().register(colourWheel, Priority.HIGH);
	}

	public void createLEDStrip() {
		if (!Config.ledStrip.present) {
			ledStrip = new MockLEDStrip();
			Log.debug("LED Strip not present, using a mock LED Strip instead.");
			return;
		}
		ledStrip = new LEDStrip(Config.ledStrip.pwmPort, Config.ledStrip.numLEDs);
	}

	public void updateIdleLED() {
		ledStrip.setIdle();
	}

	public void setLEDColour(LEDColour c) {
		ledStrip.setColour(c);
	}

	public void setLEDFinalCountdown(double time) {
		ledStrip.setProgressColour(LEDColour.GREEN, LEDColour.RED, time / Config.ledStrip.countdown);
	}

	@SuppressWarnings("resource")
	public void createLoader() {
		if (!Config.loader.present) {
			loader = new MockLoader();
			Log.debug("Created a mock loader!");
			return;
		}

		Motor spinnerMotor = MotorFactory.getLoaderSpinnerMotor();
		Motor loaderPassthroughMotor = MotorFactory.getLoaderPassthroughMotor();
		Solenoid paddleSolenoid = Hardware.Solenoids.singleSolenoid(Config.pcm.canId, Config.loader.solenoidPort,
				0.1, 0.1); // TODO: Test and work out correct timings.
		// The ball sensors are connected to the DIO ports on the rio.
		DigitalInput inBallSensor = new DigitalInput(Config.loader.ballDetector.inPort);
		DigitalInput outBallSensor = new DigitalInput(Config.loader.ballDetector.outPort);
		BooleanSupplier loaderInSensor = () -> !inBallSensor.get();
		BooleanSupplier loaderOutSensor = () -> !outBallSensor.get();
		loader = hwLoader = new Loader(spinnerMotor, loaderPassthroughMotor, paddleSolenoid, loaderInSensor,
				loaderOutSensor, ledStrip, dashboard);
		Strongback.executor().register(loader, Priority.LOW);

	}

	public void createLoaderOverride() {
		// Setup the diagBox so that it can take control.
		MockLoader simulator = new MockLoader(); // Nothing to simulate, use the mock
		MockLoader mock = new MockLoader();
		loaderOverride = new OverridableSubsystem<LoaderInterface>("loader", LoaderInterface.class, loader, simulator,
				mock);
		// Plumb accessing the lift through the override.
		loader = loaderOverride.getNormalInterface();
	}

	public void createShooter() {
		if (!Config.shooter.present) {
			shooter = new MockShooter();
			Log.debug("Created a mock shooter!");
			return;
		}

		Solenoid hoodSolenoid = Hardware.Solenoids.singleSolenoid(Config.pcm.canId, Config.shooter.solenoidPort,
				0.1, 0.1); // TODO: Test and work out correct timings.
		Motor motor = MotorFactory.getShooterMotor(clock);

		shooter = hwShooter = new Shooter(motor, hoodSolenoid, dashboard);
	}

	public void createShooterOverride() {
		// Setup the diagBox so that it can take control.
		MockShooter simulator = new MockShooter(); // Nothing to simulate, use a mock instead.
		MockShooter mock = new MockShooter();
		shooterOverride = new OverridableSubsystem<ShooterInterface>("shooter", ShooterInterface.class, shooter,
				simulator, mock);
		// Plumb accessing the shooter through the override.
		shooter = shooterOverride.getNormalInterface();
	}

	/**
	 * Create the Pneumatics Control Module (PCM) subsystem.
	 */
	public void createPneumatics() {
		if (!Config.pcm.present) {
			compressor = Mock.pneumaticsModule(Config.pcm.canId);
			Log.debug("Created a mock compressor");
			return;
		}
		compressor = Hardware.pneumaticsModule(Config.pcm.canId);
	}

	public void createVision() {
		if (!Config.vision.present) {
			vision = new MockVision();
			Log.debug("Created a mock vision subsystem");
			return;
		}
		try {
			jevois = new Jevois();
			vision = new Vision(jevois, location, dashboard, clock, Config.vision.hMin, Config.vision.sMin,
					Config.vision.vMin, Config.vision.hMax, Config.vision.sMax, Config.vision.vMax);
		} catch (IOException e) {
			Log.exception("Unable to create an instance of the jevois camera", e);
			e.printStackTrace();
			vision = new MockVision();
		}
	}
}
