package ui.screens;

import ui.controllers.AuthUiController;
import ui.navigation.ScreenIds;
import ui.navigation.ScreenNavigator;
import ui.style.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginRegisterPanel extends JPanel {
    public LoginRegisterPanel(AuthUiController controller, ScreenNavigator navigator) {
        Theme.styleRootPanel(this);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        GridBagConstraints outer = new GridBagConstraints();
        outer.insets = new Insets(8, 16, 8, 16);
        outer.fill = GridBagConstraints.NONE;
        outer.anchor = GridBagConstraints.NORTH;
        outer.weighty = 1;

        outer.gridx = 0;
        add(buildLoginCard(controller, navigator), outer);

        outer.gridx = 1;
        add(buildRegisterCard(controller), outer);
    }

    private JPanel buildLoginCard(AuthUiController controller, ScreenNavigator navigator) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());
        card.setPreferredSize(new Dimension(320, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;

        JLabel title = new JLabel("Login");
        title.setFont(Theme.TITLE_FONT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 4, 12, 4);
        card.add(title, gbc);

        gbc.insets = new Insets(4, 4, 2, 4);
        gbc.gridy = 1;
        card.add(new JLabel("Name"), gbc);

        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(260, 32));
        nameField.setFont(Theme.BODY_FONT);
        gbc.gridy = 2;
        gbc.insets = new Insets(2, 4, 10, 4);
        card.add(nameField, gbc);

        JButton loginBtn = new JButton("Login");
        Theme.styleButton(loginBtn);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 4, 4, 4);
        card.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            try {
                controller.login(nameField.getText());
                navigator.showScreen(ScreenIds.DASHBOARD);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        return card;
    }

    private JPanel buildRegisterCard(AuthUiController controller) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());
        card.setPreferredSize(new Dimension(320, 290));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;

        JLabel title = new JLabel("Register");
        title.setFont(Theme.TITLE_FONT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 4, 12, 4);
        card.add(title, gbc);

        gbc.insets = new Insets(4, 4, 2, 4);
        gbc.gridy = 1;
        card.add(new JLabel("Name"), gbc);

        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(260, 32));
        nameField.setFont(Theme.BODY_FONT);
        gbc.gridy = 2;
        gbc.insets = new Insets(2, 4, 8, 4);
        card.add(nameField, gbc);

        gbc.insets = new Insets(4, 4, 2, 4);
        gbc.gridy = 3;
        card.add(new JLabel("Email"), gbc);

        JTextField emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(260, 32));
        emailField.setFont(Theme.BODY_FONT);
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 4, 10, 4);
        card.add(emailField, gbc);

        JButton registerBtn = new JButton("Create Account");
        Theme.styleButton(registerBtn);
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 4, 4, 4);
        card.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> {
            try {
                controller.register(nameField.getText(), emailField.getText());
                JOptionPane.showMessageDialog(this, "Registered. You can now login.");
                nameField.setText("");
                emailField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        return card;
    }
}
