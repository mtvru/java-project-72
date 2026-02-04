package hexlet.code.dto.urls;

import hexlet.code.dto.BasePage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class UrlsPage extends BasePage {
    private final List<Url> urls;
    private final Map<Long, UrlCheck> latestUrlChecks;

    public UrlsPage(List<Url> urls, Map<Long, UrlCheck> latestUrlChecks) {
        this.urls = urls != null ? urls : Collections.emptyList();
        this.latestUrlChecks = latestUrlChecks != null ? latestUrlChecks : Collections.emptyMap();
    }
}
