@import hexlet.code.dto.HomePage
@import hexlet.code.util.NamedRoutes
@param HomePage page

@template.layout.page(
page = page,
content = @`
    <div class="row">
        <div class="col-md-10 col-lg-8 mx-auto">
            <h1 class="display-3 mb-0">Page analyzer</h1>
            <p class="lead">Check websites for SEO for free</p>
            <form action="${NamedRoutes.urlsPath()}" method="post" class="rss-form">
                <div class="d-flex gap-2">
                    <div class="flex-grow-1 form-floating">
                        <input
                                id="url-input"
                                type="text"
                                name="name"
                                class="form-control"
                                placeholder="Link"
                                required
                                autofocus
                                autocomplete="off"
                        >
                        <label for="url-input">Link</label>
                    </div>
                    <button type="submit" class="btn btn-lg btn-primary px-5">Check</button>
                </div>
            </form>
            <p class="mt-2 mb-0 text-muted">Example: https://www.example.com</p>
        </div>
    </div>
`
)
