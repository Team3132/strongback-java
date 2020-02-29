package frc.robot.lib;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
import frc.robot.interfaces.NetworkTableHelperInterface;

/**
 * Wrapper for accessing values from NetworkTables.
 * If the value isn't already in the config, it will insert the supplied default.
 * Note: This will not work from within unit tests as it requires loading a
 * shared library and that will only work on the roborio :(
 */
public class NetworkTablesHelper implements NetworkTableHelperInterface{
	private NetworkTable table;
	private final String tableName;
    
	public NetworkTablesHelper(String tableName) {
            this.tableName = tableName;
            table = NetworkTableInstance.getDefault().getTable(tableName);
	}
	
    // double
    public double set(String key, double defaultValue) {
        NetworkTableEntry entry = table.getEntry(key);
        if (entry.getType() == NetworkTableType.kUnassigned) {
            System.out.println("Unable to get the value for '" + key
                    + "' in table '" + tableName + "'");
        	entry.setDouble(defaultValue);
        }
        return entry.getDouble(defaultValue);
    }
    
    // boolean
    public boolean set(String key, boolean defaultValue) {
        NetworkTableEntry entry = table.getEntry(key);
        if (entry.getType() == NetworkTableType.kUnassigned) {
            System.out.println("Unable to get the value for '" + key
                    + "' in table '" + tableName + "'");
        	entry.setBoolean(defaultValue);
        }
        return entry.getBoolean(defaultValue);
    }

    // String
    public String set(String key, String defaultValue) {
        NetworkTableEntry entry = table.getEntry(key);
        if (entry.getType() == NetworkTableType.kUnassigned) {
            System.out.println("Unable to get the value for '" + key
                    + "' in table '" + tableName + "'");
        	entry.setString(defaultValue);
        }
        return entry.getString(defaultValue);
    }
}