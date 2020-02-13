/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.mock;
import frc.robot.interfaces.NetworkTableHelperInterface;


/**
 * Add your docs here.
 */
public class MockNetworkTableHelper implements NetworkTableHelperInterface {

    @Override
    public String NetworkTableHelper(String defaultName) {
        return null;
    }

    @Override
    public double get(String key, double defaultValue) {
        return 0;
    }

    @Override
    public boolean get(String key, boolean defaultValue) {
        return false;
    }

    @Override
    public String get(String key, String defaultValue) {
        return null;
    }


}
