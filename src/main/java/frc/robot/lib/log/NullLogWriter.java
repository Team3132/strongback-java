package frc.robot.lib.log;

import frc.robot.interfaces.LogWriter;

/**
 * Throws away log messages. Useful for unit tests that logging
 * doesn't need to be persisted.
 */
public class NullLogWriter implements LogWriter {
	
	public NullLogWriter() {}
	
 	@Override
	public void write(String contents) {
		// Don't do anything with it.
	}

	@Override
	public void flush() {}

	@Override
	public void close() {}

	@Override
	public void createSymbolicLink(String dir, String prefix) {}
}
