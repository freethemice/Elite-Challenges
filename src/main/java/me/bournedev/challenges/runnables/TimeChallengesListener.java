package me.bournedev.challenges.runnables;

import me.bournedev.challenges.Challenge;
import me.bournedev.challenges.ChallengeType;
import me.bournedev.challenges.gui.ChallengesGUI;
import me.bournedev.challenges.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeChallengesListener extends BukkitRunnable {

	public void run() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			Location loc = player.getLocation();
			if (Util.isInEnabledWorld(player)) {
				for (Challenge challenge : ChallengesGUI.challengesInGUI) {
					if (challenge.getChallengeType().equals(ChallengeType.WARZONE_TIME)) {
						if (Util.allowsPVP(loc)) {
							challenge.updateCounter(player.getName(), 1);
						}
					} else if (challenge.getChallengeType().equals(ChallengeType.PTIME)) {
						challenge.updateCounter(player.getName(), 1);
					}
				}
			}
		}
	}
}
