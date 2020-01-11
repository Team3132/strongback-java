package org.strongback.components;

import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.FaultID;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMax.SoftLimitDirection;

/*
 * This interface wraps the Spark MAX motor controller
 */
public interface SparkMAX {

	/**** Speed Controller Interface ****/
	/**
	 * Common interface for setting the speed of a speed controller.
	 *
	 * @param value The speed/position/duty cycle to set.
	 */
	public void set(double value, ControlType mode);

	/**
	 * Common interface for getting the current set speed of a speed controller.
	 *
	 * @return The current set speed. Value is between -1.0 and 1.0.
	 */
	public double get();

	/**
	 * Common interface for inverting direction of a speed controller.
	 *
	 * This call has no effect if the controller is a follower.
	 *
	 * @param isInverted The state of inversion, true is inverted.
	 */
	public void setInverted(boolean isInverted);

	/**
	 * Common interface for returning the inversion state of a speed controller.
	 * 
     * This call has no effect if the controller is a follower.
	 *
	 * @return isInverted The state of inversion, true is inverted.
	 */
	public boolean getInverted();

	/**
	 * Common interface for disabling a motor.
	 */
	public void disable();

	public void stopMotor();

	public void pidWrite(double output);
	public void setPIDF(double p, double i, double d, double f);
	public double getPosition();
	public void setPosition(double position);
	public double getVelocity();

	/**
	 * Scale methods that give results in ticks by multiplying by this factor and
	 * divide for methods that take ticks.
	 * 
	 * @param scale multiple by this to convert from ticks eg 1.0/4096.
	 * @return
	 */
	SparkMAX setScale(double scale);

	/**
	 * @return true if the forward limit switch is closed.
	 */
	public boolean isFwdLimitSwitchClosed();

	/**
	 * @return true if the reverse limit switch is closed.
	 */
	public boolean isRevLimitSwitchClosed();


	/**
	 * Sets the current limit in Amps.
	 *
	 * The motor controller will reduce the controller voltage output to avoid
	 * surpassing this limit. This limit is enabled by default and used for
	 * brushless only. This limit is highly recommended when using the NEO brushless
	 * motor.
	 *
	 * The NEO Brushless Motor has a low internal resistance, which can mean large
	 * current spikes that could be enough to cause damage to the motor and
	 * controller. This current limit provides a smarter strategy to deal with high
	 * current draws and keep the motor and controller operating in a safe region.
	 *
	 * @param limit The current limit in Amps.
	 *
	 * @return true if successful
	 */
	public boolean setSmartCurrentLimit(int limit);

	/**
	 * Sets the current limit in Amps.
	 *
	 * The motor controller will reduce the controller voltage output to avoid
	 * surpassing this limit. This limit is enabled by default and used for
	 * brushless only. This limit is highly recommended when using the NEO brushless
	 * motor.
	 *
	 * The NEO Brushless Motor has a low internal resistance, which can mean large
	 * current spikes that could be enough to cause damage to the motor and
	 * controller. This current limit provides a smarter strategy to deal with high
	 * current draws and keep the motor and controller operating in a safe region.
	 *
	 * The controller can also limit the current based on the RPM of the motor in a
	 * linear fashion to help with controllability in closed loop control. For a
	 * response that is linear the entire RPM range leave limit RPM at 0.
	 *
	 * @param stallLimit The current limit in Amps at 0 RPM.
	 * @param freeLimit  The current limit at free speed (5700RPM for NEO).
	 *
	 * @return true if successful
	 */
	public boolean setSmartCurrentLimit(int stallLimit, int freeLimit);

	/**
	 * Sets the current limit in Amps.
	 *
	 * The motor controller will reduce the controller voltage output to avoid
	 * surpassing this limit. This limit is enabled by default and used for
	 * brushless only. This limit is highly recommended when using the NEO brushless
	 * motor.
	 *
	 * The NEO Brushless Motor has a low internal resistance, which can mean large
	 * current spikes that could be enough to cause damage to the motor and
	 * controller. This current limit provides a smarter strategy to deal with high
	 * current draws and keep the motor and controller operating in a safe region.
	 *
	 * The controller can also limit the current based on the RPM of the motor in a
	 * linear fashion to help with controllability in closed loop control. For a
	 * response that is linear the entire RPM range leave limit RPM at 0.
	 *
	 * @param stallLimit The current limit in Amps at 0 RPM.
	 * @param freeLimit  The current limit at free speed (5700RPM for NEO).
	 * @param limitRPM   RPM less than this value will be set to the stallLimit, RPM
	 *                   values greater than limitRPM will scale linearly to
	 *                   freeLimit
	 *
	 * @return true if successful
	 */
	public boolean setSmartCurrentLimit(int stallLimit, int freeLimit, int limitRPM);

