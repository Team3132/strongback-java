package frc.robot.subsystems;

import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Solenoid;

import frc.robot.Constants;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ShooterInterface;
import frc.robot.lib.Subsystem;

/**
 * On the 2020 robot, there are three shooter motors. 
 * One with an encoder and the rest without, that are under PID control for speed control.
 */
public class Shooter extends Subsystem implements ShooterInterface {

    private final ShooterWheel flyWheel;
    private final Solenoid hood;

    public Shooter(Motor shooterMotor, Solenoid solenoid, DashboardInterface dashboard, Log log) {
        super("Shooter", dashboard, log);
        this.hood = solenoid;
        flyWheel = new ShooterWheel(shooterMotor);
        log.register(true, () -> isHoodExtended(), "%s/extended", name)
            .register(true, () -> isHoodRetracted(), "%s/retracted", name);
    }

    @Override
	public void disable() {
		super.disable();
        flyWheel.setTargetRPM(0);
	}
    
    /**
     * Set the speed on the shooter wheels.
     */
    @Override
    public ShooterInterface setTargetRPM(double speed) { 
        flyWheel.setTargetRPM(speed);
        return this;
    }

    @Override
    public double getTargetRPM() {
        return flyWheel.getTargetRPM(); 
    }
    
    @Override
    public boolean isAtTargetSpeed() {
        return Math.abs(flyWheel.getRPM() - flyWheel.getTargetRPM()) < Constants.SHOOTER_SPEED_TOLERANCE_RPM;
    }

    @Override
    public ShooterInterface setHoodExtended(boolean extend) {
        if (extend) {
            hood.extend();
        } else {
            hood.retract();
        }
        return this;
    }

    @Override
    public boolean isHoodExtended() {
        return hood.isExtended();
    }

    @Override
    public boolean isHoodRetracted() {
        return hood.isRetracted();
    }

    protected class ShooterWheel {

        private final Motor motor;
        private double targetRPM;
    
        public ShooterWheel(Motor motor) {
            this.motor = motor;

            log.register(false, () -> flyWheel.getTargetRPM(), "shooter/flyWheel/targetSpeed", name)
            .register(false, motor::getVelocity, "shooter/flyWheel/rpm", name)
            .register(false, motor::getOutputVoltage, "shooter/flyWheel/outputVoltage", name)
            .register(false, motor::getOutputPercent, "shooter/flyWheel/outputPercent", name)
            .register(false, motor::getOutputCurrent, "shooter/flyWheel/outputCurrent", name);
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
                log.sub("Turning shooter wheel off.");
                motor.set(ControlMode.PercentOutput, 0); 
            } else {
                motor.set(ControlMode.Velocity, rpm);
            }
            log.sub("Setting shooter target speed to %f", targetRPM);
        }

        public double getTargetRPM() {
            return targetRPM;
        }

        public double getRPM() {
            return motor.getSpeed();
        }

        public void setPIDF(double p, double i, double d, double f) {
            motor.setPIDF(0, p, i, d, f);
        }
    }

    @Override
    public void updateDashboard() {
        dashboard.putNumber("Shooter target rpm", flyWheel.getTargetRPM());
        dashboard.putNumber("Shooter actual rpm", flyWheel.getRPM());
        dashboard.putString("Shooter status", isAtTargetSpeed() ? "At target" : "Not at target");
        dashboard.putString("Shooter hood", isHoodExtended() ? "extended" : (isHoodRetracted() ? "retracted" : "moving"));
    }
}
