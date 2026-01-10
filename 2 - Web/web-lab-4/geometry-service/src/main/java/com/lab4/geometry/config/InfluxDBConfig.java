package com.lab4.geometry.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.lab4.geometry.model.PointResponse;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
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
            if (bucket == null) bucket = "Points";

            this.client = InfluxDBClientFactory.create(url, token.toCharArray(), org);
            logger.info("InfluxDB client initialized successfully on URL: " + url + " for Org: " + org);

        } catch (Exception e) {
            logger.severe("FATAL: Failed to initialize InfluxDB client: " + e.getMessage());
            throw new RuntimeException("Error initializing database", e);
        }
    }

    public void savePoint(PointResponse point) {
        logger.info("Saving point for user: " + point.getUsername());
        try {
            WriteApiBlocking writeApi = client.getWriteApiBlocking();

            Point influxPoint = Point.measurement("user_points")
                    .addTag("username", point.getUsername())
                    .addField("x", point.getX())
                    .addField("y", point.getY())
                    .addField("r", point.getR())
                    .addField("isHit", point.isHit())
                    .addField("scriptTime", point.getScriptTime())
                    .time(System.currentTimeMillis(), WritePrecision.MS);

            writeApi.writePoint(bucket, org, influxPoint);
            logger.info("Point saved successfully for user: " + point.getUsername());
        } catch (Exception e) {
            logger.severe("Error saving point: " + e.getMessage());
            throw new RuntimeException("Error saving point to database", e);
        }
    }

    public List<PointResponse> getPointsByUser(String username) {
        logger.info("Fetching points for user: " + username);
        try {
            QueryApi queryApi = client.getQueryApi();

            String fluxQuery = String.format(
                    "from(bucket: \"%s\")\n" +
                            "  |> range(start: -365d)\n" +
                            "  |> filter(fn: (r) => r[\"_measurement\"] == \"user_points\" and r[\"username\"] == \"%s\")\n" +
                            "  |> filter(fn: (r) => \n" +
                            "      r[\"_field\"] == \"x\" or r[\"_field\"] == \"y\" or r[\"_field\"] == \"r\" or r[\"_field\"] == \"isHit\" or r[\"_field\"] == \"scriptTime\")\n" +
                            "  |> pivot(rowKey: [\"_time\", \"username\"], columnKey: [\"_field\"], valueColumn: \"_value\")\n" +
                            "  |> sort(columns: [\"_time\"], desc: true)",
                    bucket, username);

            List<PointResponse> points = new ArrayList<>();
            List<FluxTable> tables = queryApi.query(fluxQuery);

            if (!tables.isEmpty()) {
                for (FluxTable table : tables) {
                    for (FluxRecord record : table.getRecords()) {
                        PointResponse point = new PointResponse();
                        point.setX((Double) record.getValueByKey("x"));
                        point.setY((Double) record.getValueByKey("y"));
                        point.setR((Double) record.getValueByKey("r"));
                        point.setHit((Boolean) record.getValueByKey("isHit"));
                        point.setScriptTime(((Number) record.getValueByKey("scriptTime")).longValue());
                        point.setUsername((String) record.getValueByKey("username"));
                        point.setServerTime(record.getValueByKey("_time").toString());
                        points.add(point);
                    }
                }
            }

            logger.info("Fetched " + points.size() + " points for user: " + username);
            return points;

        } catch (Exception e) {
            logger.severe("Error fetching points: " + e.getMessage());
            throw new RuntimeException("Error fetching points from database", e);
        }
    }
}
