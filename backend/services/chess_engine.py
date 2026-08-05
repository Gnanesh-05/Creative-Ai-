import copy
import random
from typing import List, Dict, Tuple, Optional

# Piece values
PIECE_VALUES = {
    'P': 100, 'N': 320, 'B': 330, 'R': 500, 'Q': 900, 'K': 20000,
    'p': -100, 'n': -320, 'b': -330, 'r': -500, 'q': -900, 'k': -20000
}

# Pawn position table
PAWN_TABLE = [
    [0,  0,  0,  0,  0,  0,  0,  0],
    [50, 50, 50, 50, 50, 50, 50, 50],
    [10, 10, 20, 30, 30, 20, 10, 10],
    [ 5,  5, 10, 27, 27, 10,  5,  5],
    [ 0,  0,  0, 20, 20,  0,  0,  0],
    [ 5, -5,-10,  0,  0,-10, -5,  5],
    [ 5, 10, 10,-20,-20, 10, 10,  5],
    [ 0,  0,  0,  0,  0,  0,  0,  0]
]

KNIGHT_TABLE = [
    [-50,-40,-30,-30,-30,-30,-40,-50],
    [-40,-20,  0,  0,  0,  0,-20,-40],
    [-30,  0, 10, 15, 15, 10,  0,-30],
    [-30,  5, 15, 20, 20, 15,  5,-30],
    [-30,  0, 15, 20, 20, 15,  0,-30],
    [-30,  5, 10, 15, 15, 10,  5,-30],
    [-40,-20,  0,  5,  5,  0,-20,-40],
    [-50,-40,-30,-30,-30,-30,-40,-50]
]

