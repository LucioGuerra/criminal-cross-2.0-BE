# Postman Assets

Import these two files in Postman:

1. Collection: `Criminal-Cross-2.0-Full.postman_collection.json`
2. Environment: `Criminal-Cross-2.0-Local.postman_environment.json`

After import, select the `Criminal-Cross-2.0 Local` environment and update:

- `auth_email`
- `auth_password`

Suggested run order (folder level):

1. `01 Auth`
2. `02 Users`
3. `03 Gym - Organizations`
4. `04 Gym - Headquarters`
5. `05 Gym - Activities`
6. `06 Gym - Activity Schedules`
7. `08 Gym - Sessions`
8. `07 Gym - Session Configuration` (can also run right after activities)
9. `10 Payments`
10. `11 Client Packages`
11. `09 Bookings`

Notes:

- Auth flow for frontend:
  - `POST /api/auth/register` and `POST /api/auth/login` now accept credentials payload (`email`, `password`).
  - Save `data.tokens.accessToken` as `bearer_token` and use `Authorization: Bearer <idToken>`.
  - Use `POST /api/auth/refresh` with `data.tokens.refreshToken` to rotate tokens.
  - Use `GET /api/auth/me` to retrieve Firebase identity enriched with local fields (`userId`, `roles`, `registered`, `active`).
- Deprecated endpoints in collection:
  - `POST /api/auth/verify-token` (returns `410 Gone`)
  - `POST /api/users/sync` (kept for backward compatibility, avoid in frontend)
- Protected endpoints require a valid bearer token (`bearer_token`) generated from register/login/refresh.
- Admin-protected routes (for example user role updates and payments) require a token with admin-level role.
- If your backend runs on a different host/port, update `base_url`.
