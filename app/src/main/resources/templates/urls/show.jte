@import hexlet.code.dto.urls.UrlPage
@import hexlet.code.model.UrlCheck
@import hexlet.code.util.NamedRoutes
@import java.time.ZoneId
@import java.time.format.DateTimeFormatter
@import java.time.format.FormatStyle
@param UrlPage page

@template.layout.page(
page = page,
content = @`
    <h1>Site: ${page.getUrl().getName()}</h1>

    <div class="table-responsive">
        <table class="table table-bordered table-hover">
            <tbody>
            <tr>
                <td>ID</td>
                <td>${page.getUrl().getId()}</td>
            </tr>
            <tr>
                <td>Name</td>
                <td>${page.getUrl().getName()}</td>
            </tr>
            <tr>
                <td>Date of creation</td>
                <td>${page.getUrl().getCreatedAt()
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG))}</td>
            </tr>
            </tbody>
        </table>
    </div>

    <h2>Checks</h2>
    <form method="post" action="${NamedRoutes.urlChecksPath(page.getUrl().getId())}">
        <button type="submit" class="btn btn-primary">Run check</button>
    </form>

    <div class="table-responsive mt-3">
        <table class="table table-bordered table-hover">
            <thead>
            <tr>
                <th class="col-1">ID</th>
                <th class="col-1">Status code</th>
                <th>Title</th>
                <th>H1</th>
                <th>Description</th>
                <th class="col-2">Date of check</th>
            </tr>
            </thead>
            <tbody>
                @for(UrlCheck check : page.getUrl().getUrlChecks())
                    <tr>
                        <td>${check.getId()}</td>
                        <td>${check.getStatusCode()}</td>
                        <td>${check.getTitle()}</td>
                        <td>${check.getH1()}</td>
                        <td>${check.getDescription()}</td>
                        <td>${
                            check.getCreatedAt()
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG))
                        }</td>
                    </tr>
                @endfor
            </tbody>
        </table>
    </div>
`
)
