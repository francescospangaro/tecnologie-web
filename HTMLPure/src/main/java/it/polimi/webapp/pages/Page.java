package it.polimi.webapp.pages;

import java.util.function.Supplier;

public record Page<T>(String path, Supplier<T> fallbackArgsFactory) {
}