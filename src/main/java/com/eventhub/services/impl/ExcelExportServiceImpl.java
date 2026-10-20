package com.eventhub.services.impl;

import com.eventhub.entities.Booking;
import com.eventhub.entities.Event;
import com.eventhub.entities.Ticket;
import com.eventhub.entities.User;
import com.eventhub.enums.BookingStatus;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExcelExportServiceImpl implements ExcelExportService {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    @Override
    public byte[] exportEventAttendees(Long eventId, String currentUsername, boolean isAdmin) {
        log.info("Generating Excel attendee report for event ID: {} (requested by: {}, isAdmin: {})", eventId, currentUsername, isAdmin);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        if (!isAdmin && !event.getOrganizer().getUsername().equalsIgnoreCase(currentUsername)) {
            throw new ForbiddenException("You are not authorized to export attendee reports for this event");
        }

        List<Ticket> tickets = ticketRepository.findByEventIdOrderByCreatedAtAsc(eventId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh Sách Khách Tham Dự");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerDataStyle = createCenterDataStyle(workbook);

            // Title Row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH KHÁCH THAM DỰ - " + event.getTitle().toUpperCase());
            titleCell.setCellStyle(titleStyle);

            // Meta Info Row
            Row metaRow = sheet.createRow(1);
            metaRow.createCell(0).setCellValue("Địa điểm: " + (event.getLocation() != null ? event.getLocation() : "N/A"));
            metaRow.createCell(3).setCellValue("Thời gian: " + (event.getStartTime() != null ? event.getStartTime().format(DATE_TIME_FORMATTER) : "N/A"));
            metaRow.createCell(6).setCellValue("Xuất lúc: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));

            // Header Row
            String[] headers = {
                    "STT", "Mã Vé", "Mã Đơn Hàng", "Họ và Tên", "Email",
                    "Số Điện Thoại", "Hạng Vé", "Vị Trí Ghế", "Trạng Thái",
                    "Thời Gian Check-in", "Ngày Mua Vé"
            };

            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 4;
            int stt = 1;
            for (Ticket ticket : tickets) {
                Row row = sheet.createRow(rowIdx++);

                User attendee = ticket.getBooking() != null ? ticket.getBooking().getUser() : null;
                String attendeeName = attendee != null && attendee.getFullName() != null ? attendee.getFullName() : (attendee != null ? attendee.getUsername() : "N/A");
                String attendeeEmail = attendee != null ? attendee.getEmail() : "N/A";
                String attendeePhone = attendee != null && attendee.getPhoneNumber() != null ? attendee.getPhoneNumber() : "N/A";

                String ticketType = ticket.getTicketType() != null ? ticket.getTicketType().getName() : "Standard";
                String seatInfo = "Tự do";
                if (ticket.getEventSeat() != null && ticket.getEventSeat().getSeat() != null) {
                    seatInfo = String.format("Dãy %s - Ghế %s (%s)",
                            ticket.getEventSeat().getSeat().getSeatRow(),
                            ticket.getEventSeat().getSeat().getSeatNumber(),
                            ticket.getEventSeat().getSeat().getZone());
                }

                String checkInTime = ticket.getCheckedInAt() != null ? ticket.getCheckedInAt().format(DATE_TIME_FORMATTER) : "-";
                String createdAt = ticket.getCreatedAt() != null ? ticket.getCreatedAt().format(DATE_TIME_FORMATTER) : "-";

                createCell(row, 0, String.valueOf(stt++), centerDataStyle);
                createCell(row, 1, ticket.getTicketCode(), centerDataStyle);
                createCell(row, 2, ticket.getBooking() != null ? ticket.getBooking().getBookingCode() : "-", centerDataStyle);
                createCell(row, 3, attendeeName, dataStyle);
                createCell(row, 4, attendeeEmail, dataStyle);
                createCell(row, 5, attendeePhone, centerDataStyle);
                createCell(row, 6, ticketType, dataStyle);
                createCell(row, 7, seatInfo, centerDataStyle);
                createCell(row, 8, ticket.getStatus().name(), centerDataStyle);
                createCell(row, 9, checkInTime, centerDataStyle);
                createCell(row, 10, createdAt, centerDataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error generating Excel attendee report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportOrganizerRevenueReport(Long organizerId, Integer year, String currentUsername, boolean isAdmin) {
        log.info("Generating Excel revenue report for organizer ID: {} in year: {} (viewer: {})", organizerId, year, currentUsername);

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with ID: " + organizerId));

        if (!isAdmin && !organizer.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new ForbiddenException("You are not authorized to export revenue reports for this organizer");
        }

        int targetYear = year != null ? year : LocalDateTime.now().getYear();

        List<Booking> bookings = bookingRepository.findByEventOrganizerIdOrderByCreatedAtDesc(organizerId).stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().getYear() == targetYear)
                .toList();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo Cáo Doanh Thu " + targetYear);

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerDataStyle = createCenterDataStyle(workbook);

            // Title Row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO DOANH THU NĂM " + targetYear + " - " + (organizer.getFullName() != null ? organizer.getFullName() : organizer.getUsername()).toUpperCase());
            titleCell.setCellStyle(titleStyle);

            BigDecimal totalSubtotal = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;
            BigDecimal totalNet = BigDecimal.ZERO;
            int confirmedCount = 0;

            for (Booking b : bookings) {
                if (b.getStatus() == BookingStatus.CONFIRMED) {
                    confirmedCount++;
                    totalSubtotal = totalSubtotal.add(b.getSubtotalAmount() != null ? b.getSubtotalAmount() : BigDecimal.ZERO);
                    totalDiscount = totalDiscount.add(b.getDiscountAmount() != null ? b.getDiscountAmount() : BigDecimal.ZERO);
                    totalNet = totalNet.add(b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO);
                }
            }

            // Summary cards
            Row summaryRow1 = sheet.createRow(2);
            summaryRow1.createCell(0).setCellValue("Tổng đơn hàng thành công: " + confirmedCount);
            summaryRow1.createCell(3).setCellValue("Tổng tiền vé: " + totalSubtotal + " VND");

            Row summaryRow2 = sheet.createRow(3);
            summaryRow2.createCell(0).setCellValue("Tổng chiết khấu voucher: " + totalDiscount + " VND");
            summaryRow2.createCell(3).setCellValue("Thực thu (Doanh thu ròng): " + totalNet + " VND");

            // Monthly breakdown header
            String[] headers = {
                    "Tháng", "Số Đơn Thành Công", "Tổng Tiền Vé (VND)",
                    "Tiền Giảm Giá (VND)", "Thực Thu (VND)"
            };

            Row headerRow = sheet.createRow(5);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (int month = 1; month <= 12; month++) {
                final int m = month;
                List<Booking> monthBookings = bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.CONFIRMED && b.getCreatedAt().getMonthValue() == m)
                        .toList();

                BigDecimal mSubtotal = monthBookings.stream().map(Booking::getSubtotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal mDiscount = monthBookings.stream().map(Booking::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal mNet = monthBookings.stream().map(Booking::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, "Tháng " + month, centerDataStyle);
                createCell(row, 1, String.valueOf(monthBookings.size()), centerDataStyle);
                createCell(row, 2, mSubtotal.toPlainString(), dataStyle);
                createCell(row, 3, mDiscount.toPlainString(), dataStyle);
                createCell(row, 4, mNet.toPlainString(), dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error generating Excel revenue report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage());
        }
    }

    private void createCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createCenterDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
