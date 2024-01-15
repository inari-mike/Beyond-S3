package s3.ai;

import static java.lang.Math.abs;

import java.util.*;

import s3.base.S3;
import s3.entities.S3PhysicalEntity;
import s3.util.Pair;

public class AStar {
	private final double start_x;
	private final double start_y;
	private final double goal_x;
	private final double goal_y;
	private final S3PhysicalEntity i_entity;
	private final S3 the_game;
	private final boolean goalIsValid;

	public AStar(double start_x, double start_y, double goal_x, double goal_y, S3PhysicalEntity i_entity, S3 the_game) {
		this.start_x = start_x;
		this.start_y = start_y;
		this.goal_x = goal_x;
		this.goal_y = goal_y;
		this.i_entity = i_entity;
		this.the_game = the_game;

		int _x = i_entity.getX();
		int _y = i_entity.getY();
		i_entity.setX((int) goal_x);
		i_entity.setY((int) goal_y);
		this.goalIsValid = the_game.anyLevelCollision(i_entity) == null;
		i_entity.setX(_x);
		i_entity.setY(_y);
	}

	public static int pathDistance(double start_x, double start_y, double goal_x, double goal_y,
			S3PhysicalEntity i_entity, S3 the_game) {
		AStar a = new AStar(start_x, start_y, goal_x, goal_y, i_entity, the_game);
		List<Pair<Double, Double>> path = a.computePath();
		if (path != null) {
			return path.size();
		}
		return -1;
	}

	public List<Pair<Double, Double>> computePath() {
		if (goalIsValid) {
			// sudo code:
			// initialize fringe
			// while fringe not empty, pop node with the smallest cost
			// if pop-out node is the target, stop, trace back
			// else, expand the node
			// add free cell nodes to the fringe
			// if fringe is empty and no path found, return null
			List<Cell> priorQueue = new ArrayList<>();
			Set<String> expandedPositions = new HashSet<>();
			Cell startCell = new Cell((int) start_x, (int) start_y, null);
			priorQueue.add(startCell);
			expandedPositions.add(startCell.ToString());
			while (!priorQueue.isEmpty()) {
				Cell currentCell = priorQueue.get(0);
				priorQueue.remove(0);
				if (currentCell.isGoal()) {
					return currentCell.getPath();
				} else {
					for (Cell neighborCell : currentCell.getNeighbors()) {
						if (!expandedPositions.contains(neighborCell.ToString())) {
							priorQueue.add(neighborCell);
							expandedPositions.add(neighborCell.ToString());
						}
					}
					Collections.sort(priorQueue);
				}
			}
			return null;
		}
		return null;
	}

	private class Cell implements Comparable {
		final static int[][] MOVES = new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

		final Cell prev;
		final int x;
		final int y;
		final int g;
		final int h;
		final int cost;
		final String string;

		public Cell(int x, int y, Cell prev) {
			this.prev = prev;
			this.x = x;
			this.y = y;
			this.g = prev != null ? prev.getG() + 1 : 0;
			this.h = (int) (abs(x - goal_x) + abs(y - goal_y));
			this.cost = this.g + this.h;
			this.string = String.format("%d-%d", x, y);
		}

		public String ToString() {
			return string;
		}

		public boolean isGoal() {
			return (x == goal_x) && (y == goal_y);
		}

		public Cell getPrev() {
			return prev;
		}

		private int getX() {
			return x;
		}

		private int getY() {
			return y;
		}

		public int getG() {
			return g;
		}

		public int getCost() {
			return cost;
		}

		public boolean isAvailable() {
			if (x >= 0 && x < the_game.getMap().getWidth() && y >= 0 && y < the_game.getMap().getHeight()) {
				int _x = i_entity.getX();
				int _y = i_entity.getY();
				i_entity.setX(x);
				i_entity.setY(y);
				boolean noCollision = the_game.anyLevelCollision(i_entity) == null;
				i_entity.setX(_x);
				i_entity.setY(_y);
				return noCollision;
			} else {
				return false;
			}
		}

		public List<Cell> getNeighbors() { // only return cells which is goal or free
			List<Cell> freeNeighbors = new ArrayList<>();
			for (int[] deltaXY : MOVES) {
				int newX = x + deltaXY[0];
				int newY = y + deltaXY[1];
				Cell neighborCell = new Cell(newX, newY, this);
				if (neighborCell.isAvailable()) {
					freeNeighbors.add(neighborCell);
				}

			}
			return freeNeighbors;
		}

		public List<Pair<Double, Double>> getPath() {
			List<Pair<Double, Double>> path = new ArrayList<>();
			Cell c = this;
			while (c.prev != null) {
				path.add(new Pair<>((double) c.getX(), (double) c.getY()));
				c = c.getPrev();
			}
			Collections.reverse(path);
			return path;
		}

		@Override
		public int compareTo(Object o) {
			return o instanceof Cell ? Integer.compare(this.getCost(), ((Cell) o).getCost()) : 0;
		}
	}
}