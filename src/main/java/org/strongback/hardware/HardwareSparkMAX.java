package org.strongback.hardware;

import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANError;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.FaultID;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMax.SoftLimitDirection;

import org.strongback.components.SparkMAX;

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
public class HardwareSparkMAX implements SparkMAX {
	private final com.revrobotics.CANSparkMax spark;
	private final com.revrobotics.CANEncoder encoder;
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
		// Assume built in hall effect sensor.
		encoder = spark.getEncoder();
		fwdLimitSwitch = spark.getForwardLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen);
		revLimitSwitch = spark.getReverseLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen);
	}

	@Override
	public void set(double value, ControlType mode) {
		//spark.set(value);
		getPID().setReference(value, mode);
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
	public void setInverted(boolean isInverted) {
		spark.setInverted(isInverted);
	}

	@Override
	public boolean getInverted() {
		return spark.getInverted();
	}

	@Override
	public void disable() {
		spark.disable();
	}

	@Override
	public void stopMotor() {
		spark.stopMotor();
	}

	@Override
	public void pidWrite(double output) {
		spark.pidWrite(output);
	}

	@Override
	public void setPIDF(double p, double i, double d, double f) {
		getPID().setP(p);
		getPID().setI(i);
		getPID().setD(d);
		getPID().setFF(f);
	}

	@Override
	public double getPosition() {
		return encoder.getPosition();
	}

	@Override
	public void setPosition(double position) {
		encoder.setPosition(position);
	}

	@Override
	public double getVelocity() {
		return encoder.getVelocity();
	}

	@Override
	public SparkMAX setScale(double scale) {
		// Position is in turns.
		encoder.setPositionConversionFactor(scale);
		// Velocity is in rpm by default. Use rps instead.
		encoder.setVelocityConversionFactor(scale / 60);
		return this;
	}

	@Override
	public boolean isFwdLimitSwitchClosed() {
		return fwdLimitSwitch.get();
	}

	@Override
	public boolean isRevLimitSwitchClosed() {
		return revLimitSwitch.get();
	}

	@Override
	public boolean setSmartCurrentLimit(int limit) {
		return spark.setSmartCurrentLimit(limit) == CANError.kOk;
	}

	@Override
	public boolean setSmartCurrentLimit(int stallLimit, int freeLimit) {
		return spark.setSmartCurrentLimit(stallLimit, freeLimit) == CANError.kOk;
	}

	@Override
	public boolean setSmartCurrentLimit(int stallLimit, int freeLimit, int limitRPM) {
		return spark.setSmartCurrentLimit(stallLimit, freeLimit, limitRPM) == CANError.kOk;
	}

	@Override
	public boolean setSecondaryCurrentLimit(double limit) {
		return spark.setSecondaryCurrentLimit(limit) == CANError.kOk;
	}

	@Override
	public boolean setSecondaryCurrentLimit(double limit, int chopCycles) {
		return spark.setSecondaryCurrentLimit(limit, chopCycles) == CANError.kOk;
	}

	@Override
	public boolean setIdleMode(IdleMode mode) {
		return spark.setIdleMode(mode) == CANError.kOk;
	}

	@Override
	public IdleMode getIdleMode() {
		return spark.getIdleMode();
	}

	@Override
	public boolean enableVoltageCompensation(double nominalVoltage) {
		return spark.enableVoltageCompensation(nominalVoltage) == CANError.kOk;
	}

	@Override
	public boolean disableVoltageCompensation() {
		return spark.disableVoltageCompensation() == CANError.kOk;
	}

	@Override
	public double getVoltageCompensationNominalVoltage() {
		return spark.getVoltageCompensationNominalVoltage();
	}

	@Override
	public boolean setOpenLoopRampRate(double rate) {
		return spark.setOpenLoopRampRate(rate) == CANError.kOk;
	}

	@Override
	public boolean setClosedLoopRampRate(double rate) {
		return spark.setClosedLoopRampRate(rate) == CANError.kOk;
	}

	@Override
	public double getOpenLoopRampRate() {
		return spark.getOpenLoopRampRate();
	}

	@Override
	public double getClosedLoopRampRate() {
		return spark.getClosedLoopRampRate();
	}

	// Next two aren't part of the SparkMAX interface.
	public boolean follow(HardwareSparkMAX leader) {
		return spark.follow(leader.getHWSpark()) == CANError.kOk;
	}

	public boolean follow(HardwareSparkMAX leader, boolean invert) {
		return spark.follow(leader.getHWSpark(), invert) == CANError.kOk;
	}

	@Override
	public boolean isFollower() {
		return spark.isFollower();
	}

	@Override
	public short getFaults() {
		return spark.getFaults();
	}

	@Override
	public short getStickyFaults() {
		return spark.getStickyFaults();
	}

	@Override
	public boolean getFault(FaultID faultID) {
		return spark.getFault(faultID);
	}

	@Override
	public boolean getStickyFault(FaultID faultID) {
		return spark.getStickyFault(faultID);
	}

	@Override
	public double getBusVoltage() {
		return spark.getBusVoltage();
	}

	@Override
	public double getAppliedOutput() {
		return spark.getAppliedOutput();
	}

	@Override
	public double getOutputCurrent() {
		return spark.getOutputCurrent();
	}

	@Override
	public double getMotorTemperature() {
		return spark.getMotorTemperature();
	}

	@Override
	public boolean clearFaults() {
		return spark.clearFaults() == CANError.kOk;
	}

	@Override
	public boolean burnFlash() {
		return spark.burnFlash() == CANError.kOk;
	}

	@Override
	public boolean setCANTimeout(int milliseconds) {
		return spark.setCANTimeout(milliseconds) == CANError.kOk;
	}

	@Override
	public boolean enableSoftLimit(SoftLimitDirection direction, boolean enable) {
		return spark.enableSoftLimit(direction, enable) == CANError.kOk;
	}

	@Override
	public boolean setSoftLimit(SoftLimitDirection direction, float limit) {
		return spark.setSoftLimit(direction, limit) == CANError.kOk;
	}

	@Override
	public double getSoftLimit(SoftLimitDirection direction) {
		return spark.getSoftLimit(direction);
	}

	@Override
	public boolean isSoftLimitEnabled(SoftLimitDirection direction) {
		return spark.isSoftLimitEnabled(direction);
	}

	public CANSparkMax getHWSpark() {
		return spark;
	}
}
