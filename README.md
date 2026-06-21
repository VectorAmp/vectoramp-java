# VectorAmp Java SDK

Java 11+ client for the public [VectorAmp](https://vectoramp.com) API.

Licensed under the [Apache License 2.0](./LICENSE).

## Install

Gradle:

```gradle
repositories { mavenCentral() }

dependencies {
  implementation 'com.vectoramp:vectoramp-java:0.1.0'
}
```

Maven:

```xml
<dependency>
  <groupId>com.vectoramp</groupId>
  <artifactId>vectoramp-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Quick start

The API key is the only required input. `VectorAmpClient.create(...)` reads it from your code or
from the `VECTORAMP_API_KEY` environment variable, and defaults to the production base URL.

```java
import com.vectoramp.VectorAmpClient;
import com.vectoramp.models.*;

try (VectorAmpClient client = VectorAmpClient.create(System.getenv("VECTORAMP_API_KEY"))) {
    // One-call create: name only. Defaults to the VectorAmp-Embedding-4B model (dim 2560),
    // cosine metric, and the SABLE index.
    Dataset dataset = client.datasets().create("product-docs");

    // Object -> method style: call instance methods on the dataset directly.
    dataset.addTexts(java.util.List.of(
        "VectorAmp ships a high-performance SABLE vector index.",
        "The API supports RAG over ingested datasets."
    ));

    AskResponse answer = dataset.ask("What index does VectorAmp use?");
    System.out.println(answer.getAnswer());
}
```

## Configuration

The default base URL is `https://api.vectoramp.com`. Override it for development or tests:

```java
VectorAmpClient client = VectorAmpClient.builder(System.getenv("VECTORAMP_API_KEY"))
    .baseUrl("https://api.vectoramp.com")
    .timeout(java.time.Duration.ofSeconds(60))
    .build();
```

Authentication uses the public API-key header `X-API-Key: <api_key>`.

## Datasets

`create`, `get`, and `list` return `Dataset` resource objects. Each keeps the raw API payload
(`getRawData()`), the dataset id, and bound SDK services, so you can call instance methods without
passing the id around. The id-based service methods (`client.datasets().search(id, ...)`) remain
available for explicit usage.

```java
Page<Dataset> page = client.datasets().list(50, 0);
Dataset dataset = client.datasets().get("dataset-uuid");

SearchResponse results = dataset.search("release notes");
dataset.insert(java.util.List.of(VectorRecord.of("doc-1", java.util.List.of(0.1, 0.2), null)));
dataset.addText("hello world");
AskResponse answer = dataset.ask("Summarize this dataset");
dataset.delete();
```

### Creating datasets

```java
// Minimal: name only (defaults applied).
Dataset docs = client.datasets().create("docs");

// Hybrid dense + sparse dataset.
Dataset hybrid = client.datasets().create("hybrid-docs", true);

// Full control, including an OpenAI embedding (dim inferred from the model).
Dataset custom = client.datasets().create(
    CreateDatasetRequest.builder("openai-docs")
        .embedding(EmbeddingConfig.openai("small")) // text-embedding-3-small, dim 1536
        .metric("cosine")
        .hybrid(true)
        .build()
);

// Custom/unknown embedding model: provide dim explicitly.
Dataset bespoke = client.datasets().create(
    CreateDatasetRequest.builder("bespoke")
        .embedding(EmbeddingConfig.of("acme", "acme-embed-v2"))
        .dim(768)
        .build()
);
```

The SDK always sends `index_type: "sable"` and never exposes other index types.

### Search

```java
// Bare text query; top_k defaults to 10.
SearchResponse results = dataset.search("machine learning best practices");

// Add options only when you need them.
SearchResponse filtered = dataset.search(
    SearchRequest.text("machine learning best practices")
        .filters(java.util.Map.of("category", "research"))
        .includeDocuments(true)
        .rerank(true) // expands to the full rerank object
);

// Hybrid dense/sparse search.
SearchResponse hybridResults = dataset.search(
    SearchRequest.text("zero downtime upgrade").hybrid("zero downtime", 0.5)
);

// Vector search.
SearchResponse vectorResults = dataset.search(
    SearchRequest.vector(java.util.List.of(0.1, 0.2, 0.3))
);
```

### Insert vectors

Vector ids accept a `String` or a number. Numeric ids are sent as JSON numbers, so the API
preserves them exactly.

```java
dataset.insert(java.util.List.of(
    VectorRecord.of("doc-001", java.util.List.of(0.1, 0.2, 0.3), java.util.Map.of("title", "Intro")),
    VectorRecord.of(42L, java.util.List.of(0.4, 0.5, 0.6), null) // serialized as "id": 42
));
```

### Add text

`addTexts` embeds text through `/datasets/{id}/embed`, then inserts the generated vectors through
`/datasets/{id}/insert` with the source text copied into `metadata.text`. Ids are auto-generated
when omitted.

```java
dataset.addText("hello world");
dataset.addTexts(java.util.List.of("hello", "world"));

// With explicit numeric ids and per-text metadata.
dataset.addTexts(AddTextsRequest.ofNumericIds(
    java.util.List.of("first", "second"),
    java.util.List.of(1L, 2L),
    java.util.List.of(java.util.Map.of("page", 1), java.util.Map.of("page", 2))
));
```

### Source documents

Document listing is cursor-based. Pass `getNextCursor()` into the next request. Downloads return
the retained original bytes; the default transport follows redirects to storage.

```java
DatasetDocumentPage docs = dataset.listDocuments(50, null, "ready");
for (DatasetDocument doc : docs.getDocuments()) {
    if (Boolean.TRUE.equals(doc.getDownloadAvailable())) {
        byte[] original = dataset.downloadDocument(doc.getId());
    }
}
if (docs.getNextCursor() != null) {
    DatasetDocumentPage next = dataset.listDocuments(50, docs.getNextCursor(), "ready");
}
```

## Ingestion

Typed source helpers cover `web`, `s3`, `gcs`, `gdrive`, `jira`, `confluence`, and `file_upload`.
Use `GenericSource` as an escape hatch for new source types.

```java
Source web = client.ingestion().createWeb("https://docs.vectoramp.com");
Source s3 = client.ingestion().createS3("my-docs-bucket");
Source gdrive = client.ingestion().createGoogleDrive("google-drive-folder-id");
Source jira = client.ingestion().createJira("atlassian-cloud-id");
Source confluence = client.ingestion().createConfluence("atlassian-cloud-id");
Source uploadSource = client.ingestion().createFileUpload("dataset-uuid");

// Names and advanced config are available when you need them.
Source deepWeb = client.ingestion().createWeb(
    WebSource.builder("docs-site").url("https://docs.vectoramp.com").crawlDepth(2).build()
);

Source spaces = client.ingestion().createConfluence(
    ConfluenceSource.builder("eng-confluence")
        .cloudId("atlassian-cloud-id")
        .spaces(java.util.List.of("ENG", "DOCS"))
        .includeAttachments(true)
        .build()
);

IngestionJob job = client.ingestion().startJob(web.getId(), "dataset-uuid");
```

`Dataset` resource objects can create a typed source and start ingestion in one flow:

```java
Source source = dataset.ingestSource(WebSource.of("https://docs.vectoramp.com"));
IngestionJob job = dataset.ingestSource(source.getId());
```

### File upload

```java
UploadSession session = dataset.ingestFiles(java.util.List.of(
    FileUpload.fromPath(java.nio.file.Path.of("docs/whitepaper.pdf"))
));
// PUT file bytes to each session.getUploads().get(i).getUploadUrl() with your HTTP client.
dataset.completeUpload(session);
```

## Intelligence / RAG

`ask` defaults to `top_k=5`, includes sources, and queries all accessible datasets when unscoped.

```java
// Top-level convenience.
AskResponse response = client.ask(
    AskRequest.of("What are the key features?").allDatasets().topK(5).includeSources(true)
);

// Or via the intelligence client (query is an alias for ask).
AskResponse scoped = client.intelligence().query(
    AskRequest.of("Summarize the roadmap").datasetId("dataset-uuid")
);

// Streaming SSE.
try (java.util.stream.Stream<SseEvent> events = client.askStream(
    AskRequest.of("Summarize the roadmap").datasetId("dataset-uuid")
)) {
    events.forEach(event -> System.out.print(event.getContent()));
}
```

### Sessions

```java
IntelligenceSession session = client.intelligence().createSession(
    SessionCreateRequest.of("support chat").datasetId("dataset-uuid")
);
client.intelligence().appendMessage(session.getId(), SessionMessageCreateRequest.user("Hello"));
java.util.List<SessionMessage> history = client.intelligence().listMessages(session.getId());
java.util.List<IntelligenceSession> recent = client.intelligence().listSessions(50);
client.intelligence().deleteSession(session.getId());
```

## Schedules

```java
Schedule schedule = client.schedules().create(
    CreateScheduleRequest.builder()
        .sourceId("source-uuid")
        .datasetId("dataset-uuid")
        .cron("0 2 * * *")
        .timezone("UTC")
        .build()
);
client.schedules().trigger(schedule.getId());
client.schedules().update(schedule.getId(), UpdateScheduleRequest.builder().enabled(false).build());
```

## Method reference

Both access styles work everywhere the language allows: `client.datasets().search(id, ...)` and
`datasetObj.search(...)`. Required arguments are listed first; optional arguments note their default.

### `client.datasets()` / `Dataset`
| Method | Object-style | Args |
|---|---|---|
| `list(limit?, offset?)` | — | `limit`, `offset` optional (API defaults) |
| `create(name)` | — | `name`; embedding `vectoramp/VectorAmp-Embedding-4B`, dim 2560, metric `cosine` |
| `create(name, hybrid)` | — | `hybrid` -> `hybrid:true` |
| `create(name, dim, metric, embedding)` | — | full explicit form |
| `create(CreateDatasetRequest)` | — | builder: `dim?`, `metric?`(cosine), `embedding?`, `hybrid?`, `metadata?` |
| `get(id)` | — | `id` |
| `delete(id)` | `ds.delete()` | `id` |
| `search(id, query)` | `ds.search(query)` | `query` (text or `SearchRequest`); `top_k` default 10 |
| `insert(id, vectors)` | `ds.insert(vectors)` | `vectors` (`VectorRecord` with String or numeric id) |
| `embed(id, texts)` | `ds.embed(texts)` | `texts` |
| `addText(id, text)` / `addTexts(id, texts)` | `ds.addText(text)` / `ds.addTexts(texts)` | `texts`; `AddTextsRequest` for ids/metadata |
| `listDocuments(id, limit?, cursor?, status?)` | `ds.listDocuments(...)` | cursor pagination |
| `downloadDocument(id, docId)` | `ds.downloadDocument(docId)` | raw bytes |

### `client.intelligence()`
| Method | Args |
|---|---|
| `ask(query)` / `query(query)` | `query` (String or `AskRequest`); `top_k` default 5, sources on, `all` datasets |
| `askStream(query)` / `stream(query)` | returns `Stream<SseEvent>` |
| `createSession(title \| SessionCreateRequest)` | optional title/workspace/dataset/metadata |
| `listSessions(limit?)` | returns `List<IntelligenceSession>` |
| `getSession(id)` / `deleteSession(id)` | `id` |
| `appendMessage(sessionId, role, content \| request)` | `role`, `content`, optional metadata |
| `listMessages(sessionId, limit?)` | returns `List<SessionMessage>` |

### `client.ingestion()`
| Method | Args |
|---|---|
| `listSources(limit?, offset?)` / `getSource(id)` | pagination / id |
| `createSource(CreateSourceRequest \| IngestionSourceInput)` | generic create |
| `createWeb/createS3/createGCS/createGoogleDrive/createJira/createConfluence/createFileUpload(...)` | typed convenience creators |
| `startJob(sourceId, datasetId, pipelineId?)` | start ingestion |
| `listJobs(datasetId?, limit?, offset?)` / `getJob(id)` / `retryJob(id)` | jobs |
| `initializeUpload(sourceId, files)` / `completeUpload(sourceId, jobId, fileIds)` | presigned upload flow |

### `client.schedules()`
`list(limit?, offset?)`, `get(id)`, `create(CreateScheduleRequest)`,
`update(id, UpdateScheduleRequest)`, `delete(id)`, `trigger(id)`.

### Source helpers
`WebSource`, `S3Source`, `GCSSource`, `GoogleDriveSource`, `JiraSource`, `ConfluenceSource`,
`FileUploadSource`, and `GenericSource` provide `.of(...)`/`.builder(...)` factories. Each maps to
its API `source_type` (`web`, `s3`, `gcs`, `gdrive`, `jira`, `confluence`, `file_upload`).

## Transport architecture

Public services depend on a small `Transport` interface. The default implementation is REST/JSON
over Java's built-in `HttpClient`; a future gRPC transport can be added without changing
`client.datasets()`, `client.ingestion()`, `client.intelligence()`, or `client.ask()` usage.

## Development

```bash
gradle clean check
```

CI runs the same command and publishes JUnit/Jacoco artifacts.
