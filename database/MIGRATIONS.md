# Alembic & Database Migrations Strategy

## Directory Structure
- `backend/alembic.ini`: Alembic configuration file pointing to the target database.
- `backend/alembic/env.py`: Async migration runner initializing `target_metadata` from `backend.models.domain.Base`.
- `backend/alembic/versions/`: Versioned migration scripts.

## Executing Migrations

### Apply Migrations to Target Database
```bash
cd backend
alembic upgrade head
```

### Create New Auto-Generated Migration
```bash
cd backend
alembic revision --autogenerate -m "Add feature table or column"
```

### Rollback Last Migration Step
```bash
cd backend
alembic downgrade -1
```

## Production Migration Workflow
1. Test migration scripts locally against a PostgreSQL docker container.
2. Ensure backward compatibility for non-breaking deployment (e.g. addition of nullable columns before deprecating old ones).
3. Execute `alembic upgrade head` in CI/CD pipeline prior to rolling out updated backend instances.
