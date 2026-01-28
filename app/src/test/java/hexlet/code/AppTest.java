package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import javax.sql.DataSource;

public class AppTest {
    private Javalin app;
    private UrlRepository repo;

    @BeforeEach
    public final void setUp() throws IOException, SQLException {
        DataSource dataSource = DataSourceFactory.createDataSource();
        DatabaseInitializer.runMigrations(dataSource);
        this.app = JavalinFactory.createApp(dataSource);
        this.repo = new UrlRepository(dataSource);
        TestUtils.clearTables(dataSource);
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
            Response response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("localhost");
        });
    }

    @Test
    void testUrlNotFound() throws Exception {
        JavalinTest.test(this.app, (server, client) -> {
            Response response = client.get(NamedRoutes.urlPath(999999L));
            assertThat(response.code()).isEqualTo(404);
        });
    }
}
