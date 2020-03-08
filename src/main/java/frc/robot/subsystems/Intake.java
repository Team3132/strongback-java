package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import org.strongback.Executable;
import org.strongback.components.Motor;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor.ControlMode;

import frc.robot.Constants;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.IntakeInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

/**
 * Intake Subsystem 2019:
 * On the 2019 robot the intake is pneumatically driven and using one motor to intake game objects 
 */
public class Intake extends Subsystem implements IntakeInterface, Executable, DashboardUpdater {
    private Motor motor;
    private Solenoid solenoid;
    private IntakeWheel intakeWheel;

    public Intake(Motor motor, Solenoid solenoid, DashboardInterface dashboard, Log log) {
        super("Intake", dashboard, log);   
        this.motor = motor;
        this.solenoid = solenoid;
        intakeWheel = new IntakeWheel(motor);
        log.register(true, () -> isExtended(), "%s/extended", name)
               .register(true, () -> isRetracted(), "%s/retracted", name)
			   .register(false, motor::getOutputVoltage, "%s/outputVoltage", name)
			   .register(false, motor::getOutputPercent, "%s/outputPercent", name)
			   .register(false, motor::getOutputCurrent, "%s/outputCurrent", name);
    }

    @Override
    public void enable() {
        motor.set(ControlMode.PercentOutput, 0);
    }

    @Override
    public void disable() {
        motor.set(ControlMode.PercentOutput, 0);
    }

    @Override
    public IntakeInterface setExtended(boolean extend) {
        if (extend) {
            solenoid.extend();
        } else {
            solenoid.retract();
        }
        return this;
    }

    @Override
    public boolean isExtended() {
        //log.sub("Is intake extended: " +  solenoid.isExtended());
        return solenoid.isExtended();
    }

    @Override
    public boolean isRetracted() {
        return solenoid.isRetracted();
    }
    
    /**
     * Set the speed on the intake wheels.
     */
    @Override
    public IntakeInterface setTargetRPM(double speed) { 
        intakeWheel.setTargetRPM(speed);
        return this;
    }

    @Override
    public double getTargetRPM() {
        return intakeWheel.getTargetRPM(); 
    }

    protected class IntakeWheel {

        private final Motor motor;
        private double targetRPM;
    
        public IntakeWheel(Motor motor) {
            this.motor = motor;

            log.register(false, () -> intakeWheel.getTargetRPM(), "%s/targetRPM", name)
            .register(false, motor::getVelocity, "%s/rpm", name)
            .register(false, motor::getOutputVoltage, "%s/outputVoltage", name)
            .register(false, motor::getOutputPercent, "%s/outputPercent", name)
            .register(false, motor::getOutputCurrent, "%s/outputCurrent", name);
        }
        
        public void setTargetRPM(double rpm) {
            if (rpm == targetRPM) {
                 return;
            }
            targetRPM = rpm;
            // Note that if velocity mode is used and the speed is ever set to 0, 
            // change the control mode from percent output, to avoid putting
            // unnecessary load on the battery and motor.
            if (rpm == 0) { 
                log.sub("Turning intake wheel off.");
                motor.set(ControlMode.PercentOutput, 0); 
            } else {
                motor.set(ControlMode.Velocity, rpm);
            }
            log.sub("Setting intake target speed to %f", targetRPM);
        }

        public double getTargetRPM() {
            return targetRPM;
        }

        public double getRPM() {
            return motor.getVelocity();
        }

        public void setPIDF(double p, double i, double d, double f) {
            motor.setPIDF(0, p, i, d, f);
        }
    }


    
    /**
     * Update the operator console with the status of the intake subsystem.
     */
	@Override
	public void updateDashboard() {
        dashboard.putString("Intake position", isExtended() ? "extended" : isRetracted() ? "retracted" : "moving");
        dashboard.putNumber("Intake motor current", motor.getOutputCurrent());
        dashboard.putNumber("Intake motor target RPM", intakeWheel.getTargetRPM());
        dashboard.putNumber("Intake motor actual RPM", intakeWheel.getRPM());
        dashboard.putNumber("Intake motor position", motor.getPosition());

	}
}

