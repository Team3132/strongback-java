package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Motor;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor.ControlMode;

import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.IntakeInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

/**
 * Intake Subsystem 2019:
 * On the 2019 robot the intake is pneumatically driven and using one motor to intake game objects 
 */
public class Intake extends Subsystem implements IntakeInterface
 {
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
               .register(false, motor::getOutputCurrent, "%s/outputCurrent", name)
               .register(false, () -> intakeWheel.getTargetRPS(), "%s/targetRPS", name)
               .register(false, () -> intakeWheel.getRPS(), "%s/rps", name);
   
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
    public IntakeInterface setTargetRPS(double rps) { 
        intakeWheel.setTargetRPS(rps);
        return this;
    }

    @Override
    public double getTargetRPS() {
        return intakeWheel.getTargetRPS(); 
    }

    protected class IntakeWheel {

        private final Motor motor;
        private double targetRPS;
    
        public IntakeWheel(Motor motor) {
            this.motor = motor;
        }
        
        public void setTargetRPS(double rps) {
            if (rps == targetRPS) {
                 return;
            }
            targetRPS = rps;
            // Note that if velocity mode is used and the speed is ever set to 0, 
            // change the control mode from percent output, to avoid putting
            // unnecessary load on the battery and motor.
            if (rps == 0) { 
                log.sub("Turning intake wheel off.");
                motor.set(ControlMode.PercentOutput, 0); 
            } else {
                motor.set(ControlMode.Velocity, rps);
            }
            log.sub("Setting intake target speed to %f", targetRPS);
        }

        public double getTargetRPS() {
            return targetRPS;
        }

        public double getRPS() {
            return motor.getVelocity();
        }
    }
    
    /**
     * Update the operator console with the status of the intake subsystem.
     */
	@Override
	public void updateDashboard() {
        dashboard.putString("Intake position", isExtended() ? "extended" : isRetracted() ? "retracted" : "moving");
        dashboard.putNumber("Intake motor current", motor.getOutputCurrent());
        dashboard.putNumber("Intake motor target RPS", intakeWheel.getTargetRPS());
        dashboard.putNumber("Intake motor actual RPS", intakeWheel.getRPS());
	}
}

