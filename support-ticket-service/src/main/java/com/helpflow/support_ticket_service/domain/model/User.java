package com.helpflow.support_ticket_service.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Modelo de dominio para usuarios y tecnicos
public class User {

    private Long id;
    private String document;
    private String name;
    private String email;
    private String password;
    private Role role;
    private String location;
    private String department;
    private UserStatus status;
    private BigDecimal salary;
    private LocalDate affiliationDate;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String document, String name, String email, String password,
                Role role, String location, String department, UserStatus status,
                BigDecimal salary, LocalDate affiliationDate, LocalDateTime createdAt) {
        this.id = id;
        this.document = document;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.location = location;
        this.department = department;
        this.status = status;
        this.salary = salary;
        this.affiliationDate = affiliationDate;
        this.createdAt = createdAt;
    }

    // Regla de negocio: saber si el usuario puede operar en el sistema
    public boolean isActive() {
        return UserStatus.ACTIVO.equals(this.status);
    }

    public boolean isTechnician() {
        return Role.ROLE_TECNICO.equals(this.role);
    }

    public boolean isAdmin() {
        return Role.ROLE_ADMIN.equals(this.role);
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getAffiliationDate() {
        return affiliationDate;
    }

    public void setAffiliationDate(LocalDate affiliationDate) {
        this.affiliationDate = affiliationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
