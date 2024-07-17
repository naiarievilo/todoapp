package dev.naiarievilo.todoapp.todolists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static dev.naiarievilo.todoapp.todolists.ListTypes.CALENDAR;

@Service
@Transactional
public class TodoListTaskService {

    private static final Logger logger = LoggerFactory.getLogger(TodoListTaskService.class);

    private final TodoListRepository listRepository;

    public TodoListTaskService(TodoListRepository listRepository) {
        this.listRepository = listRepository;
    }

    // Delete old calendar lists
    @Scheduled(cron = "${tasks.weekly}")
    public void deleteOldCalendarLists() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        try {
            listRepository.deleteAllByTypeAndDueDate(CALENDAR, startDate);
        } catch (Exception e) {
            logger.warn("Couldn't perform old calendar lists' deletion weekly task", e);
        }
    }
}
