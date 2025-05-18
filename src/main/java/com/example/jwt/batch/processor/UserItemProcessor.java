package com.example.jwt.batch.processor;

import com.example.jwt.entity.Users;
import com.example.jwt.exception.ValidationException;
import com.example.jwt.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import static com.example.jwt.utils.ValidationUtils.isValidEmail;
import static com.example.jwt.utils.ValidationUtils.isValidPassword;

@Component
@RequiredArgsConstructor
public class UserItemProcessor implements ItemProcessor<Users, Users> {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepo usersRepo;

    @Override
    public Users process(Users user) throws ValidationException {
        String email = user.getEmail();
        String password = user.getPassword();

        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid email format");
        }

        if (!isValidPassword(password)) {
            throw new ValidationException("Invalid password format");
        }

        if (usersRepo.existsByEmail(email)) {
            throw new ValidationException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setIs_deleted(0);
        return user;
    }

}

