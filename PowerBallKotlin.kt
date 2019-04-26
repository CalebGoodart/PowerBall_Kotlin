package powerball.powerball

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.collections.HashMap

class PowerBallKotlin : JavaPlugin() {

    var playerKills = HashMap<UUID, Int>()
    var playersOnDashCoolDown = HashMap<UUID, Player>()
    var gameRunning = false
    val jumpHeight = 3.0
    val killsToWin = 10
    val fastFallSpeed = -5.0

    override fun onEnable() {
        super.onEnable()

        this.getCommand("startgame")!!.setExecutor(PowerBallCommands(this))
        this.getCommand("endgame")!!.setExecutor(PowerBallCommands(this))

        this.server.pluginManager.registerEvents(PowerBallListeners(this), this)
    }

    override fun onDisable() {
        super.onDisable()
    }

    fun startGame() {

        if (gameRunning) throw IllegalStateException()

        gameRunning = true

        object : BukkitRunnable() {

            var counter = 10
            override fun run() {
                if (counter <= 0) {
                    server.broadcastMessage("Starting Game")
                    for (player in server.onlinePlayers) {
                        playerKills[player.uniqueId] = 0
                        spawn(player)
                    }
                    this.cancel()
                } else {
                    server.broadcastMessage("$counter Seconds left")
                    counter--
                }
            }
        }.runTaskTimer(this, 0, 20)

        server.broadcastMessage("Game is Starting!")
    }

    fun endGame(winner: Player) {
        if (!gameRunning) throw IllegalStateException()

        gameRunning = false
        playerKills.clear()
        server.broadcastMessage("${winner.name} Has Won!")
        val spawnPoint = Location(winner.world, 0.0, 100.0, 0.0)
        for (player in server.onlinePlayers) {
            player.inventory.clear()
            player.teleport(spawnPoint)
        }
    }

    fun spawn(player: Player) {
        playerKills.putIfAbsent(player.uniqueId, 0)
        player.inventory.clear()
        player.inventory.setItem(0, ItemStack(Material.SNOWBALL, 64))
        val spawnPoint = Location(player.world, 0.0, 100.0, 0.0)
        kit(player)
        player.teleport(spawnPoint)
    }

    fun kit(player: Player) {

        val itemStack = ItemStack(Material.SPLASH_POTION, 2)
        val potionMeta = (itemStack.itemMeta as PotionMeta)
        potionMeta.addCustomEffect(PotionEffect(PotionEffectType.SPEED, 120, 2), true)
        itemStack.itemMeta = potionMeta
        player.inventory.setItem(1, itemStack)
    }
}