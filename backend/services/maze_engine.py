import random
import heapq
from typing import List, Tuple, Dict, Any

def generate_procedural_maze(rows: int = 15, cols: int = 15) -> Dict[str, Any]:
    """
    Generates a maze using Recursive Backtracking (DFS).
    1 = Wall, 0 = Open Path.
    """
    # Ensure odd dimensions for proper wall/path grid
    r_count = rows if rows % 2 != 0 else rows + 1
    c_count = cols if cols % 2 != 0 else cols + 1

    grid = [[1 for _ in range(c_count)] for _ in range(r_count)]

    stack = [(1, 1)]
    grid[1][1] = 0

    while stack:
        curr_r, curr_c = stack[-1]
        neighbors = []
        for dr, dc in [(-2, 0), (2, 0), (0, -2), (0, 2)]:
            nr, nc = curr_r + dr, curr_c + dc
            if 0 < nr < r_count - 1 and 0 < nc < c_count - 1 and grid[nr][nc] == 1:
                neighbors.append((nr, nc, dr, dc))

        if neighbors:
            nr, nc, dr, dc = random.choice(neighbors)
            # Carve path
            grid[curr_r + dr // 2][curr_c + dc // 2] = 0
            grid[nr][nc] = 0
            stack.append((nr, nc))
        else:
            stack.pop()

    start = [1, 1]
    end = [r_count - 2, c_count - 2]
    grid[start[0]][start[1]] = 0
    grid[end[0]][end[1]] = 0

    # Calculate A* path
    solution_path = solve_maze_astar(grid, tuple(start), tuple(end))

    return {
        "rows": r_count,
        "cols": c_count,
        "grid": grid,
        "start": start,
        "end": end,
        "solutionPath": solution_path,
        "stepCount": len(solution_path)
    }

def solve_maze_astar(grid: List[List[int]], start: Tuple[int, int], end: Tuple[int, int]) -> List[List[int]]:
    """
    Solves maze using A* algorithm with Manhattan Distance heuristic.
    """
    r_count = len(grid)
    c_count = len(grid[0])

    def heuristic(a: Tuple[int, int], b: Tuple[int, int]) -> int:
        return abs(a[0] - b[0]) + abs(a[1] - b[1])

    open_set = []
    heapq.heappush(open_set, (0, start))
    came_from = {}
    g_score = {start: 0}
    f_score = {start: heuristic(start, end)}

    while open_set:
        _, current = heapq.heappop(open_set)

        if current == end:
            path = []
            curr = current
            while curr in came_from:
                path.append([curr[0], curr[1]])
                curr = came_from[curr]
            path.append([start[0], start[1]])
            path.reverse()
            return path

        for dr, dc in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            neighbor = (current[0] + dr, current[1] + dc)
            nr, nc = neighbor
            if 0 <= nr < r_count and 0 <= nc < c_count and grid[nr][nc] == 0:
                tentative_g = g_score[current] + 1
                if neighbor not in g_score or tentative_g < g_score[neighbor]:
                    came_from[neighbor] = current
                    g_score[neighbor] = tentative_g
                    f_score[neighbor] = tentative_g + heuristic(neighbor, end)
                    heapq.heappush(open_set, (f_score[neighbor], neighbor))

    return []