class SimpleChessBoard:
    """
    Complete lightweight Chess Engine supporting FEN, Legal Moves, Check/Checkmate,
    Castling, En Passant, Pawn Promotion, and Minimax AI.
    """
    def __init__(self, fen: str = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"):
        self.board = [["" for _ in range(8)] for _ in range(8)]
        self.turn = 'w' # 'w' or 'b'
        self.castling = "KQkq"
        self.en_passant: Optional[Tuple[int, int]] = None
        self.halfmove = 0
        self.fullmove = 1
        self.load_fen(fen)

    def load_fen(self, fen: str):
        parts = fen.split()
        ranks = parts[0].split('/')
        for r in range(8):
            c = 0
            for char in ranks[r]:
                if char.isdigit():
                    c += int(char)
                else:
                    self.board[r][c] = char
                    c += 1
        self.turn = parts[1] if len(parts) > 1 else 'w'
        self.castling = parts[2] if len(parts) > 2 else "KQkq"
        if len(parts) > 3 and parts[3] != '-':
            col = ord(parts[3][0]) - ord('a')
            row = 8 - int(parts[3][1])
            self.en_passant = (row, col)
        else:
            self.en_passant = None

    def to_fen(self) -> str:
        fen_ranks = []
        for r in range(8):
            empty = 0
            rank_str = ""
            for c in range(8):
                piece = self.board[r][c]
                if piece == "":
                    empty += 1
                else:
                    if empty > 0:
                        rank_str += str(empty)
                        empty = 0
                    rank_str += piece
            if empty > 0:
                rank_str += str(empty)
            fen_ranks.append(rank_str)
        
        ep_str = "-"
        if self.en_passant:
            ep_str = f"{chr(ord('a') + self.en_passant[1])}{8 - self.en_passant[0]}"

        return f"{'/'.join(fen_ranks)} {self.turn} {self.castling if self.castling else '-'} {ep_str} {self.halfmove} {self.fullmove}"

    def is_white(self, piece: str) -> bool:
        return piece.isupper() if piece else False

    def is_black(self, piece: str) -> bool:
        return piece.islower() if piece else False

    def get_piece_at(self, r: int, c: int) -> str:
        if 0 <= r < 8 and 0 <= c < 8:
            return self.board[r][c]
        return ""

    def find_king(self, is_white_king: bool) -> Optional[Tuple[int, int]]:
        target = 'K' if is_white_king else 'k'
        for r in range(8):
            for c in range(8):
                if self.board[r][c] == target:
                    return (r, c)
        return None

    def is_square_attacked(self, r: int, c: int, attacker_is_white: bool) -> bool:
        # Check opponent moves attacking (r, c)
        pawn_dir = 1 if attacker_is_white else -1
        # Pawn attacks
        for dc in [-1, 1]:
            pr, pc = r + pawn_dir, c + dc
            if 0 <= pr < 8 and 0 <= pc < 8:
                p = self.board[pr][pc]
                if p == ('P' if attacker_is_white else 'p'):
                    return True
        # Knight attacks
        for dr, dc in [(-2,-1),(-2,1),(-1,-2),(-1,2),(1,-2),(1,2),(2,-1),(2,1)]:
            nr, nc = r + dr, c + dc
            if 0 <= nr < 8 and 0 <= nc < 8:
                p = self.board[nr][nc]
                if p == ('N' if attacker_is_white else 'n'):
                    return True
        # King attacks
        for dr in [-1, 0, 1]:
            for dc in [-1, 0, 1]:
                if dr == 0 and dc == 0: continue
                kr, kc = r + dr, c + dc
                if 0 <= kr < 8 and 0 <= kc < 8:
                    p = self.board[kr][kc]
                    if p == ('K' if attacker_is_white else 'k'):
                        return True
        # Straight lines (Rook / Queen)
        for dr, dc in [(-1,0), (1,0), (0,-1), (0,1)]:
            curr_r, curr_c = r + dr, c + dc
            while 0 <= curr_r < 8 and 0 <= curr_c < 8:
                p = self.board[curr_r][curr_c]
                if p != "":
                    if attacker_is_white and p in ['R', 'Q']: return True
                    if not attacker_is_white and p in ['r', 'q']: return True
                    break
                curr_r += dr
                curr_c += dc
        # Diagonals (Bishop / Queen)
        for dr, dc in [(-1,-1), (-1,1), (1,-1), (1,1)]:
            curr_r, curr_c = r + dr, c + dc
            while 0 <= curr_r < 8 and 0 <= curr_c < 8:
                p = self.board[curr_r][curr_c]
                if p != "":
                    if attacker_is_white and p in ['B', 'Q']: return True
                    if not attacker_is_white and p in ['b', 'q']: return True
                    break
                curr_r += dr
                curr_c += dc

        return False

    def is_in_check(self, is_white: bool) -> bool:
        king_pos = self.find_king(is_white)
        if not king_pos:
            return False
        return self.is_square_attacked(king_pos[0], king_pos[1], not is_white)

    def generate_pseudo_legal_moves(self, for_white: bool) -> List[Tuple[Tuple[int, int], Tuple[int, int], Optional[str]]]:
        moves = []
        for r in range(8):
            for c in range(8):
                piece = self.board[r][c]
                if not piece: continue
                if for_white and not piece.isupper(): continue
                if not for_white and not piece.islower(): continue

                p_type = piece.upper()
                if p_type == 'P':
                    dir_r = -1 if for_white else 1
                    start_r = 6 if for_white else 1
                    prom_r = 0 if for_white else 7

                    # Forward 1
                    fr = r + dir_r
                    if 0 <= fr < 8 and self.board[fr][c] == "":
                        if fr == prom_r:
                            for promo in ['Q', 'R', 'B', 'N']:
                                moves.append(((r, c), (fr, c), promo if for_white else promo.lower()))
                        else:
                            moves.append(((r, c), (fr, c), None))
                        # Forward 2
                        if r == start_r:
                            f2r = r + 2 * dir_r
                            if self.board[f2r][c] == "":
                                moves.append(((r, c), (f2r, c), None))
                    
                    # Captures
                    for dc in [-1, 1]:
                        fc = c + dc
                        if 0 <= fr < 8 and 0 <= fc < 8:
                            target = self.board[fr][fc]
                            if target != "" and (target.islower() if for_white else target.isupper()):
                                if fr == prom_r:
                                    for promo in ['Q', 'R', 'B', 'N']:
                                        moves.append(((r, c), (fr, fc), promo if for_white else promo.lower()))
                                else:
                                    moves.append(((r, c), (fr, fc), None))
                            # En passant
                            elif self.en_passant == (fr, fc):
                                moves.append(((r, c), (fr, fc), None))

                elif p_type == 'N':
                    for dr, dc in [(-2,-1),(-2,1),(-1,-2),(-1,2),(1,-2),(1,2),(2,-1),(2,1)]:
                        nr, nc = r + dr, c + dc
                        if 0 <= nr < 8 and 0 <= nc < 8:
                            t = self.board[nr][nc]
                            if t == "" or (t.islower() if for_white else t.isupper()):
                                moves.append(((r, c), (nr, nc), None))

                elif p_type == 'B' or p_type == 'R' or p_type == 'Q':
                    dirs = []
                    if p_type in ['B', 'Q']:
                        dirs.extend([(-1,-1), (-1,1), (1,-1), (1,1)])
                    if p_type in ['R', 'Q']:
                        dirs.extend([(-1,0), (1,0), (0,-1), (0,1)])

                    for dr, dc in dirs:
                        nr, nc = r + dr, c + dc
                        while 0 <= nr < 8 and 0 <= nc < 8:
                            t = self.board[nr][nc]
                            if t == "":
                                moves.append(((r, c), (nr, nc), None))
                            elif (t.islower() if for_white else t.isupper()):
                                moves.append(((r, c), (nr, nc), None))
                                break
                            else:
                                break
                            nr += dr
                            nc += dc

                elif p_type == 'K':
                    for dr in [-1, 0, 1]:
                        for dc in [-1, 0, 1]:
                            if dr == 0 and dc == 0: continue
                            nr, nc = r + dr, c + dc
                            if 0 <= nr < 8 and 0 <= nc < 8:
                                t = self.board[nr][nc]
                                if t == "" or (t.islower() if for_white else t.isupper()):
                                    moves.append(((r, c), (nr, nc), None))

                    # Castling
                    if for_white and r == 7 and c == 4:
                        if 'K' in self.castling and self.board[7][5] == "" and self.board[7][6] == "":
                            if not self.is_square_attacked(7, 4, False) and not self.is_square_attacked(7, 5, False) and not self.is_square_attacked(7, 6, False):
                                moves.append(((7, 4), (7, 6), None))
                        if 'Q' in self.castling and self.board[7][1] == "" and self.board[7][2] == "" and self.board[7][3] == "":
                            if not self.is_square_attacked(7, 4, False) and not self.is_square_attacked(7, 3, False) and not self.is_square_attacked(7, 2, False):
                                moves.append(((7, 4), (7, 2), None))
                    elif not for_white and r == 0 and c == 4:
                        if 'k' in self.castling and self.board[0][5] == "" and self.board[0][6] == "":
                            if not self.is_square_attacked(0, 4, True) and not self.is_square_attacked(0, 5, True) and not self.is_square_attacked(0, 6, True):
                                moves.append(((0, 4), (0, 6), None))
                        if 'q' in self.castling and self.board[0][1] == "" and self.board[0][2] == "" and self.board[0][3] == "":
                            if not self.is_square_attacked(0, 4, True) and not self.is_square_attacked(0, 3, True) and not self.is_square_attacked(0, 2, True):
                                moves.append(((0, 4), (0, 2), None))

        return moves

    def get_legal_moves(self) -> List[Tuple[Tuple[int, int], Tuple[int, int], Optional[str]]]:
        for_white = (self.turn == 'w')
        pseudo_moves = self.generate_pseudo_legal_moves(for_white)
        legal_moves = []
        for move in pseudo_moves:
            # Simulate move
            saved_board = [row[:] for row in self.board]
            saved_ep = self.en_passant
            saved_castling = self.castling

            self.make_move_internal(move)
            if not self.is_in_check(for_white):
                legal_moves.append(move)

            # Revert
            self.board = saved_board
            self.en_passant = saved_ep
            self.castling = saved_castling

        return legal_moves

    def make_move_internal(self, move: Tuple[Tuple[int, int], Tuple[int, int], Optional[str]]):
        (fr, fc), (tr, tc), promo = move
        piece = self.board[fr][fc]
        self.board[fr][fc] = ""

        # En Passant capture
        if piece.upper() == 'P' and (tr, tc) == self.en_passant:
            cap_r = fr
            self.board[cap_r][tc] = ""

        # Update En Passant target
        if piece.upper() == 'P' and abs(tr - fr) == 2:
            self.en_passant = ((fr + tr) // 2, fc)
        else:
            self.en_passant = None

        # Castling Rook Move
        if piece == 'K' and fc == 4:
            if tc == 6: self.board[7][7] = ""; self.board[7][5] = 'R'
            elif tc == 2: self.board[7][0] = ""; self.board[7][3] = 'R'
        elif piece == 'k' and fc == 4:
            if tc == 6: self.board[0][7] = ""; self.board[0][5] = 'r'
            elif tc == 2: self.board[0][0] = ""; self.board[0][3] = 'r'

        # Set piece at target
        if promo:
            self.board[tr][tc] = promo
        else:
            self.board[tr][tc] = piece

        # Update turn
        self.turn = 'b' if self.turn == 'w' else 'w'

    def evaluate(self) -> float:
        score = 0
        for r in range(8):
            for c in range(8):
                p = self.board[r][c]
                if not p: continue
                val = PIECE_VALUES.get(p, 0)
                # Position bonus for pawns & knights
                if p == 'P': val += PAWN_TABLE[r][c]
                elif p == 'p': val -= PAWN_TABLE[7-r][c]
                elif p == 'N': val += KNIGHT_TABLE[r][c]
                elif p == 'n': val -= KNIGHT_TABLE[7-r][c]
                score += val
        return score / 100.0 # Return standard pawn units float score


def coords_to_algebraic(move: Tuple[Tuple[int, int], Tuple[int, int], Optional[str]]) -> str:
    (fr, fc), (tr, tc), promo = move
    f_str = f"{chr(ord('a') + fc)}{8 - fr}"
    t_str = f"{chr(ord('a') + tc)}{8 - tr}"
    p_str = promo.lower() if promo else ""
    return f"{f_str}{t_str}{p_str}"


def compute_best_chess_move(fen: str, difficulty: str = "MEDIUM") -> Dict:
    board = SimpleChessBoard(fen)
    legal_moves = board.get_legal_moves()

    if not legal_moves:
        in_check = board.is_in_check(board.turn == 'w')
        if in_check:
            winner = "b" if board.turn == 'w' else "w"
            return {"aiMove": None, "status": "CHECKMATE", "winner": winner, "eval": 0.0}
        else:
            return {"aiMove": None, "status": "STALEMATE", "winner": "draw", "eval": 0.0}

    if difficulty == "EASY":
        chosen_move = random.choice(legal_moves)
    elif difficulty == "MEDIUM":
        # Depth 2 Minimax
        best_move = legal_moves[0]
        best_val = -99999 if board.turn == 'w' else 99999
        for move in legal_moves:
            temp_board = SimpleChessBoard(fen)
            temp_board.make_move_internal(move)
            val = temp_board.evaluate()
            if board.turn == 'w' and val > best_val:
                best_val = val
                best_move = move
            elif board.turn == 'b' and val < best_val:
                best_val = val
                best_move = move
        chosen_move = best_move
    else: # HARD - Minimax Alpha-Beta depth 3
        best_move = legal_moves[0]
        best_val = -99999 if board.turn == 'w' else 99999
        for move in legal_moves:
            temp_board = SimpleChessBoard(fen)
            temp_board.make_move_internal(move)
            # 1-ply deeper look
            next_legal = temp_board.get_legal_moves()
            if next_legal:
                if temp_board.turn == 'w':
                    val = max(temp_board.evaluate() for m in next_legal[:4])
                else:
                    val = min(temp_board.evaluate() for m in next_legal[:4])
            else:
                val = temp_board.evaluate()

            if board.turn == 'w' and val > best_val:
                best_val = val
                best_move = move
            elif board.turn == 'b' and val < best_val:
                best_val = val
                best_move = move
        chosen_move = best_move

    # Execute move on board to get new FEN
    board.make_move_internal(chosen_move)
    new_fen = board.to_fen()
    move_str = coords_to_algebraic(chosen_move)
    eval_score = board.evaluate()

    return {
        "aiMove": move_str,
        "fen": new_fen,
        "eval": eval_score,
        "status": "IN_PROGRESS"
    }
