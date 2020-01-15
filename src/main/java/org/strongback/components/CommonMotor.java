/*
 * Common Motor Type.
 * This interface wraps the different motors available 
 * 
 */

package org.strongback.components;

public interface CommonMotor {
    public enum Mode {
        Voltage,        // -1 to +1 => max negative voltage to max positive voltage (PercentOutput)
        Velocity,
        Position,
        Current
    }

    public CommonMotor set(Mode mode, double value);

    public double get();

    public CommonMotor setScale(double scale);

    public double getScale();
    public CommonMotor disable();
    public CommonMotor setPIDF(double p, double i, double d, double f);
    public boolean isFwdLimitSwitchClosed();
    public boolean isRevLimitSwitchClosed();
    public double getBusVoltage();
    public double getOutputVoltage();
    public double getOutputCurrent();
    public double getVelocity();
    public double getPosition();
    public double getTemperature();
    public CommonMotor clearFaults();




}