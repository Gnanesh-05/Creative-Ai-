import random
from typing import List, Optional, Dict, Tuple

WIN_PATTERNS = [
    [0, 1, 2], [3, 4, 5], [6, 7, 8], # Rows
    [0, 3, 6], [1, 4, 7], [2, 5, 8], # Cols
    [0, 4, 8], [2, 4, 6]             # Diagonals
]

def check_tictactoe_winner(board: List[str]) -> Optional[str]:
    for combo in WIN_PATTERNS:
        p1, p2, p3 = combo
        if board[p1] != "" and board[p1] == board[p2] and board[p2] == board[p3]:
            return board[p1]
    if "" not in board:
        return "DRAW"
    return None

def minimax(board: List[str], depth: int, is_maximizing: bool, ai_symbol: str, player_symbol: str) -> Tuple[int, Optional[int]]:
    winner = check_tictactoe_winner(board)
    if winner == ai_symbol:
        return (10 - depth, None)
    elif winner == player_symbol:
        return (depth - 10, None)
    elif winner == "DRAW":
        return (0, None)

    empty_cells = [i for i, cell in enumerate(board) if cell == ""]
    best_move = None

    if is_maximizing:
        best_score = -1000
        for move in empty_cells:
            board[move] = ai_symbol
            score, _ = minimax(board, depth + 1, False, ai_symbol, player_symbol)
            board[move] = ""
            if score > best_score:
                best_score = score
                best_move = move
        return (best_score, best_move)
    else:
        best_score = 1000
        for move in empty_cells:
            board[move] = player_symbol
            score, _ = minimax(board, depth + 1, True, ai_symbol, player_symbol)
            board[move] = ""
            if score < best_score:
                best_score = score
                best_move = move
        return (best_score, best_move)

def compute_tictactoe_move(board: List[str], difficulty: str = "Unbeatable", ai_symbol: str = "O") -> Dict:
    player_symbol = "X" if ai_symbol == "O" else "O"
    empty_indices = [i for i, cell in enumerate(board) if cell == ""]

    if not empty_indices:
        winner = check_tictactoe_winner(board)
        return {"aiMoveIndex": -1, "winner": winner or "DRAW", "status": "FINISHED"}

    if difficulty == "Easy":
        chosen_index = random.choice(empty_indices)
    elif difficulty == "Medium":
        if random.random() < 0.6:
            _, chosen_index = minimax(board, 0, True, ai_symbol, player_symbol)
            if chosen_index is None:
                chosen_index = random.choice(empty_indices)
        else:
            chosen_index = random.choice(empty_indices)
    else: # Unbeatable / Hard Minimax
        _, chosen_index = minimax(board, 0, True, ai_symbol, player_symbol)
        if chosen_index is None:
            chosen_index = random.choice(empty_indices)

    # Apply move to copy to check final winner state
    new_board = list(board)
    if chosen_index in range(9):
        new_board[chosen_index] = ai_symbol

    winner = check_tictactoe_winner(new_board)

    return {
        "aiMoveIndex": chosen_index,
        "winner": winner,
        "board": new_board,
        "status": "FINISHED" if winner else "IN_PROGRESS"
    }
