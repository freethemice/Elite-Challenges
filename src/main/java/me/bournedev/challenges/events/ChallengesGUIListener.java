package me.bournedev.challenges.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bournedev.challenges.Challenge;
import me.bournedev.challenges.Core;
import me.bournedev.challenges.gui.ChallengesGUI;
import me.bournedev.challenges.utils.Util;

public class ChallengesGUIListener implements Listener {

	private FileConfiguration config = Core.instance.getConfig();

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inv = event.getInventory();
		Player player = (Player) event.getWhoClicked();
		if (inv.getName().equals(Util.color(config.getString("gui.name")))) {
			event.setCancelled(true);
			if (event.getRawSlot() >= 11 && event.getRawSlot() <= 15) {
				Inventory challengeStatistics = Bukkit.createInventory(null, config.getInt("stats-gui-format.size"),
						Util.color(config.getString("stats-gui-format.name")));
				Challenge challenge = getChallengeInGUIFromSlot(event.getRawSlot());
				for (String playerName : challenge.getCounters().keySet()) {
					int rank = challenge.getRanking(playerName);
					if (rank > 50) {
						break;
					}
					List<String> list = new ArrayList<String>();
					for (String string : config.getStringList("stats-gui-format.skull-lore")) {
						list.add(string.replace("%player%", playerName).replace("%rank%", Integer.toString(rank)));
					}
					ItemStack skull = Util
							.createItemStackSkull(playerName,
									Util.color(config.getString("stats-gui-format.skull-name")
											.replace("%player%", playerName).replace("%rank%", Integer.toString(rank))),
							list);
					challengeStatistics.addItem(skull);
				}
				player.openInventory(challengeStatistics);
			}
		} else if (inv.getName().equals(Util.color(config.getString("stats-gui-format.name")))) {
			event.setCancelled(true);
		}
	}

	public Challenge getChallengeInGUIFromSlot(int slot) {
		int counter = 0;
		for (int i = 11; i < 16; i++) {
			if (slot == i) {
				break;
			} else {
				counter++;
				continue;
			}
		}
		return ChallengesGUI.challengesInGUI.get(counter);
	}
}
