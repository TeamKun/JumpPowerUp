package net.kunmc.lab.jumppowerup;

import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public final class JumpPowerUp extends JavaPlugin implements Listener {

    private Objective score;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Scoreboard sb = getServer().getScoreboardManager().getMainScoreboard();
        Objective jc = sb.getObjective("jumpcount");
        if (jc == null)
            jc = sb.registerNewObjective("jumpcount", "dummy", "ジャンプ回数");
        score = jc;
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // スコアを0にして跳躍力上昇を消す
        for (Player p : getServer().getOnlinePlayers()) {
            Score jc = score.getScore(p.getName());
            jc.setScore(0);
            p.removePotionEffect(PotionEffectType.JUMP);
        }
    }

    @EventHandler
    public void PlayerStatisticIncrementEvent(PlayerStatisticIncrementEvent event) {
        if (event.getStatistic() == Statistic.JUMP) {
            Player player = event.getPlayer();
            Score jc = score.getScore(player.getName());
            int count = jc.getScore() + 1;
            jc.setScore(count);
            //一回目は通常ジャンプ
            if (count == 1)
                return;
            double fx = -(127.0 / 1939) * (count ^ 2) + (11430.0 / 1939) * count - 10922.0 / 1939;
            int x = (int) Math.floor(fx);
            //レベルは127までが正のためそれ以降はレベル127を付与
            if (count > 45) {
                x = 127;
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 99999, x), true);

            //ファントムをスポーン(50人クラフト要素)
            if (count % 10 == 0) {
                Location loc = player.getLocation();
                loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
            }
        }
    }

    //リスポーン時にジャンプ数のカウントを0にする
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Score jc = score.getScore(player.getName());
        jc.setScore(0);
    }

    //落下ダメージ無効
    @EventHandler
    public void onCancelFallDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Player p = (Player) e.getEntity();
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
            if (p.hasPotionEffect(PotionEffectType.JUMP)) {
                e.setCancelled(true);
            }
    }
}