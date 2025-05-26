package test;

//import com.formdev.flatlaf.FlatLaf;
//import com.formdev.flatlaf.FlatLightLaf;
//import com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class EnhanceedChatClient2 extends JFrame {
    private String serverName = "localhost";
    private int serverPort = 8888;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String username;

    // UI Components
    private JTextPane chatPane;
    private JTextField messageField;
    private JButton sendButton;
    private JButton fileButton;
    private JButton emoteButton;
    private JPanel mainPanel;
    private JPanel chatPanel;
    private JPanel inputPanel;
    private JScrollPane scrollPane;
    private JPanel userPanel;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    // Emotes map
    private Map<String, ImageIcon> emoteIcons = new HashMap<>();
    private Map<String, String> emotes = new HashMap<>();

    // Colors
    private Color primaryColor = new Color(64, 81, 181);
    private Color lightColor = new Color(98, 114, 235);
    private Color darkColor = new Color(48, 63, 159);
    private Color textColor = Color.BLACK;

    public EnhanceedChatClient2() {
        // Initialize emotes
        initEmotes();

        // Get username
        username = JOptionPane.showInputDialog(this, "Enter your username:", "Login", JOptionPane.QUESTION_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            username = "User" + new Random().nextInt(1000);
        }

        // Set up the UI
        setTitle("Chat Application - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize components
        initComponents();

        // Connect to server
        connectToServer();
    }

    private void initEmotes() {
        emotes.put(":smile:", "😊");
        emotes.put(":laugh:", "😂");
        emotes.put(":sad:", "😢");
        emotes.put(":heart:", "❤️");
        emotes.put(":thumbsup:", "👍");
        emotes.put(":thumbsdown:", "👎");
        emotes.put(":clap:", "👏");
        emotes.put(":fire:", "🔥");
        emotes.put(":star:", "⭐");
        emotes.put(":cool:", "😎");
        emotes.put(":angry:", "😠");
        emotes.put(":surprised:", "😮");
        emotes.put(":wink:", "😉");
        emotes.put(":cry:", "😭");
        emotes.put(":party:", "🎉");

        // Load emote icons (in a real app, you would load actual image files)
        for (Map.Entry<String, String> entry : emotes.entrySet()) {
            // Create a label with the emoji and convert to an icon
            JLabel label = new JLabel(entry.getValue());
            label.setFont(new Font("SansSerif", Font.PLAIN, 24));
            label.setSize(30, 30);
            BufferedImage bi = new BufferedImage(
                    label.getWidth(), label.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bi.createGraphics();
            label.paint(g);
            g.dispose();
            emoteIcons.put(entry.getKey(), new ImageIcon(bi));
        }
    }

    private void initComponents() {
        // Main panel with BorderLayout
        mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Chat pane with styled document
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatPane.setBackground(new Color(245, 245, 245));

        // Scroll pane for chat area
        scrollPane = new JScrollPane(chatPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // User panel
        userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(new TitledBorder("Online Users"));
        userPanel.setPreferredSize(new Dimension(150, 0));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane userScrollPane = new JScrollPane(userList);
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        // Input panel
        inputPanel = new JPanel(new BorderLayout(5, 5));

        // Message field
        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageField.addActionListener(e -> sendMessage());

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        // Emote button
        emoteButton = createStyledButton("😊");
        emoteButton.addActionListener(e -> showEmoteMenu());

        // File button
        fileButton = createStyledButton("📎");
        fileButton.addActionListener(e -> sendFile());

        // Send button
        sendButton = createStyledButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        // Add buttons to button panel
        buttonPanel.add(emoteButton);
        buttonPanel.add(fileButton);
        buttonPanel.add(sendButton);
        buttonPanel.setOpaque(false);

        // Add components to input panel
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // Create a split pane for chat and user list
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                scrollPane,
                userPanel
        );
        splitPane.setResizeWeight(0.8);

        // Add components to main panel
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Set content pane
        setContentPane(mainPanel);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBackground(primaryColor);
        button.setForeground(textColor);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(lightColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(primaryColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(darkColor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(lightColor);
            }
        });

        return button;
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverName, serverPort);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            // Send username to server
            outputStream.writeUTF(username);

            // Start message listener thread
            new Thread(this::listenForMessages).start();

            // Notify user of successful connection
            appendToChatArea("System", "Connected to server successfully!", Color.BLUE);

        } catch (IOException e) {
            appendToChatArea("System", "Error connecting to server: " + e.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this,
                    "Could not connect to server. Please try again later.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listenForMessages() {
        try {
            while (true) {
                String messageType = inputStream.readUTF();

                if (messageType.equals("TEXT")) {
                    String message = inputStream.readUTF();

                    // Check if it's a user list update
                    if (message.startsWith("USERLIST:")) {
                        updateUserList(message.substring(9));
                    } else {
                        // Regular message
                        String sender;
                        String content;

                        if (message.contains(": ")) {
                            sender = message.substring(0, message.indexOf(": "));
                            content = message.substring(message.indexOf(": ") + 2);

                            Color messageColor = sender.equals(username) ? new Color(0, 100, 0) : new Color(0, 0, 139);
                            appendToChatArea(sender, content, messageColor);
                        } else {
                            // System message
                            appendToChatArea("System", message, Color.BLUE);
                        }
                    }
                } else if (messageType.equals("FILE")) {
                    receiveFile();
                }
            }
        } catch (IOException e) {
            appendToChatArea("System", "Connection to server lost.", Color.RED);
            JOptionPane.showMessageDialog(this,
                    "Connection to server lost. Please restart the application.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUserList(String userListStr) {
        String[] users = userListStr.split(",");
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.trim().isEmpty()) {
                    userListModel.addElement(user);
                }
            }
        });
    }

    private void sendMessage() {
        try {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                // Replace emote codes with actual emotes
                for (Map.Entry<String, String> entry : emotes.entrySet()) {
                    message = message.replace(entry.getKey(), entry.getValue());
                }

                // Send message type
                outputStream.writeUTF("TEXT");
                // Send the message
                outputStream.writeUTF(message);

                // Clear message field
                messageField.setText("");
            }
        } catch (IOException e) {
            appendToChatArea("System", "Error sending message: " + e.getMessage(), Color.RED);
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Check file size (limit to 10MB for this example)
            if (selectedFile.length() > 10 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this,
                        "File is too large. Maximum size is 10MB.",
                        "File Size Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Send file type
                outputStream.writeUTF("FILE");
                // Send file name
                outputStream.writeUTF(selectedFile.getName());
                // Send file size
                outputStream.writeLong(selectedFile.length());

                // Send file data
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                byte[] buffer = new byte[4096];
                final int[] bytesRead = {0};

                // Show progress dialog
                JProgressBar progressBar = new JProgressBar(0, 100);
                progressBar.setStringPainted(true);
                JDialog progressDialog = new JDialog(this, "Sending File", true);
                progressDialog.setLayout(new BorderLayout());
                progressDialog.add(new JLabel("Sending " + selectedFile.getName() + "..."), BorderLayout.NORTH);
                progressDialog.add(progressBar, BorderLayout.CENTER);
                progressDialog.setSize(300, 100);
                progressDialog.setLocationRelativeTo(this);

                // Start a thread to send the file
                new Thread(() -> {
                    try {
                        long totalBytes = selectedFile.length();
                        long bytesSent = 0;

                        while ((bytesRead[0] = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead[0]);
                            bytesSent += bytesRead[0];

                            final int progress = (int) ((bytesSent * 100) / totalBytes);
                            SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                        }

                        fileInputStream.close();
                        SwingUtilities.invokeLater(() -> progressDialog.dispose());
                        appendToChatArea("System", "File sent: " + selectedFile.getName(), new Color(0, 128, 0));

                    } catch (IOException e) {
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            appendToChatArea("System", "Error sending file: " + e.getMessage(), Color.RED);
                        });
                    }
                }).start();

                // Show the progress dialog
                progressDialog.setVisible(true);

            } catch (IOException e) {
                appendToChatArea("System", "Error sending file: " + e.getMessage(), Color.RED);
            }
        }
    }

    private void receiveFile() {
        try {
            String fileName = inputStream.readUTF();
            String sender = inputStream.readUTF();
            long fileSize = inputStream.readLong();

            // Ask user if they want to receive the file
            int option = JOptionPane.showConfirmDialog(this,
                    sender + " wants to send you a file: " + fileName + " (" + formatFileSize(fileSize) + ")",
                    "File Transfer Request",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                // Ask user where to save the file
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileName));
                int result = fileChooser.showSaveDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File saveFile = fileChooser.getSelectedFile();

                    // Show progress dialog
                    JProgressBar progressBar = new JProgressBar(0, 100);
                    progressBar.setStringPainted(true);
                    JDialog progressDialog = new JDialog(this, "Receiving File", true);
                    progressDialog.setLayout(new BorderLayout());
                    progressDialog.add(new JLabel("Receiving " + fileName + " from " + sender + "..."), BorderLayout.NORTH);
                    progressDialog.add(progressBar, BorderLayout.CENTER);
                    progressDialog.setSize(300, 100);
                    progressDialog.setLocationRelativeTo(this);

                    // Start a thread to receive the file
                    new Thread(() -> {
                        try {
                            // Receive and save file
                            FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            long totalBytesRead = 0;

                            while (totalBytesRead < fileSize) {
                                bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead));
                                fileOutputStream.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;

                                final int progress = (int) ((totalBytesRead * 100) / fileSize);
                                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                            }

                            fileOutputStream.close();
                            SwingUtilities.invokeLater(() -> progressDialog.dispose());
                            appendToChatArea("System", "File received from " + sender + ": " + fileName, new Color(0, 128, 0));

                        } catch (IOException e) {
                            SwingUtilities.invokeLater(() -> {
                                progressDialog.dispose();
                                appendToChatArea("System", "Error receiving file: " + e.getMessage(), Color.RED);
                            });
                        }
                    }).start();

                    // Show the progress dialog
                    progressDialog.setVisible(true);
                }
            } else {
                // Reject the file - read and discard the data
                long bytesSkipped = 0;
                while (bytesSkipped < fileSize) {
                    long skipped = inputStream.skip(fileSize - bytesSkipped);
                    if (skipped <= 0) break;
                    bytesSkipped += skipped;
                }
                appendToChatArea("System", "File rejected: " + fileName + " from " + sender, Color.ORANGE);
            }
        } catch (IOException e) {
            appendToChatArea("System", "Error receiving file: " + e.getMessage(), Color.RED);
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return String.format("%.1f %sB", (double)size / (1L << (z*10)), " KMGTPE".charAt(z));
    }

    private void showEmoteMenu() {
        JPopupMenu emoteMenu = new JPopupMenu();
        emoteMenu.setLayout(new GridLayout(0, 5, 2, 2));

        for (Map.Entry<String, String> entry : emotes.entrySet()) {
            JMenuItem item = new JMenuItem(entry.getValue());
            item.setFont(new Font("SansSerif", Font.PLAIN, 20));
            item.setToolTipText(entry.getKey());
            item.addActionListener(e -> {
                messageField.setText(messageField.getText() + " " + entry.getKey() + " ");
                messageField.requestFocus();
            });
            emoteMenu.add(item);
        }

        emoteMenu.show(emoteButton, 0, -emoteMenu.getPreferredSize().height);
    }

    private void appendToChatArea(String sender, String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String timestamp = timeFormat.format(new Date());

            try {
                javax.swing.text.Document doc = chatPane.getDocument();
                javax.swing.text.Style style = chatPane.addStyle("Style", null);

                // Timestamp style
                StyleConstants StyleConstants = null;
                StyleConstants.setForeground(style, Color.GRAY);
                StyleConstants.setFontSize(style, 12);
                doc.insertString(doc.getLength(), "[" + timestamp + "] ", style);

                // Sender style
                StyleConstants.setForeground(style, color);
                StyleConstants.setBold(style, true);
                StyleConstants.setFontSize(style, 14);
                doc.insertString(doc.getLength(), sender + ": ", style);

                // Message style
                StyleConstants.setBold(style, false);
                StyleConstants.setForeground(style, Color.BLACK);

                // Process message for emotes
                String remaining = message;
                int pos = 0;

                while (pos < remaining.length()) {
                    boolean foundEmote = false;

                    for (Map.Entry<String, String> entry : emotes.entrySet()) {
                        String emoteCode = entry.getKey();
                        String emoteChar = entry.getValue();

                        if (remaining.indexOf(emoteChar, pos) == pos) {
                            // Insert text before emote
                            if (pos > 0) {
                                doc.insertString(doc.getLength(), remaining.substring(0, pos), style);
                            }

                            // Insert emote
                            StyleConstants.setFontSize(style, 20);
                            doc.insertString(doc.getLength(), emoteChar, style);
                            StyleConstants.setFontSize(style, 14);

                            // Update remaining text
                            remaining = remaining.substring(pos + emoteChar.length());
                            pos = 0;
                            foundEmote = true;
                            break;
                        }
                    }

                    if (!foundEmote) {
                        pos++;
                    }
                }

                // Insert any remaining text
                if (!remaining.isEmpty()) {
                    doc.insertString(doc.getLength(), remaining, style);
                }

                // Add new line
                doc.insertString(doc.getLength(), "\n", style);

                // Scroll to bottom
                chatPane.setCaretPosition(doc.getLength());

            } catch (javax.swing.text.BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
    	String username = args.length > 0 ? args[0] : "Unknown";
        SwingUtilities.invokeLater(() -> new EnhancedChatClient(username).setVisible(true));
    }
}

