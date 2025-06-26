package test;

import javax.imageio.ImageIO;

//import com.formdev.flatlaf.FlatLaf;
//import com.formdev.flatlaf.FlatLightLaf;
//import com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import services.ClickableFileListener;
import services.CryptoUtils;
import services.ImageSteganographyUtils;

import javax.sound.sampled.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Timer;
import java.text.SimpleDateFormat;

public class EnhancedChatClient extends JFrame {
	private String serverName = "172.20.10.4";
	private int serverPort = 8888;
	private Socket socket;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	private String username;
	private volatile boolean running = true;
	
	// Biến để kiểm soát ghi âm
	private boolean isRecording = false;
	private ByteArrayOutputStream audioOut;
	private TargetDataLine microphone;

	// UI Components
	private JTextPane chatPane;
	private JTextField messageField;
	private JButton sendButton;
	private JButton fileButton;
	private JButton imageButton;
	private JButton recordButton;
	private JButton emoteButton;
	private JPanel mainPanel;
	private JPanel chatPanel;
	private JPanel inputPanel;
	private JScrollPane scrollPane;
	private JPanel userPanel;
	private JList<String> userList;
	private DefaultListModel<String> userListModel;
	private JLabel typingLabel;
	private Timer typingEffectTimer;

	// Emotes map
	private Map<String, ImageIcon> emoteIcons = new HashMap<>();
	private Map<String, String> emotes = new HashMap<>();

	// Colors
	private Color primaryColor = new Color(64, 81, 181);
	private Color lightColor = new Color(98, 114, 235);
	private Color darkColor = new Color(48, 63, 159);
	private Color textColor = Color.BLACK;

