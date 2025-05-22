package com.example.jwt.service;

import com.example.jwt.entity.Users;
import com.example.jwt.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsersDetailService implements UserDetailsService {

    private final UsersRepo usersRepo;

    @Autowired
    public UsersDetailService(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        return usersRepo.findByEmail(username).orElseThrow();
    }
}
