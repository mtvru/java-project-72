@import hexlet.code.dto.BasePage
@import hexlet.code.util.NamedRoutes
@param BasePage page = null

<a href="${NamedRoutes.homePath()}" class="d-flex align-items-center mb-3 mb-md-0 me-md-auto link-body-emphasis text-decoration-none"><span class="fs-4">Page analyzer</span></a>
<ul class="nav nav-pills">
    <li class="nav-item"><a href="${NamedRoutes.homePath()}" class="nav-link ${page != null && NamedRoutes.homePath().equals(page.getPath()) ? "active" : ""}" aria-current="page">Home</a></li>
    <li class="nav-item"><a href="${NamedRoutes.urlsPath()}" class="nav-link ${page != null && NamedRoutes.urlsPath().equals(page.getPath()) ? "active" : ""}">Sites</a></li>
</ul>
