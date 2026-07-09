package kingo_ecom.service;

import kingo_ecom.dto.auth.AuthRequestDTO;
import kingo_ecom.dto.auth.AuthResponseDTO;

public interface IAuthService {
    AuthResponseDTO register(AuthRequestDTO request);

    AuthResponseDTO login(AuthRequestDTO request);
}

