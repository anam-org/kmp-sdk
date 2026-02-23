# Releasing

## Releasing the SDK

1. Update `VERSION_NAME` in `gradle.properties` and commit.
2. Tag the release: `git tag sdk/v1.0.0 && git push origin sdk/v1.0.0`
3. GitHub Actions publishes to Maven Central and creates a GitHub Release.

## Releasing the Apps

1. Tag the release: `git tag app/v1.0.0 && git push origin app/v1.0.0`
2. GitHub Actions builds both iOS and Android, uploads to TestFlight and Google Play internal testing.
3. Build number is auto-generated from the workflow run number.

### First Google Play Release (One-Time Bootstrap)

Google Play's API requires at least one AAB to have been uploaded manually before Fastlane can upload via the API. On the very first release:

1. Ensure all Android secrets are configured (see [docs/RELEASE_SETUP.md](docs/RELEASE_SETUP.md)).
2. Push your first tag: `git tag app/v0.1.0 && git push origin app/v0.1.0`
3. The workflow will build the signed AAB and upload it as a **GitHub Actions artifact**, then fail at the "Upload to Google Play" step — this is expected.
4. Download the AAB artifact from the workflow run page (Actions > Release App > the run > Artifacts section).
5. Upload the AAB manually in [Google Play Console](https://play.google.com/console) > your app > Internal testing > Create new release.
6. All subsequent `app/v*` tags will upload to Google Play automatically via Fastlane.

See [docs/RELEASE_SETUP.md](docs/RELEASE_SETUP.md) for infrastructure setup instructions.
