package jamesnguyen.flic;

import android.widget.Toast;

import java.net.InetAddress;
import java.net.Socket;

public class ServerHandler implements Runnable{

    private static ServerHandler instance = new ServerHandler();

    private final static int PORT = 1995;
    private String HOST = "192.168.43.191";
    private Socket CURRENT_SOCKET;

    private ServerHandler() {}

    public static ServerHandler getInstance() {
        return ServerHandler.instance;
    }

    public void openConnection() {
        try {
            InetAddress serverAddr = InetAddress.getByName(HOST); //CONNECT TO SERVER PORT
            CURRENT_SOCKET = new Socket(serverAddr, PORT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(Socket socket) {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHostIP(String host) {
        this.HOST = host;
    }

    public Socket getCURRENT_SOCKET() {
        return CURRENT_SOCKET;
    }

    @Override
    public void run() {
        openConnection();
    }
}
