package hexlet.code;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.sql.SQLException;
import hexlet.code.util.NamedRoutes;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import javax.sql.DataSource;

public class AppTest {
    private Javalin app;
    private DataSource dataSource;

    @BeforeEach
    public final void beforeEach() throws IOException, SQLException {
        this.dataSource = DataSourceFactory.createDataSource();
        DatabaseInitializer.runMigrations(this.dataSource);
        this.app = JavalinFactory.createApp(this.dataSource);
        TestUtils.clearTables(this.dataSource);
    }

    @AfterEach
    public final void afterEach() throws SQLException {
        this.app.stop();
        this.dataSource.getConnection().close();
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
        Long urlId = TestUtils.insertLocalhostToUrl(this.dataSource);
        JavalinTest.test(this.app, (server, client) -> {
            Response response = client.get(NamedRoutes.urlPath(urlId));
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
    void testUrlCheck() throws IOException {
        String mockHtml = "<html><head><title>Test Title</title>"
                + "<meta name=\"description\" content=\"Test Description\"></head>"
                + "<body><h1>Test H1</h1></body></html>";
        MockWebServer mockServer = new MockWebServer();
        mockServer.enqueue(new MockResponse.Builder()
                .body(mockHtml)
                .build());
        mockServer.start();
        String urlName = mockServer.url("").toString();
        final Long urlId = 1L;
        JavalinTest.test(app, (server, client) -> {
            String requestBody = "url=" + urlName;
            try (Response response = client.post(NamedRoutes.urlsPath(), requestBody)) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body().string()).contains(urlName.substring(0, urlName.length() - 1));
            }
            try (Response response = client.post(NamedRoutes.urlChecksPath(urlId))) {
                assertThat(response.code()).isEqualTo(200);
            }
            try (Response response = client.get(NamedRoutes.urlPath(urlId))) {
                String body = response.body().string();
                Document doc = Jsoup.parse(body);
                Element table = doc.select("table").get(1);
                Elements rows = table.select("tbody tr");
                Element firstRow = rows.getFirst();
                assertThat(firstRow).isNotNull();
                Elements tds = firstRow.select("td");
                String statusCode = tds.get(1).text();
                String title = tds.get(2).text();
                String h1 = tds.get(3).text();
                String description = tds.get(4).text();
                assertThat(response.code()).isEqualTo(200);
                assertThat(statusCode).isEqualTo("200");
                assertThat(title).isEqualTo("Test Title");
                assertThat(h1).isEqualTo("Test H1");
                assertThat(description).isEqualTo("Test Description");
            }
        });
        mockServer.close();
    }

    @Test
    void testLatestUrlCheck() throws Exception {
        String firstHtml = """
        <html>
            <head>
                <title>First Title</title>
                <meta name="description" content="First Description">
            </head>
            <body>
                <h1>First H1</h1>
            </body>
        </html>
        """;
        String secondHtml = """
        <html>
            <head>
                <title>Second Title</title>
                <meta name="description" content="Second Description">
            </head>
            <body>
                <h1>Second H1</h1>
            </body>
        </html>
        """;
        MockWebServer mockServer = new MockWebServer();
        mockServer.enqueue(new MockResponse.Builder()
                .body(firstHtml)
                .build());
        mockServer.enqueue(new MockResponse.Builder()
                .body(secondHtml)
                .build());
        mockServer.start();
        String urlName = mockServer.url("").toString();
        final Long urlId = 1L;
        JavalinTest.test(app, (server, client) -> {
            String requestBody = "url=" + urlName;
            try (Response response = client.post(NamedRoutes.urlsPath(), requestBody)) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body().string()).contains(urlName.substring(0, urlName.length() - 1));
            }
            try (Response response = client.post(NamedRoutes.urlChecksPath(urlId))) {
                assertThat(response.code()).isEqualTo(200);
            }
            sleep(1000);
            try (Response response = client.post(NamedRoutes.urlChecksPath(urlId))) {
                assertThat(response.code()).isEqualTo(200);
            }
            String latestDate;
            try (Response response = client.get(NamedRoutes.urlPath(urlId))) {
                String body = response.body().string();
                Document doc = Jsoup.parse(body);
                Element table = doc.select("table").get(1);
                Elements rows = table.select("tbody tr");
                Element firstRow = rows.getFirst();
                assertThat(firstRow).isNotNull();
                Elements tds = firstRow.select("td");
                latestDate = tds.get(5).text();
            }
            try (Response response = client.get(NamedRoutes.urlsPath())) {
                assertThat(response.body().string()).contains(latestDate);
            }
        });
        mockServer.close();
    }
}
