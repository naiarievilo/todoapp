package dev.naiarievilo.todoapp.users;

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

    private final UserRepository userRepository;

    public UserTaskService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "${tasks.weekly}")
    public void deleteAllUnverifiedUsersAfterDeadline() {
        LocalDateTime now = LocalDateTime.now();
        List<User> unverifiedUsers = userRepository.findAllByVerified(false);

        List<User> usersToRemove = new ArrayList<>();
        for (User unverifiedUser : unverifiedUsers) {
            LocalDateTime deadLine = unverifiedUser.getCreatedAt().plusDays(EMAIL_CONFIRMATION_PERIOD);
            if (deadLine.isBefore(now)) {
                usersToRemove.add(unverifiedUser);
            }
        }

        userRepository.deleteAllInBatch(usersToRemove);
    }
}
