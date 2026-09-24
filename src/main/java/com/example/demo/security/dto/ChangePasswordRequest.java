package com.example.demo.security.dto;

public record ChangePasswordRequest(String username, String oldpassword, String newpassword, String confirmpassword) { }
