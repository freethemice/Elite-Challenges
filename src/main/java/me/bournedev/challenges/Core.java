package me.bournedev.challenges;

import me.bournedev.challenges.events.ChallengeListener;
import me.bournedev.challenges.events.ChallengesGUIListener;
import me.bournedev.challenges.gui.ChallengesGUI;
import me.bournedev.challenges.runnables.ChallengeTimeUpdater;
import me.bournedev.challenges.runnables.TimeChallengesListener;
import me.bournedev.challenges.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

public class Core extends JavaPlugin {

	public static Core instance;
	public File data;
	public FileConfiguration dataconfig;
	public OfflineRewardsHolder rewardsHolder;
	public void onEnable() {
		instance = this;
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			getConfig().options().copyDefaults(true);
			saveDefaultConfig();
		}
		saveConfig();

		Bukkit.getPluginManager().registerEvents(new ChallengeListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChallengesGUIListener(), this);

		for (String key : getConfig().getConfigurationSection("challenges").getKeys(false)) {
			String path = "challenges." + key;
			Challenge challenge = new Challenge(key, ChallengeType.valueOf(getConfig().getString(path + ".type")),
					getConfig().getStringList(path + ".object-types"), new LinkedHashMap<String, Integer>());
			Challenge.challenges.add(challenge);
		}

		FileConfiguration config = Core.instance.getConfig();
		ChallengeTimeUpdater.counter = config.getInt("timer.challenge-duration");
		ChallengeTimeUpdater.annuncerCounter =  config.getInt("timer.challenge-annoucer-duration");
		registerDataFile();

		// If no challenges were registered from the data file.
		if (ChallengesGUI.challengesInGUI.size() == 0) {
			ChallengesGUI.resetChallengesInGUI();
		}
		rewardsHolder  = new OfflineRewardsHolder();
		rewardsHolder.load();
		//config.getStringList("messages.challenge-finished-broadcast")


		new ChallengeTimeUpdater().runTaskTimer(this, 20, 20);
		new TimeChallengesListener().runTaskTimer(this, 20, 20);
	}
	public void clearSavedData()
	{
		try {
			dataconfig.set("timer.time", null);
			dataconfig.set("challengesActive", null);
			dataconfig.set("playerData", null);
			saveYML(dataconfig, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void onDisable() {
		rewardsHolder.save();

		dataconfig.set("timer.time", Integer.toString(ChallengeTimeUpdater.counter));
		dataconfig.createSection("challengesActive");
		dataconfig.createSection("playerData");
		for (Challenge challenge : ChallengesGUI.challengesInGUI) {
			dataconfig.set("challengesActive." + challenge.getChallengeName(), challenge.getChallengeName());
			for (String playerName : challenge.getCounters().keySet()) {
				dataconfig.set("playerData." + challenge.getChallengeName() + "." + playerName,
						challenge.getCounters().get(playerName));
			}
		}
		saveYML(dataconfig, data);
	}

	public void registerDataFile() {
		data = new File(getDataFolder(), "data.yml");
		dataconfig = YamlConfiguration.loadConfiguration(data);
		if (!dataconfig.contains("challengesActive")) {
			dataconfig.createSection("challengesActive");
			dataconfig.createSection("playerData");
			dataconfig.createSection("timer");
		} else {
			if (getTimerTime() != -1) {
				ChallengeTimeUpdater.setCounter(Integer.parseInt(dataconfig.getString("timer.time")));
				for (String string : dataconfig.getConfigurationSection("challengesActive").getKeys(false)) {
					Challenge challenge = Challenge.getChallengeByName(string);
					LinkedHashMap<String, Integer> counters = new LinkedHashMap<String, Integer>();
					if (dataconfig.contains("playerData." + string)) {
						for (String playerName : dataconfig.getConfigurationSection("playerData." + string).getKeys(false)) {
							counters.put(playerName, dataconfig.getInt("playerData." + string + "." + playerName));
						}
					}
					challenge.setCounters(counters);
					ChallengesGUI.challengesInGUI.add(challenge);
				}
			}
		}
		saveYML(dataconfig, data);
	}

	public int getTimerTime() {
		try {
			return Integer.parseInt(dataconfig.getString("timer.time"));
		} catch (Exception e) {
			return -1;
		}
	}

	public void saveYML(FileConfiguration ymlConfig, File ymlFile) {
		try {
			ymlConfig.save(ymlFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("challenges")) {
				if (args.length == 0) {
					new ChallengesGUI(player);
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("time")) {
						player.sendMessage(Util.color(getConfig().getString("messages.time-message").replace("%time%",
								Util.timeMessage(ChallengeTimeUpdater.counter))));
					} else if (args[0].equalsIgnoreCase("reset")) {
						if (player.hasPermission("challenges.admin")) {
							FileConfiguration config = Core.instance.getConfig();
							ChallengeTimeUpdater.counter = config.getInt("timer.challenge-duration");
							ChallengeTimeUpdater.annuncerCounter =  config.getInt("timer.challenge-annoucer-duration");
							ChallengesGUI.resetChallengesInGUI();
							player.sendMessage(Util.color(getConfig().getString("messages.challenges-reset-message")));
						}

					} else if (args[0].equalsIgnoreCase("win")) {
						if (player.hasPermission("challenges.admin")) {
							ChallengeTimeUpdater.counter = 5;
						}
					} else if (args[0].equalsIgnoreCase("annouce") || args[0].equalsIgnoreCase("say")) {
						if (player.hasPermission("challenges.admin")) {
							ChallengeTimeUpdater.annuncerCounter = 3;
						}
					}
				}
			}
		}
		return true;
	}
}
