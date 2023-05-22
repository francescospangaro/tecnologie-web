package it.polimi.webapp;

import java.time.LocalDateTime;

public record UserSession(int id, String name, LocalDateTime loginTime) {
}