package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.mail.EmailDetails;

public interface EmailService {

    boolean sendSimpleMail(EmailDetails details);

    /**
     * Sends an HTML email using a Mustache template.
     * @param details contains recipient, subject, template name, and model
     * @return true if email sent successfully, false otherwise
     */
    boolean sendMailWithTemplate(EmailDetails details);
}
