package powerball.powerball;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class Powerball extends JavaPlugin implements Listener {

    private Map<UUID, Integer> playerKills = new HashMap<>();
    private Map<UUID, Player> playersOnDashCoolDown = new HashMap<>();
    private boolean gameRunning = false;

    @Override
    public void onEnable() {
        super.onEnable();

        this.getCommand("startgame").setExecutor(new startGame());

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        event.setJoinMessage("Welcome, " + player.getName() + "to My Server!");

        //makes the player boucne
//        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
//            public void run() {
//
//                if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BROWN_MUSHROOM_BLOCK
//                        && player.getVelocity().getY() < 0) {
//
//                    Vector c = new Vector(player.getVelocity().getX(), 3, player.getVelocity().getZ());
//                    player.setVelocity(c);
//
//                }
//            }
//        }, 0, 1);
    }

    @EventHandler
    public void playerLaunch(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && player.isOnGround() && (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BROWN_MUSHROOM_BLOCK)) {
            player.setVelocity(new Vector(player.getVelocity().getX(), 5, player.getVelocity().getZ()));
        }
    }

    @EventHandler
    public void playerFastFall(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && !(player.isOnGround()) && ((player.getVelocity().getY() <= 0))) {
            player.setVelocity(new Vector(player.getVelocity().getX(), -5, player.getVelocity().getZ()));
        }
    }

    @EventHandler
    public void playerDash(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_AIR && player.getInventory().getItemInMainHand().getType() == Material.SNOWBALL) {

            if (playersOnDashCoolDown.containsKey(player.getUniqueId())) {

                player.sendMessage("On CoolDown!");

            } else {

                Vector dashSpeed = new Vector(player.getLocation().getDirection().getX() * 3,
                        player.getLocation().getDirection().getY(),
                        player.getLocation().getDirection().getZ() * 3);

                player.setVelocity(dashSpeed);
                playersOnDashCoolDown.put(player.getUniqueId(), player);

                new BukkitRunnable() {
                    public void run() {
                        playersOnDashCoolDown.remove(player.getUniqueId());
                    }
                }.runTaskLater(this, 3 * 20);
            }
        }
    }

    @EventHandler
    public void fart(PlayerToggleSneakEvent event) {

        Player player = event.getPlayer();
        if (player.isSneaking()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT,
                    (float) (Math.random() * 10), (float) (Math.random() * 10));
        }
    }

    public void startGame() {

        gameRunning = true;

        new BukkitRunnable() {

            int counter = 10;

            public void run() {
                if (counter <= 0) {
                    getServer().broadcastMessage("starting game");
                    //spawn player and give flash
                    for (Player player : getServer().getOnlinePlayers()) {
                        spawn(player);
                    }
                    this.cancel();
                } else {
                    getServer().broadcastMessage(counter + " Seconds left!");
                    counter--;
                }
            }
        }.runTaskTimer(this, 0, 20);

        this.getServer().broadcastMessage("Game is staring!");
    }

    private void giveKit(Player player) {
        ItemStack itemStack = new ItemStack(Material.SPLASH_POTION, 2);
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 128 * 20, 10), true);
        itemStack.setItemMeta(potionMeta);
        player.getInventory().setItem(1, itemStack);
    }

    @EventHandler
    public void snowBallHit(ProjectileHitEvent event) {

        if (event.getEntity() instanceof Snowball && gameRunning) {
            if (event.getEntity().getShooter() instanceof Player) {
                if (event.getHitEntity() != null) {
                    Player killer = ((Player) event.getEntity().getShooter()).getPlayer();
                    Entity killed = event.getHitEntity();

                    playerKills.putIfAbsent(killer.getUniqueId(), 0);
                    playerKills.put(killer.getUniqueId(), playerKills.get(killer.getUniqueId()) + 1);
                    killer.sendMessage("Killed " + killed.getName());
                    if (playerKills.get(killer.getUniqueId()) == 10) {
                        endGame(killer);
                    } else {
                        if (killed instanceof Player) {
                            spawn((Player) killed);
                        } else {
                            killed.remove();
                        }
                    }
                }
            }
        }
    }

    private void spawn(Player player) {
        if (player == null) return;
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemStack(Material.SNOWBALL, 64));
        giveKit(player);
        player.teleport(new Location(player.getWorld(), 0, 100, 0));
    }

    @EventHandler
    public void fallDamage(EntityDamageEvent event) {

        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void noBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }

    private void endGame(Player winner) {

        playerKills.clear();
        this.getServer().broadcastMessage(winner.getDisplayName() + " Has won!");

        for (Player player : this.getServer().getOnlinePlayers()) {

            player.getInventory().clear();
            player.teleport(new Location(player.getWorld(), 0, 100, 0));
        }
        gameRunning = false;
    }

    public class startGame implements CommandExecutor {

        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            if (gameRunning){
                sender.sendMessage("Game is Already running!");
                return true;
            }

            startGame();
            return true;
        }
    }

    @EventHandler
    public void playerBounce(PlayerMoveEvent event){

        Player player = event.getPlayer();

        player.sendMessage(event.getTo().getY() + " " + event.getFrom().getY());


        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BROWN_MUSHROOM_BLOCK
                ) {

            Vector c = new Vector(player.getVelocity().getX(), 3, player.getVelocity().getZ());
            player.setVelocity(c);
            player.sendMessage("BOUNCE");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}


