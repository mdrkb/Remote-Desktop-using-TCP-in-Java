package remotedesktop;

/*
 * @author Rakibul Islam
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ClientSender {

    static String ip = "192.168.43.166";

    static Socket socket;
    static DataInputStream inFromServer;
    static DataOutputStream outToServer;

    public void createPromptUI() {
        JFrame framePrompt = new JFrame("Rem0te Deskt0p - Sender");
        framePrompt.setBounds(400, 150, 285, 340);
        framePrompt.setResizable(false);
        framePrompt.setAlwaysOnTop(true);
        framePrompt.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel1 = new JPanel(new GridBagLayout());
        framePrompt.getContentPane().add(panel1, BorderLayout.NORTH);
        GridBagConstraints c = new GridBagConstraints();

        Icon icon = new ImageIcon(getClass().getResource("icon.png"));
        JLabel labelImg = new JLabel(icon);
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(20, 10, 10, 10);
        panel1.add(labelImg, c);

        JLabel labelPass = new JLabel("Enter Password");
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(10, 10, 10, 10);
        panel1.add(labelPass, c);

        JPasswordField password = new JPasswordField(15);
        password.setEchoChar('*');
        password.setHorizontalAlignment(JTextField.CENTER);
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 10, 10, 10);
        c.ipady = 5;
        panel1.add(password, c);

        JLabel labelStatus = new JLabel("Authentication failed. Try again...");
        labelStatus.setForeground(Color.red);
        c.gridx = 0;
        c.gridy = 3;
        panel1.add(labelStatus, c);
        labelStatus.setVisible(false);

        JButton button = new JButton("Connect");
        c.gridx = 0;
        c.gridy = 4;
        c.insets = new Insets(0, 0, 0, 0);
        panel1.add(button, c);

        framePrompt.setVisible(true);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String pass = new String(password.getPassword());
                    outToServer.writeUTF(pass);
                    framePrompt.setVisible(false);
                    createStatusUI();
                } catch (Exception ex) {
                    System.out.println("Exception in createPromptUI(): " + ex);
                }
            }
        });
    }

    public void createStatusUI() {
        JFrame frame2 = new JFrame("Desktop sharing is 0N");
        frame2.setBounds(10, 10, 260, 0);
        frame2.setResizable(false);
        frame2.setVisible(true);
        try {
            SenderThread sendImage = new SenderThread(inFromServer, outToServer, 0);
            SenderThread getClick = new SenderThread(inFromServer, outToServer, 1);
            sendImage.join();
            getClick.join();
        } catch (Exception e) {
            System.out.println("Exception in createStatusUI(): " + e);
        }
    }

    public static void main(String[] args) throws Exception {
        socket = new Socket(ip, 1234);
        inFromServer = new DataInputStream(socket.getInputStream());
        outToServer = new DataOutputStream(socket.getOutputStream());
        new ClientSender().createPromptUI();
    }
}

class SenderThread extends Thread {

    DataInputStream inFromServer;
    DataOutputStream outToServer;
    int flag;

    int width;
    int height;

    public SenderThread(DataInputStream in, DataOutputStream out, int f) {
        inFromServer = in;
        outToServer = out;
        flag = f;
        width = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()).width;
        height = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()).height;
        start();
    }

    public byte[] getScreenshot(int x, int y) throws Exception {
        Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()); // Get screen rectangle
        BufferedImage screenShot = new Robot().createScreenCapture(rect);  // Get screenshot

        Image img = screenShot.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bufImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // Create a buffer
        Graphics2D g = bufImg.createGraphics(); // Create image graphics for buffer
        g.drawImage(img, 0, 0, null); // Draw the image into buffer
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(screenShot, "jpg", baos);
        baos.flush();
        return baos.toByteArray();
    }

    public void click(int x, int y, int z) throws Exception {
        Robot bot = new Robot();
        bot.mouseMove(x, y - 15);
        System.out.println(x + " : " + y);
        if (z == 1) {
            bot.mousePress(InputEvent.BUTTON1_MASK);
            bot.mouseRelease(InputEvent.BUTTON1_MASK);
        } else if (z == 3) {
            bot.mousePress(InputEvent.BUTTON3_MASK);
            bot.mouseRelease(InputEvent.BUTTON3_MASK);
        }
    }

    @Override
    public void run() {
        try {
            int x = 0, y = 0, z = 0;
            while (true) {
                if (flag == 0) {
                    // Send images
                    byte[] img = getScreenshot(x, y);
                    outToServer.writeInt(img.length);
                    outToServer.write(img);
                    System.out.println("Sending images...");

                } else if (flag == 1) {
                    // Do click
                    String[] mouse = inFromServer.readUTF().split("%");
                    x = Integer.parseInt(mouse[0].trim());
                    if (mouse.length > 1) {
                        y = Integer.parseInt(mouse[1].trim());
                        z = Integer.parseInt(mouse[2].trim());
                        click(x, y, z);
                    }
                    System.out.println("Receiving mouse...");
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in SenderThread(): " + e);
        }
    }
}
