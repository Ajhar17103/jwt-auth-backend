package com.example.jwt.confiq;

import com.example.jwt.batch.processor.UserItemProcessor;
import com.example.jwt.batch.reader.ExcelUserItemReader;
import com.example.jwt.batch.writer.UserItemWriter;
import com.example.jwt.entity.Users;
import com.example.jwt.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
    @StepScope
    public ExcelUserItemReader reader(@Value("#{jobParameters['filePath']}") String filePath,
                                      UsersRepo usersRepo) {
        return new ExcelUserItemReader(filePath, usersRepo);
    }

//    @Bean
//    public TaskExecutor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(4);       // Tune based on your CPU cores
//        executor.setMaxPoolSize(8);
//        executor.setQueueCapacity(100);
//        executor.setThreadNamePrefix("Batch-");
//        executor.initialize();
//        return executor;
//    }

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
