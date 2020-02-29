package frc.robot.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import frc.robot.Constants;
import frc.robot.interfaces.Log;

/**
 * Class responsible for updating values which are dependent on robot hardware.
 * (e.g. if subsystems are present or not) It reads from a text file
 * Currently the supported types are String, int, double, boolean and int array.
 * 
 * Example lines:
 * drivebase/present = true
 * drivebase/rampRate = 0.13125
 * drivebase/right/canIDs/withEncoders = 7,6
 * drivebase/right/canIDs/withoutEncoders = 5
 * 
 * TODO: Add support for comments and freeform parameters.
 * @author carlin
 */
public class RobotConfiguration {
	private String name = "RobotConfig";
	
	private Log log;
	private Map<String, String> lines;
	private Map<String, String> ignoredEntries;  // Lines/entries not used in the config file.
	private Map<String, String> nonDefaultParameters;  // Non-default values used from the config file.
	private ArrayList<String> exampleText = new ArrayList<String>();

	// These are variables which will be updated
	
	public String robotName = "3132";
	public double robotLengthWithBumpers = 0;
	public double robotWidthWithBumpers = 0;
	public double cameraFromFrontWithBumpers = 0;
	public double cameraFromLeftWithBumpers = 0;

	public int teamNumber = 3132;
	public boolean drivebaseIsPresent = true;
	public String drivebaseMotorControllerType = Constants.DRIVE_DEFAULT_CONTROLLER_TYPE;
	public int[] drivebaseCanIdsLeftWithEncoders = Constants.DRIVE_LEFT_TALON_WITH_ENCODERS_CAN_ID_LIST;
	public int[] drivebaseCanIdsLeftWithoutEncoders = Constants.DRIVE_LEFT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST;
	public int[] drivebaseCanIdsRightWithEncoders = Constants.DRIVE_RIGHT_TALON_WITH_ENCODERS_CAN_ID_LIST;
	public int[] drivebaseCanIdsRightWithoutEncoders = Constants.DRIVE_RIGHT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST;
	public boolean drivebaseCurrentLimiting = true;
	public int drivebaseContCurrent = Constants.DRIVE_CONT_CURRENT;
	public int drivebasePeakCurrent = Constants.DRIVE_PEAK_CURRENT;
	public double drivebaseRampRate = Constants.DRIVE_RAMP_RATE;
	public double drivebaseP = Constants.DRIVE_P;
	public double drivebaseI = Constants.DRIVE_I;
	public double drivebaseD = Constants.DRIVE_D;
	public double drivebaseF = Constants.DRIVE_F;
	public String drivebaseMode = Constants.DRIVE_DEFAULT_MODE;
	public double drivebaseMaxSpeed = Constants.DRIVE_MAX_SPEED;


	public boolean drivebaseSwapLeftRight = false;
	public boolean drivebaseSensorPhase = false;
	public double drivebaseCount =  Constants.DRIVE_COUNT_100ms;
	
	public boolean intakeIsPresent = false;
	public int intakeCanID = Constants.INTAKE_MOTOR_TALON_CAN_ID;

	public boolean colourWheelIsPresent = false;
	public int colourWheelCanID = Constants.COLOUR_WHEEL_CAN_ID;

	public boolean sparkTestIsPresent = false;
	public int[] sparkTestCanIds = Constants.TEST_SPARK_MOTOR_CAN_ID_LIST;

	public boolean loaderIsPresent = false;
	public int loaderCanID = Constants.LOADER_SPINNER_CAN_ID;
	public int loaderInCanID = Constants.LOADER_PASSTHROUGH_MOTOR_CAN_ID;
	public int loaderOutCanID = Constants.LOADER_FEEDER_MOTOR_CAN_ID;
	public double loaderSpinnerP = Constants.LOADER_SPINNER_P;
	public double loaderSpinnerI = Constants.LOADER_SPINNER_I;
	public double loaderSpinnerD = Constants.LOADER_SPINNER_D;
	public double loaderSpinnerF = Constants.LOADER_SPINNER_F;
	public double loaderPassthroughP = Constants.LOADER_PASSTHROUGH_P;
	public double loaderPassthroughI = Constants.LOADER_PASSTHROUGH_I;
	public double loaderPassthroughD = Constants.LOADER_PASSTHROUGH_D;
	public double loaderPassthroughF = Constants.LOADER_PASSTHROUGH_F;

