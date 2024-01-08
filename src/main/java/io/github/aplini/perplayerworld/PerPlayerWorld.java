package io.github.aplini.perplayerworld;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class PerPlayerWorld extends JavaPlugin implements Listener {
    List<String> loadLock = new ArrayList<>();
    File configFile;
    FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(this, this);

        configFile = new File("./plugins/PerPlayerWorld/config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    @Override
    public void onDisable() {}


    @EventHandler // 按键操作事件
    public void onPlayerInteract(PlayerInteractEvent event) {

        // 要求点击的是末影箱
        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null || clickedBlock.getType() != Material.ENDER_CHEST){
            return;
        }

        // 要求使用右键
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }

        // 取消事件
        event.setCancelled(true);

        // 获取玩家 UUID (无连字符
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString().replaceAll("-", "");

        // 检查是否存在这个维度
        String worldName = "world_"+ uuid;
        if(getWorld(worldName) == null){
            player.sendMessage(getM("loadWorld"));
            loadWorld(worldName);
            return;
        }
        tpPlayer(player, uuid, worldName);
    }

    public void tpPlayer(Player player, String uuid, String worldName){
        player.sendMessage(getM("tp"));
        World world = getWorld(worldName);

        // 如果玩家已经在单独的维度
        if(player.getWorld().getName().equals(worldName)){
            // 返回之前的维度
            Location location = getLocation(uuid);
            if(location == null){
                getLogger().warning("未找到上一个位置信息! "+ worldName +"@"+ player.getName());
                return;
            }
            setLocation(uuid, player.getLocation());
            player.teleport(location);
        }else{
            // 传送到单独的维度
            Location location = getLocation(uuid);
            if(location == null){
                location = world.getSpawnLocation();
            }
            setLocation(uuid, player.getLocation());
            player.teleport(location);
        }
    }

    public void loadWorld(String worldName){
        if(loadLock.contains(worldName)){
            return;
        }
        loadLock.add(worldName);
        CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().callSyncMethod(this, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getC("copyWorld")
                            .replace("${worldName}", worldName)));

            int i = 0;
            while(true){
                if(++i > 16){break;}
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException ignored) {}

                if(getWorld(worldName) == null){
                    Bukkit.getScheduler().callSyncMethod(this, () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getC("loadWorld")
                                    .replace("${worldName}", worldName)));
                }else{
                    break;
                }
            }
            loadLock.remove(worldName);
        });
    }

    // 检查服务器是否存在这个 world
    public World getWorld(String name){
        return Bukkit.getServer().getWorld(name);
    }

    public String getM(String key){
        return getConfig().getString("message."+ key, null);
    }

    public String getC(String key){
        return getConfig().getString("command."+ key, null);
    }

    public void setLocation(String uuid, Location location){

        String key = "z."+ uuid +".";

        config.set(key +"world", location.getWorld().getName());
        config.set(key +"x", location.getX());
        config.set(key +"y", location.getY());
        config.set(key +"z", location.getZ());
        config.set(key +"yaw", location.getYaw());
        config.set(key +"pitch", location.getPitch());

        // 保存配置文件
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Location getLocation(String uuid){

        String key = "z."+ uuid +".";

        if(config.get(key + "x") != null){
            return new Location(
                    getWorld((String) config.get(key + "world")),
                    (double) config.get(key + "x"),
                    (double) config.get(key + "y"),
                    (double) config.get(key + "z"),
                    (float) config.get(key + "yaw"),
                    (float) config.get(key + "pitch"));

        }
        return null;
    }
}
