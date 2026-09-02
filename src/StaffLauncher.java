import PageManager.switchPage;
import views.LoginView;

import javax.swing.*;
import java.awt.*;

public class StaffLauncher extends JFrame{
    public StaffLauncher() {
        setUndecorated(true);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        switchPage.cardLayout = cardLayout;
        switchPage.container = new JPanel(cardLayout);


        switchPage.container.add(new LoginView(true),"LOGIN");

        add(switchPage.container);
        setVisible(true);
    }

    public static void main(String[] args) {
        new StaffLauncher();
    }
}