# Auth Module

Module responsible for authentication and authorization using Firebase and custom JWT tokens.

## Overview

The Auth module implements a **hybrid authentication system**:
- **Firebase** for initial user authentication (email/password, Google, etc.)
- **Backend-managed JWT** for API access with full session control
- **Refresh tokens** stored in PostgreSQL for long-term sessions

This architecture (Option B) provides complete control over sessions, instant logout, and detailed audit capabilities.

## Responsibilities

- Firebase ID token validation for initial authentication
- Custom JWT generation and validation (RSA-256 signed)
- Refresh token management with rotation
- User session tracking and revocation
- Role-based access control
- Integration with Users module for local user data

## Architecture

```
auth/
├── application/
│   ├── ports/               # Interfaces (TokenValidator, UserProvider, RefreshTokenStore)
│   ├── service/             # AuthService (orchestration)
│   └── usecase/             # Use cases (Login, Refresh, Logout, etc.)
├── domain/
│   ├── model/               # AuthenticatedUser, AuthProvider, DecodedToken, RefreshToken
│   └── exception/           # Auth-specific exceptions
└── infrastructure/
    ├── adapter/             # Adapters (UserModuleAdapter, RefreshTokenStoreAdapter)
    ├── config/              # FirebaseConfig
    ├── controller/          # AuthResource (REST endpoints)
    ├── dto/                 # Request/Response DTOs
    ├── entity/              # JPA entities (RefreshTokenEntity)
    ├── mapper/              # MapStruct mappers
    ├── repository/          # Panache repositories
    └── security/            # Filters, validators, token generators, annotations
```

## Configuration

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `FIREBASE_CREDENTIALS_PATH` | Path to Firebase service account JSON file | Yes (unless mock mode) |
| `FIREBASE_PROJECT_ID` | Firebase project ID (usually auto-detected) | No |
| `FIREBASE_MOCK_ENABLED` | Enable mock mode for development | No (default: false) |

### application.properties

```properties
# Firebase Configuration
firebase.credentials.path=${FIREBASE_CREDENTIALS_PATH:src/main/resources/athlium-credentials.json}
firebase.project-id=${FIREBASE_PROJECT_ID:}
firebase.mock.enabled=${FIREBASE_MOCK_ENABLED:false}

# Token Configuration
auth.access-token.expiration-minutes=15
auth.refresh-token.expiration-days=30

# SmallRye JWT Configuration (for custom JWT generation)
mp.jwt.verify.issuer=https://athlium.com
smallrye.jwt.sign.key.location=/privateKey.pem
mp.jwt.verify.publickey.location=/publicKey.pem
```

## Security Keys

The module uses RSA keypair for JWT signing:
- `privateKey.pem` - Private key for signing JWTs (2048-bit RSA)
- `publicKey.pem` - Public key for validating JWTs

⚠️ **Production**: Generate new keys and store them securely. Never commit private keys to version control.

## Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Go to Project Settings > Service accounts
4. Click "Generate new private key"
5. Save the JSON file securely
6. Set the `FIREBASE_CREDENTIALS_PATH` environment variable to point to this file

### Example:

```bash
export FIREBASE_CREDENTIALS_PATH=/path/to/serviceAccountKey.json
```

## API Endpoints

### Public Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/auth/health` | Health check for auth module |
| POST | `/api/auth/verify-token` | Verify a Firebase ID token |
| POST | `/api/auth/register` | Register user in local database |
| POST | `/api/auth/login` | Login and get JWT + refresh token |
| POST | `/api/auth/refresh` | Exchange refresh token for new tokens |
| POST | `/api/auth/logout` | Revoke refresh token(s) |

### Protected Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/auth/me` | Get current authenticated user |

## Usage

### Verifying a Token

```bash
curl -X POST http://localhost:8080/api/auth/verify-token \
  -H "Content-Type: application/json" \
  -d '{"idToken": "your-firebase-id-token"}'
```

Response (user not registered locally):
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "user": {
      "firebaseUid": "abc123",
      "email": "user@example.com",
      "name": null,
      "emailVerified": false,
      "provider": "EMAIL",
      "userId": null,
      "roles": [],
      "registered": false,
      "active": false
    },
    "needsRegistration": true,
    "message": "Token verified - user needs registration"
  }
}
```

Response (user registered):
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "user": {
      "firebaseUid": "abc123",
      "email": "user@example.com",
      "name": "John Doe",
      "emailVerified": true,
      "provider": "GOOGLE",
      "userId": 1,
      "roles": ["CLIENT"],
      "registered": true,
      "active": true
    },
    "needsRegistration": false,
    "message": "Token verified successfully"
  }
}
```

