package com.example.tpo_mobile.data.modelDTO;

public class UserDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private Integer age;
    private String address;
    private String urlAvatar;

    // Constructor vacío
    public UserDTO() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUrlAvatar() { return urlAvatar; }
    public void setUrlAvatar(String urlAvatar) { this.urlAvatar = urlAvatar; }

    // Metodo de conveniencia para obtener el nombre completo
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username != null ? username : "Usuario";
        }
    }

    // Para compatibilidad con el código existente
    public String getName() {
        return getFullName();
    }

    public void setName(String name) {
        // Si se llama este metodo, dividir el nombre en firstName y lastName
        if (name != null) {
            String[] parts = name.trim().split(" ", 2);
            this.firstName = parts[0];
            if (parts.length > 1) {
                this.lastName = parts[1];
            }
        }
    }
}