package me.bournedev.challenges.runnables;

import me.bournedev.challenges.Challenge;
import me.bournedev.challenges.Core;
import me.bournedev.challenges.gui.ChallengesGUI;
import me.bournedev.challenges.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;

public class ChallengeTimeUpdater extends BukkitRunnable {

	public static int counter = 86400;
	public static int annuncerCounter = 3000;
	public void run() {
		counter = counter - 1;
		annuncerCounter = annuncerCounter - 1;
		if (counter <= 0) {
			resetChallenges();
		}
		else
		{
			if (annuncerCounter <= 0)
			{
				FileConfiguration config = Core.instance.getConfig();
				annuncerCounter =  config.getInt("timer.challenge-annoucer-duration");
				annouceLeaders();
			}
		}
	}
	public void annouceLeaders()
	{
		FileConfiguration config = Core.instance.getConfig();
		Util.sortRanks();
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		for(Player player: players)
		{
			player.sendMessage(ChatColor.RED + "Challenges Update: " + ChatColor.WHITE + Util.timeMessage(ChallengeTimeUpdater.counter));
			player.sendMessage(ChatColor.GRAY + "---------------------------------------------");
			for (Challenge challenge : ChallengesGUI.challengesInGUI) {
				ArrayList<String> playerNames = new ArrayList<String>();
				playerNames.addAll(challenge.getCounters().keySet());
				String iconPath = "challenges." + challenge.getChallengeName() + ".icon.name";
				String name = config.getString(iconPath);

				player.sendMessage(ChatColor.YELLOW + "First place: " + ChatColor.WHITE + playerNames.get(0) + ChatColor.YELLOW + " for " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', name)+ ChatColor.YELLOW + " with " + ChatColor.WHITE + challenge.getCounters().get(playerNames.get(0)));
			}
			player.sendMessage(ChatColor.GRAY + "---------------------------------------------");
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
					ItemStack reward = null;
					if (isMaterial(config.getString(rewardPath + ".material")) != null) {
						reward = Util.createItemStack(
								Material.valueOf(config.getString(rewardPath + ".material")),
								config.getInt(rewardPath + ".amount"), config.getString(rewardPath + ".name"),
								config.getInt(rewardPath + ".data"),
								config.getStringList(rewardPath + ".lore"));

						for (String ench : config.getStringList(rewardPath + ".enchantments")) {
							Enchantment enchant = Enchantment.getByName(ench.split(":")[0]);
							int level = Integer.parseInt(ench.split(":")[1]);
							reward.addUnsafeEnchantment(enchant, level);
						}
					}

					if (reward != null) {
						if (isPlayer(playerNames, rank) != null) {
							Bukkit.getPlayer(playerNames.get(rank)).getInventory().addItem(reward);
						}
						else
						{
							if (rank > -1 && rank < playerNames.size()) {
								if (Bukkit.getOfflinePlayer(playerNames.get(rank)) != null) {
									Core.instance.rewardsHolder.add(Bukkit.getOfflinePlayer(playerNames.get(rank)), reward.clone());
								}
							}
						}
					}
					if (config.contains(rewardPath + ".commands")) {
							for (String string : config.getStringList(rewardPath + ".commands")) {
								if (isPlayer(playerNames, rank) != null) {
									Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), string.replace("%player%", playerNames.get(rank)));
								}
								else
								{
									if (rank > -1 && rank < playerNames.size()) {
										if (Bukkit.getOfflinePlayer(playerNames.get(rank)) != null) {
											Core.instance.rewardsHolder.add(Bukkit.getOfflinePlayer(playerNames.get(rank)), string.replace("%player%", playerNames.get(rank)));
										}
									}
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

					string = string.replace("%challenge-" + Integer.toString(o) + "%", challenge.getChallengeName())
							.replace("%player-" + Integer.toString(o) + ":" + Integer.toString(i) + "%", key)
							.replace("%counter-" + Integer.toString(o) + ":" + Integer.toString(i) + "%", Integer.toString(challenge.getCounters().get(key)));


					i++;
				}
				o++;
			}
			for(int i = 0; i < 5; i++) {
				string = string.replace("%player-" + i + ":1%", "Nobody").replace("%player-" + i + ":2%", "Nobody").replace("%player-" + i + ":3%", "Nobody");
				string = string.replace("%counter-" + i + ":1%", "Zero").replace("%counter-" + i + ":2%", "Zero").replace("%counter-" + i + ":3%", "Zero");
				string = string.replace("%challenge-" + i + "%", "No Challaenge");
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
	public Player isPlayer(String name) {
		try {
			return Bukkit.getPlayer(name);
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