### Registering a User

After verifying a token returns `needsRegistration: true`, complete the profile:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "your-firebase-id-token",
    "name": "John",
    "lastName": "Doe"
  }'
```

Response:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user": {
      "firebaseUid": "abc123",
      "email": "user@example.com",
      "name": "John",
      "emailVerified": false,
      "provider": "EMAIL",
      "userId": 1,
      "roles": ["CLIENT"],
      "registered": true,
      "active": true
    },
    "needsRegistration": false,
    "message": "User registered successfully"
  }
}
```

### Protecting Endpoints

Use the `@Authenticated` annotation to protect endpoints:

```java
@Path("/api/protected")
@Authenticated  // All endpoints require authentication
public class ProtectedResource {

    @GET
    @Path("/data")
    public Response getData() {
        // Only authenticated users can access
    }

    @GET
    @Path("/admin")
    @Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})
    public Response adminOnly() {
        // Only users with SUPERADMIN or ORG_ADMIN role
    }

    @GET
    @Path("/public")
    @PublicEndpoint
    public Response publicData() {
        // This endpoint is public despite class-level @Authenticated
    }
}
```

### Accessing Current User

Inject `SecurityContext` to access the authenticated user:

```java
@Inject
SecurityContext securityContext;

public void someMethod() {
    if (securityContext.isAuthenticated()) {
        AuthenticatedUser user = securityContext.getCurrentUser();
        String firebaseUid = user.getFirebaseUid();
        Set<Role> roles = user.getRoles();
    }
}
```

## Mock Mode

For development without Firebase credentials, enable mock mode:

```properties
firebase.mock.enabled=true
```

In mock mode:
- Any token is accepted
- A mock user is returned with UID "mock-{token}"
- Email: mock-user@example.com
- Provider: EMAIL

⚠️ **WARNING**: Never enable mock mode in production!

## Authentication Flow (Option B - Backend JWT)

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   Frontend  │       │   Backend   │       │   Firebase  │
└──────┬──────┘       └──────┬──────┘       └──────┬──────┘
       │                     │                     │
       │ 1. Login with       │                     │
       │    Firebase SDK     │                     │
       │────────────────────────────────────────-->│
       │                     │                     │
       │ 2. Firebase Token   │                     │
       │<──────────────────────────────────────────│
       │                     │                     │
       │ 3. POST /login      │                     │
       │    {idToken}        │                     │
       │────────────────────>│                     │
       │                     │                     │
       │                     │ 4. Verify token     │
       │                     │────────────────────>│
       │                     │<────────────────────│
       │                     │                     │
       │                     │ 5. Generate JWT     │
       │                     │    + Refresh Token  │
       │                     │    (PostgreSQL)     │
       │                     │                     │
       │ 6. JWT + Refresh    │                     │
       │<────────────────────│                     │
       │                     │                     │
       │ 7. API calls with   │                     │
       │    custom JWT       │                     │
       │────────────────────>│                     │
       │                     │                     │
       │ 8. Validate JWT     │                     │
       │    (no Firebase)    │                     │
       │                     │                     │
       │ 9. Response         │                     │
       │<────────────────────│                     │
       │                     │                     │
       │ 10. POST /refresh   │                     │
       │     {refreshToken}  │                     │
       │────────────────────>│                     │
       │                     │                     │
       │ 11. New JWT +       │                     │
       │     Refresh Token   │                     │
       │<────────────────────│                     │
```

**Key Benefits:**
- ✅ Firebase only for initial authentication
- ✅ Backend has full control over sessions
- ✅ Instant logout/revocation
- ✅ Session tracking and audit
- ✅ No Firebase calls for API requests
- ✅ Custom JWT with backend-defined claims

## Frontend Integration

Firebase handles initial authentication (email/password, Google, Facebook, etc.).
After Firebase authentication, exchange the token for backend JWT and refresh tokens.

### JavaScript/TypeScript Example (Updated for Option B)

```typescript
import { 
  getAuth, 
  createUserWithEmailAndPassword, 
  signInWithEmailAndPassword,
  signInWithPopup,
  GoogleAuthProvider 
} from 'firebase/auth';

const auth = getAuth();
const API_URL = 'http://localhost:8080/api/auth';

