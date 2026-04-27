package com.vectoramp.http;

import com.vectoramp.VectorAmpApiException;
import com.vectoramp.VectorAmpException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

/** HTTP/JSON transport backed by Java's built-in HttpClient. */
public final class RestTransport implements Transport {
    private final URI baseUri;
    private final String apiKey;
    private final HttpClient httpClient;
    private final String userAgent;

    public RestTransport(URI baseUri, String apiKey, Duration timeout) {
        this(baseUri, apiKey, HttpClient.newBuilder().connectTimeout(timeout).followRedirects(HttpClient.Redirect.NORMAL).build(), "vectoramp-java/0.1.0");
    }

    public RestTransport(URI baseUri, String apiKey, HttpClient httpClient, String userAgent) {
        this.baseUri = normalizeBaseUri(baseUri);
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.userAgent = userAgent;
    }

    @Override
    public Response execute(Request request) {
        try {
            HttpResponse<String> response = httpClient.send(buildRequest(request), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new VectorAmpApiException(response.statusCode(), response.body());
            }
            return new Response(response.statusCode(), response.body(), response.headers().map());
        } catch (IOException e) {
            throw new VectorAmpException("VectorAmp API request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VectorAmpException("VectorAmp API request interrupted", e);
        }
    }

    @Override
    public InputStream stream(Request request) {
        try {
            HttpResponse<InputStream> response = httpClient.send(buildRequest(request), HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new VectorAmpApiException(response.statusCode(), body);
            }
            return response.body();
        } catch (IOException e) {
            throw new VectorAmpException("VectorAmp streaming request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VectorAmpException("VectorAmp streaming request interrupted", e);
        }
    }

    private HttpRequest buildRequest(Request request) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(request.getPath(), request.getQuery()))
                .header("User-Agent", userAgent);
        if (!request.getHeaders().containsKey("Accept")) {
            builder.header("Accept", "application/json");
        }
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("X-API-Key", apiKey);
        }
        request.getHeaders().forEach(builder::header);
        if (request.getBody() == null) {
            builder.method(request.getMethod(), HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", request.getHeaders().getOrDefault("Content-Type", "application/json"));
            builder.method(request.getMethod(), HttpRequest.BodyPublishers.ofString(request.getBody(), StandardCharsets.UTF_8));
        }
        return builder.build();
    }

    private URI uri(String path, Map<String, String> query) {
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        StringBuilder value = new StringBuilder(baseUri.toString()).append(cleanPath);
        if (query != null && !query.isEmpty()) {
            StringJoiner joiner = new StringJoiner("&");
            query.forEach((k, v) -> {
                if (v != null) {
                    joiner.add(encode(k) + "=" + encode(v));
                }
            });
            String queryString = joiner.toString();
            if (!queryString.isEmpty()) value.append('?').append(queryString);
        }
        return URI.create(value.toString());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static URI normalizeBaseUri(URI uri) {
        String value = uri.toString();
        return URI.create(value.endsWith("/") ? value : value + "/");
    }
}
