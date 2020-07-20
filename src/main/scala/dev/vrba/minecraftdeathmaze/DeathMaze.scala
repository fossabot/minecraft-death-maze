package dev.vrba.minecraftdeathmaze

import dev.vrba.minecraftdeathmaze.generators.DeathMazeWorldGenerator
import org.bukkit.Location
import org.bukkit.command.{Command, CommandSender}
import org.bukkit.plugin.java.JavaPlugin

import scala.util.Random

class DeathMaze extends JavaPlugin {
  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    if (command.getName == "death-maze") {
      this.startNewDeathMazeSession()
      return true
    }

    false
  }

  private def startNewDeathMazeSession(): Unit = {
    val players = this.getServer.getOnlinePlayers
    val world = DeathMazeWorldGenerator.generateWorld(players.size)

    players.forEach(player => player.teleport(
      new Location(
        world,
        Random.nextInt(10 + (players.size * 2)) * 4 + 2,
        1,
        Random.nextInt(10 + (players.size * 2)) * 4 + 2
      )
    )
    )
  }
}