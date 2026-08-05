import React, { useState, useEffect } from 'react';
import { RefreshCw, Trophy, Award } from 'lucide-react';

export default function TicTacToe() {
  const [board, setBoard] = useState(Array(9).fill(""));
  const [isPlayerTurn, setIsPlayerTurn] = useState(true);
  const [difficulty, setDifficulty] = useState("Smart Mind AI"); // Easy, Smart Mind AI, Unbeatable
  const [scores, setScores] = useState({ player: 0, draws: 0, ai: 0 });
  const [winner, setWinner] = useState(null); // 'X', 'O', 'Draw', null
  const [winningLine, setWinningLine] = useState(null);
  const [isAiThinking, setIsAiThinking] = useState(false);

  const winPatterns = [
    [0, 1, 2], [3, 4, 5], [6, 7, 8], // Rows
    [0, 3, 6], [1, 4, 7], [2, 5, 8], // Cols
    [0, 4, 8], [2, 4, 6]             // Diagonals
  ];

  const checkWinner = (b) => {
    for (let pattern of winPatterns) {
      const [a, c1, c2] = pattern;
      if (b[a] && b[a] === b[c1] && b[a] === b[c2]) {
        return { char: b[a], pattern };
      }
    }
    if (b.every(cell => cell !== "")) {
      return { char: "Draw", pattern: null };
    }
    return { char: null, pattern: null };
  };

  // AI Logic
  const getSmartMove = (b) => {
    // 1. Can AI win in this move?
    for (let i = 0; i < 9; i++) {
      if (b[i] === "") {
        const copy = [...b];
        copy[i] = "O";
        if (checkWinner(copy).char === "O") return i;
      }
    }

    // 2. Can Player win in their next move? Block them.
    for (let i = 0; i < 9; i++) {
      if (b[i] === "") {
        const copy = [...b];
        copy[i] = "X";
        if (checkWinner(copy).char === "X") return i;
      }
    }

    // 3. Take Center
    if (b[4] === "") return 4;

    // 4. Take Corners
    const corners = [0, 2, 6, 8].filter(c => b[c] === "");
    if (corners.length > 0) return corners[Math.floor(Math.random() * corners.length)];

    // 5. Take Sides
    const sides = [1, 3, 5, 7].filter(s => b[s] === "");
    if (sides.length > 0) return sides[Math.floor(Math.random() * sides.length)];

    return -1;
  };

  // Minimax Unbeatable Move
  const minimax = (b, depth, isMaximizing) => {
    const score = evaluateBoard(b);

    if (score === 10) return score - depth;
    if (score === -10) return score + depth;
    if (b.every(c => c !== "")) return 0;

    if (isMaximizing) {
      let best = -1000;
      for (let i = 0; i < 9; i++) {
        if (b[i] === "") {
          b[i] = "O";
          best = Math.max(best, minimax(b, depth + 1, false));
          b[i] = "";
        }
      }
      return best;
    } else {
      let best = 1000;
      for (let i = 0; i < 9; i++) {
        if (b[i] === "") {
          b[i] = "X";
          best = Math.min(best, minimax(b, depth + 1, true));
          b[i] = "";
        }
      }
      return best;
    }
  };

  const evaluateBoard = (b) => {
    for (let pattern of winPatterns) {
      const [a, c1, c2] = pattern;
      if (b[a] && b[a] === b[c1] && b[a] === b[c2]) {
        if (b[a] === "O") return 10;
        if (b[a] === "X") return -10;
      }
    }
    return 0;
  };

  const getBestMove = (b) => {
    let bestVal = -1000;
    let bestMove = -1;

    for (let i = 0; i < 9; i++) {
      if (b[i] === "") {
        b[i] = "O";
        const moveVal = minimax(b, 0, false);
        b[i] = "";
        if (moveVal > bestVal) {
          bestVal = moveVal;
          bestMove = i;
        }
      }
    }
    return bestMove;
  };

  const triggerAiMove = (currentBoard) => {
    setIsAiThinking(true);
    setTimeout(() => {
      const emptyIndices = currentBoard.map((c, i) => c === "" ? i : null).filter(c => c !== null);
      if (emptyIndices.length > 0) {
        let aiIdx = -1;
        if (difficulty === "Easy") {
          aiIdx = emptyIndices[Math.floor(Math.random() * emptyIndices.length)];
        } else if (difficulty === "Unbeatable") {
          aiIdx = getBestMove(currentBoard);
        } else {
          // Smart AI
          aiIdx = getSmartMove(currentBoard);
          if (aiIdx === -1) {
            aiIdx = emptyIndices[Math.floor(Math.random() * emptyIndices.length)];
          }
        }

        if (aiIdx !== -1) {
          const nextBoard = [...currentBoard];
          nextBoard[aiIdx] = "O";
          setBoard(nextBoard);

          const result = checkWinner(nextBoard);
          if (result.char) {
            setWinner(result.char);
            setWinningLine(result.pattern);
            if (result.char === "O") {
              setScores(s => ({ ...s, ai: s.ai + 1 }));
            } else if (result.char === "Draw") {
              setScores(s => ({ ...s, draws: s.draws + 1 }));
            }
          } else {
            setIsPlayerTurn(true);
          }
        }
      }
      setIsAiThinking(false);
    }, 500);
  };

  const handleCellClick = (index) => {
    if (board[index] !== "" || winner || !isPlayerTurn || isAiThinking) return;

    const nextBoard = [...board];
    nextBoard[index] = "X";
    setBoard(nextBoard);

    const result = checkWinner(nextBoard);
    if (result.char) {
      setWinner(result.char);
      setWinningLine(result.pattern);
      if (result.char === "X") {
        setScores(s => ({ ...s, player: s.player + 1 }));
      } else if (result.char === "Draw") {
        setScores(s => ({ ...s, draws: s.draws + 1 }));
      }
    } else {
      setIsPlayerTurn(false);
      triggerAiMove(nextBoard);
    }
  };

  const resetGame = () => {
    setBoard(Array(9).fill(""));
    setWinner(null);
    setWinningLine(null);
    setIsPlayerTurn(true);
    setIsAiThinking(false);
  };

  return (
    <div className="glass-panel" style={{ width: '100%', maxWidth: '480px', margin: '0 auto', padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
      
      {/* Top Controls */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: '11px', fontWeight: 'bold', color: 'var(--text-secondary)', letterSpacing: '1px' }}>DIFFICULTY:</span>
          <div style={{ display: 'flex', gap: '6px' }}>
            {["Easy", "Smart Mind AI", "Unbeatable"].map(level => (
              <button 
                key={level} 
                onClick={() => { setDifficulty(level); resetGame(); }}
                style={{ 
                  background: difficulty === level ? 'var(--nexus-violet)' : 'rgba(255,255,255,0.06)',
                  border: 'none',
                  color: 'white',
                  fontSize: '11px',
                  fontWeight: 'bold',
                  padding: '6px 12px',
                  borderRadius: '10px',
                  cursor: 'pointer',
                  transition: 'background 0.2s'
                }}
              >
                {level}
              </button>
            ))}
          </div>
        </div>

        {/* Scoreboard */}
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: '10px' }}>
          <div style={{ flex: 1, background: 'rgba(59,130,246,0.1)', border: '1px solid rgba(59,130,246,0.2)', padding: '10px', borderRadius: '14px', textAlign: 'center' }}>
            <div style={{ fontSize: '10px', fontWeight: 'bold', color: '#60a5fa' }}>YOU (X)</div>
            <div style={{ fontSize: '20px', fontWeight: '800' }}>{scores.player}</div>
          </div>
          <div style={{ flex: 1, background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.08)', padding: '10px', borderRadius: '14px', textAlign: 'center' }}>
            <div style={{ fontSize: '10px', fontWeight: 'bold', color: 'var(--text-secondary)' }}>DRAWS</div>
            <div style={{ fontSize: '20px', fontWeight: '800' }}>{scores.draws}</div>
          </div>
          <div style={{ flex: 1, background: 'rgba(236,72,153,0.1)', border: '1px solid rgba(236,72,153,0.2)', padding: '10px', borderRadius: '14px', textAlign: 'center' }}>
            <div style={{ fontSize: '10px', fontWeight: 'bold', color: '#f472b6' }}>MIND AI (O)</div>
            <div style={{ fontSize: '20px', fontWeight: '800' }}>{scores.ai}</div>
          </div>
        </div>
      </div>

      {/* Turn Banner / Winner Card */}
      <div style={{ 
        padding: '12px', 
        borderRadius: '14px', 
        textAlign: 'center', 
        fontWeight: 'bold', 
        fontSize: '14px',
        background: winner === "X" ? 'var(--success)' : winner === "O" ? 'var(--error)' : winner === "Draw" ? 'var(--warning)' : 'rgba(255,255,255,0.05)',
        color: 'white',
        letterSpacing: '0.5px'
      }}>
        {winner === "X" && "✨ VICTORY! You defeated the AI!"}
        {winner === "O" && "💀 DEFEAT! Mind AI won the match."}
        {winner === "Draw" && "🤝 DRAW! A perfect match."}
        {!winner && (isPlayerTurn ? "🎮 YOUR TURN" : "⚡ MIND AI IS THINKING...")}
      </div>

      {/* Tic Tac Toe Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: '12px',
        aspectRatio: '1',
        width: '100%',
        margin: '0 auto'
      }}>
        {board.map((cell, i) => {
          const isWinningCell = winningLine && winningLine.includes(i);
          return (
            <div 
              key={i}
              onClick={() => handleCellClick(i)}
              style={{
                background: isWinningCell 
                  ? 'linear-gradient(135deg, var(--nexus-violet), var(--nexus-magenta))' 
                  : cell !== "" ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.25)',
                border: isWinningCell ? '1.5px solid white' : '1px solid var(--border-glass)',
                borderRadius: '16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '36px',
                fontWeight: '800',
                cursor: cell !== "" || winner || isAiThinking ? 'not-allowed' : 'pointer',
                transition: 'all 0.2s',
                color: isWinningCell ? 'white' : cell === "X" ? '#60a5fa' : '#f472b6',
                transform: cell !== "" ? 'scale(1)' : 'none',
                boxShadow: isWinningCell ? '0 0 15px var(--magenta-glow)' : 'none'
              }}
              className="cell-btn"
            >
              {cell}
            </div>
          );
        })}
      </div>

      {/* Reset button */}
      <button 
        className="btn btn-secondary" 
        onClick={resetGame} 
        style={{ width: '100%', gap: '8px', padding: '10px' }}
      >
        <RefreshCw size={16} /> Reset Match
      </button>
    </div>
  );
}
