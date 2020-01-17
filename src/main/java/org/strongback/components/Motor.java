/*
 * Strongback
 * Copyright 2015, Strongback and individual contributors by the @authors tag.
 * See the COPYRIGHT.txt in the distribution for a full listing of individual
 * contributors.
 *
 * Licensed under the MIT License; you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://opensource.org/licenses/MIT
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.strongback.components;

import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Requirable;
import org.strongback.drive.TankDrive;

/**
 * A motor is a device that can be set to operate at a speed.
 *
 * @author Zach Anderson
 *
 */
@ThreadSafe
public interface Motor extends SpeedSensor, SpeedController, Stoppable, Requirable {

    public enum ControlMode {
        /**
         * Percent output [-1,1]
         */
        PercentOutput(0, com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput, com.revrobotics.ControlType.kDutyCycle),
        /**
         * Position closed loop
         */
        Position(1, com.ctre.phoenix.motorcontrol.ControlMode.Position, com.revrobotics.ControlType.kPosition),
        /**
         * Velocity closed loop
         */
        Velocity(2, com.ctre.phoenix.motorcontrol.ControlMode.Velocity, com.revrobotics.ControlType.kVelocity),
        /**
         * Input current closed loop
         */
        Current(3, com.ctre.phoenix.motorcontrol.ControlMode.Current, com.revrobotics.ControlType.kCurrent),
        /**
         * Follow other motor controller
         * Not supported by Spark MAX.
         */
        Follower(5, com.ctre.phoenix.motorcontrol.ControlMode.Follower, com.revrobotics.ControlType.kDutyCycle),
        /**
         * Motion Profile
         */
        MotionProfile(6, com.ctre.phoenix.motorcontrol.ControlMode.MotionProfile, com.revrobotics.ControlType.kSmartMotion),
        /**
         * Motion Magic
         */
        MotionMagic(7, com.ctre.phoenix.motorcontrol.ControlMode.MotionMagic, com.revrobotics.ControlType.kSmartVelocity),
        /**
         * Motion Profile with auxiliary output
         */
        MotionProfileArc(10, com.ctre.phoenix.motorcontrol.ControlMode.MotionProfileArc, com.revrobotics.ControlType.kSmartMotion),

        /**
         * Disable Motor Controller
         * Not supported by Spark MAX
         */
        Disabled(15, com.ctre.phoenix.motorcontrol.ControlMode.Disabled, com.revrobotics.ControlType.kDutyCycle);

        /**
         * Value of control mode
         */
        public final int value;
        public final com.ctre.phoenix.motorcontrol.ControlMode talonControlMode;
        public final com.revrobotics.ControlType revControlType;

        /**
         * Create ControlMode of initValue
         * 
         * @param initValue Value of ControlMode
         */
        ControlMode(final int initValue, com.ctre.phoenix.motorcontrol.ControlMode talonControlMode, com.revrobotics.ControlType revControlType) {
            this.value = initValue;
            this.talonControlMode = talonControlMode;
            this.revControlType = revControlType;
        }
    };

    /**
     * Tell the motor what control mode and how fast/far.
     * Some motor controllers don't support some modes.
     * 
     * @param mode percent output, position, velocity etc.
     * @param demand for percent [-1,1].
     */
    public default void set(final ControlMode mode, double demand) {
        // Default implementation. Either set() or setSpeed() needs to be implemented.
        if (mode != ControlMode.PercentOutput) {
            System.err.printf("ERROR: Unsupported motor control mode %s\n", mode);
            demand = 0;
        }
        setSpeed(demand);
    }

    /**
     * Ask for the last set demand.
     */
    public default double get() {
        return getSpeed();
    }

    /**
     * Scale the incoming and outgoing velocity and position parameters to
     * convert them from ticks[/100ms] to a useful unit.
     * @param scale set to 0.5 to halve set(...) values and double get*() results.
     * @return this
     */
    public default Motor setScale(double scale) {
        return this;
    }

    public default double getScale() {
        return 1;
    };

    public default Motor enable() {
        return this;
    }

    public default Motor disable() {
        stop();
        return this;
    }

    /**
     * Set PID parameters for motor controllers that support it.
     */
    public default Motor setPIDF(int slotIdx, double p, double i, double d, double f) {
        // Not implmented by default.
        return this;
    }

    /**
     * Tell the motor controller which set of PID values to use.
     */
    public default Motor selectProfileSlot(int slotIdx) {
        // Not implmented by default.
        return this;
    }