	public boolean pdpIsPresent = false;
	public int pdpCanId = Constants.PDP_CAN_ID;
	public boolean pdpMonitor = false;  // by default we do NOT monitor the PDP
	public int[] pdpChannelsToMonitor = new int[0];  // by default we do NOT monitor any channels

	public boolean pcmIsPresent = false;
	public int pcmCanId = Constants.PCM_CAN_ID;
	
	public boolean canifierIsPresent = false;
	public int canifierCanId;

	public boolean navxIsPresent = false;

	public boolean visionIsPresent = true;
	public double visionHMin = Constants.VISION_H_MIN;
	public double visionHMax = Constants.VISION_S_MAX;
	public double visionSMin = Constants.VISION_H_MAX;
	public double visionSMax = Constants.VISION_V_MIN;
	public double visionVMin = Constants.VISION_S_MIN;
	public double visionVMax = Constants.VISION_V_MAX;
	
	public boolean climberIsPresent = true;
	public int climberFrontCanID = Constants.CLIMBER_FRONT_CAN_ID;
	public int climberRearCanID = Constants.CLIMBER_REAR_CAN_ID;
	public int climberDriveMotorCanID = Constants.CLIMBER_DRIVE_MOTOR_CAN_ID;


	// Logging default is to not log anything to the graph, and to only log local information when we turn it on.
	// These are the safest defaults.
	public boolean doLogging = false;
	public boolean onlyLocal = true;

	public boolean ledStripIsPresent = false;

	public RobotConfiguration(String filePath, Log log) {
		this(filePath, TeamNumber.get(), log);
	}

	public RobotConfiguration(String filePath, int teamNumber, Log log) {
		this.teamNumber = teamNumber;
		this.log = log;
		
		readLines(Paths.get(filePath));
		readParameters();  // Creates example contents.
		Collections.sort(exampleText);
		writeExampleFile(filePath, String.join("\n", exampleText));
	}
	
	private void writeExampleFile(String filePath, String contents) {
		Path exampleFile = Paths.get(filePath + ".example");
		try {
			BufferedWriter writer;
			writer = Files.newBufferedWriter(exampleFile, StandardOpenOption.CREATE);
			writer.write(contents + "\n");
			writer.close();
			log.info("Wrote example config file " + exampleFile.toString());
		} catch (IOException e) {
			log.exception("Unable to write example config file " + exampleFile.toString(), e);
		}
	}

