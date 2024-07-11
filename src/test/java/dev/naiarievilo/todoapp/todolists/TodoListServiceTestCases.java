package dev.naiarievilo.todoapp.todolists;

public class TodoListServiceTestCases {

    public static final String ADDS_TODO_TO_LIST_WHEN_USER_HAS_LIST_ACCESS =
        "Adds todo to list when user has access to the list";
    public static final String CREATES_AND_RETURNS_INBOX_LIST_WHEN_LIST_DOES_NOT_EXIST =
        "Creates and returns inbox list when inbox list does not exist";
    public static final String CREATES_AND_RETURNS_TODAY_LIST_WHEN_LIST_DOES_NOT_EXIST =
        "Creates and returns today's list when list does not exist";
    public static final String CREATES_AND_RETURNS_WEEKLY_LISTS_WHEN_LISTS_DO_NOT_EXIST =
        "Creates and returns weekly lists when lists do not exist";
    public static final String CREATES_LIST_WHEN_INPUT_VALID =
        "Creates list when input is valid";
    public static final String DELETES_LIST_WHEN_USER_HAS_ACCESS =
        "Deletes list when user has access to the list";
    public static final String DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS =
        "Deletes todo from list when user has access to the list";
    public static final String DOES_NOT_ADD_TODO_TO_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS =
        "Does not add todo to list when user does not have access to the list";
    public static final String DOES_NOT_DELETE_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS =
        "Does not delete list when user does not have access to the list";
    public static final String DOES_NOT_DELETE_TODO_FROM_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS =
        "Does not delete todo from list when user does not have access to the list";
    public static final String DOES_NOT_UPDATE_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS =
        "Does not update list when user does not have access to the list";
    public static final String DOES_NOT_UPDATE_TODO_FROM_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS =
        "Does not update todo from list when user does not have access to the list";
    public static final String RETURNS_ALL_USER_CUSTOM_LISTS =
        "Returns all custom lists of user";
    public static final String RETURNS_INBOX_LIST_WHEN_LIST_EXISTS =
        "Returns inbox list when inbox list exists";
    public static final String RETURNS_LIST_WHEN_LIST_EXISTS =
        "Returns list when list exists";
    public static final String RETURNS_TODAY_LIST_WHEN_LIST_EXISTS =
        "Returns today's list when list exists";
    public static final String RETURNS_WEEKLY_LISTS_WHEN_LISTS_EXIST =
        "Returns weekly lists when list exists";
    public static final String THROWS_LIST_NOT_FOUND_WHEN_LIST_DOES_NOT_EXIST =
        "Throws `TodoListNotFoundException` when list does not exist";
    public static final String THROWS_TODO_NOT_FOUND_WHEN_TODO_NOT_IN_LIST =
        "Throw `TodoNotFoundException` when todo does not exist in list";
    public static final String THROWS_UNAUTHORIZED_DATA_ACCESS_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS =
        "Throws `UnauthorizedDataAccessException` when user is not list owner";
    public static final String UPDATES_LIST_AND_ITS_TODOS_WHEN_USER_HAS_ACCESS =
        "Updates list and its to-dos when user has access to the list";
    public static final String UPDATES_LIST_WHEN_USER_HAS_LIST_ACCESS =
        "Updates list when user has access to the list";
    public static final String UPDATES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS =
        "Updates todo from list when user has access to the list";
}
