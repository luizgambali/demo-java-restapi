package com.example.demo.customers.model;

import tools.jackson.databind.deser.jdk.UUIDDeserializer;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private UUID id;

    private String name;
    private String address;
    private String city;
    private String country;
    private String zipcode;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private boolean active;

    public Customer() {
    }

    public Customer(
            UUID id,
            String name,
            String address,
            String city,
            String country,
            String zipcode,
            String phone,
            String email,
            LocalDateTime createdAt,
            boolean active) {

        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.country = country;
        this.zipcode = zipcode;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
