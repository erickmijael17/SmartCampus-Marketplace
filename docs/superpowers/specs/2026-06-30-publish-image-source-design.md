# Publish Image Source Design

## Goal

Allow the publish form to preview a product image from a web URL or a local file, persist web URLs as metadata, and upload local files through `media-ms`.

## Scope

- Keep every backend call behind `GatewayService` and existing API services.
- Do not add Gateway routes.
- Add `media-ms` upload support under the existing `/api/v1/media/**` Gateway route.
- Do not persist local file paths because browsers do not expose reusable local filesystem paths for security; persist a Gateway-served URL returned by `media-ms`.

## Behavior

- The user can paste an image URL using `http://` or `https://`.
- The URL is validated as `http://` or `https://`; many valid image CDN URLs do not expose a file extension.
- A valid web URL shows immediately in the publish form preview.
- When the user publishes, the web URL is sent to `media-ms` as `MediaFileRequest.url`.
- After restarting the app, catalog loading reads the stored media URL and shows it in the publication.
- The user can choose a local image file with the same accepted formats.
- A local image shows as a preview using an object URL.
- Publishing a local image uploads it to `media-ms` using `multipart/form-data`.
- `media-ms` stores the file, creates a metadata row, and returns a Gateway URL under `/api/v1/media/files/{storedName}`.
- All publication images render as a centered 1:1 crop with `object-fit: cover`.

## Architecture

- `PublishPageComponent` owns UI state for image source, local preview object URL, selected file, and validation messages.
- `MarketplaceService.createListing` continues to receive `imageUrl?: string` and persists web URLs.
- `MarketplaceService.uploadListingImage` uploads local files after the publication exists.
- `MediaApiService.upload` posts `FormData` to `/api/v1/media/upload`.
- Listing cards and detail views should already consume `MarketplaceListing.imageUrl`; CSS will enforce square centered rendering where needed.

## Error Handling

- Invalid URL scheme: show `La URL debe comenzar con http:// o https://.`
- Unsupported local file type: show the same accepted format message.
- Upload failure: keep the publish form on screen and show the existing backend error message.

## Testing

- Add component unit tests for URL validation, URL preview persistence payload, local file preview validation, and local upload call.
- Add `media-ms` service unit test for storing a multipart image and returning a public Gateway URL.
- Run frontend build and `media-ms` compile after implementation.
