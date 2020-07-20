package dev.vrba.minecraftdeathmaze

import org.bukkit.command.{Command, CommandSender}
import org.bukkit.plugin.java.JavaPlugin

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
    // TODO: Generate death maze world and spread players across the map
  }
}