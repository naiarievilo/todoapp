package dev.naiarievilo.todoapp.todolists.todos;

public class TodoServiceTestCases {

    public static final String ADDS_AND_REMOVES_TODOS_FROM_PARENT_WHEN_DTO_SET_UPDATED =
        "Adds and removes todos from parent when DTO set has both added and removed todos";
    public static final String ADDS_NEW_TODO_TO_PARENT_WHEN_NEW_DTO_IN_DTO_SET =
        "Adds new todo to parent when new todo DTO is present in the DTO set";
    public static final String CREATES_TODO_WHEN_INPUT_VALID =
        "Creates todo when todo DTO is valid";
    public static final String DELETES_TODO_WHEN_TODO_EXISTS =
        "Deletes todo when todo exists";
    public static final String REMOVES_TODO_FROM_PARENT_WHEN_TODO_NOT_IN_DTO_SET =
        "Removes todo from parent when its DTO is not present in the DTO set";
    public static final String RETURNS_TODO_WHEN_TODO_EXISTS =
        "Returns todo when todo exists";
    public static final String THROWS_POSITION_EXCEEDS_MAX_ALLOWED_WHEN_POSITION_IS_GREATER_THAN_LIST_SIZE =
        "Throws `PositionExceedsMaxAllowedException` when position is greater than the number of to-dos in list";
    public static final String THROWS_POSITION_NOT_UNIQUE_WHEN_TODOS_HAVE_SAME_POSITION =
        "Throws `PositionNotUniqueException` when todos in list have the same position";
    public static final String THROWS_TODO_NOT_FOUND_WHEN_TODO_DOES_NOT_EXIST =
        "Throws `TodoNotFoundException` when todo does not exist";
    public static final String UPDATES_TODO_WHEN_INPUT_VALID =
        "Updates todo when todo DTO is valid";
}
