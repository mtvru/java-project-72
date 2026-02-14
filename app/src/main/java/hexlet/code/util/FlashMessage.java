package hexlet.code.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FlashMessage {
    PAGE_ADDED("Page has been added successfully.", "success"),
    PAGE_ALREADY_EXISTS("Page already exists.", "info"),
    PAGE_CHECKED("Page has been checked successfully.", "success"),
    INCORRECT_URL("Incorrect URL", "danger"),
    CONNECTION_ERROR("Connection error", "danger");

    private final String message;
    private final String type;
}
