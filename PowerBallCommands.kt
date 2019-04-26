package powerball.powerball

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class PowerBallCommands(plugin: JavaPlugin) : CommandExecutor {
    private val powerBallKotlin = plugin as PowerBallKotlin
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {

        if (label == "startgame"){
            try {
                powerBallKotlin.startGame()
            }catch (exception: IllegalStateException){
                sender.sendMessage("Game is already running!")
            }
        }

        if (label == "endgame"){
            val uuid = powerBallKotlin.playerKills.maxBy { it.value }!!.key
            val winner = powerBallKotlin.server.getPlayer(uuid) as Player

            try {

                if (sender is Player) powerBallKotlin.endGame(winner)
                else powerBallKotlin.endGame(winner)
            }catch (exception: IllegalStateException){
                sender.sendMessage("Game is not running!")
            }
        }

        return true
    }
}