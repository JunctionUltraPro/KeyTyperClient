package jamesnguyen.flic;

import java.io.PrintWriter;
import java.net.Socket;

public class StreamHandler {
    private static StreamHandler instance = new StreamHandler();

    private StreamHandler() {

    }

    public static StreamHandler getInstance() {
        return StreamHandler.instance;
    }

    public void writeOutputStream(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
