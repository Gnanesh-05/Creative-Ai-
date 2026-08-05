"""Initial schema migration

Revision ID: 0001_initial_schema
Revises: 
Create Date: 2026-07-28 20:45:00.000000

"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa

revision: str = '0001_initial_schema'
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

def upgrade() -> None:
    # 1. Users
    op.create_table(
        'users',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('email', sa.String(255), nullable=False),
        sa.Column('username', sa.String(100), nullable=False),
        sa.Column('hashed_password', sa.String(255), nullable=False),
        sa.Column('full_name', sa.String(255), nullable=True),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.Column('is_deleted', sa.Boolean(), default=False),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_users_email'), 'users', ['email'], unique=True)
    op.create_index(op.f('ix_users_username'), 'users', ['username'], unique=True)

    # 2. Password Reset Tokens
    op.create_table(
        'password_reset_tokens',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('token_hash', sa.String(255), nullable=False),
        sa.Column('expires_at', sa.DateTime(), nullable=False),
        sa.Column('is_used', sa.Boolean(), default=False),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_password_reset_tokens_token_hash'), 'password_reset_tokens', ['token_hash'], unique=False)

    # 3. User Sessions
    op.create_table(
        'user_sessions',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('refresh_token_hash', sa.String(255), nullable=False),
        sa.Column('user_agent', sa.String(512), nullable=True),
        sa.Column('ip_address', sa.String(45), nullable=True),
        sa.Column('expires_at', sa.DateTime(), nullable=False),
        sa.Column('is_revoked', sa.Boolean(), default=False),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )

    # 4. User Settings
    op.create_table(
        'user_settings',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('dark_mode', sa.Boolean(), default=True),
        sa.Column('notifications_enabled', sa.Boolean(), default=True),
        sa.Column('auto_save_history', sa.Boolean(), default=True),
        sa.Column('high_quality_rendering', sa.Boolean(), default=True),
        sa.Column('model_temperature', sa.Float(), default=0.7),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('user_id')
    )

    # 5. Chat Conversations
    op.create_table(
        'chat_conversations',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('title', sa.String(255), nullable=False, server_default='New Conversation'),
        sa.Column('system_instruction', sa.Text(), nullable=True),
        sa.Column('model_name', sa.String(100), default='gemini-2.0-flash'),
        sa.Column('is_deleted', sa.Boolean(), default=False),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )

    # 6. Chat Messages
    op.create_table(
        'chat_messages',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('conversation_id', sa.String(36), nullable=False),
        sa.Column('sender', sa.String(20), nullable=False),
        sa.Column('content', sa.Text(), nullable=False),
        sa.Column('tokens_used', sa.Integer(), default=0),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['conversation_id'], ['chat_conversations.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )

    # 7. Image Generations
    op.create_table(
        'image_generations',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('prompt', sa.Text(), nullable=False),
        sa.Column('style_preset', sa.String(50), default='Cyberpunk'),
        sa.Column('aspect_ratio', sa.String(20), default='1:1'),
        sa.Column('image_url', sa.Text(), nullable=False),
        sa.Column('storage_key', sa.String(512), nullable=True),
        sa.Column('seed', sa.BigInteger(), nullable=True),
        sa.Column('is_deleted', sa.Boolean(), default=False),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )

    # 8. Music Generations
    op.create_table(
        'music_generations',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('prompt', sa.Text(), nullable=False),
        sa.Column('mood', sa.String(50), nullable=False),
        sa.Column('genre', sa.String(50), default='Lo-Fi Beats'),
        sa.Column('tempo_bpm', sa.Integer(), default=90),
        sa.Column('duration_seconds', sa.Integer(), default=30),
        sa.Column('audio_url', sa.Text(), nullable=False),
        sa.Column('storage_key', sa.String(512), nullable=True),
        sa.Column('synthetic_notes', sa.Text(), nullable=True),
        sa.Column('is_deleted', sa.Boolean(), default=False),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )

    # 9. Game Sessions
    op.create_table(
        'game_sessions',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('game_type', sa.String(50), nullable=False),
        sa.Column('difficulty', sa.String(50), default='MEDIUM'),
        sa.Column('current_state_json', sa.JSON(), nullable=False),
        sa.Column('status', sa.String(20), default='IN_PROGRESS'),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )

    # 10. Game Moves
    op.create_table(
        'game_moves',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('session_id', sa.String(36), nullable=False),
        sa.Column('move_number', sa.Integer(), nullable=False),
        sa.Column('player', sa.String(20), nullable=False),
        sa.Column('move_data_json', sa.JSON(), nullable=False),
        sa.Column('fen_after', sa.String(255), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['session_id'], ['game_sessions.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )

    # 11. Game Results
    op.create_table(
        'game_results',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('session_id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('game_type', sa.String(50), nullable=False),
        sa.Column('winner', sa.String(20), nullable=False),
        sa.Column('score', sa.Integer(), default=0),
        sa.Column('total_moves', sa.Integer(), default=0),
        sa.Column('duration_seconds', sa.Integer(), default=0),
        sa.Column('ended_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['session_id'], ['game_sessions.id'], ondelete='CASCADE'),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('session_id')
    )

    # 12. Game Statistics
    op.create_table(
        'game_statistics',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('game_type', sa.String(50), nullable=False),
        sa.Column('games_played', sa.Integer(), default=0),
        sa.Column('wins', sa.Integer(), default=0),
        sa.Column('losses', sa.Integer(), default=0),
        sa.Column('draws', sa.Integer(), default=0),
        sa.Column('win_rate', sa.Float(), default=0.0),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('user_id', 'game_type', name='_user_game_type_uc')
    )

    # 13. Game Preferences
    op.create_table(
        'game_preferences',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('default_chess_difficulty', sa.String(50), default='Grandmaster Mind'),
        sa.Column('default_tictactoe_difficulty', sa.String(50), default='Unbeatable'),
        sa.Column('maze_size', sa.Integer(), default=15),
        sa.Column('sound_effects', sa.Boolean(), default=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('user_id')
    )

    # 14. Unified History
    op.create_table(
        'unified_history',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('module_type', sa.String(50), nullable=False),
        sa.Column('title', sa.String(255), nullable=False),
        sa.Column('summary', sa.Text(), nullable=True),
        sa.Column('payload', sa.JSON(), nullable=True),
        sa.Column('is_deleted', sa.Boolean(), default=False),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_unified_history_created_at'), 'unified_history', ['created_at'], unique=False)
    op.create_index(op.f('ix_unified_history_module_type'), 'unified_history', ['module_type'], unique=False)

    # 15. Usage Tracking
    op.create_table(
        'usage_tracking',
        sa.Column('id', sa.String(36), nullable=False),
        sa.Column('user_id', sa.String(36), nullable=False),
        sa.Column('usage_date', sa.String(10), nullable=False),
        sa.Column('chat_tokens', sa.Integer(), default=0),
        sa.Column('images_generated', sa.Integer(), default=0),
        sa.Column('music_generated', sa.Integer(), default=0),
        sa.Column('games_played', sa.Integer(), default=0),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('user_id', 'usage_date', name='_user_usage_date_uc')
    )

def downgrade() -> None:
    op.drop_table('usage_tracking')
    op.drop_table('unified_history')
    op.drop_table('game_preferences')
    op.drop_table('game_statistics')
    op.drop_table('game_results')
    op.drop_table('game_moves')
    op.drop_table('game_sessions')
    op.drop_table('music_generations')
    op.drop_table('image_generations')
    op.drop_table('chat_messages')
    op.drop_table('chat_conversations')
    op.drop_table('user_settings')
    op.drop_table('user_sessions')
    op.drop_table('password_reset_tokens')
    op.drop_table('users')
