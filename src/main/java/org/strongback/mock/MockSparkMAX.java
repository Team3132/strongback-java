package org.strongback.mock;

import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.FaultID;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMax.SoftLimitDirection;

import org.strongback.components.SparkMAX;

/*
 * This class implements a mock Spark MAX for testing.
 * We have some simulation of the actual hardware and the sensors.
 */
public class MockSparkMAX implements SparkMAX {
	private double value = 0;
	private boolean inverted = false;
	private boolean fwdLimitSwitchClosed = false;
	private boolean revLimitSwitchClosed = false;

	// sensor collection information

	MockSparkMAX(int id) {
	}

	@Override
	public void set(double value, ControlType mode) {
		this.value = value;
	}

	@Override
	public double get() {
		return value;
	}

	@Override
	public void setInverted(boolean isInverted) {
		this.inverted = isInverted;
	}

	@Override
	public boolean getInverted() {
		return inverted;
	}

	@Override
	public void disable() {
	}

	@Override
	public void stopMotor() {
		value = 0;
	}

	@Override
	public void pidWrite(double output) {
		value = output;
	}

	@Override
	public void setPIDF(double p, double i, double d, double f) {
	}

	@Override
	public double getPosition() {
		return 0;
	}

	@Override
	public void setPosition(double position) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public double getVelocity() {
		return 0;
	}

	@Override
	public SparkMAX setScale(double scale) {
		return this;
	}

	@Override
	public boolean isFwdLimitSwitchClosed() {
		return fwdLimitSwitchClosed;
	}

	public void setFwdLimitSwitchClosed(boolean closed) {
		fwdLimitSwitchClosed = closed;
	}

	@Override
	public boolean isRevLimitSwitchClosed() {
		return revLimitSwitchClosed;
	}

	public void setRevLimitSwitchClosed(boolean closed) {
		revLimitSwitchClosed = closed;
	}

	@Override
	public boolean setSmartCurrentLimit(int limit) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean setSmartCurrentLimit(int stallLimit, int freeLimit) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean setSmartCurrentLimit(int stallLimit, int freeLimit, int limitRPM) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean setSecondaryCurrentLimit(double limit) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean setSecondaryCurrentLimit(double limit, int chopCycles) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean setIdleMode(IdleMode mode) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public IdleMode getIdleMode() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean enableVoltageCompensation(double nominalVoltage) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean disableVoltageCompensation() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public double getVoltageCompensationNominalVoltage() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean setOpenLoopRampRate(double rate) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean setClosedLoopRampRate(double rate) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public double getOpenLoopRampRate() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public double getClosedLoopRampRate() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean isFollower() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public short getFaults() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public short getStickyFaults() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean getFault(FaultID faultID) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean getStickyFault(FaultID faultID) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public double getBusVoltage() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public double getAppliedOutput() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public double getOutputCurrent() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public double getMotorTemperature() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean clearFaults() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean burnFlash() {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean setCANTimeout(int milliseconds) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean enableSoftLimit(SoftLimitDirection direction, boolean enable) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean setSoftLimit(SoftLimitDirection direction, float limit) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public double getSoftLimit(SoftLimitDirection direction) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}

	@Override
	public boolean isSoftLimitEnabled(SoftLimitDirection direction) {
		throw new UnsupportedOperationException("Not implemented for MockSparkMAX");
	}
}
