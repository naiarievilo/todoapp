# Todoapp API

Todoapp is a REST-like API for frontend applications.

## Features

- Personal accounts:
    - Support for storing the user's avatar URLs
- Mailing service:
    - Email verification
    - Account unlocking
    - Account enabling
- To-do lists:
    - Inbox lists for general to-dos
    - Daily and weekly lists for short-term to-dos
    - Custom lists for long-term to-dos
- To-dos:
    - Support for drag-and-drop behavior through to-do positioning
    - Support for due dates for inbox and custom lists to help define deadlines for each to-do

## Requirements

- Docker >= 21.x
- Java SE 21

## Running the application

1. Clone the repository with `git clone https://github.com/naiarievilo/todoapp.git`
2. Run the application with `./mvnw spring-boot:run` or through your IDE of choice.

## Usage

After initializing the application, access http://localhost:8080/swagger-ui/index.html and search for `/api-docs` to
refer to the API's OpenAPI documentation:

![API documentation screenshot](/img/api-docs-screenshot.png)