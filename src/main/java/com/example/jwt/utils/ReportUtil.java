package com.example.jwt.utils;

import io.jsonwebtoken.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;

public class ReportUtil {

    public static String generateReport(int success, int failure) {
        // Generate Excel with Apache POI or similar
        String fileName = "bulk_report_" + System.currentTimeMillis() + ".xlsx";
        String path = System.getProperty("java.io.tmpdir") + "/" + fileName;

        // Write dummy data or actual success/failure rows
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Summary");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Success");
            header.createCell(1).setCellValue("Failure");

            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue(success);
            data.createCell(1).setCellValue(failure);

            try (FileOutputStream fos = new FileOutputStream(path)) {
                workbook.write(fos);
            }
        } catch (IOException | java.io.IOException e) {
            throw new RuntimeException("Failed to generate report", e);
        }

        return path;
    }
}

