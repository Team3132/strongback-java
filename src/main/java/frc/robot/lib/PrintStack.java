package frc.robot.lib;

public class PrintStack {
	
	public static void trace() {
		try {
			throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
