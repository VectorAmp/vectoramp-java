package com.vectoramp.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.vectoramp.VectorAmpException;
import com.vectoramp.http.Transport;
import com.vectoramp.models.AskRequest;
import com.vectoramp.models.AskResponse;
import com.vectoramp.models.IntelligenceSession;
import com.vectoramp.models.SessionCreateRequest;
import com.vectoramp.models.SessionMessage;
import com.vectoramp.models.SessionMessageCreateRequest;
import com.vectoramp.models.SseEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Client for VectorAmp intelligence question-answering APIs. */
public final class IntelligenceClient extends ApiService {
    /**
     * Creates an intelligence client backed by the supplied transport.
     *
     * @param transport HTTP transport to use for API requests
     */
    public IntelligenceClient(Transport transport) { super(transport); }

    /**
     * Runs a non-streaming intelligence query.
     *
     * @param query question or prompt text
     * @return answer and optional source/chunk metadata
     */
    public AskResponse ask(String query) { return ask(AskRequest.of(query)); }

    /**
     * Runs a non-streaming intelligence query. This method sets {@code stream=false} on the request.
     *
     * @param request query request; optional dataset, topK, includeSources, and conversation history are honored
     * @return answer and optional source/chunk metadata
     */
    public AskResponse ask(AskRequest request) {
        request.stream(false);
        return post("/intelligence/query", request, AskResponse.class);
    }

    /**
     * Alias for {@link #ask(String)} matching the cross-language {@code query} method name.
     * @param query question or prompt text
     * @return answer and optional source/chunk metadata
     */
    public AskResponse query(String query) { return ask(query); }

    /**
     * Alias for {@link #ask(AskRequest)} matching the cross-language {@code query} method name.
     * @param request query request
     * @return answer and optional source/chunk metadata
     */
    public AskResponse query(AskRequest request) { return ask(request); }

    /**
     * Alias for {@link #askStream(String)} matching the cross-language {@code stream} method name.
     * @param query question or prompt text
     * @return ordered event stream; close it if not fully consumed
     */
    public Stream<SseEvent> stream(String query) { return askStream(query); }

    /**
     * Alias for {@link #askStream(AskRequest)} matching the cross-language {@code stream} method name.
     * @param request query request
     * @return ordered event stream; close it if not fully consumed
     */
    public Stream<SseEvent> stream(AskRequest request) { return askStream(request); }

    /**
     * Creates an intelligence session.
     *
     * @param request session create request; title, workspace, dataset, and metadata are optional
     * @return created session
     */
    public IntelligenceSession createSession(SessionCreateRequest request) {
        return post("/intelligence/sessions", request == null ? new SessionCreateRequest() : request, IntelligenceSession.class);
    }

    /**
     * Creates an intelligence session with a title.
     *
     * @param title session title
     * @return created session
     */
    public IntelligenceSession createSession(String title) {
        return createSession(SessionCreateRequest.of(title));
    }

    /**
     * Lists intelligence sessions using the API default limit.
     *
     * @return sessions, most recent first
     */
    public List<IntelligenceSession> listSessions() { return listSessions(null); }

    /**
     * Lists intelligence sessions.
     *
     * @param limit optional maximum number of sessions; {@code null} uses the API default
     * @return sessions, most recent first
     */
    public List<IntelligenceSession> listSessions(Integer limit) {
        Map<String, String> query = new LinkedHashMap<>();
        if (limit != null) query.put("limit", String.valueOf(limit));
        JsonNode root = parseTree(transport.execute(new Transport.Request("GET", "/intelligence/sessions", query, Collections.emptyMap(), null)).getBody());
        return convertList(root, "sessions", new TypeReference<List<IntelligenceSession>>() {});
    }

    /**
     * Fetches an intelligence session by id.
     *
     * @param sessionId session id
     * @return session resource
     */
    public IntelligenceSession getSession(String sessionId) {
        return get("/intelligence/sessions/" + encodePath(sessionId), Collections.emptyMap(), IntelligenceSession.class);
    }

    /**
     * Deletes an intelligence session.
     *
     * @param sessionId session id
     */
    public void deleteSession(String sessionId) {
        delete("/intelligence/sessions/" + encodePath(sessionId));
    }

