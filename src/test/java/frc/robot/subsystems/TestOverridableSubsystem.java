package frc.robot.subsystems;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import frc.robot.interfaces.IntakeInterface;
import frc.robot.interfaces.Log;
import frc.robot.mock.MockIntake;
import frc.robot.mock.MockLog;

public class TestOverridableSubsystem {
    private Log log = new MockLog();
    
    @Test
    public void testNormalMode() {
        // This is when the controller should talk through to the real
        // subsystem and the button box is passed through to the mock.
        // We only want to use mock here so that they can be checked.
        IntakeInterface real = new MockIntake(log);
        IntakeInterface simulator = new MockIntake(log);
        IntakeInterface mock = new MockIntake(log);
        // Create the sim and pass it the three different endpoints.
        OverridableSubsystem<IntakeInterface> intakeOverride = new OverridableSubsystem<IntakeInterface>("intake", IntakeInterface.class, real, simulator, mock, log);
        // Get the endpoint that the controller would use.
        IntakeInterface normalIntake = intakeOverride.getNormalInterface();	
        // Get the endpoint that the diag box uses.
        IntakeInterface overrideIntake = intakeOverride.getOverrideInterface();
        // Tell the different mocks that they should all have zero power.
        real.setTargetRPS(0);
        simulator.setTargetRPS(0);
        mock.setTargetRPS(0);
        // Put the overridable interface into automatic mode (controller talks to the real subsystem)
        intakeOverride.setAutomaticMode();
        // Pretend to be the controller and send through a command.
        normalIntake.setTargetRPS(1);
        // Check that only the real interface got told about it.
        assertThat(real.getTargetRPS(), is(closeTo(1.0, 0.1)));
        assertThat(simulator.getTargetRPS(), is(closeTo(0, 0.1)));
        assertThat(mock.getTargetRPS(), is(closeTo(0, 0.1)));
        // Pretend to be the diag box and send through a command.
        overrideIntake.setTargetRPS(-1);
        // Check that only the real interface got told about it.
        assertThat(real.getTargetRPS(), is(closeTo(1.0, 0.1)));
        assertThat(simulator.getTargetRPS(), is(closeTo(0, 0.1)));
        assertThat(mock.getTargetRPS(), is(closeTo(-1, 0.1)));

        // Now change to manual mode. Controller should talk to the simulator
        // and the diag box to the real interface.
        intakeOverride.setManualMode();
        // Pretend to be the controller and send through a command.
        normalIntake.setTargetRPS(0.5);
        // Check that only the simulator got told about it.
        assertThat(real.getTargetRPS(), is(closeTo(1.0, 0.1)));
        assertThat(simulator.getTargetRPS(), is(closeTo(0.5, 0.1)));
        assertThat(mock.getTargetRPS(), is(closeTo(-1, 0.1)));
        // Pretend to be the diag box and send through a command.
        overrideIntake.setTargetRPS(-0.5);
        // Check that only the real interface got told about it.
        assertThat(real.getTargetRPS(), is(closeTo(-0.5, 0.1)));
        assertThat(simulator.getTargetRPS(), is(closeTo(0.5, 0.1)));
        assertThat(mock.getTargetRPS(), is(closeTo(-1, 0.1)));

        // And finally, turn it off. Nothing should talk to the real subsystem.
        // The controller should talk to the simulator and the diag box to
        // the mock.
        intakeOverride.turnOff();
        // Pretend to be the controller and send through a command.
        normalIntake.setTargetRPS(0.25);
        // Check that only the simulator got told about it.
        assertThat(real.getTargetRPS(), is(closeTo(-0.5, 0.1)));
        assertThat(simulator.getTargetRPS(), is(closeTo(0.25, 0.1)));
        assertThat(mock.getTargetRPS(), is(closeTo(-1, 0.1)));
        // Pretend to be the diag box and send through a command.
        overrideIntake.setTargetRPS(-0.25);
        // Check that only the mock got told about it.
        assertThat(real.getTargetRPS(), is(closeTo(-0.5, 0.1)));
        assertThat(simulator.getTargetRPS(), is(closeTo(0.25, 0.1)));
        assertThat(mock.getTargetRPS(), is(closeTo(-0.25, 0.1)));
    }

}
