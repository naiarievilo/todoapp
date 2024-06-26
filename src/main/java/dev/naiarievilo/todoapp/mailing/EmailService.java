package dev.naiarievilo.todoapp.mailing;

import dev.naiarievilo.todoapp.users.User;
import org.springframework.mail.MailException;

public interface EmailService {

    void sendEmailVerificationMessage(User user) throws MailException;

    void sendUnlockUserMessage(User user) throws MailException;

    void sendLockUserMessage(User user) throws MailException;

    void sendEnableUserMessage(User user) throws MailException;
}
