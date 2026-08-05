import React, { useState, useEffect, useRef } from 'react';
import { RefreshCw, Play, ShieldAlert, Award } from 'lucide-react';

export default function MindMaze() {
  const [mazeSize, setMazeSize] = useState(7); // 5x5, 7x7, 9x9
  const [grid, setGrid] = useState([]);
  const [playerPos, setPlayerPos] = useState({ r: 0, c: 0 });
  const [aiPos, setAiPos] = useState({ r: 0, c: 0 });
  const [goalPos, setGoalPos] = useState({ r: 6, c: 6 });
  
  const [stepCount, setStepCount] = useState(0);
  const [hasWon, setHasWon] = useState(false);
  const [aiWinner, setAiWinner] = useState(false);
  const [playerTrail, setPlayerTrail] = useState(new Set(["0,0"]));
  const [isAiRunning, setIsAiRunning] = useState(false);
  const [isAutoMoving, setIsAutoMoving] = useState(false);
  const [aiPath, setAiPath] = useState([]);

  // Use refs to access current state in async intervals
  const playerPosRef = useRef(playerPos);
  const aiPosRef = useRef(aiPos);
  const hasWonRef = useRef(hasWon);
  const aiWinnerRef = useRef(aiWinner);

  useEffect(() => { playerPosRef.current = playerPos; }, [playerPos]);
  useEffect(() => { aiPosRef.current = aiPos; }, [aiPos]);
  useEffect(() => { hasWonRef.current = hasWon; }, [hasWon]);
  useEffect(() => { aiWinnerRef.current = aiWinner; }, [aiWinner]);

  // Maze Generator: guarantees a path from (0,0) to (size-1, size-1)
  const generateMaze = (size) => {
    // Initialize all as walls (true = wall, false = path)
    const newGrid = Array.from({ length: size }, () => Array(size).fill(true));
    
    // Create a guaranteed random path from start to goal
    let curr = { r: 0, c: 0 };
    newGrid[0][0] = false;
    
    const path = [];
    path.push({ ...curr });

    while (curr.r !== size - 1 || curr.c !== size - 1) {
      const neighbors = [];
      if (curr.r < size - 1) neighbors.push({ r: curr.r + 1, c: curr.c });
      if (curr.c < size - 1) neighbors.push({ r: curr.r, c: curr.c + 1 });
      
      // Randomly select next step towards target
      const next = neighbors[Math.floor(Math.random() * neighbors.length)];
      newGrid[next.r][next.c] = false;
      curr = next;
      path.push({ ...curr });
    }

    // Carve additional random pathways so it looks like a real maze
    for (let r = 0; r < size; r++) {
      for (let c = 0; c < size; c++) {
        if (Math.random() > 0.4) {
          newGrid[r][c] = false;
        }
      }
    }
    
    // Ensure start, goal, and the guaranteed path are open
    path.forEach(node => {
      newGrid[node.r][node.c] = false;
    });

    newGrid[0][0] = false;
    newGrid[size - 1][size - 1] = false;

    return newGrid;
  };

  // BFS Solver to find shortest path
  const solveBFS = (mazeGrid, start, goal) => {
    const queue = [[start]];
    const visited = new Set([`${start.r},${start.c}`]);

    while (queue.length > 0) {
      const currentPath = queue.shift();
      const node = currentPath[currentPath.length - 1];

      if (node.r === goal.r && node.c === goal.c) {
        return currentPath;
      }

      const dirs = [[-1, 0], [1, 0], [0, -1], [0, 1]];
      for (let [dr, dc] of dirs) {
        const nr = node.r + dr;
        const nc = node.c + dc;
        const key = `${nr},${nc}`;

        if (
          nr >= 0 && nr < mazeGrid.length &&
          nc >= 0 && nc < mazeGrid.length &&
          !mazeGrid[nr][nc] &&
          !visited.has(key)
        ) {
          visited.add(key);
          queue.push([...currentPath, { r: nr, c: nc }]);
        }
      }
    }
    return [];
  };

  const initGame = (size = mazeSize) => {
    const newGrid = generateMaze(size);
    setGrid(newGrid);
    setPlayerPos({ r: 0, c: 0 });
    setAiPos({ r: 0, c: 0 });
    setGoalPos({ r: size - 1, c: size - 1 });
    setStepCount(0);
    setHasWon(false);
    setAiWinner(false);
    setPlayerTrail(new Set(["0,0"]));
    setIsAiRunning(false);
    setIsAutoMoving(false);
    setAiPath([]);
  };

  useEffect(() => {
    initGame(mazeSize);
  }, [mazeSize]);

  // Keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (hasWon || aiWinner || isAutoMoving) return;
      let dr = 0, dc = 0;
      if (e.key === "ArrowUp" || e.key === "w") dr = -1;
      else if (e.key === "ArrowDown" || e.key === "s") dr = 1;
      else if (e.key === "ArrowLeft" || e.key === "a") dc = -1;
      else if (e.key === "ArrowRight" || e.key === "d") dc = 1;

      if (dr !== 0 || dc !== 0) {
        e.preventDefault();
        movePlayer(dr, dc);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [grid, playerPos, hasWon, aiWinner, isAutoMoving]);

  const movePlayer = (dr, dc) => {
    const nr = playerPosRef.current.r + dr;
    const nc = playerPosRef.current.c + dc;

    if (nr >= 0 && nr < mazeSize && nc >= 0 && nc < mazeSize && !grid[nr][nc]) {
      const nextPos = { r: nr, c: nc };
      setPlayerPos(nextPos);
      setPlayerTrail(prev => {
        const next = new Set(prev);
        next.add(`${nr},${nc}`);
        return next;
      });
      setStepCount(s => s + 1);

      if (nr === goalPos.r && nc === goalPos.c) {
        setHasWon(true);
      }
    }
  };

  // Click-to-move auto walker
  const tapToMove = async (targetR, targetC) => {
    if (hasWon || aiWinner || isAutoMoving || grid[targetR][targetC]) return;
    if (playerPos.r === targetR && playerPos.c === targetC) return;

    const path = solveBFS(grid, playerPos, { r: targetR, c: targetC });
    if (path.length > 1) {
      setIsAutoMoving(true);
      // Skip start position (first node)
      const walkSteps = path.slice(1);
      
      for (let step of walkSteps) {
        if (hasWonRef.current || aiWinnerRef.current) break;
        setPlayerPos(step);
        setPlayerTrail(prev => {
          const next = new Set(prev);
          next.add(`${step.r},${step.c}`);
          return next;
        });
        setStepCount(s => s + 1);
        
        if (step.r === goalPos.r && step.c === goalPos.c) {
          setHasWon(true);
          break;
        }
        await new Promise(resolve => setTimeout(resolve, 55));
      }
      setIsAutoMoving(false);
    }
  };

  const startAiSolver = async () => {
    if (isAiRunning || hasWon || aiWinner) return;

    setIsAiRunning(true);
    const path = solveBFS(grid, aiPos, goalPos);
    setAiPath(path);

    for (let step of path) {
      if (hasWonRef.current) break;
      setAiPos(step);
      if (step.r === goalPos.r && step.c === goalPos.c && !hasWonRef.current) {
        setAiWinner(true);
        break;
      }
      await new Promise(resolve => setTimeout(resolve, 180));
    }
    setIsAiRunning(false);
  };

  return (
    <div className="glass-panel" style={{ width: '100%', maxWidth: '480px', margin: '0 auto', padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
      
      {/* Header and Controls */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '14px', fontWeight: '800', letterSpacing: '0.5px' }}>MIND MAZE AI</div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
            {hasWon ? `🎉 ESCAPED IN ${stepCount} STEPS!` : aiWinner ? "🤖 AI OUTRAN YOU!" : isAiRunning ? "⚡ Race in progress..." : "Find path to the pink portal!"}
          </div>
        </div>
        
        <div style={{ display: 'flex', gap: '6px' }}>
          {[5, 7, 9].map(sz => (
            <button
              key={sz}
              onClick={() => { setMazeSize(sz); }}
              style={{
                background: mazeSize === sz ? 'var(--nexus-violet)' : 'rgba(255,255,255,0.06)',
                border: 'none',
                color: 'white',
                fontSize: '11px',
                fontWeight: 'bold',
                padding: '5px 10px',
                borderRadius: '8px',
                cursor: 'pointer'
              }}
            >
              {sz}x{sz}
            </button>
          ))}
        </div>
      </div>

      {/* Grid container */}
      <div style={{
        display: 'grid',
        gridTemplateRows: `repeat(${mazeSize}, 1fr)`,
        gridTemplateColumns: `repeat(${mazeSize}, 1fr)`,
        gap: '4px',
        background: 'rgba(255,255,255,0.02)',
        border: '2px solid rgba(255,255,255,0.08)',
        borderRadius: '16px',
        padding: '6px',
        aspectRatio: '1',
        width: '100%',
        position: 'relative',
        overflow: 'hidden'
      }}>
        {grid.map((row, r) => 
          row.map((isWall, c) => {
            const isPlayer = playerPos.r === r && playerPos.c === c;
            const isAi = aiPos.r === r && aiPos.c === c;
            const isGoal = goalPos.r === r && goalPos.c === c;
            const isTrail = playerTrail.has(`${r},${c}`);

            let bg = 'rgba(0,0,0,0.3)'; // Path
            let border = 'none';
            if (isWall) {
              bg = 'linear-gradient(135deg, #1c1917, #0c0a09)'; // Wall
            } else if (isPlayer) {
              bg = '#3b82f6'; // Player (Blue)
              border = '2px solid white';
            } else if (isAi) {
              bg = '#ec4899'; // AI Bot (Magenta)
              border = '2px solid white';
            } else if (isGoal) {
              bg = 'linear-gradient(135deg, #d946ef, #a855f7)'; // Goal (Purple portal)
              border = '2px solid white';
            } else if (isTrail) {
              bg = 'rgba(59,130,246,0.15)'; // Player trail
            }

            return (
              <div
                key={`${r}-${c}`}
                onClick={() => tapToMove(r, c)}
                style={{
                  background: bg,
                  borderRadius: '6px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  cursor: isWall ? 'not-allowed' : 'pointer',
                  border: border,
                  transition: 'background 0.15s, transform 0.15s',
                  boxShadow: isGoal ? '0 0 10px rgba(217,70,239,0.5)' : 'none',
                  transform: (isPlayer || isAi) ? 'scale(0.9)' : 'none'
                }}
              >
                {isPlayer && <span style={{ fontSize: '10px', fontWeight: 'bold' }}>👤</span>}
                {isAi && <span style={{ fontSize: '10px', fontWeight: 'bold' }}>🤖</span>}
                {isGoal && <span style={{ fontSize: '10px', fontWeight: 'bold' }}>🏁</span>}
              </div>
            );
          })
        )}
      </div>

      {/* Button controls */}
      <div style={{ display: 'flex', gap: '10px' }}>
        <button
          className="btn btn-primary"
          onClick={startAiSolver}
          disabled={isAiRunning || hasWon || aiWinner}
          style={{ flex: 1, gap: '8px', fontSize: '13px', padding: '10px' }}
        >
          <Play size={16} /> Start AI Solver Race
        </button>

        <button
          className="btn btn-secondary"
          onClick={() => initGame(mazeSize)}
          style={{ gap: '8px', fontSize: '13px', padding: '10px' }}
        >
          <RefreshCw size={16} /> Regenerate
        </button>
      </div>

      <div style={{ fontSize: '10px', color: 'var(--text-muted)', textAlign: 'center' }}>
        💡 keyboard WASD/Arrows to walk, or just click any empty tile to auto-navigate.
      </div>
    </div>
  );
}
