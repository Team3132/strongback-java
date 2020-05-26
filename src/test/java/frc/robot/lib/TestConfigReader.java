package frc.robot.lib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class TestConfigReader {

	// Ensure all subsystems are enabled to their default values if the file is empty.
	@Test
	public void testMissingFile() throws IOException {
		Path tempDir = Files.createTempDirectory("robot");
		Path path = Paths.get(tempDir.toString(), "robot.config");
		ConfigReader config = new ConfigReader(path.toString());
		
		assertThat(config.getString("missing/string", "default_value"), is(equalTo("default_value")));
		assertThat(config.getBoolean("missing/boolean", false), is(equalTo(false)));
		assertThat(config.getInt("missing/int", 2), is(equalTo(2)));
		assertThat(config.getIntArray("missing/int/array", new int[]{2, 3}), is(equalTo(new int[]{2, 3})));
	}
	
	// Ensure all subsystems are enabled to their default values if the file is empty.
	@Test
	public void testEmptyFile() throws IOException {
		File file = File.createTempFile("robot", ".config");
		String exampleFile = file.toString() + ".example";
		ConfigReader config = new ConfigReader(file.toString());
		assertThat(file.exists(), is(equalTo(true)));
		
		assertThat(config.getString("missing/string", "default_value"), is(equalTo("default_value")));
		assertThat(config.getBoolean("missing/boolean", false), is(equalTo(false)));
		assertThat(config.getInt("missing/int", 2), is(equalTo(2)));
		assertThat(config.getIntArray("missing/int/array", new int[]{2, 3}), is(equalTo(new int[]{2, 3})));
		assertThat(exampleFile.toString().length(), greaterThan(20)); // make sure the example file isn't empty after first run of update
	}

	@Test
	public void testGetValueType() throws IOException {
		File file = File.createTempFile("robot", ".config");
		assertThat(file.exists(), is(equalTo(true)));

		StringBuilder str = new StringBuilder();
		str.append("robot/name=\"firstValue\"\n");  // First value of a dup is ignored.
		str.append("robot/name=\"unitTest\"\n");
		str.append("  drivebase/present=false\n");  // Leading spaces.
		str.append("pcm/canID=210\n");
		str.append("drivebase/left/canIDs/withEncoders=7, 9,15\n");
		str.append("drivebase/left/canIDs/withoutEncoders=\n");
		Files.write(file.toPath(), str.toString().getBytes(StandardCharsets.UTF_8));

		ConfigReader config = new ConfigReader(file.toString());

		assertThat(config.getString("robot/name", "not found"), is(equalTo("unitTest")));
		assertThat(config.getBoolean("drivebase/present", true), is(equalTo(false)));
		assertThat(config.getInt("pcm/canID", -1), is(equalTo(210)));
		assertThat(config.getIntArray("drivebase/left/canIDs/withEncoders", new int[]{}), is(equalTo(new int[] {7, 9, 15})));
		assertThat(config.getIntArray("drivebase/left/canIDs/withoutEncoders", new int[]{-1}), is(equalTo(new int[] {})));
	}
}