	/**
	 * Sets the secondary current limit in Amps.
	 *
	 * The motor controller will disable the output of the controller briefly if the
	 * current limit is exceeded to reduce the current. This limit is a simplified
	 * 'on/off' controller. This limit is enabled by default but is set higher than
	 * the default Smart Current Limit.
	 *
	 * The time the controller is off after the current limit is reached is
	 * determined by the parameter limitCycles, which is the number of PWM cycles
	 * (20kHz). The recommended value is the default of 0 which is the minimum time
	 * and is part of a PWM cycle from when the over current is detected. This
	 * allows the controller to regulate the current close to the limit value.
	 *
	 * The total time is set by the equation
	 *
	 * <code>
	 * t = (50us - t0) + 50us * limitCycles
	 * t = total off time after over current
	 * t0 = time from the start of the PWM cycle until over current is detected
	 * </code>
	 *
	 *
	 * @param limit The current limit in Amps.
	 *
	 * @return true if successful
	 */
	public boolean setSecondaryCurrentLimit(double limit);

	/**
	 * Sets the secondary current limit in Amps.
	 *
	 * The motor controller will disable the output of the controller briefly if the
	 * current limit is exceeded to reduce the current. This limit is a simplified
	 * 'on/off' controller. This limit is enabled by default but is set higher than
	 * the default Smart Current Limit.
	 *
	 * The time the controller is off after the current limit is reached is
	 * determined by the parameter limitCycles, which is the number of PWM cycles
	 * (20kHz). The recommended value is the default of 0 which is the minimum time
	 * and is part of a PWM cycle from when the over current is detected. This
	 * allows the controller to regulate the current close to the limit value.
	 *
	 * The total time is set by the equation
	 *
	 * <code>
	 * t = (50us - t0) + 50us * limitCycles
	 * t = total off time after over current
	 * t0 = time from the start of the PWM cycle until over current is detected
	 * </code>
	 *
	 *
	 * @param limit      The current limit in Amps.
	 * @param chopCycles The number of additional PWM cycles to turn the driver off
	 *                   after overcurrent is detected.
	 *
	 * @return true if successful
	 */
	public boolean setSecondaryCurrentLimit(double limit, int chopCycles);

	/**
	 * Sets the idle mode setting for the SPARK MAX.
	 *
	 * @param mode Idle mode (coast or brake).
	 *
	 * @return true if successful
	 */
	public boolean setIdleMode(IdleMode mode);

	/**
	 * Gets the idle mode setting for the SPARK MAX.
	 *
	 * This uses the Get Parameter API and should be used infrequently. This
	 * function uses a non-blocking call and will return a cached value if the
	 * parameter is not returned by the timeout. The timeout can be changed by
	 * calling SetCANTimeout(int milliseconds)
	 *
	 * @return IdleMode Idle mode setting
	 */
	public IdleMode getIdleMode();

	/**
	 * Sets the voltage compensation setting for all modes on the SPARK MAX and
	 * enables voltage compensation.
	 *
	 * @param nominalVoltage Nominal voltage to compensate output to
	 *
	 * @return true if successful
	 */
	public boolean enableVoltageCompensation(double nominalVoltage);

	/**
	 * Disables the voltage compensation setting for all modes on the SPARK MAX.
	 *
	 */
	public boolean disableVoltageCompensation();

	/**
	 * Get the configured voltage compensation nominal voltage value
	 *
	 * @return The nominal voltage for voltage compensation mode.
	 */
	public double getVoltageCompensationNominalVoltage();

