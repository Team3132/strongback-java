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

package org.strongback.mock;

import org.strongback.components.Motor;

/**
 * A {@link Motor} implementation useful for testing. This motor does nothing but maintain a record of the current speed.
 *
 * @author Randall Hauch
 *
 */
public class MockMotor implements Motor {

    private volatile double speed = 0;
    private volatile double position = 0;
    private volatile double output = 0;
    private volatile ControlMode mode = ControlMode.PercentOutput;

    MockMotor(double speed) {
        this.speed = speed;
    }


    @Override
    public void set(ControlMode mode, double demand) {
        // Default implementation. Either set() or setSpeed() needs to be implemented.
        switch (mode) {
            case Velocity:
                setSpeed(demand);
                break;
            case PercentOutput:
                this.mode = mode;
                speed = output = demand;
                break;
            case Position:
                this.mode = mode;
                position = demand;
                break;
            default:
                break;
        }
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public MockMotor setSpeed(double speed) {
        mode = ControlMode.Velocity;
        this.speed = speed;
        return this;
    }

    @Override
    public String toString() {
        return Double.toString(getSpeed());
    }

    @Override
    public double getPosition() {
        return position;
    }

    @Override
    public double getOutputPercent() {
        // Not implmented by default.
        return output;
    }
}
