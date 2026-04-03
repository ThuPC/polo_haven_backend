package com.datn.backend.service.auth;

import com.datn.backend.dto.request.AuthenticationRequest;
import com.datn.backend.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate_Staff (AuthenticationRequest request);

    AuthenticationResponse authenticate_Customers(AuthenticationRequest request);

}
