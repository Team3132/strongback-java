package frc.robot.lib;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

/**
 */
public class RobotName {

    /**
     * Attempts to read the first line of a file to get the robot name. If the file
     * doesn't exist it will create a new random name, write the file and return
     * that.
     * @param path location on the filesystem to look. Normally on a usb flash drive
     * @return name of the robot as a String.
     */
    public static String get(String path) {
        String robotName;
        var filename = Paths.get(path, "robotname.txt");
        try {
            robotName = new String(Files.readAllBytes(filename));
            // Take the first line.
            robotName = robotName.split("\n")[0];
            System.out.println(robotName);
            return robotName;
        } catch (Exception e) {
            // File doesn't exist, let's make a random name so it doesn't
            // conflict with any other existing robot.
            Random rand = new Random();
            robotName = String.format("noname%06d", rand.nextInt(10000));
            FileWriter f;
            try {
                f = new FileWriter(filename.toString());
                f.write(robotName);
                f.close();
            } catch (IOException e1) {
                System.err.printf("Failed to get/create a robot name, giving up");
                return "badrobot";
            }
        }
        return robotName;
    }
}