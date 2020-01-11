package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import com.ctre.phoenix.motorcontrol.ControlMode;

import org.strongback.Executable;
import org.strongback.components.TalonSRX;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.SpitterInterface;
import frc.robot.lib.Subsystem;

/**
 * On the 2019 robot there are two spitter wheels that are under PID control for speed control.
 */
public class Spitter extends Subsystem implements SpitterInterface, Executable, DashboardUpdater {

    private BooleanSupplier cargoSupplier;

    private SpitterWheel left;
    private SpitterWheel right;

    public Spitter(BooleanSupplier cargoSupplier, TalonSRX leftMotor, TalonSRX rightMotor, DashboardInterface dashboard, Log log) {
        super("Spitter", dashboard, log);
        this.cargoSupplier = cargoSupplier;
        left = new SpitterWheel("left", leftMotor);
        right = new SpitterWheel("right", rightMotor);
        log.register(false, () -> hasCargo(), "Spitter/beamBreakTripped");

    }

    /**
     * Set the duty cycle on the spitter wheels.
     */
    @Override
    public SpitterInterface setTargetDutyCycle(double dutyCycle) { 
        left.setTargetPower(dutyCycle);
        right.setTargetPower(dutyCycle);
        return this;
    }

    @Override
    public double getTargetDutyCycle() {
        return left.getTargetDutyCycle(); 
    }

    @Override
    public boolean hasCargo(){
        //System.out.println(cargoSupplier.getAsBoolean());
        return cargoSupplier.getAsBoolean();
    }

    protected class SpitterWheel {

        private final TalonSRX motor;
        private double targetDutyCycle;
    
        public SpitterWheel(String name, TalonSRX motor) {
            this.motor = motor;

            log.register(false, () -> left.getTargetDutyCycle(), "Spitter/%s/dutyCycle", name)
            .register(false, motor::getMotorOutputVoltage, "Spitter/%s/outputVoltage", name)
            .register(false, motor::getMotorOutputPercent, "Spitter/%s/outputPercent", name)
            .register(false, motor::getOutputCurrent, "Spitter/%s/outputCurrent", name);
        }
        
        public void setTargetPower(double dutyCycle) {
            if (dutyCycle == targetDutyCycle) {
                 return;
            }
            targetDutyCycle = dutyCycle;
            // Note that if velocity mode is used and the speed is ever set to 0, 
            // change the control mode from percent output, to avoid putting
            // unnecessary load on the battery and motor.
            if (dutyCycle == 0) { 
                log.sub("Turning spitter wheel off.");
                motor.set(ControlMode.PercentOutput, 0); 
            } else {
                motor.set(ControlMode.PercentOutput, dutyCycle);
            }
            log.sub("Setting spitter target duty cycle to %f", targetDutyCycle);
        }

        public double getTargetDutyCycle() {
            return targetDutyCycle;
        }
    }

    @Override
    public void updateDashboard() {
        dashboard.putString("Spitter cargo status", hasCargo() ? "has cargo" : "no cargo");
        dashboard.putNumber("Spitter duty cycle", left.getTargetDutyCycle());
    }
}



