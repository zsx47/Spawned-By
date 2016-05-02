package spawnedby;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.entity.EntityType;

public class SpawnedBy extends JavaPlugin implements Listener {
	
	public static final Logger logger = Logger.getLogger("Minecraft");
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		logger.info("[SpawnedBy] Loading mob metadata from config");
		FileConfiguration config = getConfig();
		for (World w :this.getServer().getWorlds()) {
			for (Entity ent: w.getEntities()) {
				if (config.contains(w.getName() + "." + ent.getUniqueId().toString())) {
					ent.setMetadata("SpawnedBy", new FixedMetadataValue(this, config.getString(w.getName() + "." + ent.getUniqueId().toString() + ".Owner")));
				}
			}
		}
		saveConfig();
		logger.info("[SpawnedBy] Loaded mob metadata from config");
		config.options().copyDefaults(true);
		saveConfig();
	}
	
	
	@Override
	public void onDisable() {
		logger.info("[SpawnedBy] Writing mob metadata to config");
		FileConfiguration config = getConfig();
		for (World w :this.getServer().getWorlds()) {
			for (Entity ent: w.getEntities()) {
				if (ent.hasMetadata("SpawnedBy")) {
					config.set(w.getName() + "." + ent.getUniqueId().toString() + ".Owner", ent.getMetadata("SpawnedBy").get(0).asString());
				}
			}
		}
		saveConfig();
		logger.info("[SpawnedBy] Mob metadata saved to config");
	}
	
	
	@EventHandler
	public void onPlayerClickEntity(PlayerInteractEntityEvent event) {
		if (!event.isCancelled()) {
			if (event.getPlayer().getItemInHand().getType() == Material.NETHER_STAR && event.getPlayer().hasPermission("SpawnedBy.check")) {
				event.setCancelled(true);
				if (event.getRightClicked().hasMetadata("SpawnedBy")) {
					UUID uuid = UUID.fromString(event.getRightClicked().getMetadata("SpawnedBy").get(0).asString());
					OfflinePlayer p = this.getServer().getOfflinePlayer(uuid);
					event.getPlayer().sendMessage("Spawned by " + p.getName());
				} else {
					event.getPlayer().sendMessage("No data on this entity");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpawnEgg(PlayerInteractEvent event) {
		if (!event.isCancelled()) {
			if (event.getMaterial() == Material.MONSTER_EGG && (event.getAction() == Action.RIGHT_CLICK_AIR ||  event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				BlockFace blockf = event.getBlockFace();
				Location spawnloc;
				if (blockf == BlockFace.DOWN) {
					spawnloc = event.getClickedBlock().getLocation().add(0.00, -1.00, 0.00);
				} else if (blockf == BlockFace.EAST) {
					spawnloc = event.getClickedBlock().getLocation().add(1.00, 0.00, 0.00);
				} else if (blockf == BlockFace.NORTH) {
					spawnloc = event.getClickedBlock().getLocation().add(0.00, 0.00, -1.00);
				} else if (blockf == BlockFace.SOUTH) {
					spawnloc = event.getClickedBlock().getLocation().add(0.00, 0.00, 1.00);
				} else if (blockf == BlockFace.UP) {
					spawnloc = event.getClickedBlock().getLocation().add(0.00, 1.00, 0.00);
				} else if (blockf == BlockFace.WEST) {
					spawnloc = event.getClickedBlock().getLocation().add(-1.00, 0.00, 0.00);
				} else {
					spawnloc = event.getClickedBlock().getLocation();
				}
				spawnloc = spawnloc.add(0.50, 0.00, 0.50);
				Entity ent = spawnloc.getWorld().spawn(spawnloc, EntityType.fromId(event.getItem().getData().getData()).getEntityClass());
				ent.setMetadata("SpawnedBy", new FixedMetadataValue(this, event.getPlayer().getUniqueId()));
			}
		}
	}
}