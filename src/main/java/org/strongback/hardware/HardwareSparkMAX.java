package org.strongback.hardware;

import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANError;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.FaultID;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMax.SoftLimitDirection;

import org.strongback.components.Motor;

/*
 * Package to wrap a Spark MAX controller driving a Neo.
 * 
 * This is the hardware interface class that implements the interface we use.
 * 
 * This currently only supports the built in hall effect encoder.
 * 
 * We have a scale factor. This is useful in position and velocity close loop feedback modes.
 * 	For reading it we divide by the scale factor, when writing values we multiply by the scale factor.
 */
public class HardwareSparkMAX implements Motor {
	private final com.revrobotics.CANSparkMax spark;
	private final com.revrobotics.CANEncoder encoder;
	private int slotID = 0;
	// There appears to be a bug where if the pid controller is created before
	// any followers, then the followers won't follow. Hence the pid controller
	// is created on demand in getPID() in case there have been followers added.
	// Note that set(...) works no matter what, it's just pid.setReference(...,...)
	// that doesn't.
	private com.revrobotics.CANPIDController pid;
	private final com.revrobotics.CANDigitalInput fwdLimitSwitch;
	private final com.revrobotics.CANDigitalInput revLimitSwitch;
	
	public HardwareSparkMAX(com.revrobotics.CANSparkMax spark) {
		this.spark = spark;
		setScale(1, 1);  // Change from rpm to rps.
		// Assume built in hall effect sensor.
		encoder = spark.getEncoder();
		fwdLimitSwitch = spark.getForwardLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen);
		revLimitSwitch = spark.getReverseLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen);
	}

	@Override
	public void set(ControlMode mode, double value) {
		//spark.set(value);
		getPID().setReference(value, mode.revControlType, slotID);
	}

	private CANPIDController getPID() {
		if (pid == null) {
			// Work around a bug where followers don't work if they
			// are added after the pid controller is created.
			pid = spark.getPIDController();
		}
		return pid;
	}

	@Override
	public double get() {
		return spark.get();
	}

	@Override
	public Motor setInverted(boolean isInverted) {
		// Not available in brushless mode.
		// spark.setInverted(isInverted);
		return this;
	}

	@Override
	public boolean getInverted() {
		return spark.getInverted();
	}

	@Override
	public Motor disable() {
		spark.disable();
		return this;
	}

	@Override
	public void stop() {
		spark.stopMotor();
	}

	@Override
	public Motor setPIDF(int slotIdx, double p, double i, double d, double f) {
		getPID().setP(p, slotIdx);
		getPID().setI(i, slotIdx);
		getPID().setD(d, slotIdx);
		getPID().setFF(f, slotIdx);
		return this;
	}

	public Motor selectProfileSlot(int slotIdx) {
		// Only used when set() is called.
        slotID = slotIdx;
        return this;
    }

	@Override
	public double getPosition() {
		return encoder.getPosition();
	}

	@Override
	public Motor setPosition(double position) {
		encoder.setPosition(position);
		return this;
	}

	@Override
	public double getVelocity() {
		// CANEncoder returns RPM by default. This has been scaled to divide by 60.
		return encoder.getVelocity();
	}

	/**
     * Scale the values to/from the motors into more intuitive values.
     * 
     * getPosition() returns the number of metres.
     * getVelocity() returns metres/second.
     * 
     * Also consider setScale(double ticksPerTurn, double gearRatio).
     * 
     * @param ticksPerTurn Not used for the HardwareSparkMAX with a builtin encoder.
     * @param gearRatio How many turns of the motor to turn the output shaft, eg 11
     * @param wheelDiameterMetres How many metres does the wheel move for every turn.
     * @return this.
	 */
	@Override
    public Motor setScale(double ticksPerTurn, double gearRatio, double wheelDiameter) {
		if (ticksPerTurn * gearRatio * wheelDiameter == 0) {
			throw new RuntimeException("WARNING: HardwareSparkMAX::setScale() was passed zero, this isn't what you want!");
		}
		// Encoder.getVelocity() returns RPM by default, so this needs to be converted to RPS.
		encoder.setVelocityConversionFactor(wheelDiameter / gearRatio / 60);
		// Encoder getPosition() returns rotations by default.
		encoder.setPositionConversionFactor(wheelDiameter / gearRatio);
		return this;
	}

	@Override
	public boolean isAtForwardLimit() {
		return fwdLimitSwitch.get();
	}

	@Override
	public boolean isAtReverseLimit() {
		return revLimitSwitch.get();
	}

	public boolean setSmartCurrentLimit(int limit) {
		return spark.setSmartCurrentLimit(limit) == CANError.kOk;
	}

	public boolean setSmartCurrentLimit(int stallLimit, int freeLimit) {
		return spark.setSmartCurrentLimit(stallLimit, freeLimit) == CANError.kOk;
	}

	public boolean setSmartCurrentLimit(int stallLimit, int freeLimit, int limitRPM) {
		return spark.setSmartCurrentLimit(stallLimit, freeLimit, limitRPM) == CANError.kOk;
	}

	public boolean setSecondaryCurrentLimit(double limit) {
		return spark.setSecondaryCurrentLimit(limit) == CANError.kOk;
	}

	public boolean setSecondaryCurrentLimit(double limit, int chopCycles) {
		return spark.setSecondaryCurrentLimit(limit, chopCycles) == CANError.kOk;
	}

	public boolean setIdleMode(IdleMode mode) {
		return spark.setIdleMode(mode) == CANError.kOk;
	}

	public IdleMode getIdleMode() {
		return spark.getIdleMode();
	}

	public boolean enableVoltageCompensation(double nominalVoltage) {
		return spark.enableVoltageCompensation(nominalVoltage) == CANError.kOk;
	}

	public boolean disableVoltageCompensation() {
		return spark.disableVoltageCompensation() == CANError.kOk;
	}

	public double getVoltageCompensationNominalVoltage() {
		return spark.getVoltageCompensationNominalVoltage();
	}

	public boolean setOpenLoopRampRate(double rate) {
		return spark.setOpenLoopRampRate(rate) == CANError.kOk;
	}

	public boolean setClosedLoopRampRate(double rate) {
		return spark.setClosedLoopRampRate(rate) == CANError.kOk;
	}

	public double getOpenLoopRampRate() {
		return spark.getOpenLoopRampRate();
	}

	public double getClosedLoopRampRate() {
		return spark.getClosedLoopRampRate();
	}

	public boolean follow(HardwareSparkMAX leader) {
		return spark.follow(leader.getHWSpark()) == CANError.kOk;
	}

	public boolean follow(HardwareSparkMAX leader, boolean invert) {
		return spark.follow(leader.getHWSpark(), invert) == CANError.kOk;
	}

	public boolean isFollower() {
		return spark.isFollower();
	}

	public short getFaults() {
		return spark.getFaults();
	}

	public short getStickyFaults() {
		return spark.getStickyFaults();
	}

	public boolean getFault(FaultID faultID) {
		return spark.getFault(faultID);
	}

	public boolean getStickyFault(FaultID faultID) {
		return spark.getStickyFault(faultID);
	}

	@Override
	public double getBusVoltage() {
		return spark.getBusVoltage();
	}

	@Override
	public double getOutputPercent() {
		return spark.getAppliedOutput();
	}

	@Override
	public double getOutputCurrent() {
		return spark.getOutputCurrent();
	}

	@Override
	public double getTemperature() {
		return spark.getMotorTemperature();
	}

	@Override
    public Motor setSensorPhase(boolean phase) {
		// In brushless mode, this doesn't make sense.
		// Invert the motor instead.
        return this;
    }


	public boolean clearFaults() {
		return spark.clearFaults() == CANError.kOk;
	}

	public boolean burnFlash() {
		return spark.burnFlash() == CANError.kOk;
	}

	public boolean setCANTimeout(int milliseconds) {
		return spark.setCANTimeout(milliseconds) == CANError.kOk;
	}

	public boolean enableSoftLimit(SoftLimitDirection direction, boolean enable) {
		return spark.enableSoftLimit(direction, enable) == CANError.kOk;
	}

	public boolean setSoftLimit(SoftLimitDirection direction, float limit) {
		return spark.setSoftLimit(direction, limit) == CANError.kOk;
	}

	public double getSoftLimit(SoftLimitDirection direction) {
		return spark.getSoftLimit(direction);
	}

	public boolean isSoftLimitEnabled(SoftLimitDirection direction) {
		return spark.isSoftLimitEnabled(direction);
	}

	public CANSparkMax getHWSpark() {
		return spark;
	}
}
