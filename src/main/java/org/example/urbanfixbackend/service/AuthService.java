package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.PasswordResetConfirmDTO;
import org.example.urbanfixbackend.dto.request.PasswordResetRequestDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.response.AuthResponseDTO;
import org.example.urbanfixbackend.dto.response.PasswordResetResponseDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO refreshToken(String refreshToken);
    PasswordResetResponseDTO requestPasswordReset(PasswordResetRequestDTO request);
    void confirmPasswordReset(PasswordResetConfirmDTO request);
}