// ============================================
// SIGN UP (Create account in Firebase)
// ============================================
async function signUp(email: string, password: string, name: string, lastName: string) {
  // 1. Create account in Firebase (handles password securely)
  const userCredential = await createUserWithEmailAndPassword(auth, email, password);
  const idToken = await userCredential.user.getIdToken();

  // 2. Register in your backend (creates local profile)
  const response = await fetch(`${API_URL}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken, name, lastName })
  });

  return response.json();
}

// ============================================
// LOGIN (Get backend JWT + refresh token)
// ============================================
async function login(email: string, password: string) {
  // 1. Authenticate with Firebase
  const userCredential = await signInWithEmailAndPassword(auth, email, password);
  const idToken = await userCredential.user.getIdToken();

  // 2. Login to backend (exchange for JWT + refresh token)
  const response = await fetch(`${API_URL}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken })
  });

  const data = await response.json();
  
  // 3. Check if user needs registration
  if (data.data.needsRegistration) {
    return { needsRegistration: true, idToken };
  }

  // 4. Store tokens securely
  if (data.data.tokens) {
    localStorage.setItem('accessToken', data.data.tokens.accessToken);
    localStorage.setItem('refreshToken', data.data.tokens.refreshToken);
  }

  return data;
}

// ============================================
// REFRESH TOKENS (when JWT expires)
// ============================================
async function refreshTokens() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) throw new Error('No refresh token');

  const response = await fetch(`${API_URL}/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });

  const data = await response.json();
  
  if (data.success && data.data.tokens) {
    // Update stored tokens (token rotation)
    localStorage.setItem('accessToken', data.data.tokens.accessToken);
    localStorage.setItem('refreshToken', data.data.tokens.refreshToken);
  }

  return data;
}

// ============================================
// AUTHENTICATED API CALLS (with backend JWT)
// ============================================
async function fetchProtectedData(endpoint: string, options: RequestInit = {}) {
  let accessToken = localStorage.getItem('accessToken');
  
  if (!accessToken) {
    throw new Error('Not authenticated');
  }

  // Try request with current token
  let response = await fetch(endpoint, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    }
  });

  // If token expired, refresh and retry
  if (response.status === 401) {
    await refreshTokens();
    accessToken = localStorage.getItem('accessToken');
    
    response = await fetch(endpoint, {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      }
    });
  }

  return response;
}

// Example usage
async function getMyProfile() {
  const response = await fetchProtectedData(`${API_URL}/me`);
  return response.json();
}

// ============================================
// LOGOUT
// ============================================
async function logout() {
  const refreshToken = localStorage.getItem('refreshToken');
  
  if (refreshToken) {
    await fetch(`${API_URL}/logout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
  }

  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  await auth.signOut(); // Firebase sign out
}