	private void readLines(Path path) {
		log.info("Reading config file " + path);
		lines = new HashMap<String, String>();
		ignoredEntries = new TreeMap<String, String>();
		nonDefaultParameters = new TreeMap<String, String>();
		try (BufferedReader reader = Files.newBufferedReader(path)) {
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		    	String[] parts = line.split("\\s*=\\s*", -1); // Keep empty values
		    	if (parts.length < 2) {
		    		log.error("Bad config line " + line);
		    		continue;
		    	}
		    	String tag = parts[0].trim();
		    	String value = parts[1].trim();
		    	if (lines.containsKey(tag)) {
		    		log.error("ERROR: Duplicate tag %s in configuration file, last value will be used.", tag);
		    	}
		    	lines.put(tag, value);
		    	ignoredEntries.put(parts[0].trim(), line);
		    }
		} catch (NoSuchFileException e) {
			log.error("Config file %s not found, attempting to create it", path);
			// Touch the file so at least it's there next time.
			try {
				Files.createFile(path);
			} catch (IOException e1) {}
		} catch (IOException e) {
			log.exception("Error loading configuration file " + path + ", using defaults", e);
		}
	}

	private void readParameters() {	
		drivebaseIsPresent = getAsBoolean("drivebase/present", drivebaseIsPresent);
		drivebaseMotorControllerType = getMotorControllerType("drivebase/motorControllerType", drivebaseMotorControllerType);
		drivebaseCanIdsLeftWithEncoders = getAsIntArray("drivebase/left/canIDs/withEncoders", drivebaseCanIdsLeftWithEncoders);
		drivebaseCanIdsLeftWithoutEncoders = getAsIntArray("drivebase/left/canIDs/withoutEncoders", drivebaseCanIdsLeftWithoutEncoders);
		drivebaseCanIdsRightWithEncoders = getAsIntArray("drivebase/right/canIDs/withEncoders", drivebaseCanIdsRightWithEncoders);
		drivebaseCanIdsRightWithoutEncoders = getAsIntArray("drivebase/right/canIDs/withoutEncoders", drivebaseCanIdsRightWithoutEncoders);
		drivebaseCurrentLimiting = getAsBoolean("drivebase/currentLimiting", drivebaseCurrentLimiting);
		drivebaseContCurrent = getAsInt("drivebase/maxCurrent", drivebaseContCurrent);
		drivebasePeakCurrent = getAsInt("drivebase/peakCurrent", drivebasePeakCurrent);
		drivebaseRampRate = getAsDouble("drivebase/rampRate", drivebaseRampRate);
		drivebaseP = getAsDouble("drivebase/p", drivebaseP);
		drivebaseI = getAsDouble("drivebase/i", drivebaseI);
		drivebaseD = getAsDouble("drivebase/d", drivebaseD);
		drivebaseF = getAsDouble("drivebase/f", drivebaseF);
		drivebaseMode = getAsString("drivebase/mode", drivebaseMode);
		drivebaseMaxSpeed = getAsDouble("drivebase/maxSpeed", drivebaseMaxSpeed);
		drivebaseSwapLeftRight = getAsBoolean("drivebase/swapLeftRight", drivebaseSwapLeftRight);
		drivebaseSensorPhase = getAsBoolean("drivebase/sensor/phase", drivebaseSensorPhase);
		drivebaseCount = getAsDouble("drivebase/count100ms", drivebaseCount);
	
		intakeIsPresent = getAsBoolean("intake/present", true);
		intakeCanID = getAsInt("intake/canID", Constants.INTAKE_MOTOR_TALON_CAN_ID);

		colourWheelIsPresent = getAsBoolean("colourWheel/present", false);
		colourWheelCanID = getAsInt("colourWheel/canID", Constants.COLOUR_WHEEL_CAN_ID);

		sparkTestIsPresent = getAsBoolean("sparkTest/present", false);
		sparkTestCanIds = getAsIntArray("sparkTest/canID", Constants.TEST_SPARK_MOTOR_CAN_ID_LIST);

		loaderIsPresent = getAsBoolean("loader/present", true);
		loaderCanID = getAsInt("loader/canID", Constants.LOADER_SPINNER_CAN_ID);

		
		pdpIsPresent = getAsBoolean("pdp/present", true);
		pdpCanId = getAsInt("pdp/canID", Constants.PDP_CAN_ID);
		pdpMonitor = getAsBoolean("pdp/monitor", false);		// by default we do NOT monitor the PDP
		pdpChannelsToMonitor = getAsIntArray("pdp/channels", new int[0]);	// by default we do NOT monitor and channels

		pcmIsPresent = getAsBoolean("pcm/present", true);
		pcmCanId = getAsInt("pcm/canID", Constants.PCM_CAN_ID);
		
		navxIsPresent = getAsBoolean("navx/present", false);

		robotLengthWithBumpers = getAsDouble("dimensions/robot/lengthWithBumpers", 0.0);
		robotWidthWithBumpers = getAsDouble("dimensions/robot/widthWithBumpers", 0.0);
		cameraFromFrontWithBumpers = getAsDouble("dimensions/cameraFromFrontWithBumpers", 0.0);
		cameraFromLeftWithBumpers = getAsDouble("dimensions/cameraFromLeftWithBumpers", 0.0);
		
		visionIsPresent = getAsBoolean("vision/present", true);
		visionHMin = getAsDouble("vision/hsvFilter/h/min", Constants.VISION_H_MIN);
		visionHMax = getAsDouble("vision/hsvFilter/h/max", Constants.VISION_H_MAX);
		visionSMin = getAsDouble("vision/hsvFilter/s/min", Constants.VISION_S_MIN);
		visionSMax = getAsDouble("vision/hsvFilter/s/max", Constants.VISION_S_MAX);
		visionVMin = getAsDouble("vision/hsvFilter/v/min", Constants.VISION_V_MIN);
		visionVMax = getAsDouble("vision/hsvFilter/v/max", Constants.VISION_V_MAX);

		climberIsPresent = getAsBoolean("climber/present", false);
		climberFrontCanID = getAsInt("climber/frontCanID", Constants.CLIMBER_FRONT_CAN_ID);
		climberRearCanID = getAsInt("climber/rearCanID", Constants.CLIMBER_REAR_CAN_ID);
		climberDriveMotorCanID = getAsInt("climber/driveMotorCanID", Constants.CLIMBER_DRIVE_MOTOR_CAN_ID);
			
		// logging default is to not log anything to the graph, and to only log local information when we turn it on.
		// These are the safest defaults.
		doLogging = getAsBoolean("logging/enabled", true);
		onlyLocal = getAsBoolean("logging/onlyLocal", true);

		canifierIsPresent = getAsBoolean("canifier/present", false);
		canifierCanId = getAsInt("canifier/canID", Constants.LED_CANIFIER_CAN_ID);

		ledStripIsPresent = getAsBoolean("ledStrip/present", false);

		if (!ignoredEntries.isEmpty()) {
			log.warning("WARNING: These config file lines weren't used:");
			for (String entry : ignoredEntries.values()) {
				log.warning("  %s", entry);
			}
		}
		if (!nonDefaultParameters.isEmpty()) {
			log.warning("WARNING: These parameters have non-default values:");
			for (Map.Entry<String, String> entry : nonDefaultParameters.entrySet()) {
				log.warning("  %s = %s", entry.getKey(), entry.getValue());
			}
		}
		log.info("RobotConfig finished loading parameters\n");
	}
	
	private String getMotorControllerType(String parameterName, String defaultValue) {
		String type = getAsString(parameterName, defaultValue);
		switch(type) {	
			default:
			log.error("Invalid value '%s' for parameter '%s'.  Using TalonSRX.", type, parameterName);
			// Falling through to TalonSRX.

			case Constants.MOTOR_CONTROLLER_TYPE_TALONSRX: 
			return Constants.MOTOR_CONTROLLER_TYPE_TALONSRX;

			case Constants.MOTOR_CONTROLLER_TYPE_SPARKMAX: 
			return Constants.MOTOR_CONTROLLER_TYPE_SPARKMAX;
		}
	}

	private <T> void appendExample(String key, T defaultValue) {
		exampleText.add(key + " = " + defaultValue);
	}
	
	private int getAsInt(String key, int defaultValue) {
		appendExample(key, defaultValue);
		try {
			if (lines.containsKey(key)) {
				int value = Integer.valueOf(lines.get(key));
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %d", name, key, value);
				if (value != defaultValue) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return value;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}
	
	private double getAsDouble(String key, double defaultValue) {
		appendExample(key, defaultValue);
		try {
			if (lines.containsKey(key)) {
				double value = Double.valueOf(lines.get(key));
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %f", name, key, value);
				if (value != defaultValue) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return value;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}
	
	private boolean getAsBoolean(String key, boolean defaultValue) {
		appendExample(key, defaultValue);
		try {
			if (lines.containsKey(key)) {
				boolean value = Boolean.valueOf(lines.get(key));
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %s", name, key, value);
				if (value != defaultValue) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return value;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}
	
	private String getAsString(String key, String defaultValue) {
		appendExample(key, "\"" + defaultValue + "\"");
		try {
			if (lines.containsKey(key)) {
				// Get the value between the quotes.
				String[] parts = lines.get(key).split("\"", -1);
				if (parts.length < 3) {
					log.error("Bad string value for %s, needs to be in double quotes, not: %s", key, lines.get(key));
					return defaultValue;
				}
				String value = parts[1];
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %s", name, key, value);
				if (!value.equals(defaultValue)) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return value;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}

	private int[] getAsIntArray(String key, int[] defaultValue) {
		// Joining primitive arrays seems to be painful under Java.
		appendExample(key, joinIntArray(defaultValue));
		try {
			if (lines.containsKey(key)) {
				String value = lines.get(key);
				int[] values;
				if (value.equals("")) {
					// No values.
					values = new int[0];
				} else {
					// One or more values.
					String[] parts = value.split("\\s*,\\s*");
					values = new int[parts.length];
					for (int i = 0; i < parts.length; i++) {
						values[i] = Integer.valueOf(parts[i]);
					}
				}
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %s", name, key, joinIntArray(values));
				if (!java.util.Arrays.equals(values, defaultValue)) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return values;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}

	private String joinIntArray(int[] values) {
		return Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
	}
}