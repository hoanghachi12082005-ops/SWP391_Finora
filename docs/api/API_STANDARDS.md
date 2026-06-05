# API Standards

## Current API Status

No `api` source package is currently present in `src/java`. This document defines standards for future JSON endpoints if the project adds them.

## Future API Architecture

When JSON endpoints are introduced:

- API requests should live under `/api/*`.
- API routing should be centralized in a controller/dispatcher rather than duplicated across many servlets.
- API action classes should be small and should delegate persistence to DAOs or future services.
- API response DTOs should live under `api.dto` or `dto` depending on the final package decision documented at implementation time.

## API Response Shape

Use a consistent response envelope for standard responses:

- `status`: numeric status code.
- `message`: human-readable message.
- `data`: response payload.

## Route Rules

- Use plural nouns for collections, for example `/api/products`.
- Register routes deterministically in the chosen API router.
- Document every new route in this file.
- Keep servlet context path handling compatible with `/SWP391_Finora`.

## Parameter Rules

- Validate query parameters before calling DAOs.
- Clamp pagination limits to safe maximums.
- Default `page` to `1` when omitted or invalid.
- Default `limit` to `10` unless the endpoint documents otherwise.

## Error Rules

- Return consistent JSON error responses.
- Do not expose stack traces in response bodies.
- Use appropriate HTTP status codes on the servlet response.
- Log errors safely without sensitive request data.

## Current API Debt

- API standards are forward-looking and must be reconciled with source when the first actual API package is implemented.
- Content type should be normalized to `application/json;charset=UTF-8` for JSON responses.
