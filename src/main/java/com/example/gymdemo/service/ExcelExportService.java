package com.example.gymdemo.service;

import com.example.gymdemo.entity.Attendance;
import com.example.gymdemo.entity.Payment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportAttendanceReport(List<Object[]> attendanceData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance Report");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Name", "Email", "Check In Time"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Object[] rowData : attendanceData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowData[0] != null ? rowData[0].toString() : "");
                row.createCell(1).setCellValue(rowData[1] != null ? rowData[1].toString() : "");
                row.createCell(2).setCellValue(rowData[2] != null ? rowData[2].toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportPaymentReport(List<Payment> payments) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payment Report");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Member", "Amount", "Payment Date", "Mode", "Status", "Transaction ID"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Payment payment : payments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(payment.getId());
                row.createCell(1).setCellValue(payment.getMember().getUser().getName());
                row.createCell(2).setCellValue(payment.getAmount().doubleValue());
                row.createCell(3).setCellValue(payment.getPaymentDate().toString());
                row.createCell(4).setCellValue(payment.getPaymentMode());
                row.createCell(5).setCellValue(payment.getStatus());
                row.createCell(6).setCellValue(payment.getTransactionId() != null ? payment.getTransactionId() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}

