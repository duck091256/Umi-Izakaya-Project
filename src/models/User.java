package models;

public class User {
    private String username;
    private String encryptedPassword;
    private String email;
    private String position;

    // Getters và Setters
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }
    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPosition() {
    	return position;
    }
    public void setPosition(String position) {
    	this.position = position;
    }
}