package dev.naiarievilo.todoapp.todolists;

import static dev.naiarievilo.todoapp.ResponseConstants.*;

public class TodoListControllerTestCases {

    public static final String STATUS_200_CREATES_LIST_WHEN_USER_AUTHENTICATED =
        OK + "Creates new custom list when user is authenticated";
    public static final String STATUS_200_RETURNS_ALL_CUSTOM_LISTS_WHEN_USER_AUTHENTICATED =
        OK + "Returns all custom lists when user is authenticated";
    public static final String STATUS_200_RETURNS_INBOX_LIST_WHEN_USER_AUTHENTICATED =
        OK + "Returns inbox list when user is authenticated";
    public static final String STATUS_200_RETURNS_TODAY_LIST_WHEN_USER_AUTHENTICATED =
        OK + "Returns today list when user is authenticated";
    public static final String STATUS_200_RETURNS_WEEK_LISTS_WHEN_USER_AUTHENTICATED =
        OK + "Returns week lists when user is authenticated";
    public static final String STATUS_201_ADDS_TODO_TO_LIST_WHEN_USER_HAS_LIST_ACCESS =
        CREATED + "Adds todo to list when user has access to the list";
    public static final String STATUS_204_DELETES_ALL_TODOS_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS =
        NO_CONTENT + "Deletes all todos from list when user has list access";
    public static final String STATUS_204_DELETES_LIST_WHEN_USER_HAS_LIST_ACCESS =
        NO_CONTENT + "Deletes list when user has access to the list";
    public static final String STATUS_204_DELETES_TODOS_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS =
        NO_CONTENT + "Deletes todos from list when user has access to the list";
    public static final String STATUS_204_DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS =
        NO_CONTENT + "Deletes todo from list when user has access to the list";
    public static final String STATUS_204_UPDATES_LIST_WHEN_USER_HAS_LIST_ACCESS =
        NO_CONTENT + "Updates list when user has access to list";
    public static final String STATUS_204_UPDATES_TODOS_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS =
        NO_CONTENT + "Updates todos from list when user has access to the list";
    public static final String STATUS_204_UPDATES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS =
        NO_CONTENT + "Updates todo from list when user has access to the list";
    public static final String STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_LIST_NOT_FOUND =
        NOT_FOUND + RETURNS_ERROR_MESSAGES_WHEN + "list does not exist";
    public static final String STATUS_401_RETURNS_ERROR_MESSAGE_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS =
        UNAUTHORIZED + RETURNS_ERROR_MESSAGES_WHEN + "user does not have access to the list";
    public static final String STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_TODO_NOT_FOUND =
        NOT_FOUND + RETURNS_ERROR_MESSAGES_WHEN + "todo does not exist";
}