    /**
     * Query the forward limit switch.
     * Not implemented on all motor controllers.
     * @return true if the forward limit switch is triggered.
     */
    public default boolean isAtForwardLimit() {
        // Not implmented by default.
        return false;
    }

    /**
     * Query the forward limit switch.
     * Not implemented on all motor controllers.
     * @return true if the forward limit switch is triggered.
     */
    public default boolean isAtReverseLimit() {
        // Not implmented by default.
        return false;
    }

    /**
     * Returns the bus voltage for motor controllers that support it.
     * @return voltage on the bus.
     */
    public default double getBusVoltage() {
        // Not implmented by default.
        return 0;
    }

    /**
     * Returns the voltage being supplied to the motor for motor controllers that support it.
     * @return voltage to the motor.
     */
    public default double getOutputVoltage() {
        // Not implmented by default.
        return 0;
    }

    /**
     * Returns the percentage of the power being supplied to the motor for motor controllers that support it.
     * @return percentage
     */
    public default double getOutputPercent() {
        // Not implmented by default.
        return 0;
    }

    /**
     * Returns the current being supplied to the motor for motor controllers that support it.
     * @return current in amps to the motor.
     */
    public default double getOutputCurrent() {
        // Not implmented by default.
        return 0;
    }

    /**
     * Returns the velocity after dividing by the scaling factor.
     * Not supported by all motor controllers.
     */
    public default double getVelocity() {
        // Not implmented by default.
        return 0;
    }

    /**
     * Returns the position after dividing by the scaling factor.
     * Not supported by all motor controllers.
     */
    public default double getPosition() {
        // Not implmented by default.
        return 0;
    }

    /**
     * returns the temperature of the motor controller.
     * Not supported by all motor controllers.
     */
    public default double getTemperature() {
        // Not implmented by default.
        return 0;
    }

    /**
     * Invert just the sensor.
     */
    public default Motor setSensorPhase(boolean phase) {
        // Not implmented by default.
        return this;
    }

    /**
     * Override the position of the quadrature position.
     */
    public default Motor setPosition(double position) {
        // Not implmented by default.
        return this;        
    }
    
    /**
     * Invert both the motor and the sensor.
     */
	public default Motor setInverted(boolean invert) {
        // Not implmented by default.
        return this;
    };
	
	public default boolean getInverted() {
        // Not implmented by default.
        return false;
    }

    /**
     * Gets the current speed.
     * Not supported on all motor controllers.
     *
     * @return the speed, will be between -1.0 and 1.0 inclusive
     */
    @Override
    public default double getSpeed() {
        // Not implmented by default.
        return get();
    }

    /**
     * Sets the speed of this {@link Motor}.
     *
     * @param speed the new speed as a double, clamped to -1.0 to 1.0 inclusive
     * @return this object to allow chaining of methods; never null
     */
    @Override
    public default Motor setSpeed(double speed) {
        set(ControlMode.PercentOutput, speed);
        return this;
    }

    /**
     * Stops this {@link Motor}. Same as calling {@code setSpeed(0.0)}.
     */
    @Override
    public default void stop() {
        setSpeed(0.0);
    }

    /**
     * Create a new {@link Motor} instance that is actually composed of two other motors that will be controlled identically.
     * This is useful when multiple motors are controlled in the same way, such as on one side of a {@link TankDrive}.
     *
     * @param motor1 the first motor, and the motor from which the speed is read; may not be null
     * @param motor2 the second motor; may not be null
     * @return the composite motor; never null
     */
    static Motor compose(final Motor motor1, final Motor motor2) {
        return new Motor() {
            @Override
            public double getSpeed() {
                return motor1.getSpeed();
            }

            @Override
            public Motor setSpeed(final double speed) {
                motor1.setSpeed(speed);
                motor2.setSpeed(speed);
                return this;
            }
        };
    }

    /**
     * Create a new {@link Motor} instance that is actually composed of three other motors that will be controlled identically.
     * This is useful when multiple motors are controlled in the same way, such as on one side of a {@link TankDrive}.
     *
     * @param motor1 the first motor, and the motor from which the speed is read; may not be null
     * @param motor2 the second motor; may not be null
     * @param motor3 the third motor; may not be null
     * @return the composite motor; never null
     */
    static Motor compose(final Motor motor1, final Motor motor2, final Motor motor3) {
        return new Motor() {
            @Override
            public double getSpeed() {
                return motor1.getSpeed();
            }

            @Override
            public Motor setSpeed(final double speed) {
                motor1.setSpeed(speed);
                motor2.setSpeed(speed);
                motor3.setSpeed(speed);
                return this;
            }
        };
    }
}