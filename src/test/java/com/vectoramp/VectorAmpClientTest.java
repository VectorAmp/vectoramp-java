package com.vectoramp;

import com.vectoramp.models.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorAmpClientTest {
    MockWebServer server;
    VectorAmpClient client;

    @BeforeEach void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        client = VectorAmpClient.builder("test-key").baseUrl(server.url("/").toString()).build();
    }

    @AfterEach void tearDown() throws Exception {
        client.close();
        server.shutdown();
    }

    @Test void defaultsToProductionApi() {
        assertThat(VectorAmpClient.DEFAULT_BASE_URL).isEqualTo("https://api.vectoramp.com");
    }

    @Test void listDatasetsSendsApiKeyAndPagination() throws Exception {
        server.enqueue(json("{\"datasets\":[{\"id\":\"ds1\",\"name\":\"docs\",\"dim\":3,\"metric\":\"cosine\",\"embedding\":{\"provider\":\"vectoramp\",\"model\":\"m\"},\"index_type\":\"sable\"}],\"total\":10,\"limit\":1,\"offset\":2}"));

        Page<Dataset> page = client.datasets().list(1, 2);

        assertThat(page.getItems()).hasSize(1);
        Dataset dataset = page.getItems().get(0);
        assertThat(dataset.getIndexType()).isEqualTo("sable");
        assertThat(dataset.getDatasetsClient()).isSameAs(client.datasets());
        assertThat(dataset.getRawData().get("id").asText()).isEqualTo("ds1");
        assertThat(page.getTotal()).isEqualTo(10);
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/datasets?limit=1&offset=2");
        assertThat(req.getHeader("X-API-Key")).isEqualTo("test-key");
    }

    @Test void createDatasetForcesSableIndexType() throws Exception {
        server.enqueue(json("{\"id\":\"ds1\",\"name\":\"docs\",\"dim\":2560,\"metric\":\"cosine\",\"embedding\":{\"provider\":\"vectoramp\",\"model\":\"Qwen\"},\"index_type\":\"sable\"}"));

        Dataset dataset = client.datasets().create("docs", 2560, "cosine", EmbeddingConfig.of("vectoramp", "Qwen"));

        assertThat(dataset.getId()).isEqualTo("ds1");
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/datasets");
        assertThat(req.getBody().readUtf8()).contains("\"index_type\":\"sable\"").doesNotContain("hnsw");
    }

    @Test void getDeleteSearchAndInsertDatasets() throws Exception {
        server.enqueue(json("{\"id\":\"abc\",\"name\":\"n\"}"));
        server.enqueue(json("{}"));
        server.enqueue(json("{\"results\":[{\"id\":\"v1\",\"score\":0.9,\"metadata\":{\"title\":\"doc\"}}],\"dataset_id\":\"abc\",\"query_time_ms\":3.2}"));
        server.enqueue(json("{\"inserted\":2}"));

        assertThat(client.datasets().get("abc").getName()).isEqualTo("n");
        client.datasets().delete("abc");
        SearchResponse search = client.datasets().search("abc", SearchRequest.text("hello", 5).includeDocuments(false).includeMetadata(true));
        InsertResponse insert = client.datasets().insert("abc", List.of(VectorRecord.of("v1", List.of(0.1, 0.2), Map.of("a", "b")), VectorRecord.of("v2", List.of(0.3, 0.4), null)));

        assertThat(search.getResults().get(0).getScore()).isEqualTo(0.9);
        assertThat(insert.getInserted()).isEqualTo(2);
        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/abc");
        assertThat(server.takeRequest().getMethod()).isEqualTo("DELETE");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("query_text", "include_documents", "include_metadata");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("vectors");
    }

    @Test void searchTextAliasSendsSingleQueryTextFieldForHybridDatasets() throws Exception {
        server.enqueue(json("{\"results\":[],\"dataset_id\":\"abc\"}"));

        client.datasets().search("abc", SearchRequest.searchText("rare zebra quokka", 3).includeMetadata(true));

        String body = server.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"query_text\":\"rare zebra quokka\"", "\"top_k\":3");
        assertThat(body).doesNotContain("sparse_query").doesNotContain("hybrid");
    }


    @Test void datasetDocumentsListAndDownloadUseCursorAndRawBytes() throws Exception {
        MockWebServer fileServer = new MockWebServer();
        fileServer.start();
        try {
            byte[] expected = new byte[] {0, 1, (byte) 0xff, 'V', 'A'};
            server.enqueue(json("{\"documents\":[{\"id\":\"doc1\",\"file_name\":\"a.pdf\",\"status\":\"ready\",\"download_available\":true}],\"next_cursor\":\"cur2\",\"limit\":2}"));
            server.enqueue(new MockResponse().setResponseCode(302).setHeader("Location", fileServer.url("/raw/doc.bin")));
            fileServer.enqueue(new MockResponse().setBody(new okio.Buffer().write(expected)));

            DatasetDocumentPage page = client.datasets().listDocuments("ds1", 2, "cur1", "ready");
            byte[] bytes = client.datasets().downloadDocument("ds1", "doc1");

            assertThat(page.getDocuments()).hasSize(1);
            assertThat(page.getDocuments().get(0).getId()).isEqualTo("doc1");
            assertThat(page.getNextCursor()).isEqualTo("cur2");
            assertThat(bytes).containsExactly(expected);
            assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds1/documents?limit=2&cursor=cur1&status=ready");
            assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds1/documents/doc1/download");
            assertThat(fileServer.takeRequest().getPath()).isEqualTo("/raw/doc.bin");
        } finally {
            fileServer.shutdown();
        }
    }

    @Test void addTextsEmbedsThenInsertsTextMetadata() throws Exception {
        server.enqueue(json("{\"embeddings\":[[0.1,0.2],[0.3,0.4]]}"));
        server.enqueue(json("{\"inserted\":2}"));

        InsertResponse response = client.datasets().addTexts("ds", AddTextsRequest.of(List.of("hello", "world"), List.of("a", "b"), List.of(Map.of("source", "test"), Map.of())));

        assertThat(response.getInserted()).isEqualTo(2);
        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds/embed");
        RecordedRequest insert = server.takeRequest();
        assertThat(insert.getPath()).isEqualTo("/datasets/ds/insert");
        assertThat(insert.getBody().readUtf8()).contains("\"text\":\"hello\"", "\"id\":\"a\"", "\"source\":\"test\"");
    }

    @Test void datasetResourceMethodsDelegateToServices() throws Exception {
        server.enqueue(json("{\"id\":\"ds\",\"name\":\"docs\",\"extra_field\":true}"));
        server.enqueue(json("{\"results\":[],\"dataset_id\":\"ds\"}"));
        server.enqueue(json("{\"inserted\":1}"));
        server.enqueue(json("{\"embeddings\":[[0.1,0.2]]}"));
        server.enqueue(json("{\"inserted\":1}"));
        server.enqueue(json("{\"answer\":\"yes\",\"sources\":[],\"chunks\":[],\"metadata\":{}}"));
        server.enqueue(json("{\"id\":\"src\",\"name\":\"upload\",\"type\":\"file_upload\"}"));
        server.enqueue(json("{\"job_id\":\"job\",\"uploads\":[{\"file_id\":\"f1\",\"file_name\":\"a.txt\",\"upload_url\":\"https://s3\"}]}"));
        server.enqueue(json("{\"job_id\":\"job\",\"status\":\"pending\"}"));
        server.enqueue(json("{}"));

        Dataset dataset = client.datasets().get("ds");
        assertThat(dataset.getRawData().get("extra_field").asBoolean()).isTrue();
        assertThat(dataset.search("hello").getDatasetId()).isEqualTo("ds");
        assertThat(dataset.insert(List.of(VectorRecord.of("v1", List.of(0.1, 0.2), null))).getInserted()).isEqualTo(1);
        assertThat(dataset.addText("hello").getInserted()).isEqualTo(1);
        assertThat(dataset.ask("question").getAnswer()).isEqualTo("yes");
        UploadSession upload = dataset.ingestFiles(List.of(FileUpload.of("a.txt", 2, "text/plain")));
        assertThat(upload.getJobId()).isEqualTo("job");
        assertThat(upload.getSourceId()).isEqualTo("src");
        assertThat(dataset.completeUpload(upload).getStatus()).isEqualTo("pending");
        dataset.delete();

        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds");
        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds/search");
        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds/insert");
        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds/embed");
        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds/insert");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"dataset_id\":\"ds\"");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("file_upload", "dataset_id", "file-upload-ds");
        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/sources/src/upload/init");
        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/sources/src/upload/complete");
        assertThat(server.takeRequest().getMethod()).isEqualTo("DELETE");
    }

    @Test void ingestionSourcesJobsAndUpload() throws Exception {
        server.enqueue(json("{\"sources\":[{\"id\":\"s1\",\"name\":\"upload\",\"type\":\"file_upload\"}],\"total\":1,\"limit\":50,\"offset\":0}"));
        server.enqueue(json("{\"id\":\"s1\",\"name\":\"upload\",\"type\":\"file_upload\"}"));
        server.enqueue(json("{\"job_id\":\"j1\",\"status\":\"pending\"}"));
        server.enqueue(json("{\"job_id\":\"j3\",\"status\":\"pending\"}"));
        server.enqueue(json("{\"job_id\":\"j2\",\"uploads\":[{\"file_id\":\"f1\",\"file_name\":\"a.txt\",\"upload_url\":\"https://s3\"}]}"));
        server.enqueue(json("{\"job_id\":\"j2\",\"status\":\"pending\"}"));

        assertThat(client.ingestion().listSources().getItems().get(0).getType()).isEqualTo("file_upload");
        assertThat(client.ingestion().createFileUploadSource("ds").getId()).isEqualTo("s1");
        assertThat(client.ingestion().startJob("s1", "ds").getJobId()).isEqualTo("j1");
        assertThat(client.ingestion().retryJob("j1").getJobId()).isEqualTo("j3");
        UploadSession upload = client.ingestion().initializeUpload("s1", List.of(FileUpload.of("a.txt", 2, "text/plain")));
        assertThat(upload.getSourceId()).isEqualTo("s1");
        assertThat(upload.getUploads().get(0).getFileId()).isEqualTo("f1");
        assertThat(client.ingestion().completeUpload("s1", "j2", List.of("f1")).getStatus()).isEqualTo("pending");

        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/sources");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("file_upload", "dataset_id", "file-upload-ds");
        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/jobs");
        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/jobs/j1/retry");
        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/sources/s1/upload/init");
        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/sources/s1/upload/complete");
    }

    @Test void typedIngestionSourceBuildersSerializeSupportedSourceTypes() throws Exception {
        server.enqueue(json("{\"id\":\"web\",\"name\":\"site\",\"type\":\"web\"}"));
        server.enqueue(json("{\"id\":\"s3\",\"name\":\"bucket\",\"type\":\"s3\"}"));
        server.enqueue(json("{\"id\":\"gd\",\"name\":\"drive\",\"type\":\"gdrive\"}"));
        server.enqueue(json("{\"id\":\"fu\",\"name\":\"upload\",\"type\":\"file_upload\"}"));
        server.enqueue(json("{\"id\":\"gen\",\"name\":\"generic\",\"type\":\"web\"}"));

        client.ingestion().createWeb(WebSource.builder("site").url("https://example.com").crawlDepth(2).metadata("team", "docs").build());
        client.ingestion().createS3(S3Source.builder("bucket", "docs-bucket").prefix("manuals/").region("us-east-1").build());
        client.ingestion().createGoogleDrive(GoogleDriveSource.folder("drive", "folder-1"));
        client.ingestion().createFileUpload(FileUploadSource.of("upload", "ds"));
        client.ingestion().createSource(GenericSource.builder(SourceType.WEB, "generic").config("url", "https://vectoramp.com").build());

        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"source_type\":\"web\"", "\"url\":\"https://example.com\"", "\"crawl_depth\":2", "\"team\":\"docs\"");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"source_type\":\"s3\"", "\"bucket\":\"docs-bucket\"", "\"prefix\":\"manuals/\"", "\"region\":\"us-east-1\"");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"source_type\":\"gdrive\"", "\"folder_id\":\"folder-1\"");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"source_type\":\"file_upload\"", "\"dataset_id\":\"ds\"", "\"storage_provider\":\"s3\"");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"source_type\":\"web\"", "\"name\":\"generic\"");
    }

    @Test void sourceConvenienceOverloadsGenerateDefaultNames() throws Exception {
        server.enqueue(json("{\"id\":\"web\",\"name\":\"example.com-docs\",\"type\":\"web\"}"));
        server.enqueue(json("{\"id\":\"s3\",\"name\":\"docs-bucket\",\"type\":\"s3\"}"));
        server.enqueue(json("{\"id\":\"gd\",\"name\":\"folder-1\",\"type\":\"gdrive\"}"));
        server.enqueue(json("{\"id\":\"fu\",\"name\":\"file-upload-ds\",\"type\":\"file_upload\"}"));

        assertThat(client.ingestion().createWeb("https://example.com/docs").getId()).isEqualTo("web");
        assertThat(client.ingestion().createS3("docs-bucket").getId()).isEqualTo("s3");
        assertThat(client.ingestion().createGoogleDrive("folder-1").getId()).isEqualTo("gd");
        assertThat(client.ingestion().createFileUpload("ds").getId()).isEqualTo("fu");

        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"name\":\"example.com-docs\"", "\"url\":\"https://example.com/docs\"");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"name\":\"docs-bucket\"", "\"bucket\":\"docs-bucket\"");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"name\":\"folder-1\"", "\"folder_id\":\"folder-1\"");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"name\":\"file-upload-ds\"", "\"dataset_id\":\"ds\"");
    }

    @Test void datasetIngestSourceAcceptsTypedSourceInputOrSourceId() throws Exception {
        server.enqueue(json("{\"id\":\"ds\",\"name\":\"docs\"}"));
        server.enqueue(json("{\"id\":\"src\",\"name\":\"site\",\"type\":\"web\"}"));
        server.enqueue(json("{\"job_id\":\"job\",\"status\":\"pending\"}"));

        Dataset dataset = client.datasets().get("ds");
        assertThat(dataset.ingestSource(WebSource.of("site", "https://example.com")).getId()).isEqualTo("src");
        assertThat(dataset.ingestSource("src").getJobId()).isEqualTo("job");

        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"source_type\":\"web\"", "\"url\":\"https://example.com\"");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"source_id\":\"src\"", "\"dataset_id\":\"ds\"");
    }

    @Test void askNonStreamingAndStreamingSse() throws Exception {
        server.enqueue(json("{\"answer\":\"42\",\"sources\":[],\"chunks\":[],\"metadata\":{}}"));
        server.enqueue(new MockResponse().setHeader("Content-Type", "text/event-stream").setBody("data: {\"chunk_type\":\"text\",\"content\":\"hi\",\"metadata\":{}}\n\ndata: {\"chunk_type\":\"done\",\"content\":\"\",\"metadata\":{}}\n\n"));

        assertThat(client.ask(AskRequest.of("meaning").allDatasets()).getAnswer()).isEqualTo("42");
        List<SseEvent> events = client.askStream("hello").collect(Collectors.toList());

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getChunkType()).isEqualTo("text");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"stream\":false");
        RecordedRequest stream = server.takeRequest();
        assertThat(stream.getHeader("Accept")).isEqualTo("text/event-stream");
        assertThat(stream.getBody().readUtf8()).contains("\"stream\":true");
    }

    @Test void apiErrorsThrowUsefulException() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("bad key"));
        assertThatThrownBy(() -> client.datasets().list()).isInstanceOf(VectorAmpApiException.class).hasMessageContaining("401").hasMessageContaining("bad key");
    }

    private static MockResponse json(String body) {
        return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
    }
}
