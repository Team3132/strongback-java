package frc.robot.lib.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import frc.robot.interfaces.LogWriter;

/**
 * Creates the files and the symbolic links for a single stream of data
 * normally on the USB flash drive. 
 *
 * 'Latest' symlink created to point to this file.
 */
public class TimestampedLogWriter implements LogWriter {
	
	private final String name; // eg chart
	private final String extn; // eg "html" or "csv"
	private final String baseDir; // All logs are below this directory
	private Path filePath = null;
	private BufferedWriter writer = null;

	/**
	 * Write free form data to a file and create multiple symbolic links to it.
	 * Used for csv and graphing files.
	 * 
	 * Creates
	 * <pre>
	 *    baseDir/
   	 *            data/name_000filenum.extn
   	 *            latest/Latest_name.extn -> data/name_filenum.extn
	 *            date/timestamp_name.extn -> ../data/name_filenum.extn 
	 *			  event/event_match_name.extn -> ../data/name_filenum.extn
	 * </pre>
	 *        
	 * @param baseDir  Where on the file system to put the logging directories.
	 * @param name  the type of data, eg "data", "chart"
	 * @param filenum  the number of the file. Incremented every start of the code.
	 * @param extn  the file extension
	 * @throws IOException 
	 */
	public TimestampedLogWriter(String baseDir, String name, long filenum, String extn) throws IOException {
		this.baseDir = baseDir;
		this.name = name;
		this.extn = extn;
		// The absolute path to the data file so we can write to the file.
		filePath = Paths.get(baseDir, "data", String.format("%s_%05d.%s", name, filenum, extn));
		// Ensure the parent directory exists.
		Files.createDirectories(filePath.getParent());
		// Create the file writer.
		writer = Files.newBufferedWriter(filePath);
		createSymbolicLink("latest/Latest");
	}

    /**
	 * Create symbolic links to the file. Used to create Latest_x and dated
	 * symlinks. This version also takes a directory.
	 * 
	 * @param dir sub directory relative to logging base dir to put link in.
	 * @param prefix as in <prefix>_chart.html
	 */
	@Override
	public void createSymbolicLink(String dir, String prefix) {
		Path path = Paths.get(dir, prefix);
		createSymbolicLink(path.toString());
	}
	
	/**
	 * Create symbolic links to the file. Used to create Latest_x and dated
	 * symlinks. 
	 * 
	 * @param prefix as in <prefix>_chart.html
	 */
	private void createSymbolicLink(String prefix) {
		Path symlinkPath = Paths.get(baseDir, String.format("%s_%s.%s", prefix, name, extn));
		createSymbolicLink(symlinkPath, filePath);
	}

	/**
	 * Create symbolic links to the file. Used to create Latest_x and dated
	 * symlinks. 
	 * 
	 * @param prefix as in <prefix>_chart.html
	 */
	private static void createSymbolicLink(Path from, Path to) {
		Path relPath = createRelativePath(from, to);
		try {
			// Ensure the parent directory exists.
			Files.createDirectories(from.getParent());
			Files.deleteIfExists(from);
			Files.createSymbolicLink(from, relPath);
		} catch (Exception e) {
			//e.printStackTrace();
			System.err.printf("Failed to create symbolic link: Are we on windows? %s", e);
		}
	}

	/**
	 * Does some relative path magic to ensure that the target is
	 * relative to the file so that it will work no matter where the file system
	 * is mounted.
	 * 
	 * Examples:
	 *   prefix="Latest" creates a symlink "Latest_chart.html" -> "data/chart_00007.html"
	 *   prefix="date/20180303" creates a symlink "date/20180303_chart.html" -> "../data/chart_00007.html"
	 *   
	 * @param from the full path to the symlink.
	 * @param to the full path to the file to link to.
	 * @return the relative path to the file to link to from <code>from</code>'s perspective
	 */
	private static Path createRelativePath(Path from, Path to) {
		// Make a relative path out of this path so the symbolic link will continue to
		// work no matter where the USB flash drive is mounted.
		return from.getParent().relativize(to);
	}
 
 	@Override
	public void write(String contents) {
		if (writer == null) return; // File logging not enabled.
		try {
			writer.write(contents);
			writer.flush();
		} catch (Exception e) {
			// nothing to do. If we can't write to the log file it's not a disaster.
		}
	}
	
	@Override
	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			// nothing to do. If we can't write to the log file it's not a disaster.
		}
	}
	
	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
