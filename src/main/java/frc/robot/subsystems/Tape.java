package frc.robot.subsystems;

import com.ctre.phoenix.CANifier;

import frc.robot.Constants;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.TapeInterface;
import frc.robot.lib.CanifierColourSensors;
import frc.robot.lib.Subsystem;

/**
 * FIXME: Add class 
 * On the comp 2019 robot two colour sensors (Tape subsystem) are used to assist the driver in lining up
 * to the rocket or cargo ship to score.
 */
public class Tape extends Subsystem implements TapeInterface, DashboardUpdater {
    private TapeSensor left, right;
    private double lastEdge, firstEdge;
    private double rightSensorValue;
    private double leftSensorValue;
    /**
     * TODO: Comment the description here
     */
    private final double OVER_THE_EDGE_VALUE =
         Math.abs(Constants.GRADIENT_CONSTANT * Constants.OVER_THE_EDGE_VALUE) +
            Constants.Y_AXIS_CONSTANT; // TODO: Find the correct function.
    /**
     * This is to prevent the robot from ocsillating. Since the it there is a tolerance range.
     */
    private final double HIGH_THRESHOLD = OVER_THE_EDGE_VALUE + Constants.COLOUR_SENSOR_TOLERANCE;
    private final double LOW_THRESHOLD = OVER_THE_EDGE_VALUE - Constants.COLOUR_SENSOR_TOLERANCE;

    public Tape(CANifier sensor, DashboardInterface dashboard, Log log) {    
        super("Tape", dashboard, log);
        left = new TapeSensor(sensor, Constants.COLOUR_SENSOR_LEFT);
        right = new TapeSensor(sensor, Constants.COLOUR_SENSOR_RIGHT);
    }
    
    @Override
    public boolean isTape(double sensorValue) {
        return sensorValue >= LOW_THRESHOLD && sensorValue <= HIGH_THRESHOLD;
    }

    @Override
    public boolean isCarpet(double sensorValue) {
        return sensorValue < LOW_THRESHOLD || sensorValue > HIGH_THRESHOLD;
    }

    @Override
    public boolean moveToLeft() {
        /** 
         * When the leftSensorValue is between the (high & low) thresholds and 
         * the rightSensorValue is outside the thresholds, return true
         */ 
        return isTape(leftSensorValue) && isCarpet(rightSensorValue);
    }

    @Override
    public boolean moveToRight() {
        /** 
         * When the rightSensorValue is between the (high & low) thresholds and 
         * the leftSensorValue is outside the thresholds, return true
         */ 
        return isCarpet(leftSensorValue) && isTape(rightSensorValue);
    }

    @Override
    public boolean moveStraight() {
        /**
         * When both the right and left sensor values are between the (high & low) thresholds, return true
         */
        return isTape(leftSensorValue) && isTape(leftSensorValue);
    }

    @Override
    public Direction getRecommendation() {
        /** 
         * Returns a "recommendation" to the robot from the configuration:
         * Straight, Right, Left, No Tape Detected.
         */  
        leftSensorValue = left.getSensorValue();
        rightSensorValue = right.getSensorValue();
        
        if (moveToLeft()) {
            return Direction.LEFT;
        }
        if (moveToRight()) {
            return Direction.RIGHT;
        }
        if (moveStraight()) {
            return Direction.STRAIGHT;
        }
        return Direction.NO_LINE_DETECTED;
    }

    /**
     * This class is needed to get the values from two different colour sensors.
     */
    public class TapeSensor extends CanifierColourSensors {
        private CanifierColourSensors sensor;
        private int sensorNumber;

        public TapeSensor(CANifier colourSensor, int sensorNumber) {
            super(colourSensor);
            sensor = new CanifierColourSensors(colourSensor);
        }

        public double getSensorValue() {
            return sensor.getColourIntensity(sensorNumber);
        }
        
        /** 
         * Obtaining the distance of each band, in order to determine 
         * if the robot is within the range if the robot is on, off or on
         * the edge of the tape.
         */ 
        public double getBandDistance() {
            return (Constants.BAND_OFF - Constants.BAND_ON)/3;
        }

        public boolean inRangeOff() {
            return getSensorValue() >= lastEdge;
        }
        
        public boolean inRangeOn() {
            return getSensorValue() <= firstEdge;
        }
        /**
         * This logging method is to make it easy read whether if the colour 
         * sensor is within: ON the line, OFF the line, EDGE of the line. 
         */
        public String getCurrentTapePosition() {
            lastEdge = Constants.BAND_OFF - getBandDistance();
            firstEdge = Constants.BAND_ON + getBandDistance();
    
            if (inRangeOff()) {
                log.sub("Sensor is NOT ON the line.");
                return "Off Band";
            } 
            else if (inRangeOn()) {
                log.sub("Sensor is ON the line.");
                return "On band";
            } else {
                log.sub("Sensor is on the EDGE of the line.");
            return "Edge band";
            }
        }
    }
}