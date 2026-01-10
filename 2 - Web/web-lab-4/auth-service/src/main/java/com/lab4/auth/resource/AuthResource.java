package com.lab4.auth.resource;

import com.lab4.auth.config.InfluxDBConfig;
import com.lab4.auth.model.AuthRequest;
import com.lab4.auth.model.AuthResponse;
import com.lab4.auth.model.User;
import com.lab4.auth.util.JWTUtil;
import com.lab4.auth.util.PasswordUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;
import java.util.logging.Logger;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private static final Logger logger = Logger.getLogger(AuthResource.class.getName());

    @Inject
    private InfluxDBConfig influxDB;

    @Inject
    private JWTUtil jwtUtil;

    @POST
    @Path("/register")
    public Response register(AuthRequest request) {
        logger.info("Registration attempt for user: " + request.getUsername());

        try {
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new AuthResponse(null, "Username cannot be empty"))
                        .build();
            }

            if (request.getPassword() == null || request.getPassword().length() < 4) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new AuthResponse(null, "Password must be at least 4 characters"))
                        .build();
            }

            Optional<User> existingUser = influxDB.findUserByUsername(request.getUsername());
            if (existingUser.isPresent()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new AuthResponse(null, "User already exists"))
                        .build();
            }

            String hashedPassword = PasswordUtil.hashPassword(request.getPassword());
            User user = new User(request.getUsername(), hashedPassword);
            influxDB.registerUser(user);

            String token = jwtUtil.generateToken(request.getUsername());

            logger.info("User registered successfully: " + request.getUsername());
            return Response.status(Response.Status.CREATED)
                    .entity(new AuthResponse(token, "Registration successful"))
                    .build();

        } catch (Exception e) {
            logger.severe("Registration error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new AuthResponse(null, "Registration failed: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(AuthRequest request) {
        logger.info("Login attempt for user: " + request.getUsername());

        try {
            Optional<User> userOptional = influxDB.findUserByUsername(request.getUsername());

            if (userOptional.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new AuthResponse(null, "Invalid credentials"))
                        .build();
            }

            User user = userOptional.get();
            if (!PasswordUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new AuthResponse(null, "Invalid credentials"))
                        .build();
            }

            String token = jwtUtil.generateToken(request.getUsername());

            logger.info("User logged in successfully: " + request.getUsername());
            return Response.ok(new AuthResponse(token, "Login successful")).build();

        } catch (Exception e) {
            logger.severe("Login error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new AuthResponse(null, "Login failed: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/validate")
    public Response validateToken(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new AuthResponse(null, "Missing or invalid token"))
                        .build();
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.validateToken(token);

            if (username == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new AuthResponse(null, "Invalid or expired token"))
                        .build();
            }

            return Response.ok(new AuthResponse(null, username)).build();

        } catch (Exception e) {
            logger.severe("Token validation error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new AuthResponse(null, "Validation failed"))
                    .build();
        }
    }
}
