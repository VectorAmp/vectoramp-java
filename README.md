# VectorAmp Java SDK

Java 11+ client for the public VectorAmp API.

> This repository is Maven Central-ready, but packages are **not published** yet.

## Install

Until the first published release, build locally:

```bash
gradle clean build publishToMavenLocal
```

Then add it to a Gradle project:

```gradle
repositories { mavenLocal(); mavenCentral() }

dependencies {
  implementation 'com.vectoramp:vectoramp-java:0.1.0-SNAPSHOT'
}
```

Future Maven Central coordinates:

```gradle
implementation 'com.vectoramp:vectoramp-java:<version>'
```

## Quick start

```java
import com.vectoramp.VectorAmpClient;
import com.vectoramp.models.*;

try (VectorAmpClient client = VectorAmpClient.create(System.getenv("VECTORAMP_API_KEY"))) {
    Dataset dataset = client.datasets().create(
        "product-docs",
        2560,
        "cosine",
        EmbeddingConfig.of("vectoramp", "VectorAmp-Embedding-4B")
    );

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
VectorAmpClient client = VectorAmpClient.builder("va_api_key")
    .baseUrl("https://api.vectoramp.com")
    .build();
```

Authentication uses the public API-key header:

```http
X-API-Key: <api_key>
```

## Datasets

```java
Page<Dataset> page = client.datasets().list(50, 0);
Dataset dataset = client.datasets().get("dataset-uuid");
client.datasets().delete("dataset-uuid");
```

`create`, `get`, and `list` return `Dataset` resource objects. They keep the raw API payload (`getRawData()`), the dataset id, and bound SDK services so you can call instance methods without passing the id around:

```java
Dataset dataset = client.datasets().get("dataset-uuid");

SearchResponse results = dataset.search("release notes");
dataset.insert(java.util.List.of(VectorRecord.of("doc-1", java.util.List.of(0.1, 0.2), null)));
dataset.addText("hello world");
AskResponse answer = dataset.ask("Summarize this dataset");
UploadSession upload = dataset.ingestFiles(java.util.List.of(
    FileUpload.of("docs/whitepaper.pdf", 248120, "application/pdf")
));
dataset.delete();
```

The service-style methods remain supported for explicit id-based usage.

Dataset creation intentionally **does not expose** an index-type option. The SDK always sends `index_type: "sable"`.

### Source documents

Dataset document listing is cursor-based. Pass `getNextCursor()` into the next request and do not assume offsets or totals. Downloads return the retained original bytes and the default transport follows redirects to storage.

```java
DatasetDocumentPage docs = client.datasets().listDocuments("dataset-uuid", 50, null, "ready");
for (DatasetDocument doc : docs.getDocuments()) {
    if (Boolean.TRUE.equals(doc.getDownloadAvailable())) {
        byte[] original = client.datasets().downloadDocument("dataset-uuid", doc.getId());
    }
}

if (docs.getNextCursor() != null) {
    DatasetDocumentPage next = client.datasets().listDocuments("dataset-uuid", 50, docs.getNextCursor(), "ready");
}

// Resource-style helpers are available too.
Dataset dataset = client.datasets().get("dataset-uuid");
DatasetDocumentPage page = dataset.listDocuments(25, null, null);
byte[] raw = dataset.downloadDocument(page.getDocuments().get(0).getId());
```

### Search

```java
SearchResponse results = client.datasets().search("dataset-uuid", "machine learning best practices");

// Add options only when you need them.
SearchResponse filtered = client.datasets().search(
    "dataset-uuid",
    SearchRequest.text("machine learning best practices")
        .includeDocuments(false)
        .includeMetadata(true)
        .rerank(RerankConfig.enabled()) // vectoramp / VectorAmp-Rerank-v1
);
```

Vector search:

```java
SearchResponse results = client.datasets().search(
    "dataset-uuid",
    SearchRequest.vector(java.util.List.of(0.1, 0.2, 0.3))
        .includeDocuments(true)
);
```

### Insert vectors

```java
InsertResponse inserted = client.datasets().insert("dataset-uuid", java.util.List.of(
    VectorRecord.of("doc-001", java.util.List.of(0.1, 0.2, 0.3), java.util.Map.of("title", "Intro"))
));
```

### Add text

`addTexts` embeds text through `/datasets/{id}/embed`, then inserts vectors through `/datasets/{id}/insert` with text metadata.

```java
client.datasets().addText("dataset-uuid", "hello world");
client.datasets().addTexts("dataset-uuid", java.util.List.of("hello", "world"));
```

## Ingestion

```java
Page<Source> sources = client.ingestion().listSources(50, 0);

Source web = client.ingestion().createWeb("https://docs.vectoramp.com");
Source s3 = client.ingestion().createS3("my-docs-bucket");
Source gdrive = client.ingestion().createGoogleDrive("google-drive-folder-id");
Source uploadSource = client.ingestion().createFileUpload("dataset-uuid");

// Names and advanced config are still available when you need them.
Source deepWeb = client.ingestion().createWeb(
    WebSource.builder("docs-site")
        .url("https://docs.vectoramp.com")
        .crawlDepth(2)
        .build()
);

Source regionalS3 = client.ingestion().createS3(
    S3Source.builder("s3-docs", "my-docs-bucket")
        .prefix("public/")
        .region("us-east-1")
        .build()
);
IngestionJob job = client.ingestion().startJob(web.getId(), "dataset-uuid");
```

Typed source helpers currently cover `web`, `s3`, `gcs`, `gdrive`, `file_upload`, and `jira`. Use `GenericSource` as an escape hatch when the API exposes new config before the SDK adds a typed wrapper:

```java
Source custom = client.ingestion().createSource(
    GenericSource.builder(SourceType.WEB)
        .config("url", "https://example.com")
        .metadata("owner", "docs")
        .build()
);
```

`Dataset` resource objects can create typed sources or start ingestion from an existing source id:

```java
Dataset dataset = client.datasets().get("dataset-uuid");
Source source = dataset.ingestSource(WebSource.of("https://docs.vectoramp.com"));
IngestionJob job = dataset.ingestSource(source.getId());
```

File upload flow, where supported by the REST API:

```java
UploadSession session = dataset.ingestFiles(java.util.List.of(
    FileUpload.of("docs/whitepaper.pdf", 248120, "application/pdf")
));
// Or derive file name, size, and content type from a local path:
// UploadSession session = dataset.ingestFiles(java.util.List.of(FileUpload.fromPath(java.nio.file.Path.of("docs/whitepaper.pdf"))));
// PUT file bytes to each UploadTarget.getUploadUrl() using your HTTP client.
dataset.completeUpload(session);
```

## Intelligence / RAG

Non-streaming:

```java
AskResponse response = client.ask(
    AskRequest.of("What are the key features?")
        .allDatasets()
        .topK(5)
        .includeSources(true)
);
```

Streaming SSE:

```java
try (java.util.stream.Stream<SseEvent> events = client.askStream(
    AskRequest.of("Summarize the roadmap").datasetId("dataset-uuid")
)) {
    events.forEach(event -> System.out.print(event.getContent()));
}
```

## Transport architecture

Public services depend on a small `Transport` interface. The current implementation is REST/JSON via Java `HttpClient`; a future gRPC transport can be added without changing `client.datasets()`, `client.ingestion()`, or `client.ask()` usage.

## Development

```bash
gradle clean check
```

CI runs the same command and publishes JUnit/Jacoco artifacts.
