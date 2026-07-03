# Publish Image Source Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add image URL preview and local image upload to the publish form while persisting image references through `media-ms`.

**Architecture:** Keep the existing `MarketplaceService.createListing` contract for web URLs. Add `MediaApiService.upload` and `MarketplaceService.uploadListingImage` for local files after a publication exists, then enforce 1:1 centered crops in the publish preview and existing listing images.

**Tech Stack:** Angular 20 standalone components, Reactive Forms, Jasmine/Karma, existing Gateway/media services.

## Global Constraints

- Frontend must call backend only through `GatewayService` and centralized API services.
- Existing `media-ms` JSON endpoint `/api/v1/media` remains unchanged.
- Accepted formats are `png`, `jpg`, `jpeg`, `webp`, and `gif`.
- Local image persistence uses `POST /api/v1/media/upload` and stores files under a configurable `media.storage.upload-dir`.
- No automatic commits.

---

### Task 1: Publish Image Validation And Payload

**Files:**
- Create: `frontend/src/app/pages/publish-page.component.spec.ts`
- Modify: `frontend/src/app/pages/publish-page.component.ts`
- Modify: `frontend/src/app/pages/publish-page.component.html`
- Modify: `frontend/src/app/pages/publish-page.component.css`

**Interfaces:**
- Consumes: `MarketplaceService.createListing(payload)`
- Produces: `imagePreviewUrl: string`, `imageSource: 'url' | 'local' | null`, `onImageUrlInput(): void`, `onLocalImageSelected(event: Event): void`, `clearSelectedImage(): void`

- [ ] Write failing tests for valid URL preview, invalid URL scheme, URL submit payload, and local upload after publish.
- [ ] Run `npm run test:ci -- --include src/app/pages/publish-page.component.spec.ts` from `frontend` and verify the tests fail because the behavior is missing.
- [ ] Implement image source state, validators, object URL cleanup, and local upload after publication creation.
- [ ] Update the template with URL input, file input, clear button, validation message, and square preview.
- [ ] Update component CSS for a restrained image picker and centered 1:1 preview.
- [ ] Run the targeted component tests and verify they pass.

### Task 2: Listing Image Crop Consistency

**Files:**
- Inspect/Modify: listing/card/detail CSS files that render `MarketplaceListing.imageUrl`

**Interfaces:**
- Consumes: `MarketplaceListing.imageUrl`
- Produces: square centered crop wherever publication images are shown.

- [ ] Locate listing image selectors with `rg -n "imageUrl|listing-image|product-image|object-fit" frontend/src/app`.
- [ ] Add or adjust CSS to use stable aspect ratio and `object-fit: cover`.
- [ ] Run `npm run build` from `frontend`.

### Task 3: Media Upload Backend

**Files:**
- Modify: `servicio/media-ms/src/main/java/com/upeu/media/controller/MediaFileController.java`
- Modify: `servicio/media-ms/src/main/java/com/upeu/media/service/MediaFileService.java`
- Modify: `servicio/media-ms/src/main/java/com/upeu/media/service/impl/MediaFileServiceImpl.java`
- Modify: `servicio/media-ms/src/main/java/com/upeu/media/entity/MediaFile.java`
- Modify: `infra/config/config-repo/media-ms-dev.yml`
- Modify: `infra/config/config-repo/media-ms-prod.yml`
- Modify: `servicio/media-ms/compose.yml`
- Test: `servicio/media-ms/src/test/java/com/upeu/media/service/impl/MediaFileServiceImplTest.java`

**Interfaces:**
- Produces: `POST /api/v1/media/upload`
- Produces: `GET /api/v1/media/files/{storedName}`

- [ ] Write failing service test for storing an uploaded image and returning a public URL.
- [ ] Implement upload storage, metadata persistence, and public file loading.
- [ ] Align the JPA entity with the existing Flyway table `archivos`.
- [ ] Configure upload directory and public Gateway base URL for dev/prod.
- [ ] Run `mvn -f servicio/media-ms/pom.xml -Dtest=MediaFileServiceImplTest test`.
- [ ] Run `mvn -f servicio/media-ms/pom.xml clean compile`.

### Task 4: Final Verification

**Files:**
- No production edits expected.

- [ ] Run `git status --short`.
- [ ] Report reviewed files, modified files, commands, build/test result, functionality state, risks, and next recommended phase.
