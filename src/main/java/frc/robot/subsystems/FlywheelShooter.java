package frc.robot.subsystems;

import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Solenoid;

import frc.robot.Config;
import frc.robot.interfaces.Dashboard;
import frc.robot.interfaces.Shooter;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;

/**
 * On the 2020 robot, there are three shooter motors. 
 * One with an encoder and the rest without, that are under PID control for speed control.
 */
public class FlywheelShooter extends Subsystem implements Shooter {

    private final ShooterWheel flyWheel;
    private final Solenoid hood;

    public FlywheelShooter(Motor shooterMotor, Solenoid solenoid, Dashboard dashboard) {
        super("FlywheelShooter", dashboard);
        this.hood = solenoid;
        flyWheel = new ShooterWheel(shooterMotor);
        Chart.register(() -> isHoodExtended(), "%s/extended", name);
        Chart.register(() -> isHoodRetracted(), "%s/retracted", name);
    }

    @Override
	public void disable() {
		super.disable();
        flyWheel.setTargetRPS(0);
	}
    
    /**
     * Set the speed on the shooter wheels.
     */
    @Override
    public Shooter setTargetRPS(double rps) { 
        flyWheel.setTargetRPS(rps);
        return this;
    }

    @Override
    public double getTargetRPS() {
        return flyWheel.getTargetRPS(); 
    }
    
    @Override
    public boolean isAtTargetSpeed() {
        return Math.abs(flyWheel.getRPS() - flyWheel.getTargetRPS()) < Config.shooter.speed.toleranceRPS;
    }

    @Override
    public Shooter setHoodExtended(boolean extend) {
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
        private double targetRPS;
    
        public ShooterWheel(Motor motor) {
            this.motor = motor;

            Chart.register(() -> getTargetRPS(), "%s/targetSpeed", name);
            Chart.register(() -> getRPS(), "%s/rps", name);
            Chart.register(motor::getOutputVoltage, "%s/outputVoltage", name);
            Chart.register(motor::getOutputPercent, "%s/outputPercent", name);
            Chart.register(motor::getOutputCurrent, "%s/outputCurrent", name);
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
                Log.debug("Turning shooter wheel off.");
                motor.set(ControlMode.DutyCycle, 0); 
            } else {
                motor.set(ControlMode.Speed, rps);
            }
            Log.debug("Setting shooter target speed to %f", targetRPS);
        }

        public double getTargetRPS() {
            return targetRPS;
        }

        public double getRPS() {
            return motor.getSpeed();
        }
    }

    @Override
    public void updateDashboard() {
        dashboard.putNumber("Shooter target rps", flyWheel.getTargetRPS());
        dashboard.putNumber("Shooter actual rps", flyWheel.getRPS());
        dashboard.putString("Shooter status", isAtTargetSpeed() ? "At target" : "Not at target");
        dashboard.putString("Shooter hood", isHoodExtended() ? "extended" : (isHoodRetracted() ? "retracted" : "moving"));
    }
}
