package frc.robot.lib.log;

/*
 * This class provides easy access to logging functions, allowing debugging
 * after the program has run and persisting through robot code restarts.
 * 
 * What it does depends on what user starts it.
 * - If running as lvuser, it assumes it's running on a RoboRio and will log to
 *   the flash drive for easy post match analysis.
 * - If started by any other user it will just print logging and not write
 *   anything to disk (eg in unit tests)
 * 
 * For text logs we have a series of calls: 
 *    log.{debug,info,warning,error}(String message)
 * 
 * Each message takes a varargs argument list, and prepends the message with a
 * timestamp and type of message.
 * 
 * Static implementation so that it doesn't need to be passed around everywhere.
 */

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.strongback.Strongback;

import frc.robot.Constants;
import frc.robot.interfaces.LogWriter;
import frc.robot.lib.RobotName;

/**
 * This is the log file data logger. We split the log into two separate log systems.
 * For the text file logging we use this method.
 * 
 * We create a single text log file <basePath>/<instance>.txt
 * and provide a variety of methods to append to that log file.
 */

public class Log {
	/**
	 * Logs to disk only.  For high volume debug messages
	 * @param message Message to log
	 * @param args arguments to the message format string
	 */
	public static void debug(String message, Object... args) {
		message = String.format(timeString() + ",D " + message + "\n", args); 
		// Don't print it to the console.
		writer.write(message);
	}
	
	/**
	 * Logs to disk and console.  For low volume informational messages (<1/sec).
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public static void info(String message, Object... args) {
		message = String.format(timeString() +",I " + message + "\n", args); 
		// Print to the console.
		System.out.print(message);
		writer.write(message);
	}
	
	/**
	 * Logs to disk and console. For warning Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public static void warning(String message, Object... args) {
		message = String.format(timeString() +",W " + message + "\n", args); 
		// Print to the console.
		System.err.print(message);
		writer.write(message);
	}
	
	/**
	 * Logs to disk and console. For important errors Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public static void error(String message, Object... args) {
		message = String.format(timeString() +",E " + message + "\n", args); 
		// Print to the console.
		System.err.print(message);
		writer.write(message);
	}

	/**
	 * Logs Exceptions to disk and console.
	 * @param message Message to Log
	 * @param e Exception to log after message
	 */
	public static void exception(String message, Exception e) {
		error(message + ": %s", e);
		for (StackTraceElement frame : e.getStackTrace()) {
			error("     at %s", frame.toString());			
		}
	}
	
	/**
	 * Implements the classic println interface. Single string to the console!
	 * @param message Message to Log
	 */
	public static void println(String message, Object... args) {
		message = String.format(timeString() + ",O " + message + "\n", args); 
		// Print to the console.
		System.out.print(message);
		writer.write(message);
	}

    /**
	 * Restart logging. Called each time robot is enabled or initialised.
	 */
	public static void restartLogs() {
		writer.flush();
		// Make the time start at zero within the log file. 
		timeOffset = Strongback.timeSystem().currentTime();
		// Create a new logger to get new files.
		writer = createWriter();
	}

	/**
	 * Create the date based symbolic links. These create symbolic links from date stamped
	 * version of the file to the actual file. This is a separate method as we delay
	 * creating these until we are reasonably sure the date is correct.
	 * @param timestamp The date to use.
	 */
	public static void createDateFiles(Calendar timestamp, String matchDescription) {
		String timestampStr = new SimpleDateFormat("yyyyMMdd_HH-mm-ss.SSS").format(timestamp.getTime());
		try {
			// Create links based on the timestamp.
			writer.createSymbolicLink(Constants.LOG_DATE_EXTENSION, timestampStr);
			writer.createSymbolicLink(Constants.LOG_EVENT_EXTENSION, matchDescription);
		} catch (Exception e) {
			System.out.printf("Error creating logging symlinks\n");
			e.printStackTrace();
		}
	}

	// Implementation only from here.
	
	private static double timeOffset = 0;
	private static LogWriter writer = createWriter();

	private static LogWriter createWriter() {
		if (System.getProperty("user.name").equals("lvuser")) {
			// Running on the robot. Log for real.
			String baseDir  = Paths.get(Constants.LOG_BASE_PATH, RobotName.get()).toString();
			long logNum = LogFileNumber.get();
			try {
				return new TimestampedLogWriter(baseDir, "log", logNum, "txt");
			} catch (IOException e) {
				System.err.println("Failed to create logger, maybe usb flash drive isn't plugged in?");
				e.printStackTrace();
				// Fall through to create a NullLogWriter();
			}
		}
		// Likely a unit test, only write to the console.
		return new NullLogWriter();
	}

	/*
	 * Create the timestamp for this message. We use the robot time, so each log
	 * entry is time stamped for when it happened during the match.
	 */
	private static String timeString() {
		return String.format("%.3f", Strongback.timeSystem().currentTime() - timeOffset);
	}
}
