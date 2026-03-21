package MainClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameWindow extends JFrame implements ActionListener {

    private GamePanel gamePanel;

    private JButton startB, pauseB, endB, exitB;
    private JLabel statusL;
    private JTextField statusTF;

    public GameWindow() {
        setTitle("Animal Rescue - Sidescroller Platformer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Status bar
        statusL = new JLabel("Status: ");
        statusTF = new JTextField(35);
        statusTF.setEditable(false);
        statusTF.setBackground(new Color(230, 240, 255));
        statusTF.setText("Welcome! Press Start Game to begin.");

        // Buttons
        startB = new JButton("Start Game");
        pauseB = new JButton("Pause");
        endB = new JButton("End Game");
        exitB = new JButton("Exit");

        startB.addActionListener(this);
        pauseB.addActionListener(this);
        endB.addActionListener(this);
        exitB.addActionListener(this);

        // Game panel
        gamePanel = new GamePanel();

        // Layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(startB);
        buttonPanel.add(pauseB);
        buttonPanel.add(endB);
        buttonPanel.add(exitB);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        statusPanel.add(statusL);
        statusPanel.add(statusTF);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(gamePanel, BorderLayout.CENTER);
        c.add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        gamePanel.requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == startB) {
            if (!gamePanel.isGameStarted()) {
                gamePanel.startGame();
                statusTF.setText("Game started! Explore the world and collect all animals!");
            }
            gamePanel.requestFocusInWindow();
        } else if (source == pauseB) {
            if (gamePanel.isGameStarted()) {
                gamePanel.pauseGame();
                statusTF.setText(gamePanel.isGamePaused() ? "Game paused." : "Game resumed.");
            }
            gamePanel.requestFocusInWindow();
        } else if (source == endB) {
            gamePanel.endGame();
            statusTF.setText("Game ended. Press Start Game to play again.");
            gamePanel.repaint();
        } else if (source == exitB) {
            gamePanel.endGame();
            System.exit(0);
        }
    }
}
