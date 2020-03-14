package com.UI;

import com.server.AbstractServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UIForm extends JFrame {

    private JPanel panel1;
    private JTextArea LogsTextArea;
    private JButton startButton;
    private JLabel PlayersLabel;
    private JLabel MagiCNumberLabel;
    private JRadioButton DecimalRadio;
    private JRadioButton FullRadio;
    private AbstractServer server;


    public UIForm() throws HeadlessException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        add(panel1);
        setTitle("LR3-Server");
        setSize(500, 500);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                server = AbstractServer.init(DecimalRadio.isSelected());
                server.setUIForm(getInstance());
                assert server.getForm() == getInstance();
                server.start();
                startButton.setEnabled(false);
            }
        });
    }

    public UIForm getInstance(){
        return this;
    }

    public void updatePlayers(Integer count){
        PlayersLabel.setText("Players: "+count.toString()+"/2");
    }

    public void appendLogs(String s ){
        LogsTextArea.append("\n"+s);
    }
    public void setLogs(String s){
        LogsTextArea.setText(s);
    }
}
