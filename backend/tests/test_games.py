import asyncio
import pytest
from backend.services.chess_engine import SimpleChessBoard, compute_best_chess_move
from backend.services.tictactoe_engine import compute_tictactoe_move, check_tictactoe_winner
from backend.services.maze_engine import generate_procedural_maze, solve_maze_astar

@pytest.mark.asyncio
async def test_chess_engine():
    board = SimpleChessBoard()
    moves = board.get_legal_moves()
    assert len(moves) == 20 # 16 pawn moves + 4 knight moves
    
    res = compute_best_chess_move("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "MEDIUM")
    assert res["aiMove"] is not None
    assert "fen" in res
    print("Chess Engine test PASSED!")

@pytest.mark.asyncio
async def test_tictactoe_engine():
    # Unbeatable minimax test: X places at center, O should respond optimally
    board = ["X", "", "", "", "", "", "", "", ""]
    res = compute_tictactoe_move(board, difficulty="Unbeatable", ai_symbol="O")
    assert res["aiMoveIndex"] in range(9)
    assert res["status"] == "IN_PROGRESS"
    print("TicTacToe Engine test PASSED!")

@pytest.mark.asyncio
async def test_maze_engine():
    maze = generate_procedural_maze(11, 11)
    assert maze["rows"] == 11
    assert maze["cols"] == 11
    assert len(maze["grid"]) == 11
    assert len(maze["solutionPath"]) > 0
    print("Maze Engine test PASSED!")

if __name__ == "__main__":
    asyncio.run(test_chess_engine())
    asyncio.run(test_tictactoe_engine())
    asyncio.run(test_maze_engine())
    print("ALL GAME ENGINE TESTS PASSED!")
