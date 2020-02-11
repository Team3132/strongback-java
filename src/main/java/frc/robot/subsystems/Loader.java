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
    private Motor motor, motorIn, motorOut;
    private Solenoid loaderS, paddleS;
    private double outCurrent = 0;
    

    public Loader(int teamNumber, Motor loaderMotor, Motor loaderInMotor, Motor loaderOutMotor, Solenoid loaderSolenoid, Solenoid paddleSolenoid, DashboardInterface dashboard, Log log) {
        super("Passthrough", dashboard, log);
        this.motor = loaderMotor;
        this.motorIn = loaderInMotor;
        this.motorOut = loaderOutMotor;
        this.loaderS = loaderSolenoid;
        this.paddleS = paddleSolenoid;
        log.register(true, () -> getTargetOutMotorOutput(), "%s/targetOutMotorOutput", name)
               .register(true, () -> motorIn.getVelocity(), "%s/targetInMotorOutput", name)
               .register(true, () -> motor.getVelocity(), "%s/targetMainMotorOutput", name)
               .register(true, () -> isLoaderRetracted(), "%s/loaderRetracted", name)
               .register(true, () -> isPaddleRetracted(), "%s/paddleRetracted", name)
			   .register(false, motor::getOutputVoltage, "%s/outputVoltage", name)
			   .register(false, motor::getOutputPercent, "%s/outputPercent", name)
			   .register(false, motor::getOutputCurrent, "%s/outputCurrent", name);
    }

	@Override
	public double getTargetOutMotorOutput() {
		return outCurrent;
	}

	@Override
	public void setTargetMotorVelocity(double velocity) {
        log.sub("Setting loader motor velocity to: %f", velocity);
        motor.set(ControlMode.Velocity, velocity);
    }
    @Override
	public void setTargetInMotorVelocity(double velocity) {
        log.sub("Setting loader motor velocity to: %f", velocity);
        motorIn.set(ControlMode.Velocity, velocity);
    }
    @Override
	public void setTargetOutMotorOutput(double outCurrent) {
        log.sub("Setting loader motor output to: %f", outCurrent);
        motorOut.set(ControlMode.PercentOutput, outCurrent);
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
        dashboard.putNumber("Loader motor velocity", motor.getVelocity());
        dashboard.putNumber("Loader motor velocity", motorIn.getVelocity());
        dashboard.putNumber("Loader motor velocity", motorOut.getOutputCurrent());
    }

    @Override
    public void disable()  {
        motor.set(ControlMode.Velocity, 0);
        motorIn.set(ControlMode.Velocity, 0);
        motorOut.set(ControlMode.PercentOutput, 0);
    }
}