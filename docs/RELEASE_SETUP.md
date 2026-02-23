# Release Infrastructure Setup

This document describes the one-time setup required to enable automated app releases via GitHub Actions.

## Apple Developer Setup

### Step 1: Register App ID

1. Apple Developer Portal > Certificates, Identifiers & Profiles > Identifiers
2. Register new App ID: `ai.anam.lab.client` (Explicit)

### Step 2: Create App Store Connect API Key

1. App Store Connect > Users and Access > Integrations > App Store Connect API
2. Generate key with **App Manager** role, name: `CI/CD Upload Key`
3. Download `.p8` file immediately (one-time only)
4. Record: Key ID -> `IOS_ASC_KEY_ID`, Issuer ID -> `IOS_ASC_ISSUER_ID`
5. Contents of `.p8` file -> `IOS_ASC_API_KEY` (the raw text content, not base64)

### Step 3: Create App in App Store Connect

1. New App: name "Anam", bundle ID `ai.anam.lab.client`, platform iOS

### Step 4: Set Up Fastlane Match (Certificate Repo)

1. Create a **private** Git repository (e.g., `github.com/anam-ai/ios-certificates`)
2. Generate an SSH deploy key for the repo:
   ```bash
   ssh-keygen -t ed25519 -C "fastlane-match" -f match_deploy_key
   ```
3. Add the **public** key (`match_deploy_key.pub`) as a deploy key on the cert repo (with write access)
4. The **private** key content becomes `MATCH_GIT_PRIVATE_KEY`
5. The repo SSH URL (e.g., `git@github.com:anam-ai/ios-certificates.git`) becomes `MATCH_GIT_URL`
6. On a Mac with Fastlane installed, from the project root, run:
   ```bash
   MATCH_GIT_URL="git@github.com:anam-ai/ios-certificates.git" \
   bundle exec fastlane match appstore
   ```
7. When prompted, enter a passphrase to encrypt the certificates — record this as `MATCH_PASSWORD`
8. Match will create the distribution certificate and provisioning profile automatically

## Google Play Setup

### Step 1: Create App in Google Play Console

1. Create app: name "Anam", package `ai.anam.lab.client`
2. Complete the setup checklist (privacy policy, content rating, etc.)

### Step 2: Create Service Account

1. Play Console > Settings > API access > Create new service account
2. In Google Cloud Console: create service account `github-actions-play-upload`
3. Create JSON key -> entire file contents become `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`
4. Back in Play Console: grant the service account **"Release to testing tracks"** permission for the app

### Step 3: Generate Upload Keystore

```bash
keytool -genkeypair \
  -alias anam-upload \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -keystore anam-upload.keystore \
  -storepass <CHOOSE_STRONG_PASSWORD> \
  -keypass <CHOOSE_STRONG_PASSWORD> \
  -dname "CN=Anam AI, OU=Engineering, O=Anam AI, L=London, ST=England, C=GB"
```

- Base64-encode: `base64 -i anam-upload.keystore | pbcopy` -> `ANDROID_KEYSTORE_BASE64`
- Store password -> `ANDROID_KEYSTORE_PASSWORD`
- Key alias (`anam-upload`) -> `ANDROID_KEY_ALIAS`
- Key password -> `ANDROID_KEY_PASSWORD`

### Step 4: Register Upload Key with Google Play

- On first upload: do the first AAB upload manually via Play Console (API requires at least one prior upload on the track)
- Google Play App Signing will be enabled automatically

## GitHub Secrets Summary

Add these secrets to the repository settings (Settings > Secrets and variables > Actions).

### iOS (6 secrets)

| Secret | Value |
|--------|-------|
| `IOS_ASC_KEY_ID` | App Store Connect API Key ID |
| `IOS_ASC_ISSUER_ID` | App Store Connect API Issuer ID |
| `IOS_ASC_API_KEY` | Contents of the `.p8` API key file |
| `MATCH_GIT_URL` | SSH URL of the certificates repo |
| `MATCH_GIT_PRIVATE_KEY` | SSH private key for certificates repo |
| `MATCH_PASSWORD` | Encryption passphrase for match |

### Android (5 secrets)

| Secret | Value |
|--------|-------|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded `.keystore` file |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias (e.g., `anam-upload`) |
| `ANDROID_KEY_PASSWORD` | Key password |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Service account JSON file contents |

## Known Considerations

- **First Google Play upload**: The very first AAB must be uploaded manually via Play Console. API/Fastlane uploads work for subsequent releases.
- **iOS build time**: Kotlin/Native compilation + Xcode archive can take 30-60 min. Timeout set to 90 min.
- **Xcode version pinning**: If `macos-latest` has an incompatible Xcode, add `maxim-lobanov/setup-xcode@v1` to the workflow.
- **Match cert renewal**: When the distribution certificate expires (annually), run `fastlane match nuke appstore` then `fastlane match appstore` to regenerate.
