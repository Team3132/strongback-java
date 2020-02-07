package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

public class Loader extends Subsystem implements LoaderInterface, Executable, DashboardUpdater {
    private Motor motor;
    private Solenoid solenoid;
    private double targetCurrent = 0;
    

    public Loader(int teamNumber, Motor loaderMotor, Solenoid loaderSolenoid, DashboardInterface dashboard, Log log) {
        super("Passthrough", dashboard, log);
        this.motor = loaderMotor;
        this.solenoid = loaderSolenoid;

        log.register(true, () -> getTargetMotorOutput(), "%s/targetMotorOutput", name)
               .register(true, () -> isRetracted(), "%s/retracted", name)
			   .register(false, motor::getOutputVoltage, "%s/outputVoltage", name)
			   .register(false, motor::getOutputPercent, "%s/outputPercent", name)
			   .register(false, motor::getOutputCurrent, "%s/outputCurrent", name);
    }

	@Override
	public double getTargetMotorOutput() {
		return targetCurrent;
	}

	@Override
	public void setTargetMotorOutput(double current) {
//        if (current == targetCurrent) return;
        // TODO: Use current mode once the passthru hardware has been tested.
        log.sub("Setting loader motor output to: %f", current);
        motor.set(ControlMode.PercentOutput, current);
        targetCurrent = current;
    }

    public LoaderInterface setExtended(boolean extend) {
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
     * Update the operator console with the status of the hatch subsystem.
     */
    @Override
    public void updateDashboard() {
        dashboard.putString("Hopper position", isExtended() ? "extended" : isRetracted() ? "retracted" : "moving");
        dashboard.putNumber("Passthru motor current", motor.getOutputCurrent());
        dashboard.putNumber("Passthru motor percent", motor.getOutputPercent());
    }

    @Override
    public void disable()  {
        motor.set(ControlMode.PercentOutput, 0);
    }
}