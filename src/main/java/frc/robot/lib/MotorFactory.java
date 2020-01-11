package frc.robot.lib;

import java.util.ArrayList;

import org.strongback.components.Clock;
import org.strongback.components.SparkMAX;
import org.strongback.components.TalonSRX;
import org.strongback.hardware.Hardware;
import org.strongback.hardware.HardwareSparkMAX;
import frc.robot.Constants;
import frc.robot.interfaces.Log;

import com.ctre.phoenix.ParamEnum;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class MotorFactory {

	public static TalonSRX getDriveMotor(int[] canIDsWithEncoders, int[] canIDsWithoutEncoders, boolean leftMotor,
			boolean sensorPhase, double rampRate, boolean doCurrentLimiting, int contCurrent, int peakCurrent,
			Clock clock, Log log) {
		TalonSRX motor = getTalon(canIDsWithEncoders, canIDsWithoutEncoders, leftMotor, NeutralMode.Brake, clock, log)		// don't invert output
				.setScale(Constants.DRIVE_MOTOR_POSITION_SCALE);									// number of ticks per inch of travel.
		motor.config_kF(0, Constants.DRIVE_F, 10);
		motor.config_kP(0, Constants.DRIVE_P, 10);
		motor.config_kI(0, Constants.DRIVE_I, 10);
		motor.config_kD(0, Constants.DRIVE_D, 10);
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
		return motor;
	}
	
	public static TalonSRX getLiftMotor(int[] canIDs, boolean sensorPhase, boolean invert, Log log) {
    	TalonSRX motor = getTalon(canIDs, invert, NeutralMode.Brake, log)
				.setScale(Constants.LIFT_SCALE);
		motor.setSensorPhase(sensorPhase);
		motor.configSelectedFeedbackSensor(FeedbackDevice.Analog, 0, 10);
		// Limit switches.
		motor.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at top
		// Reverse limit switch completely stop motor from working.
		motor.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at base
		motor.configSetParameter(ParamEnum.eClearPositionOnLimitR, 0, 0x00, 0x00, 10); // This makes the lift set its height to 0 when it reaches the soft stop (hall effect)
		motor.configSetParameter(ParamEnum.eClearPositionOnLimitF, 0, 0x00, 0x00, 10);
		motor.configForwardSoftLimitThreshold(Constants.LIFT_FWD_SOFT_LIMIT, 10);
		motor.configReverseSoftLimitThreshold(Constants.LIFT_REV_SOFT_LIMIT, 10);
		motor.configForwardSoftLimitEnable(false, 10);
		motor.configReverseSoftLimitEnable(false, 10);

		motor.configContinuousCurrentLimit(Constants.LIFT_CONTINUOUS_CURRENT_LIMIT, Constants.LIFT_CURRENT_TIMEOUT_MS);
		motor.configPeakCurrentLimit(Constants.LIFT_PEAK_CURRENT_LIMIT, Constants.LIFT_CURRENT_TIMEOUT_MS);
		
		motor.config_kF(0, Constants.LIFT_F_UP, 0);
		motor.config_kP(0, Constants.LIFT_P_UP, 0);
		motor.config_kI(0, Constants.LIFT_I_UP, 0);
		motor.config_kD(0, Constants.LIFT_D_UP, 30);
		motor.config_kF(1, Constants.LIFT_F_DOWN, 0);
		motor.config_kP(1, Constants.LIFT_P_DOWN, 0);
		motor.config_kI(1, Constants.LIFT_I_DOWN, 0);
		motor.config_kD(1, Constants.LIFT_D_DOWN, 20);
		
		motor.configClosedloopRamp(0, 10);
		// Set the deadband to zero.
		motor.configAllowableClosedloopError(0, 0, 10);  // 1" = 20
		motor.configAllowableClosedloopError(1, 0, 10);
		motor.configMotionAcceleration(Constants.LIFT_MOTION_ACCEL, 10);
		motor.configMotionCruiseVelocity(Constants.LIFT_MOTION_MAX, 10);

		motor.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, 10);
		motor.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, 10);
		return motor;
	}
	
	public static TalonSRX getIntakeMotor(int canID, boolean invert, Log log) {
		TalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.configClosedloopRamp(.25, 10);
		motor.configReverseSoftLimitEnable(false, 10);
		motor.configReverseLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyClosed, 10);
		motor.configVoltageCompSaturation(8, 10);
		motor.enableVoltageCompensation(true);
		return motor;
	}

	public static SparkMAX getSparkTestMotor(int[] canIDs, boolean invert, Log log) {
		SparkMAX motor = getSparkMAX(canIDs, invert, NeutralMode.Brake, log);
		ConfigHelper config = new ConfigHelper("tunnable/sparkTest/");
		double p = config.get("p", 0.0);
		double i = config.get("i", 0.0);
		double d = config.get("d", 0.0);
		double f = config.get("f", 0.1);
		motor.setPIDF(p, i, d, f);
		return motor;
	}

	public static TalonSRX getPassthroughMotor(int canID, boolean invert, Log log) {	
		TalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.configClosedloopRamp(.25, 10);
		motor.configReverseSoftLimitEnable(false, 10);
		motor.configReverseLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyClosed, 10);
		motor.configVoltageCompSaturation(12, 10);
		motor.enableVoltageCompensation(true);
		return motor;
	}

	public static TalonSRX getSpitterMotor(int canID, boolean sensorPhase, boolean invert, Log log) {
		TalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.config_kF(0, Constants.SPITTER_SPEED_F, 10);
		motor.config_kP(0, Constants.SPITTER_SPEED_P, 10);
		motor.config_kI(0, Constants.SPITTER_SPEED_I, 10);
		motor.config_kD(0, Constants.SPITTER_SPEED_D, 10);
		motor.setSensorPhase(sensorPhase);
		// TODO: find out the configuration of this spitter motor.
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setScale(36);
		return motor;
	}

	public static TalonSRX getHatchMotor(int canID, boolean sensorPhase, boolean invert, Log log) {
		TalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.setSensorPhase(sensorPhase);
		// TODO: find out the configuration of this hatch motor.
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at top
		motor.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at base
		motor.configContinuousCurrentLimit(Constants.HATCH_CONTINUOUS_CURRENT_LIMIT, Constants.HATCH_CURRENT_TIMEOUT_MS);
		motor.configPeakCurrentLimit(Constants.HATCH_PEAK_CURRENT_LIMIT, Constants.HATCH_CURRENT_TIMEOUT_MS);
		motor.config_kF(0, Constants.HATCH_POSITION_F, 10);
		motor.config_kP(0, Constants.HATCH_POSITION_P, 10);
		motor.config_kI(0, Constants.HATCH_POSITION_I, 10);
		motor.config_kD(0, Constants.HATCH_POSITION_D, 10);
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

	public static TalonSRX getClimberWinchMotor(int canID, boolean sensorPhase, boolean invert, Log log) {
		TalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.setSensorPhase(sensorPhase);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.getSensorCollection().setQuadraturePosition(0, 10);			// reset the encoders on code start. We assume we are on the floor and the lifts are retracted on code restart.
		motor.set(ControlMode.Position, 0);
		motor.configContinuousCurrentLimit(Constants.CLIMBER_CONTINUOUS_CURRENT_LIMIT, Constants.CLIMBER_CURRENT_TIMEOUT_MS);
		motor.configPeakCurrentLimit(Constants.CLIMBER_PEAK_CURRENT_LIMIT, Constants.CLIMBER_CURRENT_TIMEOUT_MS);
		motor.config_kF(0, Constants.CLIMBER_POSITION_F, 10);
		motor.config_kP(0, Constants.CLIMBER_POSITION_P, 10);
		motor.config_kI(0, Constants.CLIMBER_POSITION_I, 10);
		motor.config_kD(0, Constants.CLIMBER_POSITION_D, 10);
		motor.configClosedloopRamp(0, 10);
		// Set the deadband to zero.
		motor.configAllowableClosedloopError(0, 0, 10);  // 1" = 20
		motor.configAllowableClosedloopError(1, 0, 10);
		return motor;
	}
	
	public static TalonSRX getClimberDriveMotor(int canID, boolean invert, Log log) {	
		TalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
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
    private static TalonSRX getTalon(int[] canIDsWithEncoders, int[] canIDsWithoutEncoders, boolean invert, NeutralMode mode, Clock clock, Log log) {
    	ArrayList<TalonSRX> potentialLeaders = getTalonList(canIDsWithEncoders, invert, mode, log);
    	ArrayList<TalonSRX> followers = getTalonList(canIDsWithoutEncoders, invert, mode, log);
    	return new RedundantTalonSRX(potentialLeaders, followers, clock, log);
	}
	
	private static ArrayList<TalonSRX> getTalonList(int[] canIDs, boolean invert, NeutralMode mode, Log log) {
		ArrayList<TalonSRX> list = new ArrayList<>();
		for (int i = 0; i < canIDs.length; i++) {
			TalonSRX talon = Hardware.TalonSRXs.talonSRX(canIDs[i], invert, mode);
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
     * @return the leader TalonSRX
     */
    private static TalonSRX getTalon(int[] canIDs, boolean invert, NeutralMode mode, Log log) {

    	TalonSRX leader = Hardware.TalonSRXs.talonSRX(abs(canIDs[0]), invert, mode);
		log.register(false, () -> leader.getOutputCurrent(), "Talons/%d/Current", canIDs[0]);
		leader.configContinuousCurrentLimit(Constants.DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT, 10);
		leader.configPeakCurrentLimit(Constants.DEFAULT_TALON_PEAK_CURRENT_LIMIT, 10);

    	for (int i = 1; i < canIDs.length; i++) {
			boolean shouldInvert = invert;
			if (canIDs[i] < 0) shouldInvert = !shouldInvert;
    		TalonSRX follower = Hardware.TalonSRXs.talonSRX(abs(canIDs[i]), shouldInvert, mode);
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
	  * @return the TalonSRX motor controller.
	  */
    private static TalonSRX getTalon(int canID, boolean invert, NeutralMode mode, Log log) {
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
    private static SparkMAX getSparkMAX(int[] canIDs, boolean invert, NeutralMode mode, Log log) {

    	HardwareSparkMAX leader = Hardware.SparkMAXs.sparkMAX(abs(canIDs[0]), MotorType.kBrushless, invert);
		log.register(false, () -> leader.getOutputCurrent(), "SparkMAX/%d/Current", canIDs[0]);
		leader.setSmartCurrentLimit(Constants.DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT, 10);
		leader.setSecondaryCurrentLimit(Constants.DEFAULT_TALON_PEAK_CURRENT_LIMIT, 10);
    	for (int i = 1; i < canIDs.length; i++) {
			boolean shouldInvert = invert;
			if (canIDs[i] < 0) shouldInvert = !shouldInvert;
    		HardwareSparkMAX follower = Hardware.SparkMAXs.sparkMAX(abs(canIDs[i]), MotorType.kBrushless, shouldInvert);
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
