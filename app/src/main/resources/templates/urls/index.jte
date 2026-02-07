@import hexlet.code.model.Url
@import hexlet.code.util.NamedRoutes
@import hexlet.code.dto.urls.UrlsPage
@import java.time.ZoneId
@import java.time.format.DateTimeFormatter
@import java.time.format.FormatStyle
@param UrlsPage page

@template.layout.page(
page = page,
content = @`
    <h1>Sites</h1>
    <div class="table-responsive">
        <table class="table table-bordered table-hover">
            <thead>
            <tr>
                <th class="col-1">ID</th>
                <th>Name</th>
                <th class="col-2">Last check</th>
                <th class="col-1">Response code</th>
            </tr>
            </thead>
            <tbody>
            @for(Url url : page.getUrls())
                <tr>
                    <td>
                        ${url.getId()}
                    </td>
                    <td>
                        <a href="${NamedRoutes.urlPath(url.getId())}">${url.getName()}</a>
                    </td>
                    <td>
                        ${url.getLatestUrlCheck().map(c -> c.getCreatedAt()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG))).orElse(null)}
                    </td>
                    <td>
                        ${url.getLatestUrlCheck().map(c -> c.getStatusCode()).orElse(null)}
                    </td>
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
`
)
