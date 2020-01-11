/*
 * This class uses a canifier to look after four colour sensors.
 * The sensors we use are based on the TCS3200.
 * 
 * Four of the canifier pins are used to control aspects of the sensor.
 * SDA/S0 and SCL/S1 are used to select the scaling of the result signal
 * QUAD_A/S2 and QUAD_B/S3 are used to select the colour filters to utilise.
 * 
 * Initially the canifier is set to 2% frequency and clear (as these work on the canifier).
 * 
 * Four colour sensors can be connected. Sensor 0 to PWM0 and sensor 1 to PWM1, Sensor 2 to PWM 2 and Sensor 3 to CS.
 * 
 * For the test carpet we are using:
 * centre of white strip: 1450
 * halfway beteen centre and edge of white strip: 1600
 * over edge of white strip: 2200
 * half inch from white strip over carpet: 5500
 * 1 inch away from edge of strip over carpet: 12900
 * middle of the carpet: 13500
 */

package frc.robot.lib;

import com.ctre.phoenix.CANifier;

/**
 * Add your docs here.
 */
public class CanifierColourSensors {
    public CANifier canifier;
    
    double[][] _dutyCycleAndIntensity = new double[][]{new double[]{0, 0}, new double[]{0, 0},
                                                     new double[]{0, 0}, new double[]{0, 0}};    // structure to hold values returned by sensors

    public enum ColourFrequency {
        OFF, TWO, TWENTY, FULL
    };

    public enum ColourFilter {
        CLEAR, RED, BLUE, GREEN
    };

    public CanifierColourSensors(CANifier canifier) {
        this.canifier = canifier;
        this.setColourFrequency(ColourFrequency.TWO);   // For Canifier only TWO gives numbers
        this.setColourFilter(ColourFilter.CLEAR);
    }

    public CanifierColourSensors setColourFrequency(ColourFrequency freq) {
        boolean S0, S1;
        switch(freq) {
        case OFF:
            S0 = false; S1 = false; break;
        case TWO:
            S0 = false; S1 = true; break;
        case TWENTY:
            S0 = true; S1 = false; break;
        case FULL:
        default:
            S0 = true; S1 = true; break;
        }
        canifier.setGeneralOutput(com.ctre.phoenix.CANifier.GeneralPin.SDA, S0, true); // S0 output
        canifier.setGeneralOutput(com.ctre.phoenix.CANifier.GeneralPin.SCL, S1, true); // S1 output
        return this;
    }

    public CanifierColourSensors setColourFilter(ColourFilter filter) {
        boolean S2, S3;
        switch(filter) {
        case RED:
            S2 = false; S3 = false; break;
        case BLUE:
            S2 = false; S3 = true; break;
        case CLEAR:
        default:
            S2 = true; S3 = false; break;
        case GREEN:
            S2 = true; S3 = true; break;
        }
        canifier.setGeneralOutput(com.ctre.phoenix.CANifier.GeneralPin.QUAD_A, S2, true); // S2 output
        canifier.setGeneralOutput(com.ctre.phoenix.CANifier.GeneralPin.QUAD_B, S3, true); // S3 output
        return this;
    }

    public double getColourIntensity(int sensor) {
        if (sensor < 0 || sensor > 3) {
            return -1;
        }
        switch(sensor) {
        case 0:
        default:
            canifier.getPWMInput(CANifier.PWMChannel.PWMChannel0, _dutyCycleAndIntensity[0]);
            return _dutyCycleAndIntensity[0][0];
        case 1:
            canifier.getPWMInput(CANifier.PWMChannel.PWMChannel1, _dutyCycleAndIntensity[1]);
            return _dutyCycleAndIntensity[1][0];
        case 2:
            canifier.getPWMInput(CANifier.PWMChannel.PWMChannel2, _dutyCycleAndIntensity[2]);
            return _dutyCycleAndIntensity[2][0];
        case 3:
            canifier.getPWMInput(CANifier.PWMChannel.PWMChannel3, _dutyCycleAndIntensity[3]);
            return _dutyCycleAndIntensity[3][0];
        }
    }
}