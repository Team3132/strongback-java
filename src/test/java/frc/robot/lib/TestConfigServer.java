package frc.robot.lib;

import frc.robot.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class TestConfigServer {

    private void write(File file, String data) {
        try {
            Files.write(Paths.get(file.getAbsolutePath()), data.getBytes());
            System.out.println("Writing to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String configContent = ""+
"<\\textarea><h1>test</h1>\n" +
"@#$%^&*^%$#%@#%%^$?{5415;3\n" +
"@!^$#&*<><><><><><><>#@^#<$>%\n" +
"\n" +
"cwf5h4egbe4$GH$G#WERB%G\n" +
"      \n" +
"testtesttesttesttesttesttesttesttest\n" +
"vfrg<><><><><???0110100001101001R$%Y#$#$H$#GEBV#V$\n" +
"vGREG#$https://www.youtube.com/watch?v=ub82Xb1C8oseH$#F#FESE\n" +
"^#%>^@?!%$%G#FWFER#@^$#&\n" +
"😀 😁 😂 🤣 😃 😄 😅 😆\n" +
"F#F#$%  %$#$FGEWBREW\n" +
"(╯°□°)╯︵ ┻━┻\n" +
"qfewg#Y#r3fG$H^^$WGH34wgd\n" +
"end.";

    /**
     * Method to start the config server at localhost:5801 for manual testing.
    */
    @Test
    public void testConfigServer() throws InterruptedException, IOException {
        Path tempDir = Files.createTempDirectory("robot");
        File config = new File(tempDir.toString(), "config.txt");
        String webRoot = Paths.get("src","main", "deploy", "www").toString();
        write(config, configContent);
        new ConfigServer(webRoot, config.getAbsolutePath(), Constants.CONFIG_WEB_PORT);
        //This line makes the test wait 100 seconds, only use when testing the webserver.
        //Thread.sleep(100000);
	}
}