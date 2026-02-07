package hexlet.code.dto.urls;

import hexlet.code.dto.BasePage;
import hexlet.code.model.Url;
import lombok.Getter;

@Getter
public class UrlPage extends BasePage {
    private final Url url;

    public UrlPage(Url url) {
        this.url = url;
    }
}
