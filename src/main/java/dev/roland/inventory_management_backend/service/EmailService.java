package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.mail.EmailDetails;

public interface EmailService {

    boolean sendSimpleMail(EmailDetails details);

    boolean sendMailWithTemplate(EmailDetails details);
}
