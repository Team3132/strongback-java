package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import org.strongback.Executable;
import org.strongback.components.TalonSRX;
import org.strongback.components.Solenoid;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.IntakeInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Intake Subsystem 2019:
 * On the 2019 robot the intake is pneumatically driven and using one motor to intake game objects 
 */
public class Intake extends Subsystem implements IntakeInterface, Executable, DashboardUpdater {
    private TalonSRX motor;
    private Solenoid solenoid;
    private BooleanSupplier sensor;
    private double targetCurrent;

    public Intake(TalonSRX motor, BooleanSupplier sensor, Solenoid solenoid, DashboardInterface dashboard, Log log) {
        super("Intake", dashboard, log);   
        this.motor = motor;
        this.solenoid = solenoid;
        this.sensor = sensor;

        log.register(true, () -> isExtended(), "%s/extended", name)
               .register(true, () -> isRetracted(), "%s/retracted", name)
               .register(true, () -> hasCargo(), "%s/cargoOnBoard", name)
			   .register(false, motor::getMotorOutputVoltage, "%s/outputVoltage", name)
			   .register(false, motor::getMotorOutputPercent, "%s/outputPercent", name)
			   .register(false, motor::getOutputCurrent, "%s/outputCurrent", name);
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
    
    @Override
    public boolean hasCargo() {
        return sensor.getAsBoolean();
    }
    
    @Override
    public void setMotorOutput(double current) {
        // Prevent intake wheel from damaging the body of the robot
        if (!isExtended() && current != 0.0) {
            log.error("Intake retracted, not turning motor on.");
            return;
        } 
        log.sub("Setting intake motor speed to %.1f", current);
        targetCurrent = current;
        // TODO: Use current mode instead of percent mode when the hardware
        // has been tested.
        motor.set(ControlMode.PercentOutput, current);
    }

    @Override
    public double getMotorOutput() {
        return targetCurrent;
    }
    
    /**
     * Update the operator console with the status of the intake subsystem.
     */
	@Override
	public void updateDashboard() {
        dashboard.putString("Intake position", isExtended() ? "extended" : isRetracted() ? "retracted" : "moving");
        dashboard.putString("Intake cargo status", hasCargo() ? "has cargo" : "empty");
        dashboard.putNumber("Intake motor current", motor.getOutputCurrent());
		dashboard.putNumber("Intake motor percent", motor.getMotorOutputPercent());
	}
}