	public EnhancedChatClient(String name) {
		// Initialize emotes
		initEmotes();

		// Set up the UI
		this.username = name;
		setTitle("Chat Application - " + username);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		// Đóng socket khi user đóng cửa sổ
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		        try {
		            running = false;
		            if (outputStream != null) {
		                outputStream.writeUTF("EXIT");
		                outputStream.flush();
		            }
		            if (socket != null && !socket.isClosed()) {
		                socket.close();
		            }
		        } catch (IOException ex) {
		            ex.printStackTrace();
		        }
		    }
		});

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
			BufferedImage bi = new BufferedImage(label.getWidth(), label.getHeight(), BufferedImage.TYPE_INT_ARGB);
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

		// Typing indicator - gửi tín hiệu "TYPING"
		messageField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
		    Timer typingTimer = new Timer();

		    public void insertUpdate(javax.swing.event.DocumentEvent e) { triggerTyping(); }
		    public void removeUpdate(javax.swing.event.DocumentEvent e) { triggerTyping(); }
		    public void changedUpdate(javax.swing.event.DocumentEvent e) { triggerTyping(); }

		    private void triggerTyping() {
		        typingTimer.cancel();
		        typingTimer = new Timer();
		        typingTimer.schedule(new TimerTask() {
		            @Override
		            public void run() {
		                try {
		                    synchronized (outputStream) {
		                        outputStream.writeUTF("TYPING");
		                        outputStream.writeUTF(username);
		                        outputStream.flush();
		                    }
		                } catch (Exception ignored) {}
		            }
		        }, 0); // Gửi ngay lập tức, hoặc bạn có thể delay nếu cần
		    }
		});

		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

		// Emote button
		emoteButton = createStyledButton("😊");
		emoteButton.addActionListener(e -> showEmoteMenu());

		// File button
		fileButton = createStyledButton("📎");
		fileButton.addActionListener(e -> sendFile());

		// Image button
		imageButton = createStyledButton("🖼️");
		imageButton.addActionListener(e -> sendImage());
		
		// Record Button
		recordButton = createStyledButton("🎤");
		recordButton.addActionListener(e -> toggleRecording());
		
		// Send button
		sendButton = createStyledButton("Send");
		sendButton.addActionListener(e -> sendMessage());

		// Add buttons to button panel
		buttonPanel.add(emoteButton);
		buttonPanel.add(fileButton);
		buttonPanel.add(imageButton);
		buttonPanel.add(recordButton);
		buttonPanel.add(sendButton);
		buttonPanel.setOpaque(false);

		// Add components to input panel
		inputPanel.add(messageField, BorderLayout.CENTER);
		inputPanel.add(buttonPanel, BorderLayout.EAST);

		// Create a split pane for chat and user list
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, userPanel);
		splitPane.setResizeWeight(0.8);

		// Add components to main panel
		mainPanel.add(splitPane, BorderLayout.CENTER);
		mainPanel.add(inputPanel, BorderLayout.SOUTH);

		// Typing indicator label
		typingLabel = new JLabel();
		typingLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
		typingLabel.setForeground(Color.GRAY);
		mainPanel.add(typingLabel, BorderLayout.NORTH); // 👈 hiển thị trên cùng
		
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
			outputStream.flush();

			// Start message listener thread
			new Thread(this::listenForMessages).start();

			// Notify user of successful connection
			appendToChatArea("System", "Connected to server successfully!", Color.BLUE);

		} catch (IOException e) {
			appendToChatArea("System", "Error connecting to server: " + e.getMessage(), Color.RED);
			JOptionPane.showMessageDialog(this, "Could not connect to server. Please try again later.",
					"Connection Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void listenForMessages() {
		while (running) {
			try {
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
				} else if (messageType.equals("IMAGE")) {
				    receiveImage();
				} else if (messageType.equals("TYPING")) {
				    String typingUser = inputStream.readUTF();
				    showTypingIndicator(typingUser);
				}
			} catch (IOException e) {
				if (running) { // chỉ báo lỗi nếu chưa chủ động đóng
					appendToChatArea("System", "Connection to server lost.", Color.RED);
					JOptionPane.showMessageDialog(this, "Connection to server lost. Please restart the application.",
							"Connection Error", JOptionPane.ERROR_MESSAGE);
				}
				break; // thoát vòng lặp
			}
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

	        if (selectedFile.length() > 10 * 1024 * 1024) {
	            JOptionPane.showMessageDialog(this,
	                    "File is too large. Maximum size is 10MB.",
	                    "File Size Error",
	                    JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        // Hỏi người dùng có muốn mã hóa không
	        boolean encrypt = JOptionPane.showConfirmDialog(this, "Do you want to encrypt this file?", "Encrypt?",
	                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

	        String password = null;
	        if (encrypt) {
	            password = JOptionPane.showInputDialog(this, "Enter password to encrypt:");
	            if (password == null || password.trim().isEmpty()) {
	                appendToChatArea("System", "File sending canceled (no password entered).", Color.GRAY);
	                return;
	            }
	        }

	        // Tạo giao diện tiến trình
	        JProgressBar progressBar = new JProgressBar(0, 100);
	        progressBar.setStringPainted(true);
	        JDialog progressDialog = new JDialog(this, "Sending File", true);
	        progressDialog.setLayout(new BorderLayout());
	        progressDialog.add(new JLabel("Sending " + selectedFile.getName() + "..."), BorderLayout.NORTH);
	        progressDialog.add(progressBar, BorderLayout.CENTER);
	        progressDialog.setSize(300, 100);
	        progressDialog.setLocationRelativeTo(this);

	        // Copy biến vào biến final để sử dụng trong lambda
	        final String finalPassword = password;
	        final String fileName = selectedFile.getName();
	        final byte[] originalFileBytes;

	        try {
	            originalFileBytes = Files.readAllBytes(selectedFile.toPath());
	        } catch (IOException e) {
	            appendToChatArea("System", "Error reading file: " + e.getMessage(), Color.RED);
	            return;
	        }

	        // Thread gửi file
	        new Thread(() -> {
	            SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));

	            try {
	                byte[] fileBytes = originalFileBytes;

	                if (encrypt) {
	                    fileBytes = CryptoUtils.encryptAES(fileBytes, finalPassword);
	                }

	                String fileType = encrypt ? "ENCRYPTED_FILE" : "FILE";

	                synchronized (outputStream) {
	                    outputStream.writeUTF(fileType);
	                    outputStream.writeUTF(fileName);
	                    outputStream.writeUTF(username);
	                    outputStream.writeLong(fileBytes.length);

	                    int chunkSize = 4096;
	                    long total = fileBytes.length;
	                    long sent = 0;

	                    for (int i = 0; i < fileBytes.length; i += chunkSize) {
	                        int len = Math.min(chunkSize, fileBytes.length - i);
	                        outputStream.write(fileBytes, i, len);
	                        sent += len;
	                        int progress = (int) ((sent * 100) / total);
	                        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
	                    }

	                    outputStream.flush();
	                }

	                SwingUtilities.invokeLater(() -> {
	                    progressDialog.dispose();
	                    appendToChatArea("System", "File sent: " + fileName, new Color(0, 128, 0));
	                });

	            } catch (Exception e) {
	                SwingUtilities.invokeLater(() -> {
	                    progressDialog.dispose();
	                    appendToChatArea("System", "Error sending file: " + e.getMessage(), Color.RED);
	                });
	            }
	        }).start();
	    }
	}

	private void receiveFile() {
	    try {
	        String fileName = inputStream.readUTF();
	        String sender = inputStream.readUTF();
	        long fileSize = inputStream.readLong();

	        // Lưu file tạm thời
	        File tempDir = new File("temp_files");
	        if (!tempDir.exists()) tempDir.mkdirs();

	        File tempFile = new File(tempDir, fileName);
	        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
	        byte[] buffer = new byte[4096];
	        int bytesRead;
	        long totalBytesRead = 0;

	        while (totalBytesRead < fileSize) {
	            bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead));
	            if (bytesRead == -1) break;
	            fileOutputStream.write(buffer, 0, bytesRead);
	            totalBytesRead += bytesRead;
	        }

	        fileOutputStream.close();

	        // Thêm vào chat với nút "Save"
	        SwingUtilities.invokeLater(() -> {
	            JPanel panel = new JPanel(new BorderLayout());
	            JLabel fileLabel = new JLabel("📎 File from " + sender + ": " + fileName);
	            JButton saveButton = new JButton("Save");

	            panel.add(fileLabel, BorderLayout.CENTER);
	            panel.add(saveButton, BorderLayout.EAST);

	            // 🔊 Nếu là file âm thanh WAV thì thêm nút Play và hiển thị thời lượng
	            if (fileName.toLowerCase().endsWith(".wav")) {
	                JButton playButton = new JButton("▶️ Play");
	                playButton.addActionListener(ev -> playAudio(tempFile));
	                panel.add(playButton, BorderLayout.WEST);

	                try {
	                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(tempFile);
	                    AudioFormat format = audioIn.getFormat();
	                    long frames = audioIn.getFrameLength();
	                    double durationInSeconds = (frames + 0.0) / format.getFrameRate();
	                    audioIn.close();

	                    JLabel durationLabel = new JLabel(String.format("⏱ %.1f sec", durationInSeconds));
	                    durationLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
	                    panel.add(durationLabel, BorderLayout.SOUTH);
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }

	            saveButton.addActionListener(e -> {
	                boolean isEncrypted = fileName.startsWith("ENCRYPTED___");
	                String actualFileName = isEncrypted ? fileName.replaceFirst("ENCRYPTED___", "") : fileName;

	                JFileChooser fileChooser = new JFileChooser();
	                fileChooser.setSelectedFile(new File(actualFileName));
	                int result = fileChooser.showSaveDialog(this);

	                if (result == JFileChooser.APPROVE_OPTION) {
	                    File saveTo = fileChooser.getSelectedFile();
	                    try {
	                        if (isEncrypted) {
	                            int decryptChoice = JOptionPane.showConfirmDialog(this,
	                                    "This file is encrypted. Do you want to decrypt it?",
	                                    "Decrypt File", JOptionPane.YES_NO_OPTION);

	                            if (decryptChoice == JOptionPane.YES_OPTION) {
	                                String password = JOptionPane.showInputDialog(this, "Enter decryption password:");
	                                if (password == null || password.isEmpty()) {
	                                    JOptionPane.showMessageDialog(this, "No password entered. File will not be saved.");
	                                    return;
	                                }

	                                byte[] encryptedData = Files.readAllBytes(tempFile.toPath());

	                                try {
	                                    byte[] decryptedData = CryptoUtils.decryptAES(encryptedData, password);
	                                    Files.write(saveTo.toPath(), decryptedData);
	                                    JOptionPane.showMessageDialog(this, "File decrypted and saved successfully!");
	                                } catch (Exception ex) {
	                                    JOptionPane.showMessageDialog(this,
	                                            "Decryption failed: " + ex.getMessage(),
	                                            "Error", JOptionPane.ERROR_MESSAGE);
	                                }

	                            } else {
	                                Files.copy(tempFile.toPath(), saveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
	                                JOptionPane.showMessageDialog(this, "Encrypted file saved.");
	                            }

	                        } else {
	                            Files.copy(tempFile.toPath(), saveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
	                            JOptionPane.showMessageDialog(this, "File saved successfully!");
	                        }

	                    } catch (IOException ex) {
	                        JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
	                    }
	                }
	            });

	            try {
	                StyledDocument doc = chatPane.getStyledDocument();
	                doc.insertString(doc.getLength(), "\n", null);
	                chatPane.setCaretPosition(doc.getLength());
	                chatPane.insertComponent(panel);
	                doc.insertString(doc.getLength(), "\n", null);
	                chatPane.revalidate();
	                chatPane.repaint();
	            } catch (BadLocationException ex) {
	                ex.printStackTrace();
	                appendToChatArea("System", "Lỗi hiển thị file: " + ex.getMessage(), Color.RED);
	            }
	        });

	    } catch (IOException e) {
	        appendToChatArea("System", "Error receiving file: " + e.getMessage(), Color.RED);
	    }
	}
	
	private void sendImage() {
	    JFileChooser fileChooser = new JFileChooser();
	    FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
	            "Image files", "jpg", "jpeg", "png", "gif", "bmp");
	    fileChooser.setFileFilter(imageFilter);

	    int result = fileChooser.showOpenDialog(this);
	    if (result == JFileChooser.APPROVE_OPTION) {
	        File imageFile = fileChooser.getSelectedFile();

	        if (imageFile.length() > 10 * 1024 * 1024) {
	            JOptionPane.showMessageDialog(this,
	                    "Image is too large. Max size is 10MB.",
	                    "Size Error",
	                    JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        final File[] imageToSend = { imageFile };

	        int choice = JOptionPane.showConfirmDialog(this, "Bạn có muốn giấu tin nhắn trong ảnh này không?", "Steganography", JOptionPane.YES_NO_OPTION);

	        if (choice == JOptionPane.YES_OPTION) {
	            String secret = JOptionPane.showInputDialog(this, "Nhập tin nhắn cần giấu:");
	            if (secret != null && !secret.trim().isEmpty()) {
	                try {
	                    BufferedImage input = ImageIO.read(imageFile);
	                    File tempStego = new File("temp_images/stego_" + System.currentTimeMillis() + ".png");
	                    ImageSteganographyUtils.hideMessage(input, secret, tempStego);
	                    imageToSend[0] = tempStego;
	                } catch (Exception ex) {
	                    JOptionPane.showMessageDialog(this, "Lỗi khi giấu tin: " + ex.getMessage());
	                    return;
	                }
	            }
	        }

	        try {
	            synchronized (outputStream) {
	                outputStream.writeUTF("IMAGE");
	                outputStream.writeUTF(imageToSend[0].getName());
	                outputStream.writeUTF(username);
	                outputStream.writeLong(imageToSend[0].length());

	                FileInputStream fis = new FileInputStream(imageToSend[0]);
	                byte[] buffer = new byte[4096];
	                int bytesRead;
	                while ((bytesRead = fis.read(buffer)) != -1) {
	                    outputStream.write(buffer, 0, bytesRead);
	                }
	                fis.close();
	            }

	            // Hiển thị ảnh bên người gửi
	            final File finalImageToSend = imageToSend[0];
	            SwingUtilities.invokeLater(() -> {
	                JPanel imagePanel = new JPanel(new BorderLayout(10, 5));
	                imagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	                JLabel imageLabel = new JLabel("🖼️ You:");
	                ImageIcon imageIcon = new ImageIcon(finalImageToSend.getAbsolutePath());
	                Image scaledImage = imageIcon.getImage().getScaledInstance(200, -1, Image.SCALE_SMOOTH);
	                JLabel imgView = new JLabel(new ImageIcon(scaledImage));

	                imgView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	                imgView.addMouseListener(new MouseAdapter() {
	                    @Override
	                    public void mouseClicked(MouseEvent e) {
	                        JDialog previewDialog = new JDialog(EnhancedChatClient.this, "Image Preview", true);
	                        previewDialog.setLayout(new BorderLayout());
	                        JLabel previewLabel = new JLabel(new ImageIcon(finalImageToSend.getAbsolutePath()));
	                        previewDialog.add(new JScrollPane(previewLabel), BorderLayout.CENTER);
	                        previewDialog.setSize(600, 400);
	                        previewDialog.setLocationRelativeTo(EnhancedChatClient.this);
	                        previewDialog.setVisible(true);
	                    }
	                });

	                imagePanel.add(imageLabel, BorderLayout.NORTH);
	                imagePanel.add(imgView, BorderLayout.CENTER);

	                StyledDocument doc = chatPane.getStyledDocument();
	                try {
	                    doc.insertString(doc.getLength(), "\n", null);
	                    chatPane.setCaretPosition(doc.getLength());
	                    chatPane.insertComponent(imagePanel);
	                    doc.insertString(doc.getLength(), "\n", null);
	                } catch (BadLocationException ex) {
	                    ex.printStackTrace();
	                }
	            });

	        } catch (IOException e) {
	            appendToChatArea("System", "Error sending image: " + e.getMessage(), Color.RED);
	        }
	    }
	}

	private void receiveImage() {
	    try {
	        String fileName = inputStream.readUTF();
	        String sender = inputStream.readUTF();
	        long fileSize = inputStream.readLong();

	        File tempDir = new File("temp_images");
	        if (!tempDir.exists()) tempDir.mkdirs();

	        File imageFile = new File(tempDir, fileName);
	        FileOutputStream fos = new FileOutputStream(imageFile);
	        byte[] buffer = new byte[4096];
	        int bytesRead;
	        long totalBytesRead = 0;

	        while (totalBytesRead < fileSize) {
	            bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead));
	            if (bytesRead == -1) break;
	            fos.write(buffer, 0, bytesRead);
	            totalBytesRead += bytesRead;
	        }
	        fos.close();

	        // Hiển thị ảnh trong giao diện chat
	        SwingUtilities.invokeLater(() -> {
	            JPanel imagePanel = new JPanel(new BorderLayout(10, 5));
	            imagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	            JLabel imageLabel = new JLabel("🖼️ Image from " + sender + ":");
	            ImageIcon imageIcon = new ImageIcon(imageFile.getAbsolutePath());
	            Image scaledImage = imageIcon.getImage().getScaledInstance(200, -1, Image.SCALE_SMOOTH);
	            JLabel imgView = new JLabel(new ImageIcon(scaledImage));

	            // 👉 Tính năng preview ảnh khi click
	            imgView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	            imgView.addMouseListener(new MouseAdapter() {
	                @Override
	                public void mouseClicked(MouseEvent e) {
	                    JDialog previewDialog = new JDialog(EnhancedChatClient.this, "Image Preview", true);
	                    previewDialog.setLayout(new BorderLayout());
	                    ImageIcon fullSizeIcon = new ImageIcon(imageFile.getAbsolutePath());
	                    JLabel previewLabel = new JLabel(fullSizeIcon);
	                    previewDialog.add(new JScrollPane(previewLabel), BorderLayout.CENTER);
	                    previewDialog.setSize(600, 400);
	                    previewDialog.setLocationRelativeTo(EnhancedChatClient.this);
	                    previewDialog.setVisible(true);
	                }
	            });

	            JButton saveButton = new JButton("Save");
	            saveButton.addActionListener(e -> {
	                JFileChooser fileChooser = new JFileChooser();
	                fileChooser.setSelectedFile(new File(fileName));
	                int result = fileChooser.showSaveDialog(this);

	                if (result == JFileChooser.APPROVE_OPTION) {
	                    File saveTo = fileChooser.getSelectedFile();
	                    try {
	                        Files.copy(imageFile.toPath(), saveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
	                        JOptionPane.showMessageDialog(this, "Image saved successfully!");
	                    } catch (IOException ex) {
	                        JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage());
	                    }
	                }
	            });

	            JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
	            contentPanel.add(imgView, BorderLayout.CENTER);
	            contentPanel.add(saveButton, BorderLayout.SOUTH);

	            imagePanel.add(imageLabel, BorderLayout.NORTH);
	            imagePanel.add(contentPanel, BorderLayout.CENTER);

	            // 🕵️‍♂️ Check if image has hidden message
	            try {
	                BufferedImage img = ImageIO.read(imageFile);
	                String hiddenMessage = ImageSteganographyUtils.extractMessage(img);
	                if (hiddenMessage != null && !hiddenMessage.isEmpty()) {
	                    JButton decodeBtn = new JButton("🔍 Giải mã tin ẩn");
	                    decodeBtn.addActionListener(ev -> {
	                        JOptionPane.showMessageDialog(this, "Tin ẩn trong ảnh:\n" + hiddenMessage);
	                    });
	                    imagePanel.add(decodeBtn, BorderLayout.SOUTH);
	                }
	            } catch (Exception ignore) {}
	            
	            StyledDocument doc = chatPane.getStyledDocument();
	            try {
	                doc.insertString(doc.getLength(), "\n", null);
	                chatPane.setCaretPosition(doc.getLength());
	                chatPane.insertComponent(imagePanel);
	                doc.insertString(doc.getLength(), "\n", null);
	            } catch (BadLocationException ex) {
	                ex.printStackTrace();
	            }
	        });

	    } catch (IOException e) {
	        appendToChatArea("System", "Error receiving image: " + e.getMessage(), Color.RED);
	    }
	}

	private String formatFileSize(long size) {
		if (size < 1024)
			return size + " B";
		int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
		return String.format("%.1f %sB", (double) size / (1L << (z * 10)), " KMGTPE".charAt(z));
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

				if (message.startsWith("[FILENAME]")) {
		            String[] parts = message.substring(10).split("::");
		            String fileName = parts[0];
		            String fileSender = parts.length > 1 ? parts[1] : sender;

		            Style linkStyle = chatPane.addStyle("Link", null);
		            StyleConstants.setForeground(linkStyle, Color.BLUE);
		            StyleConstants.setUnderline(linkStyle, true);

		            // Timestamp
		            StyleConstants.setForeground(style, Color.GRAY);
		            StyleConstants.setFontSize(style, 12);
		            doc.insertString(doc.getLength(), "[" + timestamp + "] ", style);

		            // Sender
		            StyleConstants.setForeground(style, color);
		            StyleConstants.setBold(style, true);
		            StyleConstants.setFontSize(style, 14);
		            doc.insertString(doc.getLength(), fileSender + ": ", style);

		            // File name (clickable)
		            doc.insertString(doc.getLength(), fileName + "\n", linkStyle);

		            // Thêm listener 1 lần (nếu chưa)
		            if (!chatPane.getMouseListeners()[0].getClass().getName().contains("ClickableFileListener")) {
		                chatPane.addMouseListener(new ClickableFileListener(chatPane));
		            }

		            return; // Dừng tại đây vì đã xử lý rồi
		        }
				
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
	
	private void toggleRecording() {
	    if (isRecording) {
	        stopRecording();
	    } else {
	        startRecording();
	    }
	}

	private void startRecording() {
	    try {
	        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, true);
	        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

	        if (!AudioSystem.isLineSupported(info)) {
	            JOptionPane.showMessageDialog(this, "Microphone not supported.");
	            return;
	        }

	        microphone = (TargetDataLine) AudioSystem.getLine(info);
	        microphone.open(format);
	        microphone.start();

	        audioOut = new ByteArrayOutputStream();
	        isRecording = true;

	        new Thread(() -> {
	            byte[] buffer = new byte[4096];
	            while (isRecording) {
	                int bytesRead = microphone.read(buffer, 0, buffer.length);
	                audioOut.write(buffer, 0, bytesRead);
	            }
	        }).start();

	        appendToChatArea("System", "🎙️ Bắt đầu ghi âm...", Color.MAGENTA);

	    } catch (Exception e) {
	        e.printStackTrace();
	        appendToChatArea("System", "Lỗi khi ghi âm: " + e.getMessage(), Color.RED);
	    }
	}

	private void stopRecording() {
	    try {
	        isRecording = false;
	        microphone.stop();
	        microphone.close();

	        byte[] audioBytes = audioOut.toByteArray();
	        String fileName = "audio_" + System.currentTimeMillis() + ".wav";

	        // Ghi vào file WAV tạm thời
	        File audioFile = new File("temp_audio/" + fileName);
	        audioFile.getParentFile().mkdirs();
	        try (FileOutputStream fos = new FileOutputStream(audioFile)) {
	            // Viết header WAV đơn giản
	            AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, true);
	            AudioSystem.write(new AudioInputStream(
	                new ByteArrayInputStream(audioBytes), format, audioBytes.length / format.getFrameSize()
	            ), AudioFileFormat.Type.WAVE, fos);
	        }

	        // Gửi file như một file thường
	        sendAudioFile(audioFile);

	        appendToChatArea("System", "🛑 Đã dừng ghi âm và gửi tệp ghi âm.", new Color(0, 128, 0));
	    } catch (Exception e) {
	        e.printStackTrace();
	        appendToChatArea("System", "Lỗi khi dừng ghi âm: " + e.getMessage(), Color.RED);
	    }
	}
	
	private void sendAudioFile(File audioFile) {
	    try {
	        synchronized (outputStream) {
	            outputStream.writeUTF("FILE");
	            outputStream.writeUTF(audioFile.getName());
	            outputStream.writeUTF(username);
	            outputStream.writeLong(audioFile.length());

	            FileInputStream fis = new FileInputStream(audioFile);
	            byte[] buffer = new byte[4096];
	            int bytesRead;
	            while ((bytesRead = fis.read(buffer)) != -1) {
	                outputStream.write(buffer, 0, bytesRead);
	            }
	            fis.close();
	        }
	    } catch (IOException e) {
	        appendToChatArea("System", "Lỗi khi gửi tệp âm thanh: " + e.getMessage(), Color.RED);
	    }
	}
	
	private void playAudio(File audioFile) {
	    try {
	        AudioInputStream audioIn = AudioSystem.getAudioInputStream(audioFile);
	        Clip clip = AudioSystem.getClip();
	        clip.open(audioIn);
	        clip.start();
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, "Lỗi khi phát âm thanh: " + e.getMessage());
	    }
	}
	
	private void showTypingIndicator(String user) {
	    if (typingEffectTimer != null) typingEffectTimer.cancel();

	    typingEffectTimer = new Timer();
	    typingLabel.setText(user + " is typing");

	    // Hiệu ứng . .. ...
	    typingEffectTimer.scheduleAtFixedRate(new TimerTask() {
	        int dotCount = 0;
	        @Override
	        public void run() {
	            SwingUtilities.invokeLater(() -> {
	                dotCount = (dotCount + 1) % 4; // 0 → 1 → 2 → 3 → 0
	                String dots = ".".repeat(dotCount);
	                typingLabel.setText(user + " is typing" + dots);
	            });
	        }
	    }, 0, 500); // cứ 500ms cập nhật

	    // Ẩn sau 5 giây nếu không gõ nữa
	    new Timer().schedule(new TimerTask() {
	        @Override
	        public void run() {
	            if (typingEffectTimer != null) typingEffectTimer.cancel();
	            SwingUtilities.invokeLater(() -> typingLabel.setText(""));
	        }
	    }, 5000);
	}


	public static void main(String[] args) {
		String username = args.length > 0 ? args[0] : "Unknown";
		SwingUtilities.invokeLater(() -> new EnhancedChatClient(username).setVisible(true));
	}
}
