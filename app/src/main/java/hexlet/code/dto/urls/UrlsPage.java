package hexlet.code.dto.urls;

import hexlet.code.dto.BasePage;
import hexlet.code.model.Url;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class UrlsPage extends BasePage {
    private final List<Url> urls;

    public UrlsPage(List<Url> urls) {
        this.urls = urls != null ? urls : Collections.emptyList();
    }
}
