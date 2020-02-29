package frc.robot;

import java.io.File;

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
import frc.robot.controller.Controller;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.OIInterface;
import frc.robot.lib.LogDygraph;
import frc.robot.lib.NetworkTablesHelper;
import frc.robot.lib.Position;
import frc.robot.lib.PowerMonitor;
import frc.robot.lib.RedundantTalonSRX;
import frc.robot.lib.RobotConfiguration;
import frc.robot.subsystems.Subsystems;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot implements Executable {
	private Clock clock;
	private RobotConfiguration config;
	private Log log;
	private NetworkTablesHelper networkTable;

	// User interface.
	private DriverStation driverStation;
	private OIInterface oi;
	private FlightStick driverLeftJoystick, driverRightJoystick;
	private InputDevice operatorJoystick, operatorBox;

	// Main logic
	private Controller controller;

	// Subsystems/misc
	private Subsystems subsystems;
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
		log = new LogDygraph(Constants.LOG_BASE_PATH, Constants.LOG_DATA_EXTENSION, Constants.LOG_DATE_EXTENSION, Constants.LOG_NUMBER_FILE, false, clock);
		config = new RobotConfiguration(Constants.CONFIG_FILE_PATH, log);
		networkTable = new NetworkTablesHelper("");
		startWebServer();
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
		subsystems.createPneumatics();
		subsystems.createDrivebaseLocation(driverLeftJoystick, driverRightJoystick);
		subsystems.createIntake();
		subsystems.createClimber();
		subsystems.createLoader();
		subsystems.createOverrides();
		subsystems.createVision();
		subsystems.createLEDStrip();
		subsystems.createColourWheel();

		createPowerMonitor();
		createCameraServers();

		// Create the brains of the robot. This runs the sequences.
		controller = new Controller(subsystems);

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
	}

	/**
	 * Called once when the autonomous period starts.
	 */
	@Override
	public void autonomousInit() {
		log.info("auto has started");

		subsystems.enable();

		controller.doSequence(Sequences.getStartSequence());
		Position resetPose = new Position(0.0, 0.0, 0.0);
		subsystems.location.setCurrentLocation(resetPose);

		// Kick off the selected auto program.
		auto.executedSelectedSequence(controller);
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
		log.info("teleop has started");
		subsystems.enable();
		controller.doSequence(Sequences.setDrivebaseToArcade());
	}

	/**
	 * Called every 20ms while in the teleop period.
	 * All the logic is kicked off either in response to button presses
	 * or by the strongback scheduler.
	 * No spaghetti code here!
	 */

	@Override
	public void teleopPeriodic() {
	
	}

	/**
	 * Called when the test mode is enabled.
	 */
	@Override
	public void testInit() {
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
		//pdp = new PowerMonitor(new PowerDistributionPanel(config.pdpCanId), config.pdpChannelsToMonitor, enabled, log);
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
		String matchDescription = String.format("%s_%s_M%d_R%d_%s_P%d", driverStation.getEventName(),
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

		subsystems.updateDashboard();
		//pdp.updateDashboard();
		controller.updateDashboard();
	}
}
