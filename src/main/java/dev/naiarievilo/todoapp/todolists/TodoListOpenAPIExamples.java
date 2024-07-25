package dev.naiarievilo.todoapp.todolists;

public class TodoListOpenAPIExamples {

    public static final String CUSTOM_LISTS_EXAMPLE = """
        {
          "_embedded": {
            "todoListDTOList": [
              {
                "id": 9,
                "title": "My Custom List",
                "type": "custom",
                "created_at": "2024-07-25T15:07:24.545021",
                "due_date": null,
                "todos": [],
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/users/1/todolists/9"
                  },
                  "todos": {
                    "href": "http://localhost:8080/users/1/todolists/9/todos"
                  }
                }
              }
            ]
          }
        }
        """;
    public static final String INBOX_LIST_EXAMPLE = """
        {
          "id": 1,
          "title": "Inbox",
          "type": "inbox",
          "created_at": "2024-07-25T02:46:45.235567",
          "due_date": null,
          "todos": [
            {
              "id": 1,
              "task": "Task to complete",
              "completed": false,
              "position": 1,
              "created_at": "2024-07-25T15:24:45.44054",
              "due_date": null,
              "_links": {
                "self": {
                  "href": "http://localhost:8080/users/1/todolists/1/todos/1"
                }
              }
            }
          ],
          "_links": {
            "self": {
              "href": "http://localhost:8080/users/1/todolists/1"
            },
            "todos": {
              "href": "http://localhost:8080/users/1/todolists/1/todos"
            }
          }
        }
        """;
    public static final String NEW_CUSTOM_LIST_EXAMPLE = """
        {
          "id": 10,
          "title": "My Custom List",
          "type": "custom",
          "created_at": "2024-07-25T17:57:30.548317913",
          "due_date": null,
          "todos": [],
          "_links": {
            "self": {
              "href": "http://localhost:8080/users/1/todolists/10"
            },
            "todos": {
              "href": "http://localhost:8080/users/1/todolists/10/todos"
            }
          }
        }
        """;
    public static final String NEW_TODO_EXAMPLE = """
        {
          "task": "New task with deadline",
          "completed": false,
          "due_date": "2024-07-29"
        }
        """;
    public static final String TODAY_LIST_EXAMPLE = """
        {
          "id": 2,
          "title": "Thursday, 25 Jul 2024",
          "type": "calendar",
          "created_at": "2024-07-25T02:52:33.328172",
          "due_date": "2024-07-25",
          "todos": [
            {
              "id": 2,
              "task": "Task to complete",
              "completed": false,
              "position": 1,
              "created_at": "2024-07-25T15:34:31.275776",
              "due_date": null,
              "_links": {
                "self": {
                  "href": "http://localhost:8080/users/1/todolists/2/todos/2"
                }
              }
            }
          ],
          "_links": {
            "self": {
              "href": "http://localhost:8080/users/1/todolists/2"
            },
            "todos": {
              "href": "http://localhost:8080/users/1/todolists/2/todos"
            }
          }
        }
        """;
    public static final String TODOS_EXAMPLE = """
        {
          "_embedded": {
            "todoDTOList": [
              {
                "id": 1,
                "task": "Task to complete",
                "completed": false,
                "position": 1,
                "created_at": "2024-07-25T15:24:45.44054",
                "due_date": null,
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/users/1/todolists/1/todos/1"
                  }
                }
              }
            ]
          }
        }
        """;
    public static final String TODO_EXAMPLE = """
        {
          "id": 1,
          "task": "Task to complete",
          "completed": true,
          "position": 1,
          "created_at": "2024-07-25T15:24:45.44054",
          "due_date": null,
          "_links": {
            "self": {
              "href": "http://localhost:8080/users/1/todolists/1/todos/1"
            }
          }
        }
        """;
    public static final String UPDATED_TODOS_EXAMPLE = """
        [
          {
            "id": 1,
            "task": "Task to complete",
            "completed": true,
            "position": 1,
            "due_date": null
          }
        ]
        """;
    public static final String UPDATED_TODO_EXAMPLE = """
        {
          "id": 1,
          "task": "Task to complete",
          "completed": true,
          "position": 1,
          "due_date": null
        }
        """;
    public static final String WEEK_LISTS_EXAMPLE = """
        {
          "_embedded": {
            "todoListDTOList": [
              {
                "id": 3,
                "title": "Monday, 22 Jul 2024",
                "type": "calendar",
                "created_at": "2024-07-25T02:56:30.857387",
                "due_date": "2024-07-22",
                "todos": [],
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/users/1/todolists/3"
                  },
                  "todos": {
                    "href": "http://localhost:8080/users/1/todolists/3/todos"
                  }
                }
              },
              {
                "id": 4,
                "title": "Tuesday, 23 Jul 2024",
                "type": "calendar",
                "created_at": "2024-07-25T02:56:30.862066",
                "due_date": "2024-07-23",
                "todos": [],
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/users/1/todolists/4"
                  },
                  "todos": {
                    "href": "http://localhost:8080/users/1/todolists/4/todos"
                  }
                }
              },
              {
                "id": 5,
                "title": "Wednesday, 24 Jul 2024",
                "type": "calendar",
                "created_at": "2024-07-25T02:56:30.867128",
                "due_date": "2024-07-24",
                "todos": [],
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/users/1/todolists/5"
                  },
                  "todos": {
                    "href": "http://localhost:8080/users/1/todolists/5/todos"
                  }
                }
              },
              {
                "id": 2,
                "title": "Thursday, 25 Jul 2024",
                "type": "calendar",
                "created_at": "2024-07-25T02:52:33.328172",
                "due_date": "2024-07-25",
                "todos": [
                  {
                    "id": 2,
                    "task": "Task to complete",
                    "completed": false,
                    "position": 1,
                    "created_at": "2024-07-25T15:34:31.275776",
                    "due_date": null,
                    "_links": {
                      "self": {
                        "href": "http://localhost:8080/users/1/todolists/2/todos/2"
                      }
                    }
                  }
                ],
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/users/1/todolists/2"
                  },
                  "todos": {
                    "href": "http://localhost:8080/users/1/todolists/2/todos"
                  }
                }
              },
              {
                "id": 6,
                "title": "Friday, 26 Jul 2024",
                "type": "calendar",
                "created_at": "2024-07-25T02:56:30.877598",
                "due_date": "2024-07-26",
                "todos": [],
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/users/1/todolists/6"
                  },
                  "todos": {
                    "href": "http://localhost:8080/users/1/todolists/6/todos"
                  }
                }
              },
              {
                "id": 7,
                "title": "Saturday, 27 Jul 2024",
                "type": "calendar",
                "created_at": "2024-07-25T02:56:30.883132",
                "due_date": "2024-07-27",
                "todos": [],
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/users/1/todolists/7"
                  },
                  "todos": {
                    "href": "http://localhost:8080/users/1/todolists/7/todos"
                  }
                }
              },
              {
                "id": 8,
                "title": "Sunday, 28 Jul 2024",
                "type": "calendar",
                "created_at": "2024-07-25T02:56:30.88868",
                "due_date": "2024-07-28",
                "todos": [],
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/users/1/todolists/8"
                  },
                  "todos": {
                    "href": "http://localhost:8080/users/1/todolists/8/todos"
                  }
                }
              }
            ]
          }
        }
        """;
}
