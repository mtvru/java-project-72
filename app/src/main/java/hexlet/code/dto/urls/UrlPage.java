package hexlet.code.dto.urls;

import hexlet.code.dto.BasePage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UrlPage extends BasePage {
    private final Url url;
    private final List<UrlCheck> urlChecks;

    public UrlPage(Url url, List<UrlCheck> urlChecks) {
        this.url = url;
        this.urlChecks = urlChecks == null ? new ArrayList<>() : urlChecks;
    }
}
