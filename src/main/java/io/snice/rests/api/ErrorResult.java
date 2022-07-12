package io.snice.rests.api;

import java.util.Optional;

public record ErrorResult(int code, String message, Optional<String> description) {

    public ErrorResult(int code, String message) {
        this(code, message, Optional.empty());
    }
}
