package com.example.tpo_mobile.data.modelDTO;

public class RegisterRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Integer age;
    private String address;
    private String urlAvatar;

    public RegisterRequest() {}

    public RegisterRequest(String username, String firstName, String lastName,
                           String email, String password, Integer age, String address, String urlAvatar) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.age = age;
        this.address = address;
        this.urlAvatar = urlAvatar;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUrlAvatar() { return urlAvatar; }
    public void setUrlAvatar(String urlAvatar) { this.urlAvatar = urlAvatar; }
}