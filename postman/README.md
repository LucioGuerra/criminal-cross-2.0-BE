# Postman Assets

Import these two files in Postman:

1. Collection: `Criminal-Cross-2.0-Full.postman_collection.json`
2. Environment: `Criminal-Cross-2.0-Local.postman_environment.json`

After import, select the `Criminal-Cross-2.0 Local` environment and update `id_token` with a valid token.

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

- Protected endpoints require a valid bearer token (`bearer_token`) generated from login/refresh.
- Admin-protected routes (for example user role updates and payments) require a token with admin-level role.
- If your backend runs on a different host/port, update `base_url`.
