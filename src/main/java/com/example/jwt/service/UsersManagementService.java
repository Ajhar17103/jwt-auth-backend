package com.example.jwt.service;

import com.example.jwt.dto.Response;
import com.example.jwt.entity.BlacklistedToken;
import com.example.jwt.entity.Users;
import com.example.jwt.params.Request;
import com.example.jwt.repository.BlacklistedTokenRepository;
import com.example.jwt.repository.UsersRepo;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class UsersManagementService {


    private final UsersRepo usersRepo;

    private final JWTUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public UsersManagementService(UsersRepo usersRepo, JWTUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, BlacklistedTokenRepository blacklistedTokenRepository) {
        this.usersRepo = usersRepo;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }


    public Response register(Request registrationRequest) {
        Response resp = new Response();

        try{
           Users user = new Users();

           user.setEmail(registrationRequest.getEmail());
           user.setName(registrationRequest.getName());
           user.setRole(registrationRequest.getRole());
           user.setCity(registrationRequest.getCity());
           user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

           Users userResult = usersRepo.save(user);

           if(userResult.getId()>0){
               resp.setUsers(userResult);
               resp.setMessage("User Saved Successfully");
               resp.setStatusCode(200);
           }

        }
        catch (Exception e){
            resp.setStatusCode(500);
            resp.setMessage(e.getMessage());
        }
        return resp;
    }

    public Response login(Request loginRequest) {
        Response response = new Response();

        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            var user = usersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
            var jwt  = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole());
            response.setRefreshToken(refreshToken);
            response.setMessage("User Login Successfully");

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());

        }
        return response;
    }

    public Response refreshToken(Response refreshTokenRequest) {
        Response response = new Response();
        System.out.println("getToken: " + refreshTokenRequest.getToken());

        try{
            String email = jwtUtils.extractUsername(refreshTokenRequest.getToken());
            Users user = usersRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), user)) {
                String newAccessToken = jwtUtils.generateToken(user);
                String newRefreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

                response.setStatusCode(200);
                response.setToken(newAccessToken);
                response.setRefreshToken(newRefreshToken);
                response.setRole(user.getRole());
                response.setMessage("Successfully Refreshed Token");
            } else {
                response.setStatusCode(401);
                response.setMessage("Invalid or expired refresh token.");
            }
        } catch (ExpiredJwtException e) {
            response.setStatusCode(401);
            response.setMessage("Refresh token has expired. Please log in again.");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Internal server error: " + e.getMessage());
        }

        return response;
    }

    public Response logout(Response logoutRequest) {
        Response response = new Response();
        try{
        String token = logoutRequest.getToken();
        String refreshToken = logoutRequest.getRefreshToken();

        if(!blacklistedTokenRepository.existsByToken(token) && !blacklistedTokenRepository.existsByToken(refreshToken)){
            Date tokenExp = jwtUtils.extractExpiration(token);
            BlacklistedToken access = new BlacklistedToken();
            access.setType("access token");
            access.setToken(token);
            access.setExpiration(tokenExp);
            blacklistedTokenRepository.save(access);

            Date refreshExp = jwtUtils.extractExpiration(refreshToken);
            BlacklistedToken refresh = new BlacklistedToken();
            refresh.setType("refresh token");
            refresh.setToken(refreshToken);
            refresh.setExpiration(refreshExp);
            blacklistedTokenRepository.save(refresh);

            response.setStatusCode(200);
            response.setMessage("Successfully Logout!");

        }else{
            response.setStatusCode(401);
            response.setMessage("You Already logged Out!");
        }
            return response;
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Internal server error: " + e.getMessage());
            return response;
     }

   }

    public Response getAllUser() {
        Response response = new Response();
        try {
            List<Users> users = usersRepo.findAll();
            if (!users.isEmpty()){
                response.setUsersList(users);
            }
            response.setStatusCode(200);
            response.setMessage("All Users Successfully");
            return response;
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Internal server error: " + e.getMessage());
            return response;
        }
    }
}
