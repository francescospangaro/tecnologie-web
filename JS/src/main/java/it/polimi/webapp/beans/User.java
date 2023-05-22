package it.polimi.webapp.beans;

import java.time.LocalDateTime;

public record User(int id, String user, LocalDateTime loginTime) {
}