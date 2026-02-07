package hexlet.code.model;

import lombok.Getter;
import lombok.ToString;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
@ToString
public final class Url {
    private Long id;
    private final String name;
    private final Timestamp createdAt;
    private final List<UrlCheck> urlChecks = new ArrayList<>();

    public Url(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        this.name = name;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Url(String name, Timestamp createdAt) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt is required");
        }
        this.name = name;
        this.createdAt = createdAt;
    }

    public void assignId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID is required");
        }
        if (this.id != null) {
            throw new IllegalStateException("ID already assigned");
        }
        this.id = id;
    }

    public void addUrlCheck(UrlCheck check) {
        if (check == null || !this.equalsById(check.getUrlId())) {
            return;
        }
        this.urlChecks.add(check);
    }

    public Optional<UrlCheck> getLatestUrlCheck() {
        return urlChecks.stream()
                .max(Comparator.comparing(UrlCheck::getCreatedAt));
    }

    public List<UrlCheck> getUrlChecks() {
        return Collections.unmodifiableList(urlChecks);
    }

    private boolean equalsById(Long urlId) {
        return this.id != null && this.id.equals(urlId);
    }
}
