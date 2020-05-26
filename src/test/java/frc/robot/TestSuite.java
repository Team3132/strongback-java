package frc.robot;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.strongback.command.TestCommandGroup;
import frc.robot.controller.TestController;
import frc.robot.lib.TestConfigReader;
import frc.robot.lib.TestLogFileWriter;
import frc.robot.lib.TestMovementSimulator;
import frc.robot.lib.TestRedundantTalonSRX;
import frc.robot.subsystems.TestDrivebase;
import frc.robot.subsystems.TestLocation;
import frc.robot.subsystems.TestVision;

@RunWith(Suite.class)


@Suite.SuiteClasses({
		TestCommandGroup.class,
		TestController.class,
        TestConfigReader.class,
        TestLogFileWriter.class,
        TestMovementSimulator.class,
        TestRedundantTalonSRX.class,
        TestDrivebase.class,
        TestLocation.class,
        TestVision.class,
})

/**
 * Used to run all our tests via junit commandline/ant
 * See https://github.com/junit-team/junit4/wiki/aggregating-tests-in-suites
 */
public class TestSuite {
}