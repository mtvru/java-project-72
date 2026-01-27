@import hexlet.code.dto.urls.UrlPage
@import java.time.format.DateTimeFormatter
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
                    <td>${page.getUrl().getCreatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))}</td>
                </tr>
            </tbody>
        </table>
    </div>
`
)
