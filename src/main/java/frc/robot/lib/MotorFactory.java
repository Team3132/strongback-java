package frc.robot.lib;

import java.util.ArrayList;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import org.strongback.components.Clock;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.hardware.Hardware;
import org.strongback.hardware.HardwareSparkMAX;
import org.strongback.hardware.HardwareTalonSRX;

import frc.robot.Constants;
import frc.robot.interfaces.Log;

import com.ctre.phoenix.ParamEnum;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class MotorFactory {

	public static Motor getDriveMotor(String motorControllerType, int[] canIDsWithEncoders, int[] canIDsWithoutEncoders,
			boolean leftMotor, boolean sensorPhase, double rampRate, boolean doCurrentLimiting, int contCurrent,
			int peakCurrent, double p, double i, double d, double f, Clock clock, Log log) {

		switch (motorControllerType) {
		case Constants.MOTOR_CONTROLLER_TYPE_SPARKMAX: {
			HardwareSparkMAX spark = getSparkMAX(canIDsWithEncoders, leftMotor, NeutralMode.Brake, log);
			spark.setScale(Constants.DRIVE_MOTOR_POSITION_SCALE);
			spark.setPIDF(0, p, i, d, f);
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
			HardwareTalonSRX talon = getTalon(canIDsWithEncoders, canIDsWithoutEncoders, leftMotor, NeutralMode.Brake,
					clock, log); // don't invert output
			talon.setScale(Constants.DRIVE_MOTOR_POSITION_SCALE); // number of ticks per inch of travel.
			talon.setPIDF(0, p, i, d, f);
			talon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
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
	
	public static HardwareTalonSRX getIntakeMotor(int canID, boolean invert, Log log) {
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.configClosedloopRamp(.25, 10);
		motor.configReverseSoftLimitEnable(false, 10);
		motor.configReverseLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyClosed, 10);
		motor.configVoltageCompSaturation(8, 10);
		motor.enableVoltageCompensation(true);
		return motor;
	}

	public static HardwareTalonSRX getColourWheelMotor(int canID, boolean invert, Log log) {
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.configClosedloopRamp(.25, 10);
		return motor;
	}

	public static HardwareTalonSRX getLoaderSpinnerMotor(int canID, boolean invert, double p, double i, double d, double f,Log log) {	
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.setPIDF(0, p, i, d, f);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setScale(Constants.LOADER_MAIN_MOTOR_SCALE); // number of ticks per rotation.
		motor.configClosedloopRamp(0, 10);
		NetworkTablesHelper helper = new NetworkTablesHelper("loader/spinnermotor/");
		helper.set("p", p);
		helper.set("i", i);
		helper.set("d", d);
		helper.set("f", f);
		return motor;
	}
	public static HardwareTalonSRX getLoaderPassthroughMotor(int canID, boolean invert, double p, double i, double d, double f, Log log) {	
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.setPIDF(0, p, i, d, f);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setScale(Constants.LOADER_IN_MOTOR_SCALE); // number of ticks per rotation
		motor.configClosedloopRamp(0.5, 10);
		NetworkTablesHelper helper = new NetworkTablesHelper("loader/passthroughmotor/");
		helper.set("p", p);
		helper.set("i", i);
		helper.set("d", d);
		helper.set("f", f);
		return motor;
	}
	public static HardwareTalonSRX getLoaderFeederMotor(int canID, boolean invert, Log log) {	
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.configClosedloopRamp(0.5, 10);
		return motor;
	}
//sort stuff out here for motor types
	public static HardwareSparkMAX getClimberWinchMotor(int canID, boolean invert, Log log) {
		HardwareSparkMAX motor = getSparkMAX(canID, invert, NeutralMode.Brake, log);
		motor.set(ControlMode.Position, 0);
		motor.setSmartCurrentLimit(Constants.CLIMBER_PEAK_CURRENT_LIMIT, 100);
		motor.setScale(1.0 / Constants.CLIMBER_GEAR_RATIO
				* Constants.CLIMBER_DRUM_CIRCUMFRENCE_METRES);
		motor.setPIDF(0, Constants.CLIMBER_P, Constants.CLIMBER_I, Constants.CLIMBER_D, Constants.CLIMBER_F);
		return motor;
	}

	/**
     * Code to allow us to log output current per talon using redundant talons so if a talon or encoder
     * fails, it will automatically log and switch to the next one.
     * @param canIDsWithEncoders list of talons that can be the leader due to having an encoder.
     * @param canIDsWithoutEncoders list of talons without encoders that can never be the leader.
     * @param invert reverse the direction of the output.
     * @param log logger.
     * @return
     */
    private static HardwareTalonSRX getTalon(int[] canIDsWithEncoders, int[] canIDsWithoutEncoders, boolean invert, NeutralMode mode, Clock clock, Log log) {
    	ArrayList<HardwareTalonSRX> potentialLeaders = getTalonList(canIDsWithEncoders, invert, mode, log);
    	ArrayList<HardwareTalonSRX> followers = getTalonList(canIDsWithoutEncoders, invert, mode, log);
    	return new RedundantTalonSRX(potentialLeaders, followers, clock, log);
	}
	
	private static ArrayList<HardwareTalonSRX> getTalonList(int[] canIDs, boolean invert, NeutralMode mode, Log log) {
		ArrayList<HardwareTalonSRX> list = new ArrayList<>();
		for (int i = 0; i < canIDs.length; i++) {
			HardwareTalonSRX talon = Hardware.Motors.talonSRX(canIDs[i], invert, mode);
			talon.configContinuousCurrentLimit(Constants.DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT, 10);
			talon.configPeakCurrentLimit(Constants.DEFAULT_TALON_PEAK_CURRENT_LIMIT, 10);
			list.add(talon);
		}
		return list;
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
    private static HardwareTalonSRX getTalon(int[] canIDs, boolean invert, NeutralMode mode, Log log) {

    	HardwareTalonSRX leader = Hardware.Motors.talonSRX(abs(canIDs[0]), invert, mode);
		log.register(false, () -> leader.getOutputCurrent(), "Talons/%d/Current", canIDs[0]);
		leader.configContinuousCurrentLimit(Constants.DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT, 10);
		leader.configPeakCurrentLimit(Constants.DEFAULT_TALON_PEAK_CURRENT_LIMIT, 10);

    	for (int i = 1; i < canIDs.length; i++) {
			boolean shouldInvert = invert;
			if (canIDs[i] < 0) shouldInvert = !shouldInvert;
    		HardwareTalonSRX follower = Hardware.Motors.talonSRX(abs(canIDs[i]), shouldInvert, mode);
			follower.getHWTalon().follow(leader.getHWTalon());
			log.register(false, () -> follower.getOutputCurrent(), "Talons/%d/Current", canIDs[i]);
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
    private static HardwareTalonSRX getTalon(int canID, boolean invert, NeutralMode mode, Log log) {
		log.sub("%s: " + canID, "talon");
		int[] canIDs = new int[1];
		canIDs[0] = canID;
    	return getTalon(canIDs, invert, mode, log);
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
    private static HardwareSparkMAX getSparkMAX(int[] canIDs, boolean invert, NeutralMode mode, Log log) {
		HardwareSparkMAX leader = Hardware.Motors.sparkMAX(abs(canIDs[0]), MotorType.kBrushless, invert);
		leader.setIdleMode(mode == NeutralMode.Brake ? IdleMode.kBrake : IdleMode.kCoast);
		log.register(false, () -> leader.getOutputCurrent(), "SparkMAX/%d/Current", canIDs[0]);
		leader.setSmartCurrentLimit(Constants.DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT, 10);
		leader.setSecondaryCurrentLimit(Constants.DEFAULT_TALON_PEAK_CURRENT_LIMIT, 10);
    	for (int i = 1; i < canIDs.length; i++) {
			boolean shouldInvert = invert;
			if (canIDs[i] < 0) shouldInvert = !shouldInvert;
    		HardwareSparkMAX follower = Hardware.Motors.sparkMAX(abs(canIDs[i]), MotorType.kBrushless, shouldInvert);
			follower.getHWSpark().follow(leader.getHWSpark());
			log.register(false, () -> follower.getOutputCurrent(), "SparkMAX/%d/Current", canIDs[i]);
		}
		// Reset the scale. Normally velocity is in rpm when normally we'd like rps,
		// which is easier to reason about. setScale() takes care of the converstion
		// from rpm to rps for us.
		leader.setScale(1);
		return leader;
	}

    private static HardwareSparkMAX getSparkMAX(int canID, boolean invert, NeutralMode mode, Log log) {
		log.sub("%s: " + canID, " spark max");
		int[] canIDs = new int[1];
		canIDs[0] = canID;
    	return getSparkMAX(canIDs, invert, mode, log);
	}
	
	private static int abs(int value) {
		return value >= 0 ? value : -value;
	}
}
