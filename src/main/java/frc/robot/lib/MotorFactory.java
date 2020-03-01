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

	public static Motor getDriveMotor(String motorControllerType, int[] drivebaseCanIdsLeftWithEncoders, boolean leftMotor, 
			boolean sensorPhase, double rampRate, boolean doCurrentLimiting, int contCurrent,
			int peakCurrent, double p, double i, double d, double f, Clock clock, Log log) {

		switch (motorControllerType) {
		case Constants.MOTOR_CONTROLLER_TYPE_SPARKMAX: {
			HardwareSparkMAX spark = getSparkMAX(drivebaseCanIdsLeftWithEncoders, leftMotor, NeutralMode.Brake, log, p, i, d, f, new NetworkTablesHelper("drive"));
			spark.setScale(Constants.DRIVE_MOTOR_POSITION_SCALE);
			spark.setSensorPhase(sensorPhase);

			/*
			 * Setup Current Limiting
			 */
			if (doCurrentLimiting) {
				spark.setSmartCurrentLimit(contCurrent, contCurrent); // limit to 35 Amps when current exceeds 40 amps
																		// for 100ms
				spark.setSecondaryCurrentLimit(peakCurrent);
			}

			return spark;
		}

		default:
			log.error("Invalid drive motor controller '%s'. Please use 'TalonSRX' or 'SparkMAX'. Using TalonSRX.", motorControllerType);
			// Falling through to TalonSRX.

		case Constants.MOTOR_CONTROLLER_TYPE_TALONSRX:
			HardwareTalonSRX talon = getTalon(drivebaseCanIdsLeftWithEncoders, leftMotor, NeutralMode.Brake, log, p, i, d, f,
					new NetworkTablesHelper("drive")); // don't invert output
			talon.setScale(Constants.DRIVE_MOTOR_POSITION_SCALE); // number of ticks per inch of travel.
			talon.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor, 0, 10);
			talon.setSensorPhase(sensorPhase);
			talon.configClosedloopRamp(rampRate, 10);
			talon.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 10, 10);

			/*
			 * Setup Current Limiting
			 */
			if (doCurrentLimiting) {
				talon.configContinuousCurrentLimit(contCurrent, 0); // limit to 35 Amps when current exceeds 40 amps for
																	// 100ms
				talon.configPeakCurrentLimit(peakCurrent, 0);
				talon.configPeakCurrentDuration(100, 0);
				talon.enableCurrentLimit(true);
			}
			return talon;
		}
	}
	
	public static HardwareSparkMAX getIntakeMotor(int canID, boolean invert, double p, double i, double d, double f, Log log) {
		HardwareSparkMAX motor = getSparkMAX(canID, invert, NeutralMode.Brake, log, p , i, d, f, new NetworkTablesHelper("intake"));
		return motor;
	}

	public static HardwareTalonSRX getColourWheelMotor(int canID, boolean invert, double p, double i, double d, double f,Log log) {
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log, p, i, d, f, new NetworkTablesHelper("colour"));
		motor.configClosedloopRamp(.25, 10);
		return motor;
	}

	public static HardwareTalonSRX getLoaderSpinnerMotor(int canID, boolean invert, double p, double i, double d, double f,Log log) {	
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log,  p,  i,  d,  f, new NetworkTablesHelper("loader spinner"));
		// In sensor (beambreak) for loader
		motor.configForwardLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyOpen, 10);
		// Out sensor (beambreak) for loader
		motor.configReverseLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyOpen, 10);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setScale(Constants.LOADER_MAIN_MOTOR_SCALE); // number of ticks per rotation.
		motor.configClosedloopRamp(0, 10);
		return motor;
	}
	public static HardwareSparkMAX getLoaderPassthroughMotor(int canID, boolean invert,double p, double i, double d, double f, Log log) {
		HardwareSparkMAX motor = getSparkMAX(canID, invert, NeutralMode.Brake, log, p, i, d, f, new NetworkTablesHelper("loader passthrough"));
		return motor;
	}
	
	public static HardwareTalonSRX getShooterMotor(int[] canIDs, boolean sensorPhase, 
			double p, double i, double d, double f,	Clock clock, Log log) {
		HardwareTalonSRX motor = getTalon(canIDs, false, NeutralMode.Coast, log, p, i, d, f,
				new NetworkTablesHelper("shooter")); // don't invert output
		motor.setSensorPhase(sensorPhase);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setScale(36);
		
		motor.configClosedloopRamp(0.125, 10);
		motor.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 10, 10);

		return motor;
	}
    		
    /**
     * Code to allow us to log output current per Spark MAX and set up followers so that
	 * it appears as a single motor but can be an arbitary number of motors configured
	 * in the per robot configuration.
     * @param canIDs list of canIDs. First entry is the leader and the rest follow it.
     * @param invert change the direction.
     * @param log for registration of the current reporting.
     * @return the leader HardwareTalonSRX
     */
	private static HardwareTalonSRX getTalon(int[] canIDs, boolean invert, NeutralMode mode, Log log, 
			double p , double i, double d, double f, NetworkTablesHelper networkTable) {
    	HardwareTalonSRX leader = Hardware.Motors.talonSRX(abs(canIDs[0]), invert, mode);
		log.register(false, () -> leader.getOutputCurrent(), "Talons/%d/Current", canIDs[0]);
		leader.configContinuousCurrentLimit(Constants.DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT, 10);
		leader.configPeakCurrentLimit(Constants.DEFAULT_TALON_PEAK_CURRENT_LIMIT, 10);
		TunableMotor.tuneMotor(leader, abs(canIDs[0]), p, i, d, f, networkTable);

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
	  * @param canID CAN ID for this motor controller. Must be unique.
	  * @param invert Change the direction of the motor.
	  * @param mode break or coast.
	  * @param log for registration of the current values.
	  * @return the HardwareTalonSRX motor controller.
	  */

	private static HardwareTalonSRX getTalon(int canID, boolean invert, NeutralMode mode, Log log, 
			double p, double i, double d, double f, NetworkTablesHelper networkTable) {
		log.sub("%s: " + canID, "talon");
		int[] canIDs = new int[1];
		canIDs[0] = canID;
    	return getTalon(canIDs, invert, mode, log, p, i, d, f, networkTable);
	}

	/**
     * Code to allow us to log output current per Spark MAX and set up followers so that
	 * it appears as a single motor but can be an arbitary number of motors configured
	 * in the per robot configuration.
     * @param canIDs list of canIDs. First entry is the leader and the rest follow it.
     * @param invert change the direction.
     * @param log for registration of the current reporting.
     * @return the leader SparkMAX
     */

    private static HardwareSparkMAX getSparkMAX(int[] canIDs, boolean invert, NeutralMode mode, Log log, double p, double i, double d, double f, NetworkTablesHelper networkTable) {
		HardwareSparkMAX leader = Hardware.Motors.sparkMAX(abs(canIDs[0]), MotorType.kBrushless, invert);
		leader.setIdleMode(mode == NeutralMode.Brake ? IdleMode.kBrake : IdleMode.kCoast);
		log.register(false, () -> leader.getOutputCurrent(), "SparkMAX/%d/Current", canIDs[0]);
		leader.setSmartCurrentLimit(Constants.DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT, 10);
		leader.setSecondaryCurrentLimit(Constants.DEFAULT_TALON_PEAK_CURRENT_LIMIT, 10);
		TunableMotor.tuneMotor(leader, abs(canIDs[0]), p, i, d, f, networkTable);

		for (int n = 1; n < canIDs.length; n++) {
			boolean shouldInvert = invert;
			if (canIDs[n] < 0)
				shouldInvert = !shouldInvert;
			HardwareSparkMAX follower = Hardware.Motors.sparkMAX(abs(canIDs[n]), MotorType.kBrushless, shouldInvert);
			follower.getHWSpark().follow(leader.getHWSpark());
			log.register(false, () -> follower.getOutputCurrent(), "SparkMAX/%d/Current", canIDs[n]);
		}
		// Reset the scale. Normally velocity is in rpm when normally we'd like rps,
		// which is easier to reason about. setScale() takes care of the converstion
		// from rpm to rps for us.
		leader.setScale(1);
		return leader;
	}

   	private static HardwareSparkMAX getSparkMAX(int canID, boolean invert, NeutralMode mode, Log log, double p, double i, double d, double f, NetworkTablesHelper networkTable) {
		log.sub("%s: " + canID, " spark max");
		int[] canIDs = new int[1];
		canIDs[0] = canID;
		return getSparkMAX(canIDs, invert, mode, log, p, i, d, f, networkTable);
	}
	
	private static int abs(int value) {
		return value >= 0 ? value : -value;
	}
}
