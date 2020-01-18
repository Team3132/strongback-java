package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import org.strongback.Executable;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ShooterInterface;
import frc.robot.lib.Subsystem;

/**
 * On the 2019 robot there are two spitter wheels that are under PID control for speed control.
 */
public class Shooter extends Subsystem implements ShooterInterface, Executable, DashboardUpdater {

    private BooleanSupplier cargoSupplier;

    private ShooterWheel flywheel;

    public Shooter(BooleanSupplier cargoSupplier, Motor leftMotor, Motor rightMotor, DashboardInterface dashboard, Log log) {
        super("Spitter", dashboard, log);
        this.cargoSupplier = cargoSupplier;
        flywheel = new ShooterWheel("flywheel", leftMotor);
        log.register(false, () -> hasCell(), "Shooter/beamBreakTripped");

    }

    /**
     * Set the duty cycle on the spitter wheels.
     */
    @Override
    public ShooterInterface setTargetDutyCycle(double dutyCycle) { 
        flywheel.setTargetPower(dutyCycle);
        return this;
    }

    @Override
    public double getTargetDutyCycle() {
        return flywheel.getTargetDutyCycle(); 
    }

    @Override
    public boolean hasCell(){
        //System.out.println(cargoSupplier.getAsBoolean());
        return cargoSupplier.getAsBoolean();
    }

    protected class ShooterWheel {

        private final Motor motor;
        private double targetDutyCycle;
    
        public ShooterWheel(String name, Motor motor) {
            this.motor = motor;

            log.register(false, () -> flywheel.getTargetDutyCycle(), "Shooter/%s/dutyCycle", name)
            .register(false, motor::getOutputVoltage, "Shooter/%s/outputVoltage", name)
            .register(false, motor::getOutputPercent, "Shooter/%s/outputPercent", name)
            .register(false, motor::getOutputCurrent, "Shooter/%s/outputCurrent", name);
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
                log.sub("Turning shooter wheel off.");
                motor.set(ControlMode.PercentOutput, 0); 
            } else {
                motor.set(ControlMode.PercentOutput, dutyCycle);
            }
            log.sub("Setting shooter target duty cycle to %f", targetDutyCycle);
        }

        public double getTargetDutyCycle() {
            return targetDutyCycle;
        }
    }

    @Override
    public void updateDashboard() {
        dashboard.putString("Shooter cell status", hasCell() ? "has cell" : "no cell");
        dashboard.putNumber("Shooter duty cycle", flywheel.getTargetDutyCycle());
    }
}



