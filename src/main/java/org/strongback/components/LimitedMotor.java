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

import org.strongback.annotation.Immutable;
import org.strongback.util.Values;

/**
 * A {@link Motor} that is bounded by two {@link Switch} components at the extremes of it's range of motion.
 * <p>
 * {@link LimitedMotor} has three possible {@link Position positions}:
 * <ol>
 * <li> {@code HIGH} - the high switch is active</li>
 * <li> {@code LOW} - the low switch is active</li>
 * <li> {@code UNKNOWN} - neither switch is triggered (or both switches are triggered, typically as the result of a problem with
 * the robot hardware)</li>
 * </ol>
 *
 * @author Zach Anderson
 * @see Motor
 * @see Switch
 */
@Immutable
public interface LimitedMotor extends Motor {

    @Override
    public LimitedMotor setSpeed(double speed);

    /**
     * Get the switch that signals when this motor reaches its limit in the forward direction.
     *
     * @return the forward direction limit switch; never null
     */
    public Switch getForwardLimitSwitch();

    /**
     * Get the switch that signals when this motor reaches its limit in the reverse direction.
     *
     * @return the reverse direction limit switch; never null
     */
    public Switch getReverseLimitSwitch();

    /**
     * Tests if this {@link LimitedMotor} is at the high limit. This is equivalent to calling
     * {@code getForwardLimitSwitch().isTriggered()}.
     *
     * @return {@code true} if this {@link LimitedMotor} is at the forward limit; {@code false} otherwise
     */
    default public boolean isAtForwardLimit() {
        return getForwardLimitSwitch().isTriggered();
    }

    /**
     * Tests if this {@link LimitedMotor} is at the low limit. This is equivalent to calling
     * {@code getReverseLimitSwitch().isTriggered()}.
     *
     * @return {@code true} if this {@link LimitedMotor} is at the low limit; {@code false} otherwise
     */
    default public boolean isAtReverseLimit() {
        return getReverseLimitSwitch().isTriggered();
    }

    /**
     * Moves this {@link LimitedMotor} towards the forward limit. This method should be called once per loop until the movement
     * is completed.
     *
     * @param speed the speed at which the underlying {@link Motor} should spin in the forward direction
     * @return {@code true} if the motor remains moving, or {@code false} if it has reached the forward limit
     */
    default public boolean forward(double speed) {
        // Motor protection
        if (!isAtForwardLimit()) {
            setSpeed(Math.abs(speed));
        } else {
            stop();
        }
        return !isAtForwardLimit();
    }

    /**
     * Moves this {@link LimitedMotor} towards the reverse limit. This method should be called once per loop until the movement
     * is completed.
     *
     * @param speed the speed at which the underlying {@link Motor} should spin in the reverse direction
     * @return {@code true} if the motor remains moving, or {@code false} if it has reached the forward limit
     */
    default public boolean reverse(double speed) {
        // Motor protection
        if (!isAtReverseLimit()) {
            setSpeed(-Math.abs(speed));
        } else {
            stop();
        }
        return !isAtForwardLimit();
    }

    /**
     * Create a {@link LimitedMotor} around the given motor and switches.
     *
     * @param motor the {@link Motor} being limited; may not be null
     * @param forwardSwitch the {@link Switch} that signals the motor reached its limit in the forward direction, or null if
     *        there is no limit switch
     * @param reverseSwitch the {@link Switch} that signals the motor reached its limit in the reverse direction, or null if
     *        there is no limit switch
     * @return the limited motor; never null
     * @throws IllegalArgumentException if the {@code motor} parameter is null
     */
    public static LimitedMotor create(Motor motor, Switch forwardSwitch, Switch reverseSwitch) {
        if (motor == null) throw new IllegalArgumentException("The motor may not be null");
        Switch fwdSwitch = forwardSwitch != null ? forwardSwitch : Switch.neverTriggered();
        Switch revSwitch = reverseSwitch != null ? reverseSwitch : Switch.neverTriggered();
        return new LimitedMotor() {
            @Override
            public double getSpeed() {
                double speed = motor.getSpeed();
                int direction = Values.fuzzyCompare(speed, 0.0);
                if (direction > 0 && fwdSwitch.isTriggered()) return 0.0;
                if (direction < 0 && revSwitch.isTriggered()) return 0.0;
                return speed;
            }

            @Override
            public LimitedMotor setSpeed(double speed) {
                int direction = Values.fuzzyCompare(speed, 0.0);
                if (direction > 0 && !fwdSwitch.isTriggered()) {
                    motor.setSpeed(speed);
                } else if (direction < 0 && !revSwitch.isTriggered()) {
                    motor.setSpeed(speed);
                } else {
                    motor.stop();
                }
                return this;
            }

            @Override
            public Switch getForwardLimitSwitch() {
                return fwdSwitch;
            }

            @Override
            public Switch getReverseLimitSwitch() {
                return revSwitch;
            }

            @Override
            public void stop() {
                motor.stop();
            }

            @Override
            public Motor setScale(double scale) {
                motor.setScale(scale);
                return null;
            }

            @Override
            public double getScale() {
                return motor.getScale();
            }

            @Override
            public Motor enable() {
                return motor.enable();
            }

            @Override
            public Motor disable() {
                return motor.disable();
            }
        };
    }
}