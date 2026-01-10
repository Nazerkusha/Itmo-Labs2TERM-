package com.lab4.auth.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.lab4.auth.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class InfluxDBConfig {
    private static final Logger logger = Logger.getLogger(InfluxDBConfig.class.getName());

    private InfluxDBClient client;
    private String org;
    private String bucket;

    @PostConstruct
    public void init() {
        try {
            String url = System.getenv("INFLUXDB_URL");
            String token = System.getenv("INFLUXDB_TOKEN");
            org = System.getenv("INFLUXDB_ORG");
            bucket = System.getenv("INFLUXDB_BUCKET");

            if (org == null) org = "P3208";
            if (url == null) url = "http://influxdb:8086";
            if (token == null) token = "H1WwiUyu2DNsmAm-hpQCKoAuKz6v7N0BhFXnk6DWIOsNWtk0CI3HQDhDrL6RiySr6Er_KipmWyTBJPqDfF53Bg==";
            if (bucket == null) bucket = "Users";

            this.client = InfluxDBClientFactory.create(url, token.toCharArray(), org);
            logger.info("InfluxDB client initialized successfully on URL: " + url + " for Org: " + org);

        } catch (Exception e) {
            logger.severe("Failed to initialize InfluxDB client: " + e.getMessage());
            throw new RuntimeException("Error initializing database", e);
        }
    }

    public void registerUser(User user) {
        logger.info("Attempting to register user: " + user.getUsername());
        try {
            WriteApiBlocking writeApi = client.getWriteApiBlocking();

            Point point = Point.measurement("user_accounts")
                    .addTag("username", user.getUsername())
                    .addField("hashedPass", user.getPasswordHash())
                    .time(System.currentTimeMillis(), WritePrecision.MS);

            writeApi.writePoint(bucket, org, point);
            logger.info("User registered successfully: " + user.getUsername());
        } catch (Exception e) {
            logger.severe("Error registering user " + user.getUsername() + ": " + e.getMessage());
            throw new RuntimeException("Error adding user to database", e);
        }
    }

    public Optional<User> findUserByUsername(String username) {
        logger.info("Searching for user: " + username);
        try {
            QueryApi queryApi = client.getQueryApi();

            String fluxQuery = String.format(
                    "from(bucket: \"%s\")\n" +
                            "|> range(start: 0)\n" +
                            "|> filter(fn: (r) => r._measurement == \"user_accounts\")\n" +
                            "|> filter(fn: (r) => r.username == \"%s\")\n" +
                            "|> filter(fn: (r) => r._field == \"hashedPass\")\n" +
                            "|> group()\n" +
                            "|> last()\n" +
                            "|> pivot(rowKey: [\"_time\", \"username\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                    bucket, username);

            List<FluxTable> tables = queryApi.query(fluxQuery);

            if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
                return Optional.empty();
            }

            FluxRecord record = tables.get(0).getRecords().get(0);
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash((String) record.getValueByKey("hashedPass"));

            return Optional.of(user);
        } catch (Exception e) {
            logger.severe("Error searching for user " + username + ": " + e.getMessage());
            throw new RuntimeException("Error finding user in database", e);
        }
    }
}
