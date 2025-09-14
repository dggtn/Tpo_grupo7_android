package com.example.tpo_mobile.model;

public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private Integer age;
    private String address;
    private String urlAvatar;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) { this.address = address; }

    public String getUrlAvatar() { return urlAvatar; }
    public void setUrlAvatar(String urlAvatar) { this.urlAvatar = urlAvatar; }
}
