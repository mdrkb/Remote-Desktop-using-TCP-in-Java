package remotedesktop;

import java.awt.Rectangle;
import java.awt.Toolkit;

/*
 * @author Rakibul Islam
 */
public class RemoteDesktop {

    public static void main(String[] args) throws Exception {
        System.out.println("Remote Desktop by Rakib");
        float adjX = (new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()).width) / 956f;
        float adjY = (new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()).height) / 537f;
        System.out.println(adjX + "\t" + adjY);
    }
}
