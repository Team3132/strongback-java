package frc.robot.lib;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import org.apache.commons.text.StringEscapeUtils;

/**
 * This class is used to host a HTTP webserver on port 5801 allowing quick access to the config file.
 * The user is given a textarea to edit the current config file on the robot and there is also a checkbox
 * that allows them to select whether or not to restart the robot code after saving their changes to the config.
 */
public class ConfigServer extends Thread {

    private String webRoot;
    private String configFilename;
    // Port to listen connection
    private int port;

    public ConfigServer(String webRoot, String configFilename, int port) {
        this.webRoot = webRoot;
        this.configFilename = configFilename;
        this.port = port;
        start();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverConnect = new ServerSocket(port);
            System.out.println("Server started.\nListening for connections on port : " + port);
            while (true) {
                handleConnect(serverConnect.accept());
            }
        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

	private void handleConnect(Socket connect) {
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;

		try {
		    // Read characters from the client via input stream on the socket.
		    in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
		    // Get character output stream to client. (for headers)
		    out = new PrintWriter(connect.getOutputStream());
		    // Get binary output stream to client. (for requested data)
		    dataOut = new BufferedOutputStream(connect.getOutputStream());
		    // Get first line of the request from the client.
		    String input = in.readLine();
		    System.out.println("Input: " + input);
		    StringTokenizer parse = new StringTokenizer(input);
		    String method = parse.nextToken().toUpperCase(); // Get the HTTP method of the client.

		    switch (method) {
		    case "POST":
		        handlePost(in);
		        // Falling through to GET.
		    case "GET":
		    case "HEAD":
		        // GET or HEAD method
		        if (!method.equals("HEAD")) { // GET method for returning content
		            handleGet(out, dataOut);
		        }
		        break;
		    default:
		        System.out.println("Invalid method: " + method);
		        out.println("HTTP/1.1 501 Not Implemented");
		        out.println(); // Blank line between headers and content, very important !
		        out.flush();
		    }
		} catch (IOException ioe) {
		    System.err.println("Server error : " + ioe);
		} finally {
		    try {
		        in.close();
		        out.close();
		        dataOut.close();
		        connect.close();
		    } catch (Exception e) {
		        System.err.println("Error closing stream : " + e.getMessage());
		    }
		}
	}

    private void handlePost(BufferedReader in) throws IOException {
		/* This is the expected response from a web-browser, the important part is the last line with the user input.
		POST / HTTP/1.1
		Host: localhost:8080
		User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0
		Accept: text/html,application/xhtml+xml,application/xml;q=0.9,* /*;q=0.8
		Accept-Language: en-US,en;q=0.5
		Accept-Encoding: gzip, deflate
		Referer: http://localhost:8080/
		Connection: keep-alive
		Upgrade-Insecure-Requests: 1
		Content-Type: application/x-www-form-urlencoded
		Content-Length: 10

		input=test&input2=hello+world
		*/
		int length = 0;
		String input;
		do {
			input = in.readLine();
			if (input.startsWith("Content-Length")) {
				// Looking for a line like this:
				// Content-Length: 10
				String[] parts = input.split(" ");
				if (parts.length > 1) {
					length = Integer.parseInt(parts[1]);
				}
			}
			System.out.println("Input: " + input);
		} while (!input.equals(""));
		char[] params = new char[length];
		if (in.read(params, 0, length) != length) {
			System.out.println("Failed to read parameters from POST.");
			return;
		}
		String paramsString = new String(params);
		System.out.println("Filtered Params: " + paramsString);
		String[] inputs = paramsString.split("&");
		for (int i = 0; i < inputs.length; i++) {
			String[] param = inputs[i].split("=");
			System.out.println("Seen param: " + param[0]);
			if (param[0].toString().equals("config")) {
				saveConfig(URLDecoder.decode(param[1], "UTF-8"));
			} else if(param[0].toString().equals("restart")) {
				System.exit(0);
			} else {
				System.out.println("Unexpected param: " + param[0]);
			}
		}
	}

	/**
	 * Method that takes a string to save to the robot's config file.
	 * @param config Value to save to the config file.
	 */
	private void saveConfig(String config) {
		System.out.println("Save config: " + config);
		File file = new File(configFilename);
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(config);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleGet(PrintWriter out, BufferedOutputStream dataOut) throws IOException {
		String config = StringEscapeUtils.escapeHtml4(readFile(configFilename));
		String html = readFile(Paths.get(webRoot, "index.html").toString());
		byte[] fileData = html.replace("${CONFIG}", config).getBytes();
		// Send HTTP Headers
		out.println("HTTP/1.1 200 OK");
		out.println("Content-type: text/html");
		out.println("Content-length: " + fileData.length);
		out.println(); // Blank line between headers and content, very important !
		out.flush(); // Flush character output stream buffer
		dataOut.write(fileData, 0, fileData.length);
		dataOut.flush();
	}

	/**
	 * Method to read a file and return the content as a String.
	 * @param file Path of file to read.
	 * @return Content of file as String.
	 */
	private String readFile(String file) {
		String content = "failed to read file";
		try {
			content = new String(Files.readAllBytes(Paths.get(file)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

}