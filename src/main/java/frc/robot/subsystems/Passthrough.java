package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.TalonSRX;
import frc.robot.interfaces.PassthroughInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

import com.ctre.phoenix.motorcontrol.ControlMode;

public class Passthrough extends Subsystem implements PassthroughInterface, Executable, DashboardUpdater {
    private TalonSRX motor;
    private double targetCurrent = 0;
    

    public Passthrough(int teamNumber, TalonSRX passthroughMotor, DashboardInterface dashboard, Log log) {
        super("Passthrough", dashboard, log);
        this.motor = passthroughMotor;

        log.register(true, () -> getTargetMotorOutput(), "%s/targetMotorOutput", name)
			   .register(false, motor::getMotorOutputVoltage, "%s/outputVoltage", name)
			   .register(false, motor::getMotorOutputPercent, "%s/outputPercent", name)
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
        log.sub("Setting passthru motor output to: %f", current);
        motor.set(ControlMode.PercentOutput, current);
        targetCurrent = current;
    }

    /**
     * Update the operator console with the status of the hatch subsystem.
     */
    @Override
    public void updateDashboard() {
        dashboard.putNumber("Passthru motor current", motor.getOutputCurrent());
        dashboard.putNumber("Passthru motor percent", motor.getMotorOutputPercent());
    }

    @Override
    public void disable()  {
        motor.set(ControlMode.PercentOutput, 0);
    }
}