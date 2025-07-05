# PlumaSphere-Backend

PlumaSphere-Backend is a modern blog/content community backend built with Spring Boot, supporting users, posts, comments, tags, likes, file uploads, WebSocket real-time push notifications, permissions, and risk control features.

## Project Overview
- User authentication (login, anonymous identity generation), profile modification, avatar upload
- Post publishing, retrieval, tag categorization, pagination, search
- Comments, replies, comment pagination, comment likes
- Admin panel (user/IP banning, system initialization, etc.)
- WebSocket real-time push notifications (new comments, likes, etc.)
- JWT authentication, API rate limiting, CORS configuration

## Tech Stack
- Java 21
- Spring Boot 3.5.3
- Spring Security, OAuth2, JWT
- Spring Data JPA, PostgreSQL
- Spring Data Redis
- WebSocket
- MapStruct, Lombok
- Resilience4j (Rate Limiting)
- Docker Compose (Development Dependencies)

## Prerequisites
- JDK 21 or higher
- Docker & Docker Compose

## Quick Start
1. Clone the project

```bash
git clone <project-url>
cd PlumaSphere_Backend
```

2. Start the backend service (Docker Compose services will be started automatically)

```bash
./gradlew bootRun
```

3. Default service addresses:
- Backend API: http://localhost:8080
- WebSocket: ws://localhost:8080/ws

## Project Structure
```
PlumaSphere_Backend/
├── src/main/java/fans/goldenglow/plumaspherebackend/
│   ├── controller/      # REST API controllers
│   ├── service/         # Business logic layer
│   ├── entity/          # Entity classes
│   ├── repository/      # JPA data access
│   ├── dto/             # Data transfer objects
│   ├── config/          # Configuration classes
│   ├── handler/         # WebSocket handlers
│   └── ...
├── src/main/resources/
│   ├── application.yml  # Main configuration file
│   └── ...
├── compose.yaml         # One-click dependency services
├── build.gradle         # Build script
└── ...
```

## Main API Capabilities
- **User Management**: Login, anonymous identity generation (rate-limited), get/modify profile, avatar upload, paginated user list, user ban/unban
- **Post Management**: Create posts, paginated/search/tag-filtered retrieval, post details, edit/delete posts
- **Comment System**: Create comments/replies, paginated retrieval, delete comments, comment likes
- **Tag System**: Get all tags, add new tags
- **File Management**: Multi-file upload, external image fetching
- **Like System**: Post/comment likes, unlike, get like count/status
- **System Management**: System initialization (creates first admin), get/set system configurations, system status
- **Admin Panel**: User/IP ban and unban, paginated ban lists, mark/unmark IP ban
- **Authentication**: JWT login, token refresh, anonymous identity acquisition

> For detailed API documentation, please refer to the controller code or supplement with API documentation.

## User System
The system supports three ways to create users:
1. **System Initialization**: Creates the first admin user via `/api/v1/init`
2. **Anonymous Identity**: Automatically generates random users via `/api/v1/get-identity` (rate-limited)
3. **Login**: Authenticates existing users via `/api/v1/login`

## WebSocket Real-time Push
- Endpoint: `ws://localhost:8080/ws`
- Supports real-time message subscription by post (postId) or comment (commentId)
- Connection examples:
  - Subscribe to post: `ws://localhost:8080/ws?postId=123`
  - Subscribe to comment: `ws://localhost:8080/ws?commentId=456`
- Message types: New comments, likes, etc. See `WebSocketMessageType` for details

## Testing
- Unit/Integration tests:

```bash
./gradlew test
```
- Test environment uses H2 in-memory database and local Redis (port 6370), see `src/main/resources/application-test.yml` for details

## Troubleshooting
- Port conflicts: Ensure ports 5432 (PostgreSQL), 6379 (Redis), 8080 (Backend) are not occupied
- Database/Redis connection failures: Check if Docker Compose services are running properly
- WebSocket connection issues: Check CORS allowed domains and ports for frontend

## License
[GNU Affero General Public License v3.0 (AGPL-3.0)](LICENSE) 
