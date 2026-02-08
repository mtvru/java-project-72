package hexlet.code;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.net.CookieManager;
import java.sql.SQLException;
import java.util.stream.Stream;
import hexlet.code.util.FlashMessage;
import hexlet.code.util.NamedRoutes;
import io.javalin.testtools.TestConfig;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;

public final class AppTest {
    private static final int SC_OK = 200;
    private static final int SC_NOT_FOUND = 404;
    private static final int SC_ERROR = 500;
    private static final long NON_EXISTENT_ID = 999999L;
    private static final long SLEEP_TIME = 1000;
    private Javalin app;
    private DataSource dataSource;

    @BeforeEach
    public void beforeEach() throws IOException, SQLException {
        this.dataSource = DataSourceFactory.createDataSource();
        DatabaseInitializer.runMigrations(this.dataSource);
        this.app = JavalinFactory.createApp(this.dataSource);
        TestUtils.clearTables(this.dataSource);
    }

    @AfterEach
    public void afterEach() throws SQLException {
        this.app.stop();
        this.dataSource.getConnection().close();
    }

    @Test
    public void testMainPage() {
        JavalinTest.test(this.app, (server, client) -> {
            try (Response response = client.get(NamedRoutes.homePath())) {
                assertThat(response.code()).isEqualTo(SC_OK);
                assertThat(response.body().string()).contains("Page analyzer");
            }
        });
    }

    @Test
    public void testUrlsPage() {
        JavalinTest.test(this.app, (server, client) -> {
            try (Response response = client.get(NamedRoutes.urlsPath())) {
                assertThat(response.code()).isEqualTo(SC_OK);
            }
        });
    }

    @Test
    public void testUrlPage() throws SQLException {
        Long urlId = TestUtils.insertLocalhostToUrl(this.dataSource);
        JavalinTest.test(this.app, (server, client) -> {
            try (Response response = client.get(NamedRoutes.urlPath(urlId))) {
                assertThat(response.code()).isEqualTo(SC_OK);
            }
        });
    }

    @Test
    public void testCreateUrl() {
        JavalinTest.test(this.app, (server, client) -> {
            String requestBody = "url=http://localhost:7070/";
            try (Response response = client.post(NamedRoutes.urlsPath(), requestBody)) {
                assertThat(response.code()).isEqualTo(SC_OK);
                assertThat(response.body().string()).contains("localhost");
            }
        });
    }

    @Test
    public void testUrlNotFound() {
        JavalinTest.test(this.app, (server, client) -> {
            try (Response response = client.get(NamedRoutes.urlPath(NON_EXISTENT_ID))) {
                assertThat(response.code()).isEqualTo(SC_NOT_FOUND);
            }
        });
    }

