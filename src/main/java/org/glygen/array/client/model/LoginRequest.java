package org.glygen.array.client.model;

public class LoginRequest {
	public String username;
    public String password;
	
    public String getUsername() {
		return username;
	}
    
    public String getPassword() {
		return password;
	}
    
    public void setUsername(String username) {
		this.username = username;
	}
    
    public void setPassword(String password) {
		this.password = password;
	}
}
