package com.vectoramp.http;

import java.io.InputStream;
import java.util.Map;

/** Transport boundary so the SDK can add gRPC later without changing public services. */
public interface Transport extends AutoCloseable {
    Response execute(Request request);

    default InputStream stream(Request request) {
        throw new UnsupportedOperationException("Streaming is not supported by this transport");
    }

    @Override
    default void close() {}

    final class Request {
        private final String method;
        private final String path;
        private final Map<String, String> query;
        private final Map<String, String> headers;
        private final String body;

        public Request(String method, String path, Map<String, String> query, Map<String, String> headers, String body) {
            this.method = method;
            this.path = path;
            this.query = query;
            this.headers = headers;
            this.body = body;
        }

        public String getMethod() { return method; }
        public String getPath() { return path; }
        public Map<String, String> getQuery() { return query; }
        public Map<String, String> getHeaders() { return headers; }
        public String getBody() { return body; }
    }

    final class Response {
        private final int statusCode;
        private final String body;
        private final Map<String, java.util.List<String>> headers;

        public Response(int statusCode, String body, Map<String, java.util.List<String>> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }

        public int getStatusCode() { return statusCode; }
        public String getBody() { return body; }
        public Map<String, java.util.List<String>> getHeaders() { return headers; }
    }
}
