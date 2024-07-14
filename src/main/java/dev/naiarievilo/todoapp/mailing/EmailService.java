package dev.naiarievilo.todoapp.mailing;

import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users_info.UserInfo;
import dev.naiarievilo.todoapp.users_info.UserInfoService;
import jakarta.mail.MessagingException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static dev.naiarievilo.todoapp.mailing.MailingConfiguration.UTF_8;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.*;

@Service
@Retryable(retryFor = MailException.class)
public class EmailService {

    private static final String APP_NAME_KEY = "appName";
    private static final String USER_FIRST_NAME_KEY = "userFirstName";

    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final UserInfoService userInfoService;
    private final JwtService jwtService;
    private final String baseUri;
    private final String appEmail;
    private final String appName;
    private final ClientUriProperties clientUriProperties;

    public EmailService(JavaMailSender mailSender, UserInfoService userInfoService, JwtService jwtService,
        SpringTemplateEngine templateEngine, MailingConfiguration mailingConfiguration,
        ClientUriProperties clientUriProperties
    ) {
        this.appName = mailingConfiguration.getAppName();
        this.clientUriProperties = clientUriProperties;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
        this.appEmail = mailingConfiguration.getAppEmail();
        this.templateEngine = templateEngine;
        this.userInfoService = userInfoService;
        this.baseUri = String.format("%s://%s:%d",
            mailingConfiguration.getScheme(), mailingConfiguration.getDomain(), mailingConfiguration.getPort());
    }

    public void sendEmailVerificationMessage(User user) throws MailException {
        UserInfo userInfo = userInfoService.getUserInfoById(user.getId());
        String emailVerificationToken = jwtService.createToken(user, VERIFICATION_TOKEN);
        String emailVerificationUri = baseUri + "/users/verify?token=" + emailVerificationToken;

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put(APP_NAME_KEY, appName);
        templateModel.put(USER_FIRST_NAME_KEY, userInfo.getFirstName());
        templateModel.put("verificationUri", emailVerificationUri);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        String htmlBody = templateEngine.process("/email-verification.html", thymeleafContext);

        mailSender.send(mimeMessage -> {
            var helper = new MimeMessageHelper(mimeMessage, UTF_8);
            helper.setFrom(appEmail, appName);
            helper.addTo(user.getEmail(), userInfo.getFirstName() + " " + userInfo.getLastName());
            helper.setSentDate(new Date());
            helper.setSubject(appName.concat(": Email verification"));
            helper.setText(htmlBody, true);
        });
    }

    public void sendUnlockUserMessage(User user) throws MailException {
        UserInfo userInfo = userInfoService.getUserInfoById(user.getId());
        String unlockUserToken = jwtService.createToken(user, UNLOCK_TOKEN);
        String unlockUserUri = baseUri + "/users/unlock?token=" + unlockUserToken;

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put(APP_NAME_KEY, appName);
        templateModel.put(USER_FIRST_NAME_KEY, userInfo.getFirstName());
        templateModel.put("unlockUserUri", unlockUserUri);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        String htmlBody = templateEngine.process("/unlock-user.html", thymeleafContext);

        mailSender.send(mimeMessage -> {
            var helper = new MimeMessageHelper(mimeMessage, UTF_8);
            helper.setFrom(appEmail, appName);
            helper.addTo(user.getEmail(), userInfo.getFirstName() + " " + userInfo.getLastName());
            helper.setSubject(appName.concat(": unlock user"));
            helper.setText(htmlBody, true);
        });
    }

    public void sendLockUserMessage(User user) throws MailException {
        UserInfo userInfo = userInfoService.getUserInfoById(user.getId());

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put(APP_NAME_KEY, appName);
        templateModel.put(USER_FIRST_NAME_KEY, userInfo.getFirstName());
        templateModel.put("unlockUserPage", clientUriProperties.unlockUser());

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        String htmlBody = templateEngine.process("/lock-user.html", thymeleafContext);

        mailSender.send(mimeMessage -> {
            var helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(appEmail, appName);
            helper.addTo(user.getEmail(), userInfo.getFirstName() + " " + userInfo.getLastName());
            helper.setSubject(appName.concat(": User temporarily locked"));
            helper.setText(htmlBody, true);
        });
    }

    public void sendEnableUserMessage(User user) throws MailException {
        UserInfo userInfo = userInfoService.getUserInfoById(user.getId());
        String enableUserToken = jwtService.createToken(user, ENABLE_TOKEN);
        String enableUserUri = baseUri + "/users/enable?token=" + enableUserToken;

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put(APP_NAME_KEY, appName);
        templateModel.put(USER_FIRST_NAME_KEY, userInfo.getFirstName());
        templateModel.put("enableUserUri", enableUserUri);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        String htmlBody = templateEngine.process("/enable-user.html", thymeleafContext);

        mailSender.send(mimeMessage -> {
            var helper = new MimeMessageHelper(mimeMessage, UTF_8);
            helper.setFrom(appEmail, appName);
            helper.addTo(user.getEmail(), userInfo.getFirstName() + " " + userInfo.getLastName());
            helper.setSubject(appName.concat(": enable user"));
            helper.setText(htmlBody, true);
        });
    }

    @Recover
    public void recoverSendMessageFailure(MailException e, User user) throws MessagingException {
        throw new MessagingException(e.getMessage(), e);
    }
}
