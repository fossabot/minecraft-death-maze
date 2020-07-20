package dev.vrba.minecraftdeathmaze.generators

import org.bukkit.Material._
import org.bukkit._
import org.bukkit.generator.ChunkGenerator

import scala.util.Random

object DeathMazeWorldGenerator {

  private object Builder {
    val nodeSize = 4
    val height = 5
    val buildingBlocks = Set(
      STONE,
      STONE_BRICKS,
      COBBLESTONE,
      CHISELED_STONE_BRICKS,
      MOSSY_COBBLESTONE,
      MOSSY_STONE_BRICKS,
      DEAD_BRAIN_CORAL_BLOCK,
      CRACKED_STONE_BRICKS,
      AIR
    )

    def fill(world: World, start: (Int, Int, Int), end: (Int, Int, Int), materials: Set[Material]): Unit =
      fill(
        new Location(world, start._1, start._2, start._3),
        new Location(world, end._1, end._2, end._3),
        materials
      )

    def fill(start: Location, end: Location, materials: Set[Material]): Unit = {
      // Refuse to fill space between two different worlds
      if (start.getWorld != end.getWorld) return

      for (x <- start.getBlockX to end.getBlockX;
           y <- start.getBlockY to end.getBlockY;
           z <- start.getBlockZ to end.getBlockZ)
      // Fill each block within the selected region
        start.getWorld.getBlockAt(x, y, z).setType(Random.shuffle(materials.toList).head)
    }
  }

  def generateWorld(players: Int): World = {
    val world = generateEmptyWorld()
    val maze = MazeGenerator.generateMaze(10 + (2 * players))

    buildMaze(world, maze)
  }

  private def buildMaze(world: World, maze: MazeGenerator.Maze): World = {
    // Build the floor
    Builder.fill(
      world,
      (-Builder.nodeSize, 0, -Builder.nodeSize),
      (Builder.nodeSize * maze.size, 0, Builder.nodeSize * maze.size),
      Builder.buildingBlocks - AIR
    )

    for (node <- maze.nodes) {
      for (neighborNode <- maze.neighbours(node)) {
        // Trump says that we gotta build a wall between those two nodes
        if (maze.hasDoorBetween(node, neighborNode)) {
          println(node)
          println(neighborNode)
          Builder.fill(
            world,
            (node.x * Builder.nodeSize, 1, node.y * Builder.nodeSize),
            (neighborNode.x * Builder.nodeSize, Builder.height, neighborNode.y * Builder.nodeSize),
            Builder.buildingBlocks
          )
        }
      }
    }

    world
  }

  private def generateEmptyWorld(): World = {
    val creator = new WorldCreator("dm_" + System.currentTimeMillis())

    creator.`type`(WorldType.FLAT)
    creator.generator(EmptyWorldChunkGenerator())

    val world = creator.createWorld()

    // Disable both monster and animal spawning
    world.setSpawnFlags(false, false)
    world.setDifficulty(Difficulty.HARD)

    world
  }

  private case class EmptyWorldChunkGenerator() extends ChunkGenerator {
    // Generate default chunk data without any populators so it yields an empty chunk
    override def generateChunkData(world: World,
                                   random: java.util.Random,
                                   x: Int,
                                   z: Int,
                                   biome: ChunkGenerator.BiomeGrid)
    : ChunkGenerator.ChunkData = createChunkData(world)
  }

}
