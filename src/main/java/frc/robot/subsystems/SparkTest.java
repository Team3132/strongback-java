package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.SparkTestInterface;
import frc.robot.lib.Subsystem;

/**
 * Dummy subsystem to drive the Spark MAX for testing purposes.
 * This was used to qualify the Spark MAX motor controllers.
 */
public class SparkTest extends Subsystem implements SparkTestInterface, Executable, DashboardUpdater {
    private Motor motor;
    private double targetOutput = 0;

    public SparkTest(Motor motor, DashboardInterface dashboard, Log log) {
        super("SparkTest", dashboard, log);   
        this.motor = motor;

        log.register(false, motor::getOutputPercent, "%s/outputPercent", name)
			   .register(false, motor::getVelocity, "%s/speed", name)
			   .register(false, motor::getPosition, "%s/position", name)
			   .register(false, motor::getOutputCurrent, "%s/outputCurrent", name)
			   .register(false, this::getMotorOutput, "%s/target", name);
    }

    @Override
    public void setMotorOutput(double output) {
        //log.sub("Setting spark test motor output to %.1f", output);
        targetOutput = output;
        //motor.set(output, ControlType.kDutyCycle);
        motor.set(ControlMode.Velocity ,output);
    }

    @Override
    public double getMotorOutput() {
        return targetOutput;
    }
    
    /**
     * Update the operator console with the status of the intake subsystem.
     */
	@Override
	public void updateDashboard() {
        dashboard.putNumber("Spark test motor target", getMotorOutput());
        dashboard.putNumber("Spark test motor current", motor.getOutputCurrent());
        dashboard.putNumber("Spark test motor percent", motor.getOutputPercent());
        dashboard.putNumber("Spark test motor speed", motor.getVelocity());
        dashboard.putNumber("Spark test motor position", motor.getPosition());
	}
}