async function logoutAllDevices() {
  await fetch(`${API_URL}/logout`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ logoutAll: true })
  });

  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  await auth.signOut();
}
```

### Key Concepts

| Concept | Firebase | Your Backend |
|---------|----------|--------------|
| Store passwords | ✅ Yes | ❌ Never |
| Create accounts | ✅ `createUserWithEmailAndPassword()` | ❌ No |
| Validate credentials | ✅ `signInWithEmailAndPassword()` | ❌ No |
| Issue initial tokens | ✅ `getIdToken()` | ❌ No |
| Validate Firebase tokens | ❌ No | ✅ `verifyIdToken()` |
| Issue API tokens (JWT) | ❌ No | ✅ Custom JWT (RSA-256) |
| Validate API tokens | ❌ No | ✅ JWT signature verification |
| Store user profile | ❌ Basic only | ✅ Full profile + roles |
| Manage roles | ❌ Optional claims | ✅ PostgreSQL |
| Session management | ❌ No | ✅ Refresh tokens + rotation |
| Instant logout | ❌ No | ✅ Token revocation |

## Complete API Documentation

### 1. Register User

**Endpoint:** `POST /api/auth/register`  
**Access:** Public

Creates a new user in the local database after Firebase authentication.

**Request:**
```json
{
  "idToken": "firebase-id-token...",
  "name": "Ignacio",
  "lastName": "Lances"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user": {
      "firebaseUid": "Uc8fLm9pZbev6PZcIY1xBQ1twC52",
      "email": "user@example.com",
      "name": "Ignacio",
      "emailVerified": false,
      "provider": "EMAIL",
      "userId": 1,
      "roles": ["CLIENT"],
      "registered": true,
      "active": true
    },
    "needsRegistration": false,
    "message": "User registered successfully"
  }
}
```

### 2. Login

**Endpoint:** `POST /api/auth/login`  
**Access:** Public

Authenticates user and returns custom JWT + refresh token.

**Request:**
```json
{
  "idToken": "firebase-id-token..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "user": {
      "firebaseUid": "Uc8fLm9pZbev6PZcIY1xBQ1twC52",
      "email": "user@example.com",
      "name": "Ignacio",
      "emailVerified": false,
      "provider": "EMAIL",
      "userId": 1,
      "roles": ["CLIENT"],
      "registered": true,
      "active": true
    },
    "tokens": {
      "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...",
      "refreshToken": "s5Dt0ilDDe4I_-rLniyIiXmlUZ_vCJ...",
      "expiresIn": 900,
      "tokenType": "Bearer"
    },
    "message": "Login successful"
  }
}
```

**Response if needs registration (200 OK):**
```json
{
  "success": true,
  "data": {
    "user": {
      "firebaseUid": "Uc8fLm9pZbev6PZcIY1xBQ1twC52",
      "email": "user@example.com",
      "name": null,
      "userId": null,
      "roles": null,
      "registered": false,
      "active": false
    },
    "needsRegistration": true,
    "message": "User needs to complete registration"
  }
}
```

### 3. Refresh Tokens

**Endpoint:** `POST /api/auth/refresh`  
**Access:** Public

Exchanges refresh token for new JWT + refresh token (token rotation).

**Request:**
```json
{
  "refreshToken": "s5Dt0ilDDe4I_-rLniyIiXmlUZ_vCJ..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "user": {
      "firebaseUid": "Uc8fLm9pZbev6PZcIY1xBQ1twC52",
      "email": "user@example.com",
      "name": "Ignacio",
      "emailVerified": true,
      "provider": null,
      "userId": 1,
      "roles": ["CLIENT"],
      "registered": true,
      "active": true
    },
    "tokens": {
      "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...",
      "refreshToken": "HKFZicv6Bn7M0pne3LeSEPEfb6cWHAGI...",
      "expiresIn": 900,
      "tokenType": "Bearer"
    },
    "message": "Token refreshed successfully"
  }
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid, expired, or revoked refresh token
- `403 Forbidden` - User account is deactivated

### 4. Logout

**Endpoint:** `POST /api/auth/logout`  
**Access:** Public

Revokes refresh token(s).

**Request (logout current device):**
```json
{
  "refreshToken": "s5Dt0ilDDe4I_-rLniyIiXmlUZ_vCJ..."
}
```

**Request (logout all devices):**
```json
{
  "logoutAll": true
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": {
    "tokensRevoked": 1,
    "message": "Logged out successfully"
  }
}
```

### 5. Get Current User

**Endpoint:** `GET /api/auth/me`  
**Access:** Protected (requires JWT in Authorization header)

Returns authenticated user information.

**Request:**
```
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "user": {
      "firebaseUid": "Uc8fLm9pZbev6PZcIY1xBQ1twC52",
      "email": "user@example.com",
      "name": "Ignacio",
      "emailVerified": true,
      "provider": null,
      "userId": 1,
      "roles": ["CLIENT"],
      "registered": true,
      "active": true
    },
    "needsRegistration": false,
    "message": "Current user retrieved successfully"
  }
}
```

**Error Response:**
- `401 Unauthorized` - Invalid, expired JWT or missing Authorization header

## Supported Auth Providers

- Email/Password
- Google
- Facebook

More providers can be added by extending the `AuthProvider` enum.

## Token Architecture Details

### Custom JWT Structure

**Header:**
```json
{
  "typ": "JWT",
  "alg": "RS256"
}
```

**Payload:**
```json
{
  "iss": "https://athlium.com",
  "upn": "user@example.com",
  "sub": "Uc8fLm9pZbev6PZcIY1xBQ1twC52",
  "userId": 1,
  "email": "user@example.com",
  "firebaseUid": "Uc8fLm9pZbev6PZcIY1xBQ1twC52",
  "groups": ["CLIENT"],
  "iat": 1769915326,
  "exp": 1769916226,
  "jti": "6f3fe026-abae-4117-9406-61c35c7fe13b"
}
```

