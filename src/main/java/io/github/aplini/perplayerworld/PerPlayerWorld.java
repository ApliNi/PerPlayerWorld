package io.github.aplini.perplayerworld;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class PerPlayerWorld extends JavaPlugin implements Listener {
    List<String> loadLock = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(this, this);
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
        String uuid = player.getUniqueId().toString();

        // 检查是否存在这个维度
        String worldName = "world_"+ uuid.replaceAll("-", "");
        if(getWorld(worldName) == null){
            player.sendMessage(getM("loadWorld"));
            loadWorld(worldName);
            return;
        }
        tpPlayer(player, worldName);
    }

    public void tpPlayer(Player player, String worldName){
        player.sendMessage(getM("tp"));
        World world = getWorld(worldName);
        // 如果玩家已经在单独的维度
        if(player.getWorld().getName().equals(worldName)){
            // 返回之前的维度
            Location location = (Location) world.getMetadata("ApliNi.PPW.pLocation").get(0).value();
            if(location != null){
                player.teleport(location);
            }else{
                getLogger().warning("维度元数据中不包含上一个位置信息! "+ worldName +"@"+ player.getName());
            }
        }else{
            // 传送到单独的维度
            world.setMetadata("ApliNi.PPW.pLocation", new FixedMetadataValue(this, player.getLocation()));
            player.teleport(world.getSpawnLocation());
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


}
