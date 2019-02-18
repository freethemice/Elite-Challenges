package me.bournedev.challenges.events;

import me.bournedev.challenges.Challenge;
import me.bournedev.challenges.ChallengeType;
import me.bournedev.challenges.Core;
import me.bournedev.challenges.gui.ChallengesGUI;
import me.bournedev.challenges.utils.Util;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class ChallengeListener implements Listener {

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		new BukkitRunnable()
		{

			@Override
			public void run() {
				Core.instance.rewardsHolder.checkforPlayer(event.getPlayer());
			}
		}.runTaskLater(Core.instance, 20);

	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		if (Util.allowsBreaking(loc, player)) {
			this.updateChallenge(player, ChallengeType.BLOCK_BREAK, block.getType().name(), 1);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		if (Util.allowsPlacing(loc, player)) {
			this.updateChallenge(player, ChallengeType.BLOCK_PLACE, block.getType().name(), 1);
		}
	}

	@EventHandler
	public void onFurnaceExtract(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		ItemStack cursor = event.getCursor();
		if (event.getInventory().getType().equals(InventoryType.FURNACE)) {
			if (event.getSlotType().equals(SlotType.RESULT)) {
				int amount = item.getAmount();
				if (event.isShiftClick()) {
					if (this.freeSpace(player, item.getType(), amount) <= amount) {
						amount = this.freeSpace(player, item.getType(), amount);
					}
				} else {
					if (cursor.getType().equals(item.getType())) {
						if (cursor.getAmount() + item.getAmount() > cursor.getMaxStackSize()) {
							return;
						}
					} else if (cursor.getAmount() != 0) {
						return;
					}
				}
				this.updateChallenge(player, ChallengeType.FURNACE_SMELT, item.getType().name(), amount);
			}
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getKiller() instanceof Player) {
			Player player = event.getEntity().getKiller();
			this.updateChallenge(player, ChallengeType.ENTITY_DEATH, event.getEntityType().name(), 1);
		}
	}

	@EventHandler
	public void onFish(PlayerFishEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getCaught();
		if (entity != null) {
			if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
				ItemStack item = ((Item) entity).getItemStack();
				this.updateChallenge(player, ChallengeType.FISHING, item.getType().name(), 1);
			}
		}
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack cursor = event.getCursor();
		ItemStack item = event.getCurrentItem();
		int possibleCreations = 1;
		int amountOfItems = item.getAmount();
		if (event.isShiftClick()) {
			possibleCreations = possibleCreations(event.getInventory().getMatrix());
			amountOfItems = event.getRecipe().getResult().getAmount() * possibleCreations;
			if (freeSpace(player, item.getType(), amountOfItems) <= amountOfItems) {
				amountOfItems = freeSpace(player, item.getType(), amountOfItems);
			}
		}
		if (cursor.getType() != Material.AIR) {
			if (cursor.getType().equals(item.getType())) {
				if (cursor.getAmount() + item.getAmount() > cursor.getMaxStackSize()) {
					return;
				}
			} else {
				return;
			}
		}
		this.updateChallenge(player, ChallengeType.CRAFT_ITEM, item.getType().name(), amountOfItems);
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			if (player.getItemInHand() != null) {
				ItemStack item = player.getItemInHand();
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasDisplayName()) {
						this.updateChallenge(player, ChallengeType.PLAYER_INTERACT, item.getItemMeta().getDisplayName(),
								1);
					}
				}
			}
		}
	}

	public int freeSpace(Player player, Material itemToAdd, int amountToAdd) {
		int freeSpace = 0;
		for (ItemStack i : player.getInventory()) {
			if (i == null) {
				freeSpace += itemToAdd.getMaxStackSize();
			} else if (i.getType() == itemToAdd) {
				freeSpace += itemToAdd.getMaxStackSize() - i.getAmount();
			}
		}
		return freeSpace;
	}

	public int possibleCreations(ItemStack[] matrix) {
		int itemsChecked = 0;
		int possibleCreations = 1;
		for (ItemStack item : matrix) {
			if (item != null && !item.getType().equals(Material.AIR)) {
				if (itemsChecked == 0)
					possibleCreations = item.getAmount();
				else
					possibleCreations = Math.min(possibleCreations, item.getAmount());
				itemsChecked++;
			}
		}
		return possibleCreations;
	}

	public void updateChallenge(Player player, ChallengeType challengeType, String objectType, int additionToCounter) {
		if (player.getGameMode() != GameMode.SURVIVAL) return;
		if (Util.isInEnabledWorld(player)) {
			for (Challenge challenge : ChallengesGUI.challengesInGUI) {
				if (challenge.getChallengeType().equals(challengeType)) {
					ArrayList<String> objectTypes = new ArrayList<String>();
					for (String string : challenge.getObjectiveObjectTypes()) {
						objectTypes.add(Util.color(string));
					}
					if (objectTypes.contains(objectType)) {
						challenge.updateCounter(player.getName(), additionToCounter);
					}
				}
			}
		}
	}
}
