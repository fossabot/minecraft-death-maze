package dev.vrba.minecraftdeathmaze.generators

import scala.util.Random

object MazeGenerator {

  case class Direction(x: Int, y: Int)

  case class MazeNodeDoor(from: MazeNode, to: MazeNode)

  case class MazeNode(x: Int, y: Int) {
    def +(direction: Direction): MazeNode = MazeNode(x + direction.x, y + direction.y)
  }

  // This code is assuming the maze is always square
  case class Maze(size: Int, nodes: Set[MazeNode], doors: Set[MazeNodeDoor]) {
    private val directions = Set(
      Direction(-1, 0),
      Direction(0, -1),
      Direction(1, 0),
      Direction(0, 1),
    )

    private def isWithinBounds(node: MazeNode): Boolean = (0 to size).contains(node.x) && (0 to size).contains(node.y)

    def neighbours(node: MazeNode): Set[MazeNode] = directions.map(node + _).filter(isWithinBounds)

    def unvisitedNeighbours(node: MazeNode): Set[MazeNode] = neighbours(node) -- nodes

    def hasDoorBetween(first: MazeNode, second: MazeNode): Boolean =
      doors.contains(MazeNodeDoor(first, second)) || doors.contains(MazeNodeDoor(second, first))
  }

  private def resolveNode(node: MazeNode, maze: Maze): Maze = {
    var extended = Maze(maze.size, maze.nodes + node, maze.doors)
    val neighbours = Random.shuffle(extended.unvisitedNeighbours(node).toList)

    neighbours foreach { neighbour =>
      if (!extended.nodes.contains(neighbour)) {
        extended = resolveNode(
          neighbour,
          Maze(extended.size, extended.nodes + node, extended.doors + MazeNodeDoor(node, neighbour))
        )
      }
    }

    extended
  }

  // Start in the center for a better gaussian distance distribution
  def generateMaze(size: Int): Maze = resolveNode(MazeNode(size / 2, size / 2), Maze(size, Set.empty, Set.empty))
}
