# Changelog

All notable changes to this project will be documented in this file.

This project follows semantic versioning.

## [Unreleased]

### Changed

- **Breaking:** `AskRequest`'s dataset scope is now `datasetIds` (a `List<String>` serialized as
  `dataset_ids`) instead of the retired singular `dataset_id`. `POST /intelligence/query` answers
  any request carrying the old field with a 400 naming the replacement. `AskRequest.getDatasetId()`
  is replaced by `getDatasetIds()`.
- `AskRequest.datasetId(String)` now *adds* a dataset to the scope, so repeating it widens the
  scope. `allDatasets()` clears the scope rather than sending the retired `"all"` sentinel — an
  absent `dataset_ids` is how the API says "every dataset the caller can see".
- `Dataset.ask` / `Dataset.askStream` scope to the dataset's own id via `datasetIds`.
- `AskRequest` is now `@JsonInclude(NON_NULL)`, so unset options — `datasetIds` included — are
  omitted from the request body instead of being serialized as `null`. An empty or `"all"`-only
  scope is omitted rather than sent as `[]`.

### Added

- `AskRequest.datasetIds(List<String>)` and `AskRequest.datasetIds(String...)` for scoping one
  question to several datasets.

## [0.4.0] - 2026-08-20

### Added

- Add `GitHubSource` and `GitLabSource` typed source builders.
- Add `client.ingestion().createGitHub(...)` and `client.ingestion().createGitLab(...)` convenience methods.

## [0.3.0] - 2026-07-20

### Added

- Add typed metadata-schema fields when creating datasets.
- Add metadata-schema merge/patch and full replacement operations.
- Document create, merge, and replace schema workflows.

## [0.2.0] - 2026-07-14

### Added

- Add vector deletion helpers for dataset resources and `DatasetsClient`.
- Add organization secret helpers, including OpenAI API key storage helpers.
- Add OpenAI BYOM dataset creation helper and default OpenAI secret references.

## [0.1.0] - 2026-07-02

### Added

- Initial public-ready package baseline for VectorAmp SDK/CLI migration to GitHub.
- GitHub Actions CI workflow.
