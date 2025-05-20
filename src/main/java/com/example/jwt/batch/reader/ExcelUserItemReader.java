package com.example.jwt.batch.reader;

import com.example.jwt.entity.Users;
import com.example.jwt.repository.UsersRepo;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Component
@StepScope
@Slf4j
public class ExcelUserItemReader implements ItemReader<Users>, ItemStream {

    private Iterator<Row> rowIterator;
    private Set<String> existingEmails;
    private Set<String> seenEmailsInExcel; // Track duplicates within Excel
    private Workbook workbook;
    private final UsersRepo usersRepo;
    private final String filePath;
    private int currentRowNum = 0;

    public ExcelUserItemReader(@Value("#{jobParameters['filePath']}") String filePath,
                               UsersRepo usersRepo) {
        this.filePath = filePath;
        this.usersRepo = usersRepo;
    }

    private void init() throws IOException {
        if (workbook != null) return;

        workbook = new XSSFWorkbook(new FileInputStream(filePath));
        Sheet sheet = workbook.getSheetAt(0);
        rowIterator = sheet.iterator();

        if (rowIterator.hasNext()) {
            rowIterator.next(); // skip header
            currentRowNum++;
        }

        existingEmails = new HashSet<>(usersRepo.findAllEmails());
        seenEmailsInExcel = new HashSet<>();
    }

    @Override
    public Users read() throws IOException {
        if (workbook == null) init();
        if (rowIterator == null) return null;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            currentRowNum++;

            String name = getCellValue(row.getCell(0));
            String email = getCellValue(row.getCell(1));
            String role = getCellValue(row.getCell(2));
            String password = getCellValue(row.getCell(3));

            if (existingEmails.contains(email)) {
                log.warn("Skipping duplicate email (DB): {}", email);
                continue;
            }

            if (seenEmailsInExcel.contains(email)) {
                log.warn("Skipping duplicate email (Excel): {}", email);
                continue;
            }

            seenEmailsInExcel.add(email);

            Users user = new Users();
            user.setName(name);
            user.setEmail(email);
            user.setRole(role);
            user.setPassword(password);
            return user;
        }

        return null;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> cell.toString().trim();
        };
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            init();
            if (executionContext.containsKey("currentRowNum")) {
                int savedRowNum = executionContext.getInt("currentRowNum");
                while (currentRowNum < savedRowNum && rowIterator.hasNext()) {
                    rowIterator.next();
                    currentRowNum++;
                }
            }
        } catch (IOException e) {
            throw new ItemStreamException("Failed to open Excel workbook", e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt("currentRowNum", currentRowNum);
    }

    @Override
    public void close() throws ItemStreamException {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                throw new ItemStreamException("Failed to close workbook", e);
            }
        }
    }

    @PreDestroy
    public void preDestroy() {
        close();
    }
}
