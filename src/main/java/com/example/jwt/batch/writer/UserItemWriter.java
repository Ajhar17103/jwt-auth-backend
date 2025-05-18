package com.example.jwt.batch.writer;

import com.example.jwt.entity.Users;
import com.example.jwt.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserItemWriter implements ItemWriter<Users> {

    private final UsersRepo usersRepo;

    @Override
    public void write(Chunk<? extends Users> chunk) throws Exception {
        usersRepo.saveAll(chunk.getItems());
    }
}

