package frc.robot.lib;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import org.strongback.components.Clock;
import org.strongback.components.Motor;
import org.strongback.hardware.Hardware;
import org.strongback.hardware.HardwareSparkMAX;
import org.strongback.hardware.HardwareTalonSRX;

import frc.robot.Constants;
import frc.robot.interfaces.Log;

public class MotorFactory {

	public static Motor getDriveMotor(boolean leftMotor, RobotConfiguration config, Clock clock, Log log) {
		leftMotor = leftMotor ^ config.drivebaseSwapLeftRight;
		int[] canIds = leftMotor ? config.drivebaseCanIdsLeftWithEncoders : config.drivebaseCanIdsRightWithEncoders;

		switch (config.drivebaseMotorControllerType) {
			case Constants.MOTOR_CONTROLLER_TYPE_SPARKMAX: {
			HardwareSparkMAX spark = getSparkMAX("drive", canIds, leftMotor, NeutralMode.Brake, config.drivebasePIDF, log);
			spark.setScale(Constants.SPARKMAX_ENCODER_TICKS, Constants.DRIVE_GEABOX_RATIO, Constants.DRIVE_METRES_PER_REV);
			spark.setSensorPhase(config.drivebaseSensorPhase);

			/*
			 * Setup Current Limiting
			 */
			if (config.drivebaseCurrentLimiting) {
				spark.setSmartCurrentLimit(config.drivebaseContCurrent, config.drivebaseContCurrent); // limit to 35 Amps when current exceeds 40 amps
																		// for 100ms
				spark.setSecondaryCurrentLimit(config.drivebasePeakCurrent);
			}
			return spark;
		}

		default:
			log.error("Invalid drive motor controller '%s'. Please use 'TalonSRX' or 'SparkMAX'. Using TalonSRX.", config.drivebaseMotorControllerType);
			// Falling through to TalonSRX.

		case Constants.MOTOR_CONTROLLER_TYPE_TALONSRX:
			HardwareTalonSRX talon = getTalon("drive", canIds, leftMotor, NeutralMode.Brake, config.drivebasePIDF, log); // don't invert output
			talon.setScale(Constants.FALCON_ENCODER_TICKS, Constants.DRIVE_GEABOX_RATIO, Constants.DRIVE_METRES_PER_REV);
			talon.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor, 0, 10);
			talon.setSensorPhase(config.drivebaseSensorPhase);
			talon.configClosedloopRamp(config.drivebaseRampRate, 10);
			talon.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 10, 10);