**Claims:**
- `iss` - Issuer (backend identifier)
- `upn` - User Principal Name (email)
- `sub` - Subject (Firebase UID)
- `userId` - Internal user ID
- `groups` - User roles
- `iat` - Issued at
- `exp` - Expiration (15 minutes)
- `jti` - JWT ID (unique)

### Refresh Token

- **Format:** Secure random 64-byte base64url string
- **Storage:** PostgreSQL with metadata
- **Expiration:** 30 days (configurable)
- **Rotation:** New token on each refresh, old token revoked
- **Metadata:** Device info, IP address, timestamps

### Token Validation Flow

```
1. Extract Authorization header
2. Check issuer to determine token type:
   - "https://athlium.com" → Backend JWT (validate with public key)
   - "https://securetoken.google.com/..." → Firebase token (validate with Firebase)
3. Verify signature
4. Check expiration
5. Extract user claims
6. Enrich with fresh database data
7. Populate SecurityContext
```

## Security Features

### Refresh Token Protection

- ✅ **Token Rotation**: Old token invalidated on each refresh
- ✅ **Reuse Detection**: If revoked token is used, all user tokens revoked
- ✅ **Device Tracking**: Store device info and IP per session
- ✅ **Expiration**: Configurable TTL (default 30 days)
- ✅ **Revocation**: Instant logout via token revocation

### JWT Security

- ✅ **RSA-256 Signing**: Industry-standard asymmetric encryption
- ✅ **Short Expiration**: 15-minute lifetime limits exposure
- ✅ **Signature Verification**: Every request validated
- ✅ **Issuer Validation**: Ensures token from correct source
- ✅ **Stateless Validation**: No database lookup per request

### Database Schema

The `refresh_tokens` table is automatically created:

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    firebase_uid VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP
);

CREATE INDEX idx_refresh_token_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_firebase_uid ON refresh_tokens(firebase_uid);
CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);
```

## Testing

### Complete Flow Test

```bash
# 1. Get Firebase token
FIREBASE_TOKEN=$(curl -s -X POST \
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123", "returnSecureToken": true}' \
  | jq -r '.idToken')

# 2. Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"idToken\": \"$FIREBASE_TOKEN\", \"name\": \"John\", \"lastName\": \"Doe\"}"

# 3. Login (get JWT + refresh token)
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"idToken\": \"$FIREBASE_TOKEN\"}")

ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.data.tokens.accessToken')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.data.tokens.refreshToken')

# 4. Access protected endpoint
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# 5. Refresh tokens
NEW_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")

NEW_ACCESS_TOKEN=$(echo $NEW_RESPONSE | jq -r '.data.tokens.accessToken')
NEW_REFRESH_TOKEN=$(echo $NEW_RESPONSE | jq -r '.data.tokens.refreshToken')

# 6. Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$NEW_REFRESH_TOKEN\"}"
```

## Troubleshooting

### Common Issues

**JWT validation fails:**
- Verify RSA keys exist: `privateKey.pem` and `publicKey.pem`
- Check issuer matches: `mp.jwt.verify.issuer` in properties
- Ensure keys are in `src/main/resources/`

**Refresh token not working:**
- Check PostgreSQL connection
- Verify `refresh_tokens` table exists
- Check token expiration date

**Firebase authentication fails:**
- Verify `athlium-credentials.json` path is correct
- Check Firebase project ID
- Ensure service account has proper permissions

## Production Deployment

### Security Checklist

- [ ] Generate new RSA keypair (2048-bit or higher)
- [ ] Store private key securely (e.g., secrets manager)
- [ ] Set `quarkus.hibernate-orm.database.generation=none`
- [ ] Enable Flyway: `quarkus.flyway.migrate-at-start=true`
- [ ] Use environment variables for sensitive data
- [ ] Configure HTTPS only
- [ ] Set secure CORS policy
- [ ] Enable rate limiting
- [ ] Monitor failed authentication attempts
- [ ] Set up log aggregation for security events

### Environment Variables

```bash
export FIREBASE_CREDENTIALS_PATH=/secure/path/to/credentials.json
export JWT_PRIVATE_KEY_PATH=/secure/path/to/privateKey.pem
export JWT_PUBLIC_KEY_PATH=/secure/path/to/publicKey.pem
export AUTH_JWT_ISSUER=https://api.yourcompany.com
```

## Future Enhancements (Phase 3)

- Multi-provider linking (link Google account to email account)
- OAuth2 provider support (GitHub, Microsoft)
- Two-factor authentication (2FA)
- Account recovery flows
- Passwordless authentication (magic links)