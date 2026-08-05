# Database Architecture & Connection Management

## Overview
Creative AI utilizes **PostgreSQL** as its core relational data store, accessed via **SQLAlchemy 2.0 Async ORM** and **asyncpg** driver inside the FastAPI backend service.

## Architecture Highlights

1. **Storage Decoupling Pattern**:
   - Audio files and generated images are **NEVER** stored directly as binary BLOBs inside PostgreSQL.
   - Images and audio media files are stored in object storage (AWS S3 / Google Cloud Storage / Local volume).
   - Only media metadata (`prompt`, `style_preset`, `aspect_ratio`, `tempo_bpm`, `storage_key`) and signed access URLs (`image_url`, `audio_url`) are persisted in PostgreSQL.

2. **Connection Pooling Strategy**:
   - `pool_size`: 10 persistent connections per backend instance.
   - `max_overflow`: 20 temporary burst connections.
   - `pool_pre_ping`: Active heartbeat checks before issuing queries to discard dead database connections automatically.
   - `pool_recycle`: Connections recycled every 3600 seconds to avoid stale TCP sockets.

3. **Transaction Management & Async Isolation**:
   - Explicit session management in FastAPI dependencies (`get_db` generator).
   - Automatic `commit()` upon handler completion and `rollback()` on exceptions.
   - Read-heavy operations utilize non-blocking async queries (`select(...).where(...)`).

4. **Multi-Tenant Ownership Controls**:
   - Every user-owned model includes a indexed `user_id` foreign key.
   - API endpoints enforce `user_id` filtering derived from authenticated JWT payloads, preventing cross-tenant data leakage.
