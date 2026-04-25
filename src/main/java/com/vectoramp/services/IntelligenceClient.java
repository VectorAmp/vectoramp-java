package com.vectoramp.services;

import com.vectoramp.VectorAmpException;
import com.vectoramp.http.Transport;
import com.vectoramp.models.AskRequest;
import com.vectoramp.models.AskResponse;
import com.vectoramp.models.SseEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class IntelligenceClient extends ApiService {
    public IntelligenceClient(Transport transport) { super(transport); }

    public AskResponse ask(String query) { return ask(AskRequest.of(query)); }

    public AskResponse ask(AskRequest request) {
        request.stream(false);
        return post("/intelligence/query", request, AskResponse.class);
    }

    public Stream<SseEvent> askStream(String query) { return askStream(AskRequest.of(query)); }

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
