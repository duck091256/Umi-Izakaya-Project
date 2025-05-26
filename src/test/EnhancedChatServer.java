package test;

import java.io.*;
import java.net.*;
import java.util.*;

public class EnhancedChatServer {
    private static final int PORT = 8888;
    private static HashSet<ClientHandler> clients = new HashSet<>();
    private static ArrayList<String> usernames = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Chat Server started on port " + PORT);
            System.out.println("Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            try {
                client.sendMessage("TEXT", message);
            } catch (IOException e) {
                System.out.println("Error broadcasting message: " + e.getMessage());
            }
        }
    }

    public static void sendFile(String fileName, long fileSize, byte[] fileData, String senderName, ClientHandler recipient) {
        try {
            recipient.sendFile(fileName, senderName, fileSize, fileData);
        } catch (IOException e) {
            System.out.println("Error sending file: " + e.getMessage());
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        usernames.remove(client.getUsername());
        System.out.println("Client disconnected: " + client.getUsername());
        broadcastMessage(client.getUsername() + " has left the chat.", null);
        updateUserList();
    }

    public static void updateUserList() {
        StringBuilder userList = new StringBuilder("USERLIST:");
        for (String username : usernames) {
            userList.append(username).append(",");
        }
        broadcastMessage(userList.toString(), null);
    }

    public static void addUser(String username) {
        usernames.add(username);
        updateUserList();
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream inputStream;
        private DataOutputStream outputStream;
        private String username;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.username = inputStream.readUTF();

            addUser(username);

            StringBuilder userList = new StringBuilder("USERLIST:");
            for (String username : usernames) {
                userList.append(username).append(",");
            }
            sendMessage("TEXT", userList.toString());

            broadcastMessage(username + " has joined the chat.", null);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String messageType = inputStream.readUTF();

                    if (messageType.equals("TEXT")) {
                        String message = inputStream.readUTF();
                        broadcastMessage(username + ": " + message, this);
                    } else if (messageType.equals("FILE")) {
                        handleFileTransfer(false);
                    } else if (messageType.equals("ENCRYPTED_FILE")) {
                        handleFileTransfer(true);
                    } else if (messageType.equals("EXIT")) {
                        System.out.println("[SERVER] " + username + " is exiting.");
                        break;
                    } else if (messageType.equals("IMAGE")) {
                        handleImageTransfer(); // 👈 gọi hàm xử lý ảnh
                    } else if (messageType.equals("TYPING")) {
                        String typingUser = inputStream.readUTF();
                        for (ClientHandler client : clients) {
                            if (!client.getUsername().equals(typingUser)) {
                                client.sendMessage("TYPING", typingUser);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    removeClient(this);
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    System.out.println("Error closing socket: " + ex.getMessage());
                }
            }
        }

        private void handleFileTransfer(boolean isEncrypted) throws IOException {
            String fileName = inputStream.readUTF();
            String senderName = inputStream.readUTF(); // ❗ Đọc đúng sender từ client
            long fileSize = inputStream.readLong();

            byte[] buffer = new byte[4096];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            long totalRead = 0;
            while (totalRead < fileSize) {
                int read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead));
                if (read == -1) break;
                baos.write(buffer, 0, read);
                totalRead += read;
            }

            byte[] fileData = baos.toByteArray();
            String broadcastName = isEncrypted ? "ENCRYPTED___" + fileName : fileName;

            for (ClientHandler client : clients) {
                if (client != this) {
                    client.sendFile(broadcastName, senderName, fileSize, fileData); // ❗ dùng đúng sender
                }
            }

            String lowerFile = fileName.toLowerCase();
            String displayMessage;

            if (lowerFile.endsWith(".wav")) {
                displayMessage = senderName + " sent a voice message.";
            } else if (lowerFile.matches(".*\\.(jpg|jpeg|png|gif|bmp)")) {
                displayMessage = senderName + " sent an image.";
            } else if (isEncrypted) {
                displayMessage = senderName + " shared an encrypted file: " + fileName;
            } else {
                displayMessage = senderName + " shared a file: " + fileName;
            }

            broadcastMessage(displayMessage, null);
        }

        public void handleImageTransfer() throws IOException {
            String fileName = inputStream.readUTF();
            String senderName = inputStream.readUTF();
            long fileSize = inputStream.readLong();

            byte[] buffer = new byte[4096];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            long totalRead = 0;
            while (totalRead < fileSize) {
                int read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead));
                if (read == -1) break;
                baos.write(buffer, 0, read);
                totalRead += read;
            }

            byte[] fileData = baos.toByteArray();

            for (ClientHandler client : clients) {
                if (client != this) {
                    client.sendImage(fileName, senderName, fileSize, fileData); // 👈 phát ảnh đi các client khác
                }
            }
        }

        public void sendImage(String fileName, String sender, long fileSize, byte[] fileData) throws IOException {
            outputStream.writeUTF("IMAGE");
            outputStream.writeUTF(fileName);
            outputStream.writeUTF(sender);
            outputStream.writeLong(fileSize);
            outputStream.write(fileData);
        }

        public void sendMessage(String type, String message) throws IOException {
            outputStream.writeUTF(type);
            outputStream.writeUTF(message);
        }

        public void sendFile(String fileName, String sender, long fileSize, byte[] fileData) throws IOException {
            outputStream.writeUTF("FILE");
            outputStream.writeUTF(fileName);
            outputStream.writeUTF(sender);
            outputStream.writeLong(fileSize);
            outputStream.write(fileData);
        }

        public String getUsername() {
            return username;
        }
    }
}
