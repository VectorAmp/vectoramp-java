# Changelog

All notable changes to this project will be documented in this file.

This project follows semantic versioning.

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
