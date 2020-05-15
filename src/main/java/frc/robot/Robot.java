package frc.robot;

import java.io.File;
import java.util.function.Supplier;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.jibble.simplewebserver.SimpleWebServer;
import org.strongback.Executable;
import org.strongback.Executor.Priority;
import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.DriverStation;
import org.strongback.components.ui.FlightStick;
import org.strongback.components.ui.InputDevice;
import org.strongback.hardware.Hardware;
import org.strongback.hardware.HardwareDriverStation;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpiutil.net.PortForwarder;
import frc.robot.controller.Controller;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.OIInterface;
import frc.robot.lib.ConfigServer;
import frc.robot.lib.LEDColour;
import frc.robot.lib.LogGraph;
import frc.robot.lib.Position;
import frc.robot.lib.PowerMonitor;
import frc.robot.lib.RedundantTalonSRX;
import frc.robot.lib.RobotConfiguration;
import frc.robot.lib.RobotName;
import frc.robot.lib.WheelColour;
import frc.robot.subsystems.Subsystems;

public class Robot extends IterativeRobot implements Executable {
	private Clock clock;
	private RobotConfiguration config;
	private LogGraph log;

	// User interface.
	private DriverStation driverStation;
	private OIInterface oi;
	private FlightStick driverLeftJoystick, driverRightJoystick;
	private InputDevice operatorJoystick, operatorBox;

	// Main logic
	private Controller controller;

	// Subsystems/misc
	private Subsystems subsystems;
	@SuppressWarnings("unused")
	private PowerMonitor pdp;
	private Auto auto;

	/*
	 * We wish to delay our full setup until; the driver's station has connected. At
	 * at that point we have received the number of joysticks, the configuration of
	 * the joysticks, and information about autonomous selections.
	 * 
	 * This can be done by waiting until robotPeriodic() is called the first time,
	 * as this is called once we are in communications with the driver's station.
	 */
	@Override
	public void robotInit() {
		clock = Strongback.timeSystem();
		var robotName = RobotName.get(Constants.ROBOT_NAME_FILE_PATH);
		log = new LogGraph(robotName, Constants.WEB_BASE_PATH, Constants.LOG_BASE_PATH, Constants.LOG_DATA_EXTENSION,
				Constants.LOG_DATE_EXTENSION, Constants.LOG_LATEST_EXTENSION, Constants.LOG_EVENT_EXTENSION, false, clock);
		config = new RobotConfiguration(Constants.CONFIG_FILE_PATH, log);
		startWebServer();
		startConfigServer();
		log.info("Waiting for driver's station to connect before setting up UI");
		// Do the reset of the initialization in init().
	}

	private boolean setupCompleted = false;
	public void maybeInit() {
		if (setupCompleted) return;
		try {
			init();
			setupCompleted = true;
		} catch (Exception e) {
			// Write the exception to the log file.
			log.exception("Exception caught while initializing robot", e);
			throw e; // Cause it to abort the robot startup.
		}
	}

	/**
	 * Initialize the robot now that the drivers station has connected.
	 */
	public void init() {
		Strongback.logConfiguration();
		Strongback.setExecutionPeriod(Constants.EXECUTOR_CYCLE_INTERVAL_MSEC);

		log.info("Robot initialization started");

		createInputDevices();

		// Setup the hardware/subsystems. Listed here so can be quickly jumped to.
		subsystems = new Subsystems(createDashboard(), config, clock, log);
		subsystems.createLEDStrip();
		subsystems.createPneumatics();
		subsystems.createDrivebaseLocation(driverLeftJoystick, driverRightJoystick);
		subsystems.createIntake();
		subsystems.createShooter();
		subsystems.createLoader();
		subsystems.createOverrides();
		subsystems.createVision();
		subsystems.createColourWheel();

		createPowerMonitor();
		createCameraServers();

		// Create the brains of the robot. This runs the sequences.
		controller = new Controller(subsystems, getFMSColourSupplier());

		// Setup the interface to the user, mapping buttons to sequences for the controller.
		setupUserInterface();

		startLogging();  // All subsystems have registered by now, enable logging.
		Strongback.executor().register(this, Priority.LOW);

		// Start the scheduler to keep all the subsystems working in the background.
		Strongback.start();

		// Setup the auto sequence chooser.
		auto = new Auto(log);

		log.info("Robot initialization successful");
	}

	/**
	 * Called every 20ms while the drivers station is connected.
	 */
	@Override
	public void robotPeriodic() {
		// Nothing to do. Dashboard is updated by the executor.
	}

	/**
	 * Called when the robot starts the disabled mode. Normally on first start
	 * and after teleop and autonomous finish.
	 */
	@Override
	public void disabledInit() {
		PortForwarder.add(Constants.RSYNC_PORT, Constants.RSYNC_HOSTNAME, 22); // Start forwarding to port 22 (ssh port) for pulling logs using rsync.
		maybeInit();  // Called before robotPeriodic().
		log.info("disabledInit");
		// Log any failures again on disable.
		RedundantTalonSRX.printStatus();
		// Tell the controller to give up on whatever it was processing.
		controller.doSequence(Sequences.getEmptySequence());
		// Disable all subsystems
		subsystems.disable();
	}

