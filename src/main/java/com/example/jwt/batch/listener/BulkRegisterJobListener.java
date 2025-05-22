package com.example.jwt.batch.listener;

import com.example.jwt.utils.ReportUtil;
import org.springframework.batch.core.*;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class BulkRegisterJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // You can initialize any context here if needed
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long successCountLong = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getWriteCount)
                .sum();

        long failureCountLong = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getSkipCount)
                .sum();

        int successCount = (int) successCountLong;
        int failureCount = (int) failureCountLong;

        // Generate the report
        String reportFilePath = ReportUtil.generateReport(successCount, failureCount);
        String downloadUrl = "/api/report/download/" + Paths.get(reportFilePath).getFileName();

        ExecutionContext context = jobExecution.getExecutionContext();
        context.putInt("successCount", successCount);
        context.putInt("failureCount", failureCount);
        context.putString("downloadUrl", downloadUrl);
    }
}

