package me.bournedev.challenges;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class OfflineRewardsHolder {

    public File data;
    public FileConfiguration dataconfig;

    private HashMap<UUID, List<Object>> holder = new HashMap<UUID, List<Object>>();
    public OfflineRewardsHolder()
    {
        data = new File(Core.instance.getDataFolder(), "offline.yml");
        dataconfig = YamlConfiguration.loadConfiguration(data);
    }
    public void load()
    {
        holder.clear();
        if (dataconfig.contains("offlineplayers")) {
            for (String uuidstg : dataconfig.getConfigurationSection("offlineplayers").getKeys(false)) {
                List<Object> things = new ArrayList<Object>();
                for (String string : dataconfig.getConfigurationSection("offlineplayers." + uuidstg).getKeys(false)) {
                    if (dataconfig.isItemStack("offlineplayers." + uuidstg + "." + string)) {
                        ItemStack itemStack = dataconfig.getItemStack("offlineplayers." + uuidstg + "." + string);
                        things.add(itemStack.clone());
                    } else {
                        String command = dataconfig.getString("offlineplayers." + uuidstg + "." + string);
                        things.add(command);
                    }
                }
                holder.put(UUID.fromString(uuidstg), things);
            }
            dataconfig.set("offlineplayers", null);
            Core.instance.saveYML(dataconfig, data);
        }
    }
    public void save()
    {
        dataconfig.createSection("offlineplayers");
        for (UUID uuid: holder.keySet())
        {
            List<Object> objectList = holder.get(uuid);
            dataconfig.createSection("offlineplayers." + uuid.toString());
            int i =0;
            for(Object object: objectList)
            {
                dataconfig.set("offlineplayers." + uuid.toString() + "." + i , object);
                i++;
            }

        }
        Core.instance.saveYML(dataconfig, data);
    }
    public void add(OfflinePlayer player, String command)
    {
        add(player.getUniqueId(), command);
    }
    public void add(OfflinePlayer player, ItemStack itemStack)
    {
        add(player.getUniqueId(), itemStack);
    }
    private void add(UUID uuid, Object something)
    {
        List<Object> things = new ArrayList<Object>();
        if (holder.containsKey(uuid))
        {
            things = holder.get(uuid);
        }
        things.add(something);
        holder.put(uuid, things);
    }
    public void checkforPlayer(Player player) {

        if (holder.containsKey(player.getUniqueId())) {
            List<Object> rewards = holder.get(player.getUniqueId());
            for (Object obj : rewards) {
                if (obj instanceof ItemStack) {
                    player.getInventory().addItem((ItemStack) obj);
                } else {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), ((String) obj));
                }
            }
            holder.remove(player.getUniqueId());
            player.sendMessage(ChatColor.DARK_GREEN + "You have been given rewards from challenges");
        }

    }



}
