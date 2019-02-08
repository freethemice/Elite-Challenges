package me.bournedev.challenges.runnables;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.bournedev.challenges.Challenge;
import me.bournedev.challenges.Core;
import me.bournedev.challenges.gui.ChallengesGUI;
import me.bournedev.challenges.utils.Util;

public class ChallengeTimeUpdater extends BukkitRunnable {

	public static int counter = 86400;

	public void run() {
		counter = counter - 1;
		if (counter == 0) {
			resetChallenges();
		}
	}
	
	public void resetChallenges() {
		FileConfiguration config = Core.instance.getConfig();
		counter =  config.getInt("timer.challenge-duration");
		Util.sortRanks();
		for (Challenge challenge : ChallengesGUI.challengesInGUI) {
			ArrayList<String> playerNames = new ArrayList<String>();
			playerNames.addAll(challenge.getCounters().keySet());
			for (String key : config.getConfigurationSection("rewards").getKeys(false)) {
				for (String i : config.getConfigurationSection("rewards." + key).getKeys(false)) {
					String rewardPath = "rewards." + key + "." + i;
					int rank = Integer.parseInt(key) - 1;
					if (isPlayer(playerNames, rank) != null) {
						if (isMaterial(config.getString(rewardPath + ".material")) != null) {
							ItemStack reward = Util.createItemStack(
									Material.valueOf(config.getString(rewardPath + ".material")),
									config.getInt(rewardPath + ".amount"), config.getString(rewardPath + ".name"),
									config.getInt(rewardPath + ".data"),
									config.getStringList(rewardPath + ".lore"));

							for (String ench : config.getStringList(rewardPath + ".enchantments")) {
								Enchantment enchant = Enchantment.getByName(ench.split(":")[0]);
								int level = Integer.parseInt(ench.split(":")[1]);
								reward.addUnsafeEnchantment(enchant, level);
							}
							Bukkit.getPlayer(playerNames.get(rank)).getInventory().addItem(reward);
						} else {
							for (String string : config.getStringList(rewardPath + ".commands")) {
								Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
										string.replace("%player%", playerNames.get(rank)));
							}
						}
					}
				}
			}
		}
		for (String string : config.getStringList("messages.challenge-finished-broadcast")) {
			int o = 0;
			for (Challenge challenge : ChallengesGUI.challengesInGUI) {
				int i = 1;
				for (String key : challenge.getCounters().keySet()) {
					if (i == 4) {
						break;
					}
					string = string.replace("%challenge-" + Integer.toString(o) + "%", challenge.getChallengeName()).replace("%player-" + Integer.toString(o) + ":" + Integer.toString(i) + "%", key).replace("%counter-" + Integer.toString(o) + ":" + Integer.toString(i) + "%", Integer.toString(challenge.getCounters().get(key)));
					i++;
				}
				o++;
			}
			Bukkit.broadcastMessage(Util.color(string));
		}
		ChallengesGUI.resetChallengesInGUI();
	}

	public Player isPlayer(ArrayList<String> playerNames, int rank) {
		try {
			return Bukkit.getPlayer(playerNames.get(rank));
		} catch (Exception e) {
			return null;
		}
	}

	public Material isMaterial(String string) {
		try {
			return Material.valueOf(string);
		} catch (Exception e) {
			return null;
		}
	}

	public static int getCounter() {
		return counter;
	}

	public static void setCounter(int counter) {
		ChallengeTimeUpdater.counter = counter;
	}
}