    /**
     * Appends a message to an intelligence session.
     *
     * @param sessionId session id
     * @param request message role, content, and optional metadata
     * @return the appended message
     */
    public SessionMessage appendMessage(String sessionId, SessionMessageCreateRequest request) {
        return post("/intelligence/sessions/" + encodePath(sessionId) + "/messages", request, SessionMessage.class);
    }

    /**
     * Appends a message to an intelligence session.
     *
     * @param sessionId session id
     * @param role message role: user, assistant, system, or tool
     * @param content message content
     * @return the appended message
     */
    public SessionMessage appendMessage(String sessionId, String role, String content) {
        return appendMessage(sessionId, SessionMessageCreateRequest.of(role, content));
    }

    /**
     * Lists messages in an intelligence session using the API default limit.
     *
     * @param sessionId session id
     * @return messages in chronological order
     */
    public List<SessionMessage> listMessages(String sessionId) { return listMessages(sessionId, null); }

    /**
     * Lists messages in an intelligence session.
     *
     * @param sessionId session id
     * @param limit optional maximum number of messages; {@code null} uses the API default
     * @return messages in chronological order
     */
    public List<SessionMessage> listMessages(String sessionId, Integer limit) {
        Map<String, String> query = new LinkedHashMap<>();
        if (limit != null) query.put("limit", String.valueOf(limit));
        JsonNode root = parseTree(transport.execute(new Transport.Request("GET", "/intelligence/sessions/" + encodePath(sessionId) + "/messages", query, Collections.emptyMap(), null)).getBody());
        return convertList(root, "messages", new TypeReference<List<SessionMessage>>() {});
    }

    private static <T> List<T> convertList(JsonNode root, String field, TypeReference<List<T>> type) {
        JsonNode array = root.path(field);
        if (array.isMissingNode() || array.isNull()) {
            array = root.isArray() ? root : MAPPER.createArrayNode();
        }
        List<T> result = MAPPER.convertValue(array, type);
        return result == null ? new ArrayList<>() : result;
    }

    private static String encodePath(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Runs an intelligence query as server-sent events.
     *
     * @param query question or prompt text
     * @return ordered event stream; close it if not fully consumed
     */
    public Stream<SseEvent> askStream(String query) { return askStream(AskRequest.of(query)); }

    /**
     * Runs an intelligence query as server-sent events. This method sets {@code stream=true} on the request.
     *
     * @param request query request; optional dataset, topK, includeSources, and conversation history are honored
     * @return ordered event stream; close it if not fully consumed
     */
    public Stream<SseEvent> askStream(AskRequest request) {
        request.stream(true);
        InputStream input = transport.stream(new Transport.Request("POST", "/intelligence/query", Collections.emptyMap(), Collections.singletonMap("Accept", "text/event-stream"), json(request)));
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        Iterator<SseEvent> iterator = new Iterator<>() {
            private SseEvent next;
            private boolean done;

            @Override public boolean hasNext() {
                if (next != null) return true;
                if (done) return false;
                next = readNext();
                return next != null;
            }

            @Override public SseEvent next() {
                if (!hasNext()) throw new NoSuchElementException();
                SseEvent event = next;
                next = null;
                if ("done".equals(event.getChunkType()) || "error".equals(event.getChunkType())) {
                    done = true;
                    closeQuietly(reader);
                }
                return event;
            }

            private SseEvent readNext() {
                try {
                    String line;
                    StringBuilder data = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty()) {
                            if (data.length() > 0) return parse(data.toString(), SseEvent.class);
                            continue;
                        }
                        if (line.startsWith("data:")) {
                            if (data.length() > 0) data.append('\n');
                            data.append(line.substring(5).trim());
                        }
                    }
                    done = true;
                    closeQuietly(reader);
                    if (data.length() > 0) return parse(data.toString(), SseEvent.class);
                    return null;
                } catch (IOException e) {
                    done = true;
                    closeQuietly(reader);
                    throw new VectorAmpException("Failed reading VectorAmp SSE stream", e);
                }
            }
        };
        Spliterator<SseEvent> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false).onClose(() -> closeQuietly(reader));
    }

    private static void closeQuietly(AutoCloseable closeable) {
        try { closeable.close(); } catch (Exception ignored) { }
    }
}
