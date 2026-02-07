package hexlet.code.model;

import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@ToString
public final class UrlCheck {
    private Long id;
    private final Integer statusCode;
    private final String title;
    private final String h1;
    private final String description;
    private final Long urlId;
    private final Timestamp createdAt;

    public UrlCheck(Long urlId, Integer statusCode, String title, String h1, String description) {
        if (urlId == null) {
            throw new IllegalArgumentException("urlId is required");
        }
        this.urlId = urlId;
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public UrlCheck(
            Long urlId,
            Integer statusCode,
            String title,
            String h1,
            String description,
            Timestamp createdAt
    ) {
        if (urlId == null) {
            throw new IllegalArgumentException("UrlId is required");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt is required");
        }
        this.urlId = urlId;
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
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
}
