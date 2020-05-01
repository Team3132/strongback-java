package frc.robot.drive.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static frc.robot.interfaces.DrivebaseInterface.DriveRoutineParameters.generateTrajectory;

import org.junit.Test;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;

import frc.robot.Constants;

/**
 * Tests trajectory generation and caching 
 * 
 * To run the tests: ./gradlew test --tests "frc.robot.drive.util.TestTrajectoryCaching"
 */
public class TestTrajectoryCaching {

    Pose2d start = new Pose2d(0, 0, new Rotation2d(0));
    List<Translation2d> interiorWaypoints = List.of();
    Pose2d end = new Pose2d(1, 1, new Rotation2d(0));
    boolean forward = true;
    boolean relative = true; // should not affect trajectories

    /**
     * Check that cached trajectories return the same trajectory as TrajectoryGenerator
     */
    private void testTrajectory(Pose2d start, List<Translation2d> interiorWaypoints, Pose2d end, boolean forward, boolean relative) {
        // Ensure there is no existing trajectory file
        clearPath(start, interiorWaypoints, end, forward);
        // Creating trajectory file (and saving it to deploy/paths/test)
        Trajectory trajectoryA = generateTrajectory(start, interiorWaypoints, end, forward, relative);      
        // Compare created trajectory to TrajectoryGenerator's trajectory
        Trajectory expectedTrajectory = TrajectoryGenerator.generateTrajectory(start, interiorWaypoints, end, createConfig(forward));
        assertTrue(trajectoryA.getStates().equals(expectedTrajectory.getStates()));

        // Creating trajectory from existing file we created earlier
        Trajectory trajectoryB = generateTrajectory(start, interiorWaypoints, end, forward, relative);    
        // Compare trajectory to TrajectoryGenerator's trajectory
        assertTrue(trajectoryB.getStates().equals(expectedTrajectory.getStates()));
    }

    @Test
    public void testInitial() {
        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /**
     * Changing Pose2d start
     */
    @Test
    public void testStart() {
        start = new Pose2d(-1,-1, new Rotation2d(30));

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /*
    * Changing List<Translation2d> interiorWaypoints
    */
    @Test
    public void testInteriorWaypoints() {
        Translation2d translation1 = new Translation2d(2,2);
        Translation2d translation2 = new Translation2d(-5,-5);
        interiorWaypoints = List.of(translation1, translation2);

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /*
    * Changing Pose2d end 
    */
    @Test
    public void testEnd() {
        end = new Pose2d(1,1, new Rotation2d(5));

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /*
    * Changing boolean forward 
    */
    @Test
    public void initialTest() {
        forward = false; 

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /*
    * Changing boolean relative 
    */
    @Test
    public void testRelative() {        
        relative = false;

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }
    
    /**
     * Removes trajectory file
     */
    private void clearPath(Pose2d start, List<Translation2d> interiorWaypoints, Pose2d end, boolean forward) {
        int hash = Arrays.deepHashCode(new Object[] { start, interiorWaypoints, end, forward });
        // System.out.println(hash);
        String trajectoryJSON = "paths/test/" + String.valueOf(hash) + ".wpilib.json";
        Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON);
        
        try {
            Files.deleteIfExists(trajectoryPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create config for trajectory
     */
    private TrajectoryConfig createConfig (boolean forward) {
        TrajectoryConfig config = new TrajectoryConfig(Constants.DriveConstants.kMaxSpeedMetersPerSecond,
                Constants.DriveConstants.kMaxAccelerationMetersPerSecondSquared)
                        // Add kinematics to ensure max speed is actually obeyed
                        .setKinematics(Constants.DriveConstants.kDriveKinematics)
                        // Apply the voltage constraint
                        .addConstraint(Constants.DriveConstants.kAutoVoltageConstraint)
                        .setReversed(!forward);
        return config;
    }
}
