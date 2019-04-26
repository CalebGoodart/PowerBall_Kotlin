package powerball.powerball

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class PowerBallListeners(plugin: JavaPlugin) : Listener{

    val powerBallKotlin = plugin as PowerBallKotlin

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {

        val player = event.player

        event.joinMessage = "Welcome ${player.name}"

        if (powerBallKotlin.gameRunning) powerBallKotlin.spawn(player)
    }

    @EventHandler
    fun playerLaunch(event: PlayerToggleSneakEvent) {
        val player = event.player

        if (player.isOnGround && player.location.block.getRelative(BlockFace.DOWN).type == Material.BROWN_MUSHROOM_BLOCK) {
            player.velocity = Vector(player.velocity.x, powerBallKotlin.jumpHeight, player.velocity.z)
        }
    }

    @EventHandler
    fun playerBounce(event: PlayerMoveEvent) {

        val player = event.player
        val to = event.to
        val from = event.from

        if (to == from) return

        if (from.y - to!!.y > .6 &&
                to.block.getRelative(BlockFace.DOWN).type == Material.BROWN_MUSHROOM_BLOCK) {

            player.velocity = Vector(player.velocity.x, powerBallKotlin.jumpHeight, player.velocity.z)
        }
    }

    @EventHandler
    fun playerFastFall(event: PlayerToggleSneakEvent) {
        val player = event.player

        if (!player.isOnGround && player.velocity.y <= 0) {

            player.velocity = Vector(player.velocity.x, powerBallKotlin.fastFallSpeed, player.velocity.z)
        }
    }

    @EventHandler
    fun playerDash(event: PlayerInteractEvent) {
        val player = event.player

        if (event.action == Action.LEFT_CLICK_AIR && player.inventory.itemInMainHand.type == Material.SNOWBALL) {

            if (powerBallKotlin.playersOnDashCoolDown.containsKey(player.uniqueId)) {
                player.sendMessage("On CoolDown!")
            } else {

                player.velocity = Vector(player.location.direction.x * 3,
                        0.0, player.location.direction.z * 3)
                powerBallKotlin.playersOnDashCoolDown[player.uniqueId] = player

                object : BukkitRunnable(){
                    override fun run(){
                        powerBallKotlin.playersOnDashCoolDown.remove(player.uniqueId)
                    }
                }.runTaskLater(powerBallKotlin, 3 * 20)
            }
        }
    }

    @EventHandler
    fun snowBallHit(event: ProjectileHitEvent){

        if (event.entity is Snowball && powerBallKotlin.gameRunning){
            if (event.entity.shooter is Player){
                if (event.hitEntity != null){
                    val killer = (event.entity.shooter as Player).player as Player
                    val killed = event.hitEntity

                    powerBallKotlin.playerKills[killer.uniqueId] = powerBallKotlin.playerKills[killer.uniqueId]!!.plus(1)
                    killer.sendMessage("Killed ${killed!!.name}")
                    if (powerBallKotlin.playerKills[killer.uniqueId] == powerBallKotlin.killsToWin){
                        PowerBallKotlin().endGame(killer)
                    }else{
                        if (killed is Player){
                            killed.sendMessage("Killed by ${killer.name}")
                            powerBallKotlin.spawn(killed)
                        }else{
                            killed.remove()
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private fun fallDamage(event: EntityDamageEvent){
        if (event.cause.equals(EntityDamageEvent.DamageCause.FALL)){
            event.isCancelled = true
        }
    }

    @EventHandler
    fun noBlockBreak(event: BlockBreakEvent){
        if (!event.player.isOp){
            event.isCancelled = true
        }
    }
}