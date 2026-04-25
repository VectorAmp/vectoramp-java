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
        EmbeddingConfig.of("vectoramp", "Qwen/Qwen3-Embedding-4B")
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

SearchResponse results = dataset.search(SearchRequest.text("release notes", 5));
dataset.insert(java.util.List.of(VectorRecord.of("doc-1", java.util.List.of(0.1, 0.2), null)));
dataset.addTexts(java.util.List.of("hello world"));
AskResponse answer = dataset.ask("Summarize this dataset");
UploadSession upload = dataset.ingestFiles("docs-upload", java.util.List.of(
    new FileUpload("docs/whitepaper.pdf", 248120, "application/pdf")
));
dataset.delete();
```

The service-style methods remain supported for explicit id-based usage.

Dataset creation intentionally **does not expose** an index-type option. The SDK always sends `index_type: "sable"`.

### Search

```java
SearchResponse results = client.datasets().search(
    "dataset-uuid",
    SearchRequest.text("machine learning best practices", 10)
        .includeDocuments(false)
        .includeMetadata(true)
);
```

Vector search:

```java
SearchResponse results = client.datasets().search(
    "dataset-uuid",
    SearchRequest.vector(java.util.List.of(0.1, 0.2, 0.3), 5)
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
client.datasets().addTexts("dataset-uuid", java.util.List.of("hello world"));
```

## Ingestion

```java
Page<Source> sources = client.ingestion().listSources(50, 0);

Source web = client.ingestion().createWeb(
    WebSource.builder("docs-site")
        .url("https://docs.vectoramp.com")
        .crawlDepth(2)
        .build()
);

Source s3 = client.ingestion().createS3(
    S3Source.builder("s3-docs", "my-docs-bucket")
        .prefix("public/")
        .region("us-east-1")
        .build()
);

Source gdrive = client.ingestion().createGoogleDrive(
    GoogleDriveSource.folder("drive-docs", "google-drive-folder-id")
);

Source uploadSource = client.ingestion().createFileUpload("dataset-uuid", "docs-upload");
IngestionJob job = client.ingestion().startJob(web.getId(), "dataset-uuid");
```

Typed source helpers currently cover `web`, `s3`, `gdrive`, and `file_upload`. Use `GenericSource` as an escape hatch when the API exposes new config before the SDK adds a typed wrapper:

```java
Source custom = client.ingestion().createSource(
    GenericSource.builder(SourceType.WEB, "custom-web")
        .config("url", "https://example.com")
        .metadata("owner", "docs")
        .build()
);
```

`Dataset` resource objects can create typed sources or start ingestion from an existing source id:

```java
Dataset dataset = client.datasets().get("dataset-uuid");
Source source = dataset.ingestSource(WebSource.of("docs-site", "https://docs.vectoramp.com"));
IngestionJob job = dataset.ingestSource(source.getId());
```

File upload flow, where supported by the REST API:

```java
UploadSession session = client.ingestion().initializeUpload(uploadSource.getId(), java.util.List.of(
    new FileUpload("docs/whitepaper.pdf", 248120, "application/pdf")
));
// PUT file bytes to each UploadTarget.getUploadUrl() using your HTTP client.
client.ingestion().completeUpload(uploadSource.getId(), session.getJobId(), java.util.List.of(
    session.getUploads().get(0).getFileId()
));
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
