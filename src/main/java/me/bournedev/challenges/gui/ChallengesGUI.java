package me.bournedev.challenges.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import me.bournedev.challenges.Challenge;
import me.bournedev.challenges.Core;
import me.bournedev.challenges.runnables.ChallengeTimeUpdater;
import me.bournedev.challenges.utils.Util;

public class ChallengesGUI {
	public static ArrayList<Challenge> challengesInGUI = new ArrayList<Challenge>();
	
	public ChallengesGUI(Player player) {
		openChallengesGUI(player);
	}
	
	@SuppressWarnings("deprecation")
	public void openChallengesGUI(Player player) {
		Util.sortRanks();
		FileConfiguration config = Core.instance.getConfig();
		Inventory challengesGUI = Bukkit.createInventory(null, config.getInt("gui.size"), Util.color(config.getString("gui.name")));
		for (String key : config.getConfigurationSection("gui.icons").getKeys(false)) {
			String iconPath = "gui.icons." + key;
			ItemStack icon = createIcon(iconPath, config.getStringList(iconPath + ".lore"));
			challengesGUI.setItem(Integer.parseInt(key), icon);
		}
		int slot = 11;
		for (Challenge challenge : challengesInGUI) {
			String iconPath = "challenges." + challenge.getChallengeName() + ".icon";
			List<String> newList = new ArrayList<String>();
			for (String string : config.getStringList(iconPath + ".lore")) {
				string = string.replace("%date%", Util.getDateMessage()).replace("%time%", Util.timeMessage(ChallengeTimeUpdater.counter)).replace("%player%", player.getName()).replace("%counter%", Integer.toString(challenge.getCounters().get(player.getName()))).replace("%rank%", Integer.toString(challenge.getRanking(player.getName())));
				int i = 1;
				for (String playerName : challenge.getCounters().keySet()) {
					string = string.replace("%player" + Integer.toString(i) + "%", playerName).replace("%counter" + Integer.toString(i) + "%", Integer.toString(challenge.getCounters().get(playerName)));
					if (Util.hasFactions()) {
						FPlayer fplayer = FPlayers.getInstance().getByOfflinePlayer(Bukkit.getOfflinePlayer(playerName));
						Faction faction = fplayer.getFaction();
						string = string.replace("%faction" + Integer.toString(i) + "%", faction.getTag());
					}
					i++;
				}
				string = string.replace("null", "None");
				newList.add(string);
			}
			ItemStack icon = createIcon(iconPath, newList);
			challengesGUI.setItem(slot, icon);
			slot++;
		}
		player.openInventory(challengesGUI);
	}
	
	public static void resetChallengesInGUI() {
		challengesInGUI.clear();
		for (Challenge challenge : Challenge.getRandomChallenges(5)) {
			challengesInGUI.add(challenge);
		}
	}
	
	public ItemStack createIcon(String iconPath, List<String> list) {
		FileConfiguration config = Core.instance.getConfig();
		ItemStack icon = Util.createItemStack(Material.valueOf(config.getString(iconPath + ".material")), config.getInt(iconPath + ".amount"), config.getString(iconPath + ".name"), config.getInt(iconPath + ".data"), list);
		return icon;
	}
}
