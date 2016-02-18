package remotedesktop;

/*
 * @author Rakibul Islam
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.*;
import java.io.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ClientReceiver {

    static String ip = "192.168.43.166";

    static Socket socket;
    static DataInputStream inFromServer;
    static DataOutputStream outToServer;

    JFrame frame;
    JPanel panel;
    Graphics g;
    static int width;
    static int height;

    public void createPromptUI() {
        JFrame framePrompt = new JFrame("Rem0te Deskt0p - Receiver");
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
                    if (inFromServer.readUTF().equals("1")) {
                        labelStatus.setVisible(false);
                        framePrompt.setVisible(false);

                        createDisplayUI();

                        Thread getImage = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (true) {
                                    try {
                                        int length = inFromServer.readInt();
                                        byte[] img = new byte[length];
                                        inFromServer.readFully(img, 0, img.length);
                                        ImageIcon imgIcon = new ImageIcon(img);
                                        Image image = imgIcon.getImage();
                                        g = panel.getGraphics();
                                        g.drawImage(image, 0, 0, width, height - 25, null);
                                        System.out.println("Receiving image...");
                                        Thread.sleep(1);
                                    } catch (Exception ex) {
                                        System.out.println("Exception in getImage thread: " + ex);
                                    }
                                }
                            }
                        });

                        Thread sendClick = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                frame.addMouseListener(new MouseAdapter() {
                                    @Override
                                    public void mousePressed(MouseEvent e) {
                                        try {
                                            int x = e.getX();
                                            int y = e.getY();
                                            int z = e.getButton();
                                            String coordinate = x + "%" + y + "%" + z;
                                            outToServer.writeUTF(coordinate);
                                            System.out.println(x + " : " + y + " : " + z);
                                        } catch (Exception ex) {
                                            System.out.println("Exception in sendClick thread: " + ex);
                                        }
                                    }
                                });
                            }
                        });
                        getImage.start();
                        sendClick.start();

                    } else {
                        labelStatus.setVisible(true);
                    }
                } catch (Exception ex) {
                    System.out.println("Exception in createPromptUI(): " + ex);
                }
            }
        });
    }

    public void createDisplayUI() {
        frame = new JFrame("REM0TE DESKT0P - Receiver");
        frame.setResizable(false);
        frame.setAlwaysOnTop(false);
        panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.add(panel);
        frame.setBounds(0, 0, width, height);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        socket = new Socket(ip, 1234);
        inFromServer = new DataInputStream(socket.getInputStream());
        outToServer = new DataOutputStream(socket.getOutputStream());

        width = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()).width;
        height = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()).height;

        new ClientReceiver().createPromptUI();

    }
}
