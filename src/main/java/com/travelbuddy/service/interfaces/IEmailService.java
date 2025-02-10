package com.travelbuddy.service.interfaces;

public interface IEmailService {
    void sendEmail(String to, String subject, String text);
}
