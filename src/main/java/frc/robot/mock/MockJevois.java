package frc.robot.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import frc.robot.interfaces.JevoisInterface;

public class MockJevois implements JevoisInterface {

    List<String> lines = new ArrayList<String>();

    public MockJevois() {
    }

    @Override
    public void setCameraMode(CameraMode mode) {
    }

    /**
     * Returns the next available line queued up by sendLine(),
     * and blocks waiting if there is no line available.
     */
    @Override
    public String readLine() throws IOException {
        try {
            synchronized (lines) {
                // Spin here waiting for sendLine to insert something
                // into the list of lines.
                while (lines.isEmpty()) {
                    lines.wait();
                }
                // lines is no longer empty, return the first entry.
                return lines.remove(0);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "thread interrupted";
        }
    }

    @Override
    public String issueCommand(String command) throws IOException {
        //throw new IOException("mock");
        return "mock";
    }

    /**
     * Queue up a line for readLine() to return.
     */
    public void sendLine(String line) {
        synchronized (lines) {
            lines.add(line);
            // Let anyone waiting on lines know something has been inserted.
            lines.notifyAll();
        }

    }
}
