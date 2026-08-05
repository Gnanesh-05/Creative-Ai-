import React, { useState } from 'react';
import TicTacToe from '../games/TicTacToe';
import ChessGame from '../games/ChessGame';
import MindMaze from '../games/MindMaze';
import { Gamepad2, Grid, Puzzle, Award } from 'lucide-react';

export default function GameCenterView() {
  const [activeGame, setActiveGame] = useState(0); // 0: Tic Tac Toe, 1: Chess AI, 2: Mind Maze

  const gamesList = [
    { id: 0, title: "Tic-Tac-Toe", icon: Grid, component: TicTacToe },
    { id: 1, title: "Chess AI", icon: Puzzle, component: ChessGame },
    { id: 2, title: "Mind Maze", icon: Award, component: MindMaze }
  ];

  const ActiveComponent = gamesList[activeGame].component;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', padding: '10px 0' }}>
      
      {/* Page Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
        <Gamepad2 size={24} style={{ color: 'var(--nexus-violet)' }} />
        <h2 style={{ fontSize: '20px', fontWeight: '800', fontFamily: 'var(--font-brand)', letterSpacing: '1px', textTransform: 'uppercase' }}>
          AI Game Center
        </h2>
        <span style={{ fontSize: '11px', background: 'linear-gradient(135deg, var(--nexus-violet), var(--nexus-magenta))', color: 'white', padding: '3px 10px', borderRadius: '12px', fontWeight: 'bold' }}>
          MIND ENGINE ACTIVE
        </span>
      </div>

      {/* Tabs */}
      <div className="glass-panel" style={{ 
        display: 'flex', 
        padding: '4px', 
        borderRadius: '24px', 
        maxWidth: '480px', 
        width: '100%',
        margin: '0 auto',
        border: '1px solid var(--border-glass)'
      }}>
        {gamesList.map(game => {
          const Icon = game.icon;
          const isSelected = activeGame === game.id;
          return (
            <button
              key={game.id}
              onClick={() => setActiveGame(game.id)}
              style={{
                flex: 1,
                background: isSelected ? 'linear-gradient(135deg, var(--nexus-violet), var(--nexus-magenta))' : 'transparent',
                border: 'none',
                color: 'white',
                padding: '12px',
                borderRadius: '20px',
                fontSize: '13px',
                fontWeight: 'bold',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
                transition: 'all 0.3s'
              }}
            >
              <Icon size={14} />
              {game.title}
            </button>
          );
        })}
      </div>

      {/* Active Game Container */}
      <div style={{ marginTop: '10px', display: 'flex', justifyContent: 'center' }}>
        <ActiveComponent />
      </div>

    </div>
  );
}