	/**
	 * Sets the ramp rate for open loop control modes.
	 *
	 * This is the maximum rate at which the motor controller's output is allowed to
	 * change.
	 *
	 * @param rate Time in seconds to go from 0 to full throttle.
	 *
	 * @return true if successful
	 */
	public boolean setOpenLoopRampRate(double rate);

	/**
	 * Sets the ramp rate for closed loop control modes.
	 *
	 * This is the maximum rate at which the motor controller's output is allowed to
	 * change.
	 *
	 * @param rate Time in seconds to go from 0 to full throttle.
	 *
	 * @return true if successful
	 */
	public boolean setClosedLoopRampRate(double rate);

	/**
	 * Get the configured open loop ramp rate
	 *
	 * This is the maximum rate at which the motor controller's output is allowed to
	 * change.
	 *
	 * @return ramp rate time in seconds to go from 0 to full throttle.
	 */
	public double getOpenLoopRampRate();

	/**
	 * Get the configured closed loop ramp rate
	 *
	 * This is the maximum rate at which the motor controller's output is allowed to
	 * change.
	 *
	 * @return ramp rate time in seconds to go from 0 to full throttle.
	 */
	public double getClosedLoopRampRate();

	/**
	 * Returns whether the controller is following another controller
	 *
	 * @return True if this device is following another controller false otherwise
	 */
	public boolean isFollower();

	/**
	 * @return All fault bits as a short
	 */
	public short getFaults();

	/**
	 * @return All sticky fault bits as a short
	 */
	public short getStickyFaults();

	/**
	 * Get the value of a specific fault
	 *
	 * @param faultID The ID of the fault to retrive
	 *
	 * @return True if the fault with the given ID occurred.
	 */
	public boolean getFault(FaultID faultID);

	/**
	 * Get the value of a specific sticky fault
	 *
	 * @param faultID The ID of the sticky fault to retrive
	 *
	 * @return True if the sticky fault with the given ID occurred.
	 */
	public boolean getStickyFault(FaultID faultID);

	/**
	 * @return The voltage fed into the motor controller.
	 */
	public double getBusVoltage();

	/**
	 * @return The motor controller's applied output duty cycle.
	 */
	public double getAppliedOutput();

	/**
	 * @return The motor controller's output current in Amps.
	 */
	public double getOutputCurrent();

	/**
	 * @return The motor temperature in Celsius.
	 */
	public double getMotorTemperature();

	/**
	 * Clears all sticky faults.
	 *
	 * @return true if successful
	 */
	public boolean clearFaults();

	/**
	 * Writes all settings to flash.
	 *
	 * @return true if successful
	 */
	public boolean burnFlash();

	/**
	 * Sets timeout for sending CAN messages with SetParameter* and GetParameter* calls.
	 * These calls will block for up to this amoutn of time before returning a timeout
	 * error. A timeout of 0 will make the SetParameter* calls non-blocking, and instead
	 * will check the response in a separate thread. With this configuration, any
	 * error messages will appear on the drivestration but will not be returned by the
	 * GetLastError() call.
	 *
	 * @param milliseconds The timeout in milliseconds.
	 *
	 * @return true if successful
	 */
	public boolean setCANTimeout(int milliseconds);

	/**
     * Enable soft limits
     *
     * @param direction the direction of motion to restrict
     * 
     * @param enable set true to enable soft limits
	 * 
	 * @return true if successful
     */
	public boolean enableSoftLimit(SoftLimitDirection direction, boolean enable);

	 /**
     * Set the soft limit based on position. The default unit is
     * rotations, but will match the unit scaling set by the user.
     * 
     * Note that this value is not scaled internally so care must
     * be taken to make sure these units match the desired conversion
     *
     * @param direction the direction of motion to restrict
     * 
     * @param limit position soft limit of the controller
	 * 
	 * @return true if successful
     */
	public boolean setSoftLimit(SoftLimitDirection direction, float limit);

	/**
     * Get the soft limit setting in the controller
     *
     * @param direction the direction of motion to restrict
     * 
     * @return position soft limit setting of the controller
     */
	public double getSoftLimit(SoftLimitDirection direction);

	/**
	 * @param direction The direction of the motion to restrict
	 * 
     * @return true if the soft limit is enabled.
     */
	public boolean isSoftLimitEnabled(SoftLimitDirection direction);
}
