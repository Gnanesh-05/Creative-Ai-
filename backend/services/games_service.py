import logging
from typing import List, Dict, Any, Optional
from sqlalchemy import select
from sqlalchemy.orm import Session
from backend.models.domain import GameSession, GameMove, GameResult, GameStatistic, GamePreference
from backend.schemas.games import (
    StartGameSessionRequest, ChessMoveRequest, TicTacToeMoveRequest, MazeRequest,
    EndGameSessionRequest, LLMGameAnalysisRequest, GameResponse, GameStatisticResponse, GamePreferenceRequest,
    GamePreferenceResponse
)
from backend.services.chess_engine import compute_best_chess_move, SimpleChessBoard, coords_to_algebraic
from backend.services.tictactoe_engine import compute_tictactoe_move
from backend.services.maze_engine import generate_procedural_maze
from backend.providers.llm_provider import llm_provider

logger = logging.getLogger(__name__)

class GamesService:
    async def create_session(self, db: Session, user_id: str, req: StartGameSessionRequest) -> GameSession:
        initial_state = req.initial_config or {}
        if req.game_type == "CHESS" and "fen" not in initial_state:
            initial_state["fen"] = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        elif req.game_type == "TICTACTOE" and "board" not in initial_state:
            initial_state["board"] = [""] * 9
        elif req.game_type == "MAZE" and "grid" not in initial_state:
            maze_data = generate_procedural_maze(15, 15)
            initial_state.update(maze_data)

        session = GameSession(
            user_id=user_id,
            game_type=req.game_type.upper(),
            difficulty=req.difficulty,
            current_state_json=initial_state,
            status="IN_PROGRESS"
        )
        db.add(session)
        await db.commit()
        await db.refresh(session)
        return session

    async def fun_compute_chess_move(self, req: ChessMoveRequest, db: Optional[Session] = None, user_id: Optional[str] = None) -> GameResponse:
        if req.session_id and db and user_id:
            stmt = select(GameSession).where(GameSession.id == req.session_id, GameSession.user_id == user_id)
            result = await db.execute(stmt)
            session = result.scalars().first()
            if session:
                prev_state = session.current_state_json or {}
                prev_fen = prev_state.get("fen")
                if prev_fen:
                    board = SimpleChessBoard(prev_fen)
                    legal_moves = [coords_to_algebraic(m) for m in board.get_legal_moves()]
                    player_move = f"{req.moveFrom}{req.moveTo}".lower()
                    if not any(lm.startswith(player_move) for lm in legal_moves):
                        raise ValueError("Illegal move")

        result = compute_best_chess_move(req.fen, req.difficulty)
        
        # If session_id is supplied and DB exists, record the move
        if req.session_id and db and user_id:
            stmt = select(GameSession).where(GameSession.id == req.session_id, GameSession.user_id == user_id)
            db_res = await db.execute(stmt)
            session = db_res.scalars().first()
            if session:
                count_stmt = select(GameMove).where(GameMove.session_id == req.session_id)
                count_result = await db.execute(count_stmt)
                move_count = len(count_result.scalars().all())
                db_move = GameMove(
                    session_id=req.session_id,
                    move_number=move_count + 1,
                    player="ai",
                    move_data_json={"move": result.get("aiMove"), "eval": result.get("eval")},
                    fen_after=result.get("fen")
                )
                session.current_state_json = {"fen": result.get("fen"), "eval": result.get("eval")}
                if result.get("status") in ["CHECKMATE", "STALEMATE"]:
                    session.status = "WON" if result.get("winner") == "human" else ("LOST" if result.get("winner") == "ai" else "DRAW")
                db.add(db_move)
                await db.commit()

        return GameResponse(
            status=result.get("status", "SUCCESS"),
            boardState=result.get("fen"),
            aiMove=result.get("aiMove"),
            fen=result.get("fen"),
            evaluation=result.get("eval"),
            winner=result.get("winner"),
            session_id=req.session_id
        )

    async def fun_compute_tictactoe_move(self, req: TicTacToeMoveRequest, db: Optional[Session] = None, user_id: Optional[str] = None) -> GameResponse:
        if req.session_id and db and user_id:
            stmt = select(GameSession).where(GameSession.id == req.session_id, GameSession.user_id == user_id)
            result = await db.execute(stmt)
            session = result.scalars().first()
            if session:
                prev_state = session.current_state_json or {}
                prev_board = prev_state.get("board")
                if prev_board:
                    diffs = 0
                    for i in range(9):
                        if prev_board[i] != req.board[i]:
                            if prev_board[i] != "":
                                raise ValueError("Cannot modify already occupied cell")
                            diffs += 1
                    if diffs > 1:
                        raise ValueError("Multiple moves in a single turn are not allowed")

        result = compute_tictactoe_move(req.board, req.difficulty, req.ai_symbol)
        
        if req.session_id and db and user_id:
            stmt = select(GameSession).where(GameSession.id == req.session_id, GameSession.user_id == user_id)
            db_res = await db.execute(stmt)
            session = db_res.scalars().first()
            if session:
                count_stmt = select(GameMove).where(GameMove.session_id == req.session_id)
                count_result = await db.execute(count_stmt)
                move_count = len(count_result.scalars().all())
                db_move = GameMove(
                    session_id=req.session_id,
                    move_number=move_count + 1,
                    player="ai",
                    move_data_json={"ai_index": result.get("aiMoveIndex")},
                    fen_after=None
                )
                session.current_state_json = {"board": result.get("board")}
                if result.get("winner"):
                    session.status = "WON" if result.get("winner") == "X" else ("LOST" if result.get("winner") == "O" else "DRAW")
                db.add(db_move)
                await db.commit()

        return GameResponse(
            status=result.get("status", "SUCCESS"),
            boardState=result.get("board"),
            aiMove=str(result.get("aiMoveIndex")),
            winner=result.get("winner"),
            session_id=req.session_id
        )

    async def fun_generate_maze(self, req: MazeRequest, db: Optional[Session] = None, user_id: Optional[str] = None) -> GameResponse:
        maze_data = generate_procedural_maze(req.rows, req.cols)
        
        if req.session_id and db and user_id:
            stmt = select(GameSession).where(GameSession.id == req.session_id, GameSession.user_id == user_id)
            result = await db.execute(stmt)
            session = result.scalars().first()
            if session:
                session.current_state_json = maze_data
                await db.commit()

        return GameResponse(
            status="SUCCESS",
            boardState=maze_data,
            session_id=req.session_id
        )

    async def end_session(self, db: Session, user_id: str, req: EndGameSessionRequest) -> Dict[str, Any]:
        stmt = select(GameSession).where(GameSession.id == req.session_id, GameSession.user_id == user_id)
        result = await db.execute(stmt)
        session = result.scalars().first()
        if not session:
            return {"status": "ERROR", "message": "Session not found"}

        session.status = "WON" if req.winner == "human" else ("LOST" if req.winner == "ai" else "DRAW")
        
        # Add result record
        game_result = GameResult(
            session_id=session.id,
            user_id=user_id,
            game_type=session.game_type,
            winner=req.winner,
            score=req.score,
            total_moves=req.total_moves,
            duration_seconds=req.duration_seconds
        )
        db.add(game_result)

        # Update user statistics
        stat_stmt = select(GameStatistic).where(GameStatistic.user_id == user_id, GameStatistic.game_type == session.game_type)
        stat_result = await db.execute(stat_stmt)
        stat = stat_result.scalars().first()
        if not stat:
            stat = GameStatistic(user_id=user_id, game_type=session.game_type)
            db.add(stat)

        stat.games_played += 1
        if req.winner == "human":
            stat.wins += 1
        elif req.winner == "ai":
            stat.losses += 1
        else:
            stat.draws += 1

        if stat.games_played > 0:
            stat.win_rate = float(stat.wins) / float(stat.games_played)

        await db.commit()
        return {"status": "SUCCESS", "winner": req.winner, "games_played": stat.games_played, "win_rate": stat.win_rate}

    async def get_user_statistics(self, db: Session, user_id: str) -> List[GameStatisticResponse]:
        stmt = select(GameStatistic).where(GameStatistic.user_id == user_id)
        result = await db.execute(stmt)
        stats = result.scalars().all()
        # Ensure default entries for CHESS, TICTACTOE, MAZE
        existing_types = {s.game_type for s in stats}
        result_list = [
            GameStatisticResponse(
                game_type=s.game_type,
                games_played=s.games_played,
                wins=s.wins,
                losses=s.losses,
                draws=s.draws,
                win_rate=s.win_rate
            ) for s in stats
        ]
        for gt in ["CHESS", "TICTACTOE", "MAZE"]:
            if gt not in existing_types:
                result_list.append(GameStatisticResponse(game_type=gt, games_played=0, wins=0, losses=0, draws=0, win_rate=0.0))
        return result_list

    async def get_user_preferences(self, db: Session, user_id: str) -> GamePreferenceResponse:
        stmt = select(GamePreference).where(GamePreference.user_id == user_id)
        result = await db.execute(stmt)
        pref = result.scalars().first()
        if not pref:
            pref = GamePreference(user_id=user_id)
            db.add(pref)
            await db.commit()
            await db.refresh(pref)
        return GamePreferenceResponse(
            default_chess_difficulty=pref.default_chess_difficulty,
            default_tictactoe_difficulty=pref.default_tictactoe_difficulty,
            maze_size=pref.maze_size,
            sound_effects=pref.sound_effects
        )

    async def update_user_preferences(self, db: Session, user_id: str, req: GamePreferenceRequest) -> GamePreferenceResponse:
        stmt = select(GamePreference).where(GamePreference.user_id == user_id)
        result = await db.execute(stmt)
        pref = result.scalars().first()
        if not pref:
            pref = GamePreference(user_id=user_id)
            db.add(pref)

        pref.default_chess_difficulty = req.default_chess_difficulty
        pref.default_tictactoe_difficulty = req.default_tictactoe_difficulty
        pref.maze_size = req.maze_size
        pref.sound_effects = req.sound_effects
        await db.commit()
        await db.refresh(pref)

        return GamePreferenceResponse(
            default_chess_difficulty=pref.default_chess_difficulty,
            default_tictactoe_difficulty=pref.default_tictactoe_difficulty,
            maze_size=pref.maze_size,
            sound_effects=pref.sound_effects
        )

    async def analyze_game_with_llm(self, req: LLMGameAnalysisRequest) -> str:
        prompt = (
            f"Game Type: {req.game_type}\n"
            f"Current Game State: {req.game_state_description}\n"
            f"User Question: {req.user_query}\n\n"
            "As an expert AI Game Analyst & Grandmaster Coach, provide clear strategic advice, "
            "explain optimal tactics or pathfinding algorithms (e.g. A* Manhattan heuristic, Minimax positional evaluation), "
            "and give concise, encouraging tips."
        )
        sys_instruction = "You are Game Mind AI, an analytical grandmaster gaming coach."
        response = await llm_provider.fun_generate_reply(
            message=prompt,
            system_instruction=sys_instruction,
            temperature=0.7
        )
        return response
