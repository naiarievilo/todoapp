package dev.naiarievilo.todoapp.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static dev.naiarievilo.todoapp.users.UserService.EMAIL_CONFIRMATION_PERIOD;

@Service
@Transactional
public class UserTaskService {

    private static final Logger logger = LoggerFactory.getLogger(UserTaskService.class);

    private final UserRepository userRepository;

    public UserTaskService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "${tasks.weekly}")
    public void deleteUnverifiedUsersAfterDeadline() {
        LocalDateTime now = LocalDateTime.now();
        List<User> unverifiedUsers = userRepository.findAllByVerified(false);

        List<User> usersToRemove = new ArrayList<>();
        for (User unverifiedUser : unverifiedUsers) {
            LocalDateTime deadLine = unverifiedUser.getCreatedAt().plusDays(EMAIL_CONFIRMATION_PERIOD);
            if (deadLine.isBefore(now)) {
                usersToRemove.add(unverifiedUser);
            }
        }

        try {
            userRepository.deleteAllInBatch(usersToRemove);
        } catch (Exception e) {
            logger.warn("Couldn't perform unverified users' deletion weekly task", e);
        }
    }
}
