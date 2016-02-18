package remotedesktop;

/*
 * @author Rakibul Islam
 */
import java.net.*;
import java.io.*;

public class Server {

    static String pass = "";

    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(1234);
        System.out.println("Socket is open: " + !ss.isClosed());

        Socket s[] = new Socket[2];
        DataInputStream[] inFromClient = new DataInputStream[2];
        DataOutputStream[] outToClient = new DataOutputStream[2];

        // Accept both clients
        for (int i = 0; i < 2; i++) {
            System.out.println("Waiting...");
            s[i] = ss.accept();
            inFromClient[i] = new DataInputStream(s[i].getInputStream());
            outToClient[i] = new DataOutputStream(s[i].getOutputStream());
            System.out.println("Client" + i + " is connected.");

            // Authenticate
            if (i == 0 && pass.equals("")) {
                pass = inFromClient[0].readUTF();
                outToClient[i].writeUTF("1");
            } else if (i == 1 && !pass.equals("")) {
                while (!inFromClient[i].readUTF().equals(pass)) {
                    outToClient[i].writeUTF("0");
                }
                outToClient[i].writeUTF("1");
            }
        }
        System.out.println("Authentication done successfully.");

        SThread sender2receiver = new SThread(inFromClient[0], outToClient[1], 0);
        SThread receiver2sender = new SThread(inFromClient[1], outToClient[0], 1);
        sender2receiver.join();
        receiver2sender.join();
    }
}

class SThread extends Thread {

    DataInputStream inFromClient;
    DataOutputStream outToClient;
    int src;

    public SThread(DataInputStream in, DataOutputStream out, int flag) {
        inFromClient = in;
        outToClient = out;
        src = flag;
        start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (src == 0) {
                    // Transfer images
                    int length = inFromClient.readInt();
                    byte[] img = new byte[length];
                    inFromClient.readFully(img, 0, img.length);
                    outToClient.writeInt(length);
                    outToClient.write(img);
                    System.out.println("Transfering Image...");

                } else if (src == 1) {
                    // Transfer mouse coordinates
                    outToClient.writeUTF(inFromClient.readUTF());
                    System.out.println("Transfering Mouse...");
                }
            }
        } catch (Exception e) {
            System.out.println("Error in Server thread: " + e);
        }
    }
}