			/*
			 * Setup Current Limiting
			 */
			if (config.drivebaseCurrentLimiting) {
				talon.configContinuousCurrentLimit(config.drivebaseContCurrent, 0); // limit to 35 Amps when current exceeds 40 amps for
																	// 100ms
				talon.configPeakCurrentLimit(config.drivebasePeakCurrent, 0);
				talon.configPeakCurrentDuration(100, 0);
				talon.enableCurrentLimit(true);
			}
			return talon;
		}
	}
	
	public static HardwareSparkMAX getIntakeMotor(RobotConfiguration config, Log log) {
		HardwareSparkMAX motor = getSparkMAX("intake", config.intakeCanID, true, NeutralMode.Brake, config.intakePIDF, log);
		motor.setScale(Constants.SPARKMAX_ENCODER_TICKS, Constants.INTAKE_ENCODER_GEARBOX_RATIO);
		motor.setClosedLoopRampRate(0.5);
		return motor;
	}

	public static HardwareTalonSRX getColourWheelMotor(RobotConfiguration config, Log log) {
		HardwareTalonSRX motor = getTalon("colour", config.colourWheelCanID, true, NeutralMode.Brake, config.colourWheelPIDF, log);
		motor.configClosedloopRamp(.25, 10);
		return motor;
	}

	public static HardwareTalonSRX getLoaderSpinnerMotor(RobotConfiguration config, Log log) {	
		HardwareTalonSRX motor = getTalon("loader spinner", config.loaderSpinnerCanID, false, NeutralMode.Brake,
				config.loaderSpinnderPIDF, log);
		// In sensor (beambreak) for loader
		motor.configForwardLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyOpen, 10);
		// Out sensor (beambreak) for loader
		motor.configReverseLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyOpen, 10);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setScale(Constants.VERSA_INTEGRATED_ENCODER_TICKS, Constants.LOADER_MAIN_MOTOR_GEARBOX_RATIO);
		motor.configClosedloopRamp(0, 10);
		return motor;
	}

	public static HardwareSparkMAX getLoaderPassthroughMotor(RobotConfiguration config, Log log) {
		HardwareSparkMAX motor = getSparkMAX("loader passthrough", config.loaderPassthroughCanID, true,
				NeutralMode.Brake, config.loaderPassthroughPIDF, log);
		return motor;
	}
	
	public static HardwareTalonSRX getShooterMotor(RobotConfiguration config, Clock clock, Log log) {
		HardwareTalonSRX motor = getTalon("shooter", config.shooterCanIds, false, NeutralMode.Coast,
				config.shooterPIDF, log);
		motor.setSensorPhase(true);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setScale(Constants.S4T_ENCODER_TICKS, Constants.SHOOTER_GEARBOX_RATIO);
		motor.selectProfileSlot(0, 0);
		
		motor.configClosedloopRamp(1, 10);
		motor.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 10, 10);

		return motor;
	}
    		
    /**
     * Code to allow us to log output current per Spark MAX and set up followers so that
	 * it appears as a single motor but can be an arbitary number of motors configured
	 * in the per robot configuration.
	 * @param name the name to use for saving the PIDF values in the network tables.
     * @param canIDs list of canIDs. First entry is the leader and the rest follow it.
     * @param invert change the direction.
	 * @param mode what to do when the the speed is set to zero.
	 * @param pidf the P, I, D & F values to use.
     * @param log for registration of the current reporting.
     * @return the leader HardwareTalonSRX
     */
	private static HardwareTalonSRX getTalon(String name, int[] canIDs, boolean invert, NeutralMode mode, 
			PIDF pidf, Log log) {
    	HardwareTalonSRX leader = Hardware.Motors.talonSRX(abs(canIDs[0]), invert, mode);
		log.register(false, () -> leader.getOutputCurrent(), "Talons/%d/Current", canIDs[0]);
		leader.configContinuousCurrentLimit(Constants.DEFAULT_CONTINUOUS_CURRENT_LIMIT, 10);
		leader.configPeakCurrentLimit(Constants.DEFAULT_PEAK_CURRENT_LIMIT, 10);
		TunableMotor.tuneMotor(leader, pidf, new NetworkTablesHelper(name));

    	for (int n = 1; n < canIDs.length; n++) {
			boolean shouldInvert = invert;
			if (canIDs[n] < 0) shouldInvert = !shouldInvert;
    		HardwareTalonSRX follower = Hardware.Motors.talonSRX(abs(canIDs[n]), shouldInvert, mode);
			follower.getHWTalon().follow(leader.getHWTalon());
			log.register(false, () -> follower.getOutputCurrent(), "Talons/%d/Current", canIDs[n]);
		}
		return leader;
	}

	/**
	 * Code to allow us to log output current for a single talon.
	 * 
	 * @param name   the name to use for saving the PIDF values in the network tables.
	 * @param canID  CAN ID for this motor controller. Must be unique.
	 * @param invert change the direction.
	 * @param mode   what to do when the the speed is set to zero.
	 * @param pidf   the P, I, D & F values to use.
	 * @param log    for registration of the current reporting.
	 * @return the HardwareTalonSRX motor controller.
	 */
	private static HardwareTalonSRX getTalon(String name, int canID, boolean invert,
			NeutralMode mode,			PIDF pidf, Log log) {
		log.sub("%s: " + canID, "talon");
		int[] canIDs = new int[1];
		canIDs[0] = canID;
    	return getTalon(name, canIDs, invert, mode, pidf, log);
	}

	/**
     * Code to allow us to log output current per Spark MAX and set up followers so that
	 * it appears as a single motor but can be an arbitary number of motors configured
	 * in the per robot configuration.
	 * 
	 * @param name the name to use for saving the PIDF values in the network tables.
     * @param canIDs list of canIDs. First entry is the leader and the rest follow it.
     * @param invert change the direction.
	 * @param mode what to do when the the speed is set to zero.
	 * @param pidf the P, I, D & F values to use.
     * @param log for registration of the current reporting.
     * @return the leader SparkMAX
     */

    private static HardwareSparkMAX getSparkMAX(String name, int[] canIDs, boolean invert, NeutralMode mode, PIDF pidf, Log log) {
		HardwareSparkMAX leader = Hardware.Motors.sparkMAX(abs(canIDs[0]), MotorType.kBrushless, invert);
		leader.setIdleMode(mode == NeutralMode.Brake ? IdleMode.kBrake : IdleMode.kCoast);
		log.register(false, () -> leader.getOutputCurrent(), "SparkMAX/%d/Current", canIDs[0]);
		leader.setSmartCurrentLimit(Constants.DEFAULT_CONTINUOUS_CURRENT_LIMIT, 10);
		leader.setSecondaryCurrentLimit(Constants.DEFAULT_PEAK_CURRENT_LIMIT, 10);
		TunableMotor.tuneMotor(leader, pidf, new NetworkTablesHelper(name));

		for (int n = 1; n < canIDs.length; n++) {
			boolean shouldInvert = invert;
			if (canIDs[n] < 0)
				shouldInvert = !shouldInvert;
			HardwareSparkMAX follower = Hardware.Motors.sparkMAX(abs(canIDs[n]), MotorType.kBrushless, shouldInvert);
			follower.getHWSpark().follow(leader.getHWSpark());
			log.register(false, () -> follower.getOutputCurrent(), "SparkMAX/%d/Current", canIDs[n]);
		}
		return leader;
	}

	/**
	 * Code to allow us to log output current for a single Spark MAX.
	 * 
	 * @param name   the name to use for saving the PIDF values in the network tables.
	 * @param canID  CAN ID for this motor controller. Must be unique.
	 * @param invert change the direction.
	 * @param mode   what to do when the the speed is set to zero.
	 * @param pidf   the P, I, D & F values to use.
	 * @param log    for registration of the current reporting.
	 * @return the HardwareSparkMAX motor controller.
	 */
	private static HardwareSparkMAX getSparkMAX(String name, int canID, boolean invert, NeutralMode mode, PIDF pidf, Log log) {
		log.sub("%s: " + canID, " spark max");
		int[] canIDs = new int[1];
		canIDs[0] = canID;
		return getSparkMAX(name, canIDs, invert, mode, pidf, log);
	}
	
	private static int abs(int value) {
		return value >= 0 ? value : -value;
	}
}
