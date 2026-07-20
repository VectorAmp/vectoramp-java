package com.vectoramp;

import com.fasterxml.jackson.databind.JsonNode;
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

    @Test void createDatasetWithOnlyNameUsesDefaults() throws Exception {
        server.enqueue(json("{\"id\":\"ds1\",\"name\":\"docs\",\"dim\":2560,\"metric\":\"cosine\",\"index_type\":\"sable\"}"));

        Dataset dataset = client.datasets().create("docs");

        assertThat(dataset.getId()).isEqualTo("ds1");
        String body = server.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"name\":\"docs\"");
        assertThat(body).contains("\"dim\":2560");
        assertThat(body).contains("\"metric\":\"cosine\"");
        assertThat(body).contains("\"index_type\":\"sable\"");
        assertThat(body).contains("\"provider\":\"vectoramp\"");
        assertThat(body).contains("\"model\":\"VectorAmp-Embedding-4B\"");
        // dim is sent as a JSON number, never as dimension, and hybrid is omitted by default.
        assertThat(body).doesNotContain("dimension").doesNotContain("hybrid").doesNotContain("hnsw");
    }

    @Test void createDatasetHybridSendsHybridTrue() throws Exception {
        server.enqueue(json("{\"id\":\"ds1\",\"name\":\"docs\",\"dim\":2560,\"index_type\":\"sable\"}"));
        client.datasets().create("docs", true);
        String body = server.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"hybrid\":true");
        assertThat(body).contains("\"index_type\":\"sable\"");

        server.enqueue(json("{\"id\":\"ds2\",\"name\":\"docs\",\"dim\":1536,\"index_type\":\"sable\"}"));
        client.datasets().create(CreateDatasetRequest.builder("docs")
                .embedding(EmbeddingConfig.openai("small"))
                .hybrid(true)
                .build());
        String openaiBody = server.takeRequest().getBody().readUtf8();
        // openai/text-embedding-3-small infers dim 1536 without an explicit dim.
        assertThat(openaiBody).contains("\"dim\":1536", "\"provider\":\"openai\"", "\"model\":\"text-embedding-3-small\"", "\"hybrid\":true");
    }

    @Test void insertPreservesNumericVectorIdsAsJsonNumbers() throws Exception {
        server.enqueue(json("{\"inserted\":3}"));

        InsertResponse response = client.datasets().insert("ds", List.of(
                VectorRecord.of(42L, List.of(0.1, 0.2), Map.of("title", "doc")),
                VectorRecord.of(7L, List.of(0.3, 0.4), null),
                VectorRecord.of("str-id", List.of(0.5, 0.6), null)));

        assertThat(response.getInserted()).isEqualTo(3);
        String body = server.takeRequest().getBody().readUtf8();
        // Numeric ids serialize as JSON numbers (no quotes); string ids keep their quotes.
        assertThat(body).contains("\"id\":42").contains("\"id\":7").contains("\"id\":\"str-id\"");
        assertThat(body).doesNotContain("\"id\":\"42\"").doesNotContain("\"id\":\"7\"");
    }

    @Test void createsAndUpdatesMetadataSchema() throws Exception {
        List<MetadataSchemaField> schema = List.of(MetadataSchemaField.of("price", MetadataFieldType.F32));
        server.enqueue(json("{\"id\":\"ds1\",\"name\":\"products\",\"dim\":2560}"));
        client.datasets().create(CreateDatasetRequest.builder("products").metadataSchema(schema).build());
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"schema\":[{\"name\":\"price\",\"type\":\"f32\"}]");

        server.enqueue(json("{\"id\":\"ds1\",\"name\":\"products\",\"dim\":2560,\"schema_version\":2}"));
        client.datasets().patchMetadataSchema("ds/1", schema);
        okhttp3.mockwebserver.RecordedRequest merge = server.takeRequest();
        assertThat(merge.getMethod()).isEqualTo("PATCH");
        assertThat(merge.getPath()).isEqualTo("/datasets/ds%2F1/schema");
        assertThat(merge.getBody().readUtf8()).contains("\"mode\":\"merge\"");

        server.enqueue(json("{\"id\":\"ds1\",\"name\":\"products\",\"dim\":2560,\"schema_version\":3}"));
        client.datasets().replaceMetadataSchema("ds1", List.of());
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"schema\":[]", "\"mode\":\"replace\"");
    }

    @Test void addTextsPreservesNumericIdsAsJsonNumbers() throws Exception {
        server.enqueue(json("{\"embeddings\":[[0.1,0.2]]}"));
        server.enqueue(json("{\"inserted\":1}"));

        client.datasets().addTexts("ds", AddTextsRequest.ofNumericIds(
                List.of("hello"), List.of(99L), null));

        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/ds/embed");
        String insertBody = server.takeRequest().getBody().readUtf8();
        assertThat(insertBody).contains("\"id\":99").doesNotContain("\"id\":\"99\"");
    }

    @Test void confluenceSourceSerializesConfluenceType() throws Exception {
        server.enqueue(json("{\"id\":\"conf\",\"name\":\"docs\",\"type\":\"confluence\"}"));
        server.enqueue(json("{\"id\":\"conf2\",\"name\":\"company.atlassian.net\",\"type\":\"confluence\"}"));

        client.ingestion().createConfluence(ConfluenceSource.builder("eng-confluence")
                .cloudId("cloud-123")
                .spaces(List.of("ENG", "DOCS"))
                .includeAttachments(true)
                .build());
        String body = server.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"source_type\":\"confluence\"");
        assertThat(body).contains("\"cloud_id\":\"cloud-123\"");
        assertThat(body).contains("\"spaces\":[\"ENG\",\"DOCS\"]");
        assertThat(body).contains("\"include_attachments\":true");
        assertThat(body).contains("\"name\":\"eng-confluence\"");

        client.ingestion().createConfluence("cloud-123");
        assertThat(server.takeRequest().getBody().readUtf8())
                .contains("\"source_type\":\"confluence\"", "\"cloud_id\":\"cloud-123\"");

        assertThat(SourceType.CONFLUENCE).isEqualTo("confluence");
        assertThat(ConfluenceSource.basicAuth("https://company.atlassian.net", "me@x.com", "tok")
                .toCreateSourceRequest().getConfig())
                .containsEntry("base_url", "https://company.atlassian.net")
                .containsEntry("auth_mode", "basic")
                .containsEntry("username", "me@x.com")
                .containsEntry("api_token", "tok");
    }

    @Test void intelligenceSessionsCrudAndMessages() throws Exception {
        server.enqueue(json("{\"id\":\"sess1\",\"title\":\"chat\",\"status\":\"active\",\"organization_id\":\"org1\"}").setResponseCode(201));
        server.enqueue(json("{\"sessions\":[{\"id\":\"sess1\",\"title\":\"chat\"},{\"id\":\"sess2\",\"title\":\"other\"}]}"));
        server.enqueue(json("{\"id\":\"sess1\",\"title\":\"chat\",\"status\":\"active\"}"));
        server.enqueue(json("{\"id\":\"msg1\",\"session_id\":\"sess1\",\"role\":\"user\",\"content\":\"hi\"}").setResponseCode(201));
        server.enqueue(json("{\"messages\":[{\"id\":\"msg1\",\"role\":\"user\",\"content\":\"hi\"}]}"));
        server.enqueue(json("{}"));

        IntelligenceSession created = client.intelligence().createSession(
                SessionCreateRequest.of("chat").datasetId("ds1"));
        assertThat(created.getId()).isEqualTo("sess1");
        assertThat(created.getOrganizationId()).isEqualTo("org1");
        RecordedRequest createReq = server.takeRequest();
        assertThat(createReq.getPath()).isEqualTo("/intelligence/sessions");
        assertThat(createReq.getMethod()).isEqualTo("POST");
        assertThat(createReq.getBody().readUtf8()).contains("\"title\":\"chat\"", "\"dataset_id\":\"ds1\"");

        List<IntelligenceSession> sessions = client.intelligence().listSessions(50);
        assertThat(sessions).hasSize(2);
        assertThat(sessions.get(1).getId()).isEqualTo("sess2");
        assertThat(server.takeRequest().getPath()).isEqualTo("/intelligence/sessions?limit=50");

        IntelligenceSession one = client.intelligence().getSession("sess1");
        assertThat(one.getTitle()).isEqualTo("chat");
        assertThat(server.takeRequest().getPath()).isEqualTo("/intelligence/sessions/sess1");

        SessionMessage appended = client.intelligence().appendMessage("sess1",
                SessionMessageCreateRequest.user("hi"));
        assertThat(appended.getId()).isEqualTo("msg1");
        RecordedRequest appendReq = server.takeRequest();
        assertThat(appendReq.getPath()).isEqualTo("/intelligence/sessions/sess1/messages");
        assertThat(appendReq.getBody().readUtf8()).contains("\"role\":\"user\"", "\"content\":\"hi\"");

        List<SessionMessage> messages = client.intelligence().listMessages("sess1", 100);
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo("hi");
        assertThat(server.takeRequest().getPath()).isEqualTo("/intelligence/sessions/sess1/messages?limit=100");

        client.intelligence().deleteSession("sess1");
        RecordedRequest deleteReq = server.takeRequest();
        assertThat(deleteReq.getMethod()).isEqualTo("DELETE");
        assertThat(deleteReq.getPath()).isEqualTo("/intelligence/sessions/sess1");
    }

    @Test void intelligenceAccessorAndQueryAlias() throws Exception {
        server.enqueue(json("{\"answer\":\"yes\",\"sources\":[]}"));
        assertThat(client.intelligence()).isSameAs(client.intelligence());
        assertThat(client.intelligence().query("question").getAnswer()).isEqualTo("yes");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"stream\":false");
    }

    @Test void getDeleteSearchAndInsertDatasets() throws Exception {
        server.enqueue(json("{\"id\":\"abc\",\"name\":\"n\"}"));
        server.enqueue(json("{}"));
        server.enqueue(json("{\"results\":[{\"id\":\"v1\",\"score\":0.9,\"metadata\":{\"title\":\"doc\"}}],\"dataset_id\":\"abc\",\"query_time_ms\":3.2}"));
        server.enqueue(json("{\"inserted\":2}"));

        assertThat(client.datasets().get("abc").getName()).isEqualTo("n");
        client.datasets().delete("abc");
        SearchResponse search = client.datasets().search("abc", SearchRequest.text("hello", 5).includeDocuments(false).includeMetadata(true).rerank(true));
        InsertResponse insert = client.datasets().insert("abc", List.of(VectorRecord.of("v1", List.of(0.1, 0.2), Map.of("a", "b")), VectorRecord.of("v2", List.of(0.3, 0.4), null)));

        assertThat(search.getResults().get(0).getScore()).isEqualTo(0.9);
        assertThat(insert.getInserted()).isEqualTo(2);
        assertThat(server.takeRequest().getPath()).isEqualTo("/datasets/abc");
        assertThat(server.takeRequest().getMethod()).isEqualTo("DELETE");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("query_text", "include_documents", "include_metadata", "\"rerank\":true");
        assertThat(server.takeRequest().getBody().readUtf8()).contains("vectors");
    }

    @Test void searchTextAliasSendsSingleQueryTextFieldForHybridDatasets() throws Exception {
        server.enqueue(json("{\"results\":[],\"dataset_id\":\"abc\"}"));

        client.datasets().search("abc", SearchRequest.searchText("rare zebra quokka", 3).includeMetadata(true).rerank(RerankConfig.enabled()));

        String body = server.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"query_text\":\"rare zebra quokka\"", "\"top_k\":3", "\"rerank\":{\"enabled\":true}");
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

    @Test void typedSourceValueObjectsExposeRequestFieldsAndValidation() {
        CreateSourceRequest raw = new CreateSourceRequest(
                "future",
                "custom",
                "desc",
                Map.of("endpoint", "https://example.com/feed"),
                Map.of("team", "docs")
        );
        assertThat(raw.toCreateSourceRequest()).isSameAs(raw);
        assertThat(raw.getSourceType()).isEqualTo("future");
        assertThat(raw.getName()).isEqualTo("custom");
        assertThat(raw.getDescription()).isEqualTo("desc");
        assertThat(raw.getConfig()).containsEntry("endpoint", "https://example.com/feed");
        assertThat(raw.getMetadata()).containsEntry("team", "docs");

        assertThat(CreateSourceRequest.fileUpload("ds").getName()).isEqualTo("file-upload-ds");
        assertThat(CreateSourceRequest.fileUpload("upload", "ds").getName()).isEqualTo("upload");
        assertThat(FileUploadSource.builder("upload", "ds")
                .storageProvider("gcs")
                .syncMode("incremental")
                .description("uploads")
                .config("path", "incoming/")
                .metadata("team", "docs")
                .metadata(Map.of("owner", "eng"))
                .build()
                .toCreateSourceRequest()
                .getConfig())
                .containsEntry("storage_provider", "gcs")
                .containsEntry("sync_mode", "incremental")
                .containsEntry("path", "incoming/");

        CreateSourceRequest web = WebSource.forUrl("https://docs.example.com/manual")
                .description("docs")
                .urls(List.of("https://docs.example.com/manual", "https://docs.example.com/api"))
                .syncMode("full")
                .metadata(Map.of("team", "docs"))
                .build()
                .toCreateSourceRequest();
        assertThat(web.getSourceType()).isEqualTo(SourceType.WEB);
        assertThat(web.getName()).isEqualTo("docs.example.com-manual");
        assertThat(web.getConfig()).containsKeys("url", "urls", "sync_mode");
        assertThat(web.getMetadata()).containsEntry("team", "docs");

        assertThat(S3Source.builder("bucket")
                .roleArn("arn:aws:iam::1:role/docs")
                .syncMode("incremental")
                .metadata("env", "test")
                .build()
                .toCreateSourceRequest()
                .getConfig())
                .containsEntry("bucket", "bucket")
                .containsEntry("role_arn", "arn:aws:iam::1:role/docs");
        assertThat(GoogleDriveSource.builder("drive")
                .fileIds(List.of("file-1"))
                .sharedDriveId("shared")
                .config("include_shared_drives", true)
                .build()
                .toCreateSourceRequest()
                .getConfig())
                .containsEntry("file_ids", List.of("file-1"))
                .containsEntry("shared_drive_id", "shared");

        assertThat(GCSSource.of("bucket").getSourceType()).isEqualTo(SourceType.GCS);
        assertThat(GCSSource.of("gcs-name", "bucket").toCreateSourceRequest().getConfig())
                .containsEntry("bucket", "bucket");
        assertThat(JiraSource.of("cloud").getSourceType()).isEqualTo(SourceType.JIRA);
        assertThat(JiraSource.of("jira-name", "cloud", List.of("ENG"))
                .toCreateSourceRequest()
                .getConfig())
                .containsEntry("cloud_id", "cloud")
                .containsEntry("project_keys", List.of("ENG"));

        assertThat(GenericSource.builder("future")
                .description("custom")
                .config(Map.of("a", 1))
                .metadata(Map.of("m", true))
                .build()
                .toCreateSourceRequest()
                .getName())
                .isEqualTo("future-source");
        assertThatThrownBy(() -> GenericSource.builder(" ", "x").build())
                .isInstanceOf(IllegalArgumentException.class);
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

    @Test void schedulesCrudAndTrigger() throws Exception {
        server.enqueue(json("{\"schedules\":[{\"id\":\"sch_1\",\"cron\":\"0 * * * *\",\"enabled\":true}],\"total\":1,\"limit\":10,\"offset\":0}"));
        server.enqueue(json("{\"id\":\"sch_1\",\"cron\":\"0 * * * *\",\"enabled\":true}"));
        server.enqueue(json("{\"id\":\"sch_2\",\"cron\":\"0 0 * * *\",\"enabled\":true}").setResponseCode(201));
        server.enqueue(json("{\"id\":\"sch_2\",\"cron\":\"0 0 * * *\",\"enabled\":false}"));
        server.enqueue(json("{\"deleted\":true}"));
        server.enqueue(json("{\"job_id\":\"job_42\"}").setResponseCode(202));

        Page<Schedule> page = client.schedules().list(10, 0);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getItems().get(0).getId()).isEqualTo("sch_1");
        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/schedules?limit=10&offset=0");

        Schedule one = client.schedules().get("sch_1");
        assertThat(one.getCron()).isEqualTo("0 * * * *");
        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/schedules/sch_1");

        Schedule created = client.schedules().create(CreateScheduleRequest.builder()
                .sourceId("src_1")
                .datasetId("ds_1")
                .cron("0 0 * * *")
                .timezone("UTC")
                .build());
        assertThat(created.getId()).isEqualTo("sch_2");
        RecordedRequest createReq = server.takeRequest();
        assertThat(createReq.getPath()).isEqualTo("/ingestion/schedules");
        String createBody = createReq.getBody().readUtf8();
        assertThat(createBody).contains("\"source_id\":\"src_1\"");
        assertThat(createBody).contains("\"dataset_id\":\"ds_1\"");
        assertThat(createBody).contains("\"cron\":\"0 0 * * *\"");
        assertThat(createBody).contains("\"timezone\":\"UTC\"");

        Schedule updated = client.schedules().update("sch_2", UpdateScheduleRequest.builder().enabled(false).build());
        assertThat(updated.isEnabled()).isFalse();
        RecordedRequest updateReq = server.takeRequest();
        assertThat(updateReq.getMethod()).isEqualTo("PATCH");
        assertThat(updateReq.getBody().readUtf8()).isEqualTo("{\"enabled\":false}");

        client.schedules().delete("sch_2");
        RecordedRequest deleteReq = server.takeRequest();
        assertThat(deleteReq.getMethod()).isEqualTo("DELETE");

        TriggerScheduleResponse trig = client.schedules().trigger("sch_1");
        assertThat(trig.getJobId()).isEqualTo("job_42");
        RecordedRequest trigReq = server.takeRequest();
        assertThat(trigReq.getPath()).isEqualTo("/ingestion/schedules/sch_1/trigger");
        assertThat(trigReq.getMethod()).isEqualTo("POST");
    }

    @Test void sourceManagementMethods() throws Exception {
        server.enqueue(json("{}")); // deleteSource(s1)
        server.enqueue(json("{}")); // deleteSource(s2, true)
        server.enqueue(json("{\"sources\":[{\"id\":\"u1\",\"name\":\"old\",\"type\":\"web\"}],\"total\":1,\"limit\":50,\"offset\":0}")); // listUnusedSources()
        server.enqueue(json("{\"sources\":[],\"total\":0,\"limit\":5,\"offset\":10}")); // listUnusedSources(5,10)
        server.enqueue(json("{\"deleted\":2,\"source_ids\":[\"u1\",\"u2\"]}")); // cleanup
        server.enqueue(json("{\"jobs\":[\"j1\"],\"schedules\":[]}")); // references
        server.enqueue(json("{\"valid\":true,\"warnings\":[],\"samples\":[]}")); // validate

        client.ingestion().deleteSource("s1");
        client.ingestion().deleteSource("s2", true);

        Page<Source> unused = client.ingestion().listUnusedSources();
        assertThat(unused.getItems()).hasSize(1);
        assertThat(unused.getItems().get(0).getId()).isEqualTo("u1");
        assertThat(unused.getTotal()).isEqualTo(1);

        Page<Source> unusedPage = client.ingestion().listUnusedSources(5, 10);
        assertThat(unusedPage.getItems()).isEmpty();
        assertThat(unusedPage.getOffset()).isEqualTo(10);

        JsonNode cleanup = client.ingestion().cleanupUnusedSources();
        assertThat(cleanup.get("deleted").asInt()).isEqualTo(2);

        JsonNode refs = client.ingestion().getSourceReferences("s1");
        assertThat(refs.get("jobs").get(0).asText()).isEqualTo("j1");

        JsonNode validation = client.ingestion().validateSource("web", Map.of("url", "https://example.com"));
        assertThat(validation.get("valid").asBoolean()).isTrue();

        RecordedRequest del1 = server.takeRequest();
        assertThat(del1.getMethod()).isEqualTo("DELETE");
        assertThat(del1.getPath()).isEqualTo("/ingestion/sources/s1");

        RecordedRequest del2 = server.takeRequest();
        assertThat(del2.getMethod()).isEqualTo("DELETE");
        assertThat(del2.getPath()).isEqualTo("/ingestion/sources/s2?force=true");

        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/sources/unused");
        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/sources/unused?limit=5&offset=10");

        RecordedRequest cleanupReq = server.takeRequest();
        assertThat(cleanupReq.getMethod()).isEqualTo("POST");
        assertThat(cleanupReq.getPath()).isEqualTo("/ingestion/sources/cleanup");

        assertThat(server.takeRequest().getPath()).isEqualTo("/ingestion/sources/s1/references");

        RecordedRequest validateReq = server.takeRequest();
        assertThat(validateReq.getMethod()).isEqualTo("POST");
        assertThat(validateReq.getPath()).isEqualTo("/ingestion/sources/validate");
        assertThat(validateReq.getBody().readUtf8())
                .contains("\"source_type\":\"web\"", "\"config\":{\"url\":\"https://example.com\"}");
    }

    @Test void connectionsClientCrud() throws Exception {
        server.enqueue(json("{\"connections\":[{\"id\":\"c1\",\"provider\":\"google\",\"status\":\"connected\",\"authorization_url\":null},{\"id\":\"c2\",\"provider\":\"atlassian\",\"status\":\"pending\"}]}"));
        server.enqueue(json("{\"connections\":[{\"id\":\"c1\",\"provider\":\"google\",\"status\":\"connected\"}]}"));
        server.enqueue(json("{\"id\":\"c3\",\"provider\":\"google\",\"status\":\"pending\",\"authorization_url\":\"https://auth.example.com/consent\"}").setResponseCode(201));
        server.enqueue(json("{\"id\":\"c4\",\"provider\":\"google\",\"status\":\"pending\"}").setResponseCode(201));
        server.enqueue(json("{\"id\":\"c1\",\"provider\":\"google\",\"status\":\"connected\"}"));
        server.enqueue(json("{}"));

        assertThat(client.connections()).isSameAs(client.connections());

        List<Connection> all = client.connections().list();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getId()).isEqualTo("c1");
        assertThat(all.get(0).getProvider()).isEqualTo("google");
        assertThat(all.get(0).getStatus()).isEqualTo("connected");

        List<Connection> filtered = client.connections().list("google");
        assertThat(filtered).hasSize(1);

        Connection created = client.connections().create("google");
        assertThat(created.getId()).isEqualTo("c3");
        assertThat(created.getAuthorizationUrl()).isEqualTo("https://auth.example.com/consent");

        Connection createdTyped = client.connections().create("google", "gdrive");
        assertThat(createdTyped.getId()).isEqualTo("c4");

        Connection one = client.connections().get("c1");
        assertThat(one.getStatus()).isEqualTo("connected");

        client.connections().delete("c1");

        assertThat(server.takeRequest().getPath()).isEqualTo("/connections");
        assertThat(server.takeRequest().getPath()).isEqualTo("/connections?provider=google");

        RecordedRequest createReq = server.takeRequest();
        assertThat(createReq.getMethod()).isEqualTo("POST");
        assertThat(createReq.getPath()).isEqualTo("/connections");
        assertThat(createReq.getHeader("X-API-Key")).isEqualTo("test-key");
        assertThat(createReq.getBody().readUtf8()).contains("\"provider\":\"google\"").doesNotContain("source_type");

        RecordedRequest createTypedReq = server.takeRequest();
        assertThat(createTypedReq.getBody().readUtf8()).contains("\"provider\":\"google\"", "\"source_type\":\"gdrive\"");

        assertThat(server.takeRequest().getPath()).isEqualTo("/connections/c1");

        RecordedRequest deleteReq = server.takeRequest();
        assertThat(deleteReq.getMethod()).isEqualTo("DELETE");
        assertThat(deleteReq.getPath()).isEqualTo("/connections/c1");
    }

    @Test void googleDriveAuthAndConnectionSerialization() throws Exception {
        Map<String, Object> saConfig = GoogleDriveSource.builder("drive")
                .folderId("folder-1")
                .serviceAccountJson("{\"type\":\"service_account\"}")
                .build()
                .toCreateSourceRequest()
                .getConfig();
        assertThat(saConfig)
                .containsEntry("auth_mode", "service_account")
                .containsEntry("service_account_json", "{\"type\":\"service_account\"}")
                .containsEntry("folder_id", "folder-1");

        Map<String, Object> oauthConfig = GoogleDriveSource.builder("drive")
                .oauth(Map.of("access_token", "tok", "refresh_token", "ref"))
                .build()
                .toCreateSourceRequest()
                .getConfig();
        assertThat(oauthConfig)
                .containsEntry("auth_mode", "oauth")
                .containsEntry("oauth_credentials", Map.of("access_token", "tok", "refresh_token", "ref"));

        assertThat(GoogleDriveSource.builder("drive").connection("conn-1").build()
                .toCreateSourceRequest().getConfig())
                .containsEntry("connection_id", "conn-1");

        // connection() also added to GCS, Confluence, and Jira sources.
        assertThat(GCSSource.builder("gcs").config("bucket", "b").connection("conn-gcs").build()
                .toCreateSourceRequest().getConfig())
                .containsEntry("connection_id", "conn-gcs");
        assertThat(ConfluenceSource.builder("conf").cloudId("c").connection("conn-conf").build()
                .toCreateSourceRequest().getConfig())
                .containsEntry("connection_id", "conn-conf");
        assertThat(JiraSource.builder("jira").config("cloud_id", "c").connection("conn-jira").build()
                .toCreateSourceRequest().getConfig())
                .containsEntry("connection_id", "conn-jira");

        // The auth/connection config serializes over the wire on create.
        server.enqueue(json("{\"id\":\"gd\",\"name\":\"drive\",\"type\":\"gdrive\"}"));
        client.ingestion().createGoogleDrive(GoogleDriveSource.builder("drive")
                .folderId("folder-1")
                .serviceAccountJson("{\"type\":\"service_account\"}")
                .connection("conn-1")
                .build());
        assertThat(server.takeRequest().getBody().readUtf8())
                .contains("\"source_type\":\"gdrive\"", "\"auth_mode\":\"service_account\"",
                        "\"connection_id\":\"conn-1\"", "\"folder_id\":\"folder-1\"");
    }

    @Test void apiErrorsThrowUsefulException() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("bad key"));
        assertThatThrownBy(() -> client.datasets().list()).isInstanceOf(VectorAmpApiException.class).hasMessageContaining("401").hasMessageContaining("bad key");
    }

    private static MockResponse json(String body) {
        return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
    }
}