	/**
	 * Called every 20ms while the robot is in disabled mode.
	 */
	@Override
	public void disabledPeriodic() {
		subsystems.updateIdleLED();
	}

	/**
	 * Called once when the autonomous period starts.
	 */
	@Override
	public void autonomousInit() {
		PortForwarder.remove(Constants.RSYNC_PORT); // Stop forwarding port to stop rsync and save bandwidth.
		log.restartLogs();
		log.info("auto has started");
		subsystems.enable();

		controller.doSequence(Sequences.getStartSequence());
		Position resetPose = new Position(0.0, 0.0, 0.0);
		subsystems.location.setCurrentLocation(resetPose);

		// Kick off the selected auto program.
		auto.executedSelectedSequence(controller);
		// Gets the amount set in SmartDashboard and sets the init ball count
		int initialNumBalls = auto.getSelectedBallAmount(); 
		subsystems.loader.setInitBallCount(initialNumBalls);
	}

	/**
	 * Called every 20ms while in the autonomous period.
	 */
	@Override
	public void autonomousPeriodic() {
	}

	/**
	 * Called once when the teleop period starts.
	 */
	@Override
	public void teleopInit() {
		PortForwarder.remove(Constants.RSYNC_PORT); // Stop forwarding port to stop rsync and save bandwidth.
		log.restartLogs();
		log.info("teleop has started");
		subsystems.enable();
		controller.doSequence(Sequences.setDrivebaseToArcade());
		subsystems.setLEDColour(allianceLEDColour());
	}

	/**
	 * Called every 20ms while in the teleop period.
	 * All the logic is kicked off either in response to button presses
	 * or by the strongback scheduler.
	 * No spaghetti code here!
	 */

	@Override
	public void teleopPeriodic() {
		if (0 <= driverStation.getMatchTime() && driverStation.getMatchTime() <= Constants.LED_STRIP_COUNTDOWN) { // While in teleop out of a match, the match time is -1.
			subsystems.setLEDFinalCountdown(driverStation.getMatchTime());
		}
	}

	/**
	 * Called when the test mode is enabled.
	 */
	@Override
	public void testInit() {
		log.restartLogs();
		subsystems.enable();
	}

	/**
	 * Called every 20ms during test mode.
	 */
	@Override
	public void testPeriodic() {
	}

	/**
	 * Plumb the dashboard requests through to a real dashboard.
	 * Allows us to mock out the dashboard in unit tests.
	 */
	private DashboardInterface createDashboard() {
		return new DashboardInterface() {
			@Override
			public void putString(String key, String value) {
				SmartDashboard.putString(key, value);
			}
			@Override
			public void putNumber(String key, double value) {
				SmartDashboard.putNumber(key, value);
			}
			@Override
			public void putBoolean(String key, Boolean value) {
				SmartDashboard.putBoolean(key, value);
			}
		};
	}

	/**
	 * Create the camera servers so the driver & operator can see what the robot can see.
	 */
	public void createCameraServers() {
		if (!config.visionIsPresent) {
			log.sub("Vision not enabled, not creating a camera server");
			return;
		}
		UsbCamera camera = CameraServer.getInstance().startAutomaticCapture(0);
		// Select FIRST Python processor on the Jevois camera by setting a particular
		// resolution, frame rate and format.
		camera.setVideoMode(VideoMode.PixelFormat.kYUYV, Constants.CAMERA_RESOLUTION_WIDTH,
				Constants.CAMERA_RESOULTION_HEIGHT, Constants.CAMERA_FRAMES_PER_SECOND);
	}

	/**
	 * Motor the power draw. With the wpilibj interface this can slow down the
	 * entire robot due to lock conflicts.
	 */
	private void createPowerMonitor() {
		// Do not monitor if not present, or we have been asked not to monitor
		boolean enabled = config.pdpIsPresent || config.pdpMonitor;
		pdp = new PowerMonitor(new PowerDistributionPanel(config.pdpCanId), config.pdpChannelsToMonitor, enabled, log);
	}

	/**
	 * Create the simple web server so we can interrogate the robot during operation.
	 * The web server lives on a port that is available over the firewalled link.
	 * We use port 5800, the first of the opened ports.
	 * 
	 */
	private void startWebServer() {
		File fileDir = new File(Constants.WEB_BASE_PATH);
		try {
			new SimpleWebServer(fileDir, Constants.WEB_PORT);
			log.sub("WebServer started at port: " + Constants.WEB_PORT);
		} catch (Exception e) {
			log.sub("Failed to start webserver on directory " + fileDir.getAbsolutePath());

			e.printStackTrace();
		}
	}

