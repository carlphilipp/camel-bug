package org.apache.camel.bug.integration;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.restassured.RestAssured;
import org.apache.camel.bug.Application;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.apache.camel.bug.route.RestApiRoute.HTTP_HEADER_API_KEY;

@SpringBootTest(
    classes = Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@RunWith(SpringRunner.class)
public class IntegrationTest {

    private static final String API_KEY = "MY_CLIENT_API_KEY";
    private static MongodExecutable MONGOD_EXECUTABLE;

    @BeforeClass
    public static void setup() throws IOException {
        setupEmbedMongo();
    }

    @AfterClass
    public static void cleanup() {
        if (MONGOD_EXECUTABLE != null) {
            MONGOD_EXECUTABLE.stop();
        }
    }

    @LocalServerPort
    private int randomServerPort;

    @PostConstruct
    public void postConstruct() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = randomServerPort;
    }

    /**
     * The route works when mongodb returns the data from the DB
     */
    @Test
    public void testWithGoodApiKey() {
        // @formatter:off
        given().
            contentType(JSON).
            header(HTTP_HEADER_API_KEY, API_KEY).
        when().
            get("/endpoint").
        then().
            log().ifError().
            statusCode(HTTP_OK);
        // @formatter:on
    }

    /**
     * The route fails when mongodb does not return any data. This should pass
     */
    @Test
    public void testWithWrongApiKeyShouldPass() {
        // @formatter:off
        given().
            contentType(JSON).
            header(HTTP_HEADER_API_KEY,"wrong-api-key").
        when().
            get("/endpoint").
        then().
            log().ifError().
            statusCode(HTTP_UNAUTHORIZED);
        // @formatter:on
    }

    /**
     * A test to show that the fix I used work
     */
    @Test
    public void testWithWrongApiKeyFixed() {
        // @formatter:off
        given().
            contentType(JSON).
            header(HTTP_HEADER_API_KEY,"wrong-api-key").
        when().
            get("/endpoint-fixed").
        then().
            log().ifError().
            statusCode(HTTP_UNAUTHORIZED);
        // @formatter:on
    }

    private static void setupEmbedMongo() throws IOException {
        final String bindIp = "localhost";
        final int port = 12345;
        final String collection = "client-api-keys";

        final MongodStarter starter = MongodStarter.getDefaultInstance();
        final IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(bindIp, port, Network.localhostIsIPv6()))
            .build();
        MONGOD_EXECUTABLE = starter.prepare(mongodConfig);
        MONGOD_EXECUTABLE.start();
        MongoClient mongo = new MongoClient(bindIp, port);
        MongoDatabase db = mongo.getDatabase("database");
        db.createCollection(collection);
        final Document document = new Document();
        document.put("key", API_KEY);
        document.put("clientId", 61);
        db.getCollection(collection).insertOne(document);
    }
}
