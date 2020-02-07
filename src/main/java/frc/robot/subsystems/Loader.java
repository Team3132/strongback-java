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
    private Solenoid loaderS, paddleS;
    private double targetCurrent = 0;
    

    public Loader(int teamNumber, Motor loaderMotor, Solenoid loaderSolenoid, Solenoid paddleSolenoid, DashboardInterface dashboard, Log log) {
        super("Passthrough", dashboard, log);
        this.motor = loaderMotor;
        this.loaderS = loaderSolenoid;
        this.paddleS = paddleSolenoid;

        log.register(true, () -> getTargetMotorOutput(), "%s/targetMotorOutput", name)
               .register(true, () -> isLoaderRetracted(), "%s/loaderRetracted", name)
               .register(true, () -> isPaddleRetracted(), "%s/paddleRetracted", name)
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

    public LoaderInterface setLoaderExtended(boolean extend) {
        if (extend) {
            loaderS.extend();
        } else {
            loaderS.retract();
        }
        return this;
    }

    public LoaderInterface setPaddleExtended(boolean extend) {
        if (extend) {
            paddleS.extend();
        } else {
            paddleS.retract();
        }
        return this;
    }


    @Override
    public boolean isLoaderExtended() {
        //log.sub("Is intake extended: " +  solenoid.isExtended());
        return loaderS.isExtended();
    }

    @Override
    public boolean isLoaderRetracted() {
        return loaderS.isRetracted();
    }

    @Override
    public boolean isPaddleExtended() {
        //log.sub("Is intake extended: " +  solenoid.isExtended());
        return paddleS.isExtended();
    }

    @Override
    public boolean isPaddleRetracted() {
        return paddleS.isRetracted();
    }
    /**
     * Update the operator console with the status of the hatch subsystem.
     */
    @Override
    public void updateDashboard() {
        dashboard.putString("Loader position", isLoaderExtended() ? "extended" : isLoaderRetracted() ? "retracted" : "moving");
        dashboard.putString("Loader Paddle position", isPaddleExtended() ? "extended" : isPaddleRetracted() ? "retracted" : "moving");
        dashboard.putNumber("Passthru motor current", motor.getOutputCurrent());
        dashboard.putNumber("Passthru motor percent", motor.getOutputPercent());
    }

    @Override
    public void disable()  {
        motor.set(ControlMode.PercentOutput, 0);
    }
}