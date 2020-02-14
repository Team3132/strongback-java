package frc.robot.lib;

import java.util.ArrayList;

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
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class MotorFactory {

	public static Motor getDriveMotor(int[] canIDsWithEncoders, int[] canIDsWithoutEncoders, boolean leftMotor,
			boolean sensorPhase, double rampRate, boolean doCurrentLimiting, int contCurrent, int peakCurrent,
			double p, double i, double d, double f, Clock clock, Log log) {
		HardwareTalonSRX motor = getTalon(canIDsWithEncoders, canIDsWithoutEncoders, leftMotor, NeutralMode.Brake, clock, log); // don't invert output
	    motor.setScale(Constants.DRIVE_MOTOR_POSITION_SCALE); // number of ticks per inch of travel.
		motor.setPIDF(0, p, i, d, f);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setSensorPhase(sensorPhase);
		motor.configClosedloopRamp(rampRate, 10);
		motor.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 10, 10);
		/*
		 * Setup Current Limiting
		 */
		if (doCurrentLimiting) {
			motor.configContinuousCurrentLimit(contCurrent, 0);		// limit to 35 Amps when current exceeds 40 amps for 100ms
			motor.configPeakCurrentLimit(peakCurrent, 0);
			motor.configPeakCurrentDuration(100, 0);
			motor.enableCurrentLimit(true);
		}
		// Save PID values into Network Tables
		NetworkTablesHelper helper = new NetworkTablesHelper("drive");
		helper.set("p", p);
		helper.set("i", i);
		helper.set("d", d);
		helper.set("f", f);
		return motor;
	}
	
	public static HardwareTalonSRX getLiftMotor(int[] canIDs, boolean sensorPhase, boolean invert, Log log) {
    	HardwareTalonSRX motor = getTalon(canIDs, invert, NeutralMode.Brake, log);
		motor.setSensorPhase(sensorPhase);
		motor.configSelectedFeedbackSensor(FeedbackDevice.Analog, 0, 10);
		// Limit switches.
		motor.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at top
		// Reverse limit switch completely stop motor from working.
		motor.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at base
		motor.configSetParameter(ParamEnum.eClearPositionOnLimitR, 0, 0x00, 0x00, 10); // This makes the lift set its height to 0 when it reaches the soft stop (hall effect)
		motor.configSetParameter(ParamEnum.eClearPositionOnLimitF, 0, 0x00, 0x00, 10);
		motor.configForwardSoftLimitEnable(false, 10);
		motor.configReverseSoftLimitEnable(false, 10);

		motor.configClosedloopRamp(0, 10);
		// Set the deadband to zero.
		motor.configAllowableClosedloopError(0, 0, 10);  // 1" = 20
		motor.configAllowableClosedloopError(1, 0, 10);
		
		motor.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, 10);
		motor.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, 10);
		return motor;
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

	public static HardwareSparkMAX getSparkTestMotor(int[] canIDs, boolean invert, Log log) {
		HardwareSparkMAX motor = getSparkMAX(canIDs, invert, NeutralMode.Brake, log);
		NetworkTablesHelper config = new NetworkTablesHelper("tunnable/sparkTest/");
		double p = config.get("p", 0.0);
		double i = config.get("i", 0.0);
		double d = config.get("d", 0.0);
		double f = config.get("f", 0.1);
		motor.setPIDF(0, p, i, d, f);
		return motor;
	}

	public static HardwareTalonSRX getPassthroughMotor(int canID, boolean invert, Log log) {	
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.configClosedloopRamp(.25, 10);
		motor.configReverseSoftLimitEnable(false, 10);
		motor.configReverseLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyClosed, 10);
		motor.configVoltageCompSaturation(12, 10);
		motor.enableVoltageCompensation(true);
		return motor;
	}

	public static HardwareTalonSRX getSpitterMotor(int canID, boolean sensorPhase, boolean invert, Log log) {
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.setPIDF(0, Constants.SPITTER_SPEED_P, Constants.SPITTER_SPEED_I, Constants.SPITTER_SPEED_D, Constants.SPITTER_SPEED_F);
		motor.setSensorPhase(sensorPhase);
		// TODO: find out the configuration of this spitter motor.
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setScale(36);
		return motor;
	}

	public static HardwareTalonSRX getHatchMotor(int canID, boolean sensorPhase, boolean invert, Log log) {
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.setSensorPhase(sensorPhase);
		// TODO: find out the configuration of this hatch motor.
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at top
		motor.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at base
		motor.configClosedloopRamp(0.1, 10);
		double scaleFactor = 2456/13.15; // 13.15" for 2456 ticks
		motor.setScale(scaleFactor);
		// Set the deadband to zero.
		motor.configAllowableClosedloopError(0, 0, 10);  // 1" = 20
		motor.configAllowableClosedloopError(1, 0, 10);
		motor.configSetParameter(ParamEnum.eClearPositionOnLimitR, 1, 0x00, 0x00, 10); // This makes the hatch set its position to 0 when it reaches the soft stop (hall effect)
		motor.configSetParameter(ParamEnum.eClearPositionOnLimitF, 0, 0x00, 0x00, 10);

		return motor;
	}

	public static HardwareTalonSRX getClimberWinchMotor(int canID, boolean sensorPhase, boolean invert, Log log) {
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.setSensorPhase(sensorPhase);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.getSensorCollection().setQuadraturePosition(0, 10);			// reset the encoders on code start. We assume we are on the floor and the lifts are retracted on code restart.
		motor.set(ControlMode.Position, 0);
		motor.configContinuousCurrentLimit(Constants.CLIMBER_CONTINUOUS_CURRENT_LIMIT, Constants.CLIMBER_CURRENT_TIMEOUT_MS);
		motor.configPeakCurrentLimit(Constants.CLIMBER_PEAK_CURRENT_LIMIT, Constants.CLIMBER_CURRENT_TIMEOUT_MS);
		motor.setPIDF(0, Constants.CLIMBER_POSITION_P, Constants.CLIMBER_POSITION_I, Constants.CLIMBER_POSITION_D, Constants.CLIMBER_POSITION_F);
		motor.configClosedloopRamp(0, 10);
		// Set the deadband to zero.
		motor.configAllowableClosedloopError(0, 0, 10);  // 1" = 20
		motor.configAllowableClosedloopError(1, 0, 10);
		return motor;
	}
	
	public static HardwareTalonSRX getClimberDriveMotor(int canID, boolean invert, Log log) {	
		HardwareTalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.configClosedloopRamp(.25, 10);
		motor.configVoltageCompSaturation(8, 10);
		motor.enableVoltageCompensation(true);
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

	
	private static int abs(int value) {
		return value >= 0 ? value : -value;
	}
}