    @Test
    public void testCreateDuplicateUrl() {
        OkHttpClient httpClientWithCookies = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                .followRedirects(true)
                .build();
        TestConfig config = new TestConfig(
                true,
                true,
                httpClientWithCookies
        );
        JavalinTest.test(this.app, config, (server, client) -> {
            String requestBody = "url=https://localhost";
            try (Response response = client.post(NamedRoutes.urlsPath(), requestBody)) {
                assertThat(response.code()).isEqualTo(SC_OK);
                assertThat(response.body().string()).contains(FlashMessage.PAGE_ADDED.getMessage());
            }
            try (Response response = client.post(NamedRoutes.urlsPath(), requestBody + "/")) {
                assertThat(response.code()).isEqualTo(SC_OK);
                assertThat(response.body().string()).contains(FlashMessage.PAGE_ALREADY_EXISTS.getMessage());
            }
            try (Response response = client.get(NamedRoutes.urlsPath())) {
                String body = response.body().string();
                Document doc = Jsoup.parse(body);
                Elements links = doc.select("a:contains(https://localhost)");
                assertThat(links).hasSize(1);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("urlCheckProvider")
    public void testUrlCheck(
        MockWebServer mockServer,
        int expectedStatus,
        String expectedTitle,
        String expectedH1,
        String expectedDescription
    ) throws IOException {
        mockServer.start();
        String urlName = mockServer.url("").toString();
        final Long urlId = 1L;
        OkHttpClient httpClientWithCookies = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                .followRedirects(true)
                .build();
        TestConfig config = new TestConfig(
                true,
                true,
                httpClientWithCookies
        );
        JavalinTest.test(app, config, (server, client) -> {
            String requestBody = "url=" + urlName;
            try (Response response = client.post(NamedRoutes.urlsPath(), requestBody)) {
                assertThat(response.code()).isEqualTo(SC_OK);
                assertThat(response.body().string()).contains(urlName.substring(0, urlName.length() - 1));
            }
            try (Response response = client.post(NamedRoutes.urlChecksPath(urlId))) {
                assertThat(response.code()).isEqualTo(SC_OK);
                assertThat(response.body().string()).contains(FlashMessage.PAGE_CHECKED.getMessage());
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
                final int h1Pos = 3;
                String h1 = tds.get(h1Pos).text();
                final int descriptionPos = 4;
                String description = tds.get(descriptionPos).text();
                assertThat(response.code()).isEqualTo(SC_OK);
                assertThat(statusCode).isEqualTo(String.valueOf(expectedStatus));
                assertThat(title).isEqualTo(expectedTitle);
                assertThat(h1).isEqualTo(expectedH1);
                assertThat(description).isEqualTo(expectedDescription);
            }
        });
        mockServer.close();
    }

    public static Stream<Arguments> urlCheckProvider() {
        String mockHtml = "<html><head><title>Test Title</title>"
                + "<meta name=\"description\" content=\"Test Description\"></head>"
                + "<body><h1>Test H1</h1></body></html>";
        MockWebServer okServer = new MockWebServer();
        okServer.enqueue(new MockResponse.Builder()
                .body(mockHtml)
                .build());
        MockWebServer errorServer = new MockWebServer();
        errorServer.enqueue(new MockResponse.Builder()
                .code(SC_ERROR)
                .body("Internal Server Error")
                .build());
        return Stream.of(
                Arguments.of(okServer, SC_OK, "Test Title", "Test H1", "Test Description"),
                Arguments.of(errorServer, SC_ERROR, "", "", "")
        );
    }

    @Test
    public void testUrlCheckUnableMockServer() throws IOException {
        MockWebServer mockServer = new MockWebServer();
        mockServer.start();
        mockServer.close();
        String urlName = mockServer.url("").toString();
        final Long urlId = 1L;
        OkHttpClient httpClientWithCookies = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                .followRedirects(true)
                .build();
        TestConfig config = new TestConfig(
                false,
                true,
                httpClientWithCookies
        );
        JavalinTest.test(app, config, (server, client) -> {
            String requestBody = "url=" + urlName;
            try (Response response = client.post(NamedRoutes.urlsPath(), requestBody)) {
                assertThat(response.code()).isEqualTo(SC_OK);
                assertThat(response.body().string()).contains(urlName.substring(0, urlName.length() - 1));
            }
            try (Response response = client.post(NamedRoutes.urlChecksPath(urlId))) {
                assertThat(response.body().string()).contains(FlashMessage.INCORRECT_URL.getMessage());
            }
        });
    }

    @Test
    void testLatestUrlCheck() throws IOException {
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
        try (MockWebServer mockServer = new MockWebServer()) {
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
                    assertThat(response.code()).isEqualTo(SC_OK);
                    assertThat(response.body().string()).contains(urlName.substring(0, urlName.length() - 1));
                }
                try (Response response = client.post(NamedRoutes.urlChecksPath(urlId))) {
                    assertThat(response.code()).isEqualTo(SC_OK);
                }
                sleep(SLEEP_TIME);
                try (Response response = client.post(NamedRoutes.urlChecksPath(urlId))) {
                    assertThat(response.code()).isEqualTo(SC_OK);
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
                    final int latestDatePos = 5;
                    latestDate = tds.get(latestDatePos).text();
                }
                try (Response response = client.get(NamedRoutes.urlsPath())) {
                    assertThat(response.body().string()).contains(latestDate);
                }
            });
        }
    }
}
