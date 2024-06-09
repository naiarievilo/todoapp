package dev.naiarievilo.todoapp.security;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ErrorDetails {

    private LocalDateTime timestamp;
    private int status;
    private String reason;
    private List<String> messages = new ArrayList<>();

    public ErrorDetails() {
    }

    public ErrorDetails(HttpStatus status, String message) {
        this(status, List.of(message));
    }

    public ErrorDetails(HttpStatus status, Collection<String> messages) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.reason = status.getReasonPhrase();
        this.messages.addAll(messages);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
