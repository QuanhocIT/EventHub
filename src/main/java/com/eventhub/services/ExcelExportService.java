package com.eventhub.services;

public interface ExcelExportService {

    byte[] exportEventAttendees(Long eventId, String currentUsername, boolean isAdmin);

    byte[] exportOrganizerRevenueReport(Long organizerId, Integer year, String currentUsername, boolean isAdmin);
}
