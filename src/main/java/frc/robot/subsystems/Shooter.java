package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Solenoid;

import frc.robot.Constants;
import frc.robot.interfaces.NetworkTableHelperInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ShooterInterface;
import frc.robot.lib.Subsystem;
import frc.robot.lib.NetworkTablesHelper;

/**
 * On the 2020 robot, there are three shooter motors. 
 * One with an encoder and the rest without, that are under PID control for speed control.
 */
public class Shooter extends Subsystem implements ShooterInterface, Executable, DashboardUpdater {

    private ShooterWheel flyWheel;
    private Solenoid solenoid;

    public Shooter(Motor shooterMotor, Solenoid solenoid, NetworkTableHelperInterface networkTable, DashboardInterface dashboard, Log log) {
        super("Shooter", networkTable, dashboard, log);
        this.solenoid = solenoid;
        flyWheel = new ShooterWheel(shooterMotor);

        log.register(true, () -> isHoodExtended(), "%s/extended", name)
               .register(true, () -> isHoodRetracted(), "%s/retracted", name);
    }

    //TODO: Get the PID values from the network tables.
	@Override
	public void enable() {
        double p = networkTable.get("p", Constants.SHOOTER_P);
        double i = networkTable.get("i", Constants.SHOOTER_I);
        double d = networkTable.get("d", Constants.SHOOTER_D);
        double f = networkTable.get("f", Constants.SHOOTER_F);
        super.enable();
        flyWheel.setPIDF(p, i, d, f); 
		log.info("Drivebase  PID values: %f %f %f %f", p, i, d, f);
	}

	public void disable() {
		super.disable();
        flyWheel.setTargetSpeed(0);
	}
    
    /**
     * Set the speed on the shooter wheels.
     */
    @Override
    public ShooterInterface setTargetSpeed(double speed) { 
        System.out.println("Setting target speed");
        flyWheel.setTargetSpeed(speed);
        return this;
    }

    @Override
    public double getTargetSpeed() {
        return flyWheel.getTargetSpeed(); 
    }
    
    public boolean isAtTargetSpeed() {
        if (flyWheel.getSpeed() >= flyWheel.getTargetSpeed()) {
            return true;
        }
        return false;
    }

    @Override
    public ShooterInterface setHoodExtended(boolean extend) {
        if (extend) {
            solenoid.extend();
        } else {
            solenoid.retract();
        }
        return this;
    }

    @Override
    public boolean isHoodExtended() {
        //log.sub("Is hood extended: " +  solenoid.isExtended());
        return solenoid.isExtended();
    }

    @Override
    public boolean isHoodRetracted() {
        return solenoid.isRetracted();
    }

    protected class ShooterWheel {

        private final Motor motor;
        private double targetSpeed;
    
        public ShooterWheel(Motor motor) {
            this.motor = motor;

            log.register(false, () -> flyWheel.getTargetSpeed(), "Shooter/flyWheel/targetSpeed", name)
            .register(false, motor::getSpeed, "Shooter/flyWheel/speed", name)
            .register(false, motor::getOutputVoltage, "Shooter/flyWheel/outputVoltage", name)
            .register(false, motor::getOutputPercent, "Shooter/flyWheel/outputPercent", name)
            .register(false, motor::getOutputCurrent, "Shooter/flyWheel/outputCurrent", name);
        }
        
        public void setTargetSpeed(double speed) {
            if (speed == targetSpeed) {
                 return;
            }
            targetSpeed = speed;
            // Note that if velocity mode is used and the speed is ever set to 0, 
            // change the control mode from percent output, to avoid putting
            // unnecessary load on the battery and motor.
            if (speed == 0) { 
                log.sub("Turning shooter wheel off.");
                motor.set(ControlMode.PercentOutput, 0); 
            } else {
                motor.set(ControlMode.Velocity, speed);
            }
            log.sub("Setting shooter target speed to %f", targetSpeed);
        }

        public double getTargetSpeed() {
            return targetSpeed;
        }

        public double getSpeed() {
            return motor.getSpeed();
        }

        public void setPIDF(double p, double i, double d, double f) {
            motor.setPIDF(0, p, i, d, f);
        }
    }

    @Override
    public void updateDashboard() {
        dashboard.putNumber("Shooter target speed", flyWheel.getTargetSpeed());
    }
}