	/**
	 * Creates the web server for allowing easy modification of the robot's config file using port 5801.
	 */
	private void startConfigServer() {
		try {
			new ConfigServer(Constants.CONFIG_WEB_ROOT, Constants.CONFIG_FILE_PATH, Constants.ROBOT_NAME_FILE_PATH, Constants.CONFIG_WEB_PORT);
			log.sub("Config webserver started at port: " + Constants.WEB_PORT);
		} catch (Exception e) {
			log.sub("Failed to start config webserver.");
			e.printStackTrace();
		}
	}

	/**
	 * Create the joysticks
	 */
	private void createInputDevices() {
		driverStation = new HardwareDriverStation();
		driverLeftJoystick = Hardware.HumanInterfaceDevices.logitechAttack3D(0);
		driverRightJoystick = Hardware.HumanInterfaceDevices.logitechAttack3D(1);
		operatorJoystick = Hardware.HumanInterfaceDevices.driverStationJoystick(2);
		operatorBox = Hardware.HumanInterfaceDevices.driverStationJoystick(3);
	}

	/**
	 * Setup the button mappings on the joysticks and the operators button
	 * box if it's attached.
	 */
	private void setupUserInterface() {
		oi = new OI(controller, subsystems, log);
		oi.configureJoysticks(driverLeftJoystick, driverRightJoystick, operatorJoystick);
		if (operatorBox.getButtonCount() > 0) {
			log.info("Operator box detected");
			oi.configureDiagBox(operatorBox);
		}
		log.register(false, driverStation::getMatchTime, "DriverStation/MatchTime");
	}

	/**
	 * Start the logging.
	 */
	private void startLogging() {
		// Tell the logger what symbolic link to the log file based on the match name to use.
		String matchDescription = String.format("_%t_%s_%s_M%d_R%d_%s_P%d", 
				new SimpleDateFormat("yyyyMMdd't'hhmmss").format(Calendar.getInstance().getTime()),
				driverStation.getEventName(),
				driverStation.getMatchType().toString(), driverStation.getMatchNumber(),
				driverStation.getReplayNumber(), driverStation.getAlliance().toString(), driverStation.getLocation());
		log.logCompletedElements(matchDescription);
		if (config.doLogging) {
			// Low priority means run every 20 * 4 = 80ms, or at 12.5Hz
			// It polls almost everything on the CAN bus, so don't want it to be too fast.
			Strongback.executor().register(log, Priority.LOW);
		} else {
			log.error("Logging: Dygraph logging disabled");
		}
	}

	@Override
	public void execute(long timeInMillis) {
		//log.sub("Updating smartDashboard");
		maybeUpdateSmartDashboard();
	}

	private double lastDashboardUpdateSec = 0;
	/**
	 * Possibly update the smartdashboard.
	 * Don't do this too often due to the amount that is sent to the dashboard.
	 */
	private void maybeUpdateSmartDashboard() {
		double now = Strongback.timeSystem().currentTime();
		if (now < lastDashboardUpdateSec + Constants.DASHBOARD_UPDATE_INTERVAL_SEC)
			return;
		lastDashboardUpdateSec = now;
		subsystems.dashboard.putString("FMS Colour: ", getFMSColour().toString());
		subsystems.updateDashboard();
		//pdp.updateDashboard();
		controller.updateDashboard();
	}

	private String lastColour = "";
	public WheelColour getFMSColour() {
		String fmsColour = driverStation.getGameSpecificMessage();
		if (!fmsColour.equals(lastColour)) {
			log.info("FMS Colour: %s", fmsColour);
			lastColour = fmsColour;
		}
		if (fmsColour.length() == 0) {
			return WheelColour.UNKNOWN;
		}
		switch (fmsColour.charAt(0)) {
		case 'B':
			return WheelColour.RED;
		case 'G':
			return WheelColour.YELLOW;
		case 'R':
			return WheelColour.BLUE;
		case 'Y':
			return WheelColour.GREEN;
		default:
			return WheelColour.UNKNOWN;
		}
	}

	/**
	 * Determines the desired colour wheel colour from FMS. Single letter R, G, B, or Y indicates colour.
	 * If there is no letter or a letter other than those, the colour defaults to unknown.
	 * Colours are flipped around so that the sensor on the robot will look for the colour perpendicular to the field sensor.
	 * @return The colour the robots sensor should look for.
	 */
	private Supplier<WheelColour> getFMSColourSupplier() {
		return new Supplier<WheelColour>() {
			@Override
			public WheelColour get() {
				return getFMSColour();
			}
		};
	}

	private LEDColour allianceLEDColour() {
		switch (driverStation.getAlliance()) {
		case Red:
			return LEDColour.RED;
		case Blue:
			return LEDColour.BLUE;
		default:
			return LEDColour.WHITE;
		}
	}
}