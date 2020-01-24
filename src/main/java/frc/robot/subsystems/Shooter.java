package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import org.strongback.Executable;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import frc.robot.Constants;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ShooterInterface;
import frc.robot.lib.Subsystem;
import frc.robot.lib.NetworkTablesHelper;

/**
 * On the 2020 robot there is one shooter wheel that are under PID control for speed control.
 */
public class Shooter extends Subsystem implements ShooterInterface, Executable, DashboardUpdater {

    private BooleanSupplier cargoSupplier;

    private ShooterWheel flywheel;

    public Shooter(Motor shooterMotor, DashboardInterface dashboard, Log log) {
        super("Shooter", dashboard, log);
        this.cargoSupplier = cargoSupplier;
        flywheel = new ShooterWheel("flywheel", shooterMotor);
        //log.register(false, () -> hasCell(), "Shooter/beamBreakTripped");
    }

	@Override
	public void enable() {
		NetworkTablesHelper helper = new NetworkTablesHelper("drive");
		double shooterP = helper.get("p", Constants.SHOOTER_P);
		double shooterI = helper.get("i", Constants.SHOOTER_I);
		double shooterD = helper.get("d", Constants.SHOOTER_D);
		double shooterF = helper.get("f", Constants.SHOOTER_F);
		//flywheel.setPIDF(0, helper.get("p", Constants.DRIVE_P), helper.get("i", Constants.DRIVE_I),
		//		helper.get("d", Constants.DRIVE_D), helper.get("f", Constants.DRIVE_F));
		super.enable();
		log.info("Shooter PID values: %f %f %f %f", shooterP, shooterI, shooterD, shooterF);
	}

	public void disable() {
		super.disable();
		flywheel.setTargetSpeed(0);
	}
    
    /**
     * Set the speed on the shooter wheels.
     */
    @Override
    public ShooterInterface setTargetSpeed(double speed) { 
        flywheel.setTargetSpeed(speed);
        return this;
    }

    @Override
    public double getTargetSpeed() {
        return flywheel.getTargetSpeed(); 
    }

    @Override
    public boolean hasCell(){
        //System.out.println(cargoSupplier.getAsBoolean());
        return cargoSupplier.getAsBoolean();
    }

    protected class ShooterWheel {

        private final Motor motor;
        private double targetSpeed;
    
        public ShooterWheel(String name, Motor motor) {
            this.motor = motor;

            log.register(false, () -> flywheel.getTargetSpeed(), "Shooter/%s/speed", name)
            .register(false, motor::getOutputVoltage, "Shooter/%s/outputVoltage", name)
            .register(false, motor::getOutputPercent, "Shooter/%s/outputPercent", name)
            .register(false, motor::getOutputCurrent, "Shooter/%s/outputCurrent", name);
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
    }

    @Override
    public void updateDashboard() {
        dashboard.putString("Shooter cell status", hasCell() ? "has cell" : "no cell");
        dashboard.putNumber("Shooter target speed", flywheel.getTargetSpeed());
    }
}



