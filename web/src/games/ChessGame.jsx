import React, { useState, useEffect } from 'react';
import { RefreshCw } from 'lucide-react';

export default function ChessGame() {
  const [boardState, setBoardState] = useState(initialChessBoard());
  const [selectedSquare, setSelectedSquare] = useState(null);
  const [validMoves, setValidMoves] = useState([]);
  const [isWhiteTurn, setIsWhiteTurn] = useState(true);
  const [statusText, setStatusText] = useState("Your Turn (White)");
  const [isAiThinking, setIsAiThinking] = useState(false);
  const [whiteCaptured, setWhiteCaptured] = useState(0);
  const [blackCaptured, setBlackCaptured] = useState(0);

  const getChessSymbol = (piece) => {
    if (!piece) return "";
    const symbols = {
      white: { K: "♔", Q: "♕", R: "♖", B: "♗", N: "♘", P: "♙" },
      black: { K: "♚", Q: "♛", R: "♜", B: "♝", N: "♞", P: "♟" }
    };
    return symbols[piece.isWhite ? "white" : "black"][piece.type] || "";
  };

  function initialChessBoard() {
    const b = Array.from({ length: 8 }, () => Array(8).fill(null));
    const mainRow = ["R", "N", "B", "Q", "K", "B", "N", "R"];

    // Black pieces
    for (let c = 0; c < 8; c++) {
      b[0][c] = { type: mainRow[c], isWhite: false };
      b[1][c] = { type: "P", isWhite: false };
    }

    // White pieces
    for (let c = 0; c < 8; c++) {
      b[6][c] = { type: "P", isWhite: true };
      b[7][c] = { type: mainRow[c], isWhite: true };
    }

    return b;
  }

  const getValidMovesForPiece = (r, c, board) => {
    const piece = board[r][c];
    if (!piece) return [];
    const moves = [];
    const isWhite = piece.isWhite;

    switch (piece.type) {
      case "P": {
        const dir = isWhite ? -1 : 1;
        const nextR = r + dir;
        if (nextR >= 0 && nextR <= 7 && board[nextR][c] === null) {
          moves.push([nextR, c]);
          const startRow = isWhite ? 6 : 1;
          const doubleR = r + 2 * dir;
          if (r === startRow && board[doubleR][c] === null) {
            moves.push([doubleR, c]);
          }
        }
        // Pawn captures
        for (let dc of [-1, 1]) {
          const capC = c + dc;
          if (nextR >= 0 && nextR <= 7 && capC >= 0 && capC <= 7) {
            const target = board[nextR][capC];
            if (target !== null && target.isWhite !== isWhite) {
              moves.push([nextR, capC]);
            }
          }
        }
        break;
      }
      case "N": {
        const offsets = [
          [-2, -1], [-2, 1], [-1, -2], [-1, 2],
          [1, -2], [1, 2], [2, -1], [2, 1]
        ];
        for (let [dr, dc] of offsets) {
          const nr = r + dr;
          const nc = c + dc;
          if (nr >= 0 && nr <= 7 && nc >= 0 && nc <= 7) {
            const target = board[nr][nc];
            if (target === null || target.isWhite !== isWhite) {
              moves.push([nr, nc]);
            }
          }
        }
        break;
      }
      case "K": {
        for (let dr = -1; dr <= 1; dr++) {
          for (let dc = -1; dc <= 1; dc++) {
            if (dr === 0 && dc === 0) continue;
            const nr = r + dr;
            const nc = c + dc;
            if (nr >= 0 && nr <= 7 && nc >= 0 && nc <= 7) {
              const target = board[nr][nc];
              if (target === null || target.isWhite !== isWhite) {
                moves.push([nr, nc]);
              }
            }
          }
        }
        break;
      }
      case "R":
      case "B":
      case "Q": {
        const directions = [];
        if (piece.type === "R" || piece.type === "Q") {
          directions.push([-1, 0], [1, 0], [0, -1], [0, 1]);
        }
        if (piece.type === "B" || piece.type === "Q") {
          directions.push([-1, -1], [-1, 1], [1, -1], [1, 1]);
        }

        for (let [dr, dc] of directions) {
          let nr = r + dr;
          let nc = c + dc;
          while (nr >= 0 && nr <= 7 && nc >= 0 && nc <= 7) {
            const target = board[nr][nc];
            if (target === null) {
              moves.push([nr, nc]);
            } else {
              if (target.isWhite !== isWhite) {
                moves.push([nr, nc]);
              }
              break; // Blocked
            }
            nr += dr;
            nc += dc;
          }
        }
        break;
      }
      default:
        break;
    }
    return moves;
  };

  const calculateAiChessMove = (board) => {
    const allAiPieces = [];
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        const piece = board[r][c];
        if (piece && !piece.isWhite) {
          allAiPieces.push([r, c]);
        }
      }
    }

    if (allAiPieces.length === 0) return null;

    const possibleMoves = [];
    const captureMoves = [];

    for (let [pr, pc] of allAiPieces) {
      const dests = getValidMovesForPiece(pr, pc, board);
      for (let [dr, dc] of dests) {
        const movePair = { from: [pr, pc], to: [dr, dc] };
        possibleMoves.push(movePair);
        const target = board[dr][dc];
        if (target && target.isWhite) {
          captureMoves.push(movePair);
        }
      }
    }

    if (captureMoves.length > 0) {
      return captureMoves[Math.floor(Math.random() * captureMoves.length)];
    } else if (possibleMoves.length > 0) {
      return possibleMoves[Math.floor(Math.random() * possibleMoves.length)];
    }
    return null;
  };

  const triggerAiMove = (currentBoard) => {
    setIsAiThinking(true);
    setStatusText("🧠 Mind AI analyzing position...");
    setTimeout(() => {
      const aiMove = calculateAiChessMove(currentBoard);
      if (aiMove) {
        const { from, to } = aiMove;
        const newBoard = currentBoard.map(row => [...row]);
        const captured = newBoard[to[0]][to[1]];

        if (captured && captured.isWhite) {
          setWhiteCaptured(c => c + 1);
        }

        newBoard[to[0]][to[1]] = newBoard[from[0]][from[1]];
        newBoard[from[0]][from[1]] = null;

        setBoardState(newBoard);
        setIsWhiteTurn(true);
        setStatusText("Your Turn (White)");
      } else {
        setStatusText("Checkmate / Stalemate! Match Over.");
      }
      setIsAiThinking(false);
    }, 600);
  };

  const onSquareClick = (r, c) => {
    if (!isWhiteTurn || isAiThinking) return;

    const piece = boardState[r][c];
    const isValDest = validMoves.some(([vr, vc]) => vr === r && vc === c);

    if (selectedSquare && isValDest) {
      const [sr, sc] = selectedSquare;
      const newBoard = boardState.map(row => [...row]);
      const captured = newBoard[r][c];

      if (captured && !captured.isWhite) {
        setBlackCaptured(c => c + 1);
      }

      newBoard[r][c] = newBoard[sr][sc];
      newBoard[sr][sc] = null;

      // Pawn promotion (simple Queen promotion)
      if (newBoard[r][c]?.type === "P" && r === 0) {
        newBoard[r][c] = { type: "Q", isWhite: true };
      }

      setBoardState(newBoard);
      setSelectedSquare(null);
      setValidMoves([]);
      setIsWhiteTurn(false);
      triggerAiMove(newBoard);
    } else if (piece && piece.isWhite) {
      setSelectedSquare([r, c]);
      setValidMoves(getValidMovesForPiece(r, c, boardState));
    } else {
      setSelectedSquare(null);
      setValidMoves([]);
    }
  };

  const resetChess = () => {
    setBoardState(initialChessBoard());
    setSelectedSquare(null);
    setValidMoves([]);
    setIsWhiteTurn(true);
    setStatusText("Your Turn (White)");
    setWhiteCaptured(0);
    setBlackCaptured(0);
  };

  return (
    <div className="glass-panel" style={{ width: '100%', maxWidth: '520px', margin: '0 auto', padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
      
      {/* Header and Capture counts */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '14px', fontWeight: '800', letterSpacing: '0.5px' }}>MIND CHESS ENGINE</div>
          <div style={{ fontSize: '11px', color: isWhiteTurn ? 'var(--nexus-violet)' : 'var(--nexus-magenta)', fontWeight: 'bold' }}>{statusText}</div>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          <span style={{ fontSize: '11px', background: 'rgba(255,255,255,0.06)', padding: '5px 10px', borderRadius: '8px', fontWeight: 'bold' }}>
            AI Got: {whiteCaptured}
          </span>
          <span style={{ fontSize: '11px', background: 'rgba(255,255,255,0.06)', padding: '5px 10px', borderRadius: '8px', fontWeight: 'bold' }}>
            You Got: {blackCaptured}
          </span>
        </div>
      </div>

      {/* 8x8 Chessboard */}
      <div style={{
        display: 'grid',
        gridTemplateRows: 'repeat(8, 1fr)',
        border: '3px solid white',
        borderRadius: '16px',
        overflow: 'hidden',
        aspectRatio: '1',
        width: '100%'
      }}>
        {boardState.map((row, r) => (
          <div key={r} style={{ display: 'flex', width: '100%', height: '100%' }}>
            {row.map((piece, c) => {
              const isLightSquare = (r + c) % 2 === 0;
              const isSelected = selectedSquare && selectedSquare[0] === r && selectedSquare[1] === c;
              const isValidDest = validMoves.some(([vr, vc]) => vr === r && vc === c);

              let squareColor = isLightSquare ? '#f1f5f9' : '#64748b';
              if (isSelected) squareColor = '#fde047'; // yellow
              else if (isValidDest) squareColor = '#86efac'; // green tint

              return (
                <div
                  key={c}
                  onClick={() => onSquareClick(r, c)}
                  style={{
                    flex: 1,
                    height: '100%',
                    background: squareColor,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: (isWhiteTurn && !isAiThinking) ? 'pointer' : 'not-allowed',
                    fontSize: '28px',
                    position: 'relative',
                    userSelect: 'none'
                  }}
                >
                  {piece && (
                    <span style={{ 
                      color: piece.isWhite ? '#1e293b' : '#ffffff',
                      textShadow: piece.isWhite ? '0 0 2px white' : '0 0 2px black',
                      fontWeight: 'bold'
                    }}>
                      {getChessSymbol(piece)}
                    </span>
                  )}
                  {isValidDest && !piece && (
                    <div style={{ width: '10px', height: '10px', borderRadius: '50%', background: '#16a34a' }} />
                  )}
                </div>
              );
            })}
          </div>
        ))}
      </div>

      {/* Restart Button */}
      <button 
        className="btn btn-secondary" 
        onClick={resetChess}
        style={{ width: '100%', gap: '8px', padding: '10px' }}
      >
        <RefreshCw size={16} /> New Chess Match
      </button>

    </div>
  );
}
