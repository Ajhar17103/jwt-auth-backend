package com.example.jwt.confiq;

import com.example.jwt.batch.processor.UserItemProcessor;
import com.example.jwt.batch.reader.ExcelUserItemReader;
import com.example.jwt.batch.writer.UserItemWriter;
import com.example.jwt.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final UserItemProcessor processor;
    private final UserItemWriter writer;

    @Bean
    public Step step1(ExcelUserItemReader reader) {
        return new StepBuilder("step1", jobRepository)
                .<Users, Users>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job userImportJob(Step step1) {
        return new JobBuilder("userImportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }
}
