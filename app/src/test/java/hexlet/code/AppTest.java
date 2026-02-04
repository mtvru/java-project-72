package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import javax.sql.DataSource;

public class AppTest {
    private Javalin app;
    private UrlRepository repo;
    private UrlCheckRepository checkRepo;
    private MockWebServer mockServer;

    @BeforeEach
    public final void beforeEach() throws IOException, SQLException {
        this.mockServer = new MockWebServer();
        this.mockServer.start();
        DataSource dataSource = DataSourceFactory.createDataSource();
        DatabaseInitializer.runMigrations(dataSource);
        this.app = JavalinFactory.createApp(dataSource);
        this.repo = new UrlRepository(dataSource);
        this.checkRepo = new UrlCheckRepository(dataSource);
        TestUtils.clearTables(dataSource);
    }

    @AfterEach
    public final void afterEach() {
        mockServer.close();
    }

    @Test
    public void testMainPage() {
        JavalinTest.test(this.app, (server, client) -> {
            Response response = client.get(NamedRoutes.homePath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Page analyzer");
        });
    }

    @Test
    public void testUrlsPage() {
        JavalinTest.test(this.app, (server, client) -> {
            Response response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testUrlPage() throws SQLException {
        Url url = new Url("http://localhost");
        this.repo.save(url);
        JavalinTest.test(this.app, (server, client) -> {
            Response response = client.get(NamedRoutes.urlPath(url.getId()));
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testCreateUrl() {
        JavalinTest.test(this.app, (server, client) -> {
            String requestBody = "url=http://localhost:7070/";
            try (Response response = client.post(NamedRoutes.urlsPath(), requestBody)) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body().string()).contains("localhost");
            }
        });
    }

    @Test
    void testUrlNotFound() {
        JavalinTest.test(this.app, (server, client) -> {
            Response response = client.get(NamedRoutes.urlPath(999999L));
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void testUrlCheck() throws Exception {
        //TODO
        String mockHtml = "<html><head><title>Test Title</title><meta name=\"description\" content=\"Test Description\"></head>"
                + "<body><h1>Test H1</h1></body></html>";
        mockServer.enqueue(new MockResponse.Builder()
                .body(mockHtml)
                .build());
        String urlName = mockServer.url("/").toString();
        Url url = new Url(urlName.substring(0, urlName.length() - 1));
        this.repo.save(url);
        JavalinTest.test(this.app, (server, client) -> {
            try (Response response = client.post(NamedRoutes.urlChecksPath(url.getId()))) {
                assertThat(response.code()).isEqualTo(200);
            }
            UrlCheck latestCheck = this.checkRepo.findByUrlId(url.getId()).getFirst();
            assertThat(latestCheck.getStatusCode()).isNull();
        });
    }
}
