package dev.naiarievilo.todoapp.todolists;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.ListTypes.*;
import static dev.naiarievilo.todoapp.todolists.TodoListServiceImpl.CALENDAR_LIST_TITLE;

public class TodoListsTestHelper {

    public static final Long LIST_ID_1 = 1L;
    public static final Long LIST_ID_2 = 2L;
    public static final Long LIST_ID_3 = 3L;
    public static final String LIST_TITLE_1 = "List title 1";
    public static final String LIST_TITLE_2 = "List title 2";
    public static final String LIST_TITLE_3 = "List title 3";
    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate START_OF_WEEK = TODAY.minusDays(TODAY.getDayOfWeek().getValue() - 1);
    public static final LocalDate NEXT_WEEK = START_OF_WEEK.plusWeeks(1);

    public static TodoList inboxList() {
        TodoList inboxList = new TodoList();
        inboxList.setTitle(INBOX.getType());
        inboxList.setType(INBOX);
        return inboxList;
    }

    public static TodoList todayList() {
        TodoList todayList = new TodoList();
        todayList.setTitle(TODAY.format(CALENDAR_LIST_TITLE));
        todayList.setType(CALENDAR);
        todayList.setDueDate(TODAY);
        return todayList;
    }

    public static Set<TodoList> weeklyLists() {
        LocalDate dayOfWeek = START_OF_WEEK;
        Set<TodoList> weeklyLists = new LinkedHashSet<>();
        while (dayOfWeek.isBefore(NEXT_WEEK)) {
            TodoList listDay = new TodoList();
            listDay.setTitle(dayOfWeek.format(CALENDAR_LIST_TITLE));
            listDay.setType(CALENDAR);
            listDay.setDueDate(dayOfWeek);
            weeklyLists.add(listDay);
            dayOfWeek = dayOfWeek.plusDays(1);
        }

        return weeklyLists;
    }

    public static TodoList list_1() {
        TodoList list = new TodoList();
        list.setId(LIST_ID_1);
        list.setTitle(LIST_TITLE_1);
        list.setType(PERSONALIZED);
        return list;
    }

}
