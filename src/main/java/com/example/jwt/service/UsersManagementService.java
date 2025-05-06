package com.example.jwt.service;

import com.example.jwt.dto.ReqRes;
import com.example.jwt.entity.Users;
import com.example.jwt.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class UsersManagementService {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public ReqRes register(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();

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

    public ReqRes login(ReqRes loginRequest) {
        ReqRes response = new ReqRes();

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

    public ReqRes refreshToken(ReqRes refreshTokenRequest) {
        ReqRes response = new ReqRes();

        try{
           String email = jwtUtils.extractUsername(refreshTokenRequest.getToken());
           Users user = usersRepo.findByEmail(email).orElseThrow();

           if(jwtUtils.isTokenValid(refreshTokenRequest.getToken(), user)){
               var jwt=jwtUtils.generateToken(user);

               response.setStatusCode(200);
               response.setToken(jwt);
               response.setRole(user.getRole());
               response.setRefreshToken(refreshTokenRequest.getToken());
               response.setMessage("Successfully Refreshed Token");

           }
            response.setStatusCode(200);
            return response;

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    public String logout(ReqRes logoutRequest) {
        return "logout successfu";
  }

}
