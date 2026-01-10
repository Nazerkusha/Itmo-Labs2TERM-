package com.lab4.geometry.resource;

import com.lab4.geometry.config.InfluxDBConfig;
import com.lab4.geometry.model.PointRequest;
import com.lab4.geometry.model.PointResponse;
import com.lab4.geometry.util.HitChecker;
import com.lab4.geometry.util.JWTUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/geometry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GeometryResource {
    private static final Logger logger = Logger.getLogger(GeometryResource.class.getName());

    @Inject
    private InfluxDBConfig influxDB;

    @Inject
    private JWTUtil jwtUtil;

    private String getUsernameFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new WebApplicationException("Missing or invalid Authorization header", Response.Status.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.validateToken(token);

        if (username == null) {
            throw new WebApplicationException("Invalid or expired token", Response.Status.UNAUTHORIZED);
        }

        return username;
    }

    @POST
    @Path("/check")
    public Response checkPoint(PointRequest request, @HeaderParam("Authorization") String authHeader) {
        try {
            String username = getUsernameFromToken(authHeader);
            logger.info(String.format("Checking point for user %s: x=%.2f, y=%.2f, r=%.2f", 
                    username, request.getX(), request.getY(), request.getR()));

            if (request.getR() <= 0 || request.getR() > 5) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "R must be between 0 and 5");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }

            if (Math.abs(request.getX()) > 5 || Math.abs(request.getY()) > 5) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "X and Y must be between -5 and 5");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }

            long startTime = System.nanoTime();
            boolean hit = HitChecker.checkHit(request.getX(), request.getY(), request.getR());
            long scriptTime = System.nanoTime() - startTime;

            PointResponse response = new PointResponse(
                    request.getX(),
                    request.getY(),
                    request.getR(),
                    hit,
                    scriptTime,
                    Instant.now().toString(),
                    username
            );

            influxDB.savePoint(response);

            logger.info(String.format("Point checked: hit=%b for user %s", hit, username));
            return Response.ok(response).build();

        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error checking point: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to check point: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @GET
    @Path("/points")
    public Response getPoints(@HeaderParam("Authorization") String authHeader) {
        try {
            String username = getUsernameFromToken(authHeader);
            logger.info("Fetching points for user: " + username);

            List<PointResponse> points = influxDB.getPointsByUser(username);

            return Response.ok(points).build();

        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Error fetching points: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch points: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
