package frc.robot.drive.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
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
import edu.wpi.first.wpilibj.trajectory.TrajectoryUtil;
import edu.wpi.first.wpilibj.Filesystem;

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

        // Double check that file does not exist
        assertFalse(Files.exists(getPath(start, interiorWaypoints, end, forward)));

        // Creating trajectory file (and saving it to deploy/paths/test)
        Trajectory trajectoryA = generateTrajectory(start, interiorWaypoints, end, forward, relative);

        // Double check that file has been created
        assertTrue(Files.exists(getPath(start, interiorWaypoints, end, forward)));      

        // Compare created trajectory to TrajectoryGenerator's trajectory
        Trajectory expectedTrajectory = TrajectoryGenerator.generateTrajectory(start, interiorWaypoints, end, createConfig(forward));
        assertTrue(trajectoryA.getStates().equals(expectedTrajectory.getStates()));

        // Creating trajectory from existing file we created earlier
        Trajectory trajectoryB = generateTrajectory(start, interiorWaypoints, end, forward, relative);    
        // Compare trajectory to TrajectoryGenerator's trajectory
        assertTrue(trajectoryB.getStates().equals(expectedTrajectory.getStates()));
    }

    /**
     * Test that we are actually reading from a cached file (src/main/deploy/paths/test/1532419827.wpilib.json)
     */

    @Test
    public void testReadingFromFile() {
        
        Pose2d testStart = new Pose2d(-31, 32, new Rotation2d(10));
        Translation2d testTranslation1 = new Translation2d(1,1);
        Translation2d testTranslation2 = new Translation2d(-2,-2);
        List<Translation2d> testInteriorWaypoints = List.of(testTranslation1, testTranslation2);
        Pose2d testEnd = new Pose2d(53,31, new Rotation2d(-80));
        boolean testForward = true;

        int hash = Arrays.deepHashCode(new Object[] {testStart, testInteriorWaypoints, testEnd, testForward});
        
        String trajectoryJSON = "paths/test/" + String.valueOf(hash) + ".wpilib.json";
        Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON);
            
        try {
            Files.deleteIfExists(trajectoryPath);
        } catch (IOException e) {
            System.out.println(e);
        }
        
        Trajectory trajectory = TrajectoryGenerator.generateTrajectory(testStart, testInteriorWaypoints, testEnd, createConfig(testForward));

        try {
            TrajectoryUtil.toPathweaverJson(trajectory, trajectoryPath);
        } catch (IOException e) {
            fail(e.toString());
        }

        Trajectory trajectoryA;
        try {
            trajectoryA = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
        } catch (FileNotFoundException e) {
            fail("Trajectory file not found.");
            return;
        } catch (IOException e1) {
            fail(e1.toString());
            return;
        }
        assertTrue(trajectoryA.getStates().equals(trajectory.getStates()));
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
     * returns path for trajectory file
     */
    private Path getPath(Pose2d start, List<Translation2d> interiorWaypoints, Pose2d end, boolean forward) {
        int hash = Arrays.deepHashCode(new Object[] { start, interiorWaypoints, end, forward });
        // System.out.println(hash);
        String trajectoryJSON = "paths/test/" + String.valueOf(hash) + ".wpilib.json";
        Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON);
        return trajectoryPath;
    }

    /**
     * Removes trajectory file
     */
    private void clearPath(Pose2d start, List<Translation2d> interiorWaypoints, Pose2d end, boolean forward) {
        try {
            Files.deleteIfExists(getPath(start, interiorWaypoints, end, forward));
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
