package com.jbou.mgtntrun;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;


public class Main extends JavaPlugin implements Listener {

	MinigamesAPI api = null;
	PluginInstance pli = null;
	static JavaPlugin plugin;
	
	public void onEnable() {
		plugin = this;
		api = MinigamesAPI.getAPI().setupAPI(this, "tntrun", IArena.class);
		PluginInstance pinstance = api.pinstances.get(this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		Bukkit.getPluginManager().registerEvents(this, this);
		//add arenasetup
		pli = pinstance;
		pli.getArenaListener().loseY = 100;
		// pinstance.pvp = false;
	}

	public static ArrayList<Arena> loadArenas(JavaPlugin plugin, ArenasConfig cf) {
		ArrayList<Arena> ret = new ArrayList<Arena>();
		FileConfiguration config = cf.getConfig();
		if (!config.isSet("arenas")) {
			return ret;
		}
		for (String arena : config.getConfigurationSection("arenas.").getKeys(false)) {
			if (Validator.isArenaValid(plugin, arena, cf.getConfig())) {
				ret.add(initArena(arena));
			}
		}
		return ret;
	}

	public static IArena initArena(String arena) {
		IArena a = new IArena(plugin, arena);
		ArenaSetup s = MinigamesAPI.getAPI().pinstances.get(plugin).arenaSetup;
		a.init(Util.getSignLocationFromArena(plugin, arena), Util.getAllSpawns(plugin, arena), Util.getMainLobby(plugin), Util.getComponentForArena(plugin, arena, "lobby"), s.getPlayerCount(plugin, arena, true), s.getPlayerCount(plugin, arena, false), s.getArenaVIP(plugin, arena));
		return a;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return api.getCommandHandler().handleArgs(this, "tntrun", "/" + cmd.getName(), sender, args);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (pli.global_players.containsKey(e.getPlayer().getName()) && !pli.global_lost.containsKey(e.getPlayer().getName())) {
			final IArena a = (IArena) pli.global_players.get(e.getPlayer().getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				Location loc = e.getPlayer().getLocation().add(0, -1, 0);
				// remove block under player feet
				Block blocktoremove = getBlockUnderPlayer(loc,e.getPlayer().getWorld());
				if (blocktoremove != null) {
					final Location blockloc = blocktoremove.getLocation();
					Bukkit.getScheduler().scheduleSyncDelayedTask(
							plugin,
							new Runnable() {
								@Override
								public void run() {
									a.getSmartReset().addChanged(blockloc.getBlock(),false);
									blockloc.getBlock().setType(Material.AIR);
								}
							}, 5); //add delay in config
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player p = (Player) event.getEntity();
			Player attacker = (Player) event.getDamager();
			if (pli.global_players.containsKey(p.getName()) && pli.global_players.containsKey(attacker.getName())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (pli.global_players.containsKey(p.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					if (event.getCause() == DamageCause.ENTITY_ATTACK) {
						p.setHealth(20D);
						event.setCancelled(true);
						return;
					} else if (event.getCause() == DamageCause.FALL) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName()) && !pli.global_lost.containsKey(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}
	
	
	private static double PLAYER_BOUNDINGBOX_ADD = 0.3;
	private Block getBlockUnderPlayer(Location location, World world) {
		PlayerPosition loc = new PlayerPosition(location);
		Block b11 = loc.getBlock(location.getWorld(), +PLAYER_BOUNDINGBOX_ADD, -PLAYER_BOUNDINGBOX_ADD);
		if (b11.getType() != Material.AIR) {
			return b11;
		}
		Block b12 = loc.getBlock(location.getWorld(), -PLAYER_BOUNDINGBOX_ADD, +PLAYER_BOUNDINGBOX_ADD);
		if (b12.getType() != Material.AIR) {
			return b12;
		}
		Block b21 = loc.getBlock(location.getWorld(), +PLAYER_BOUNDINGBOX_ADD, +PLAYER_BOUNDINGBOX_ADD);
		if (b21.getType() != Material.AIR) {
			return b21;
		}
		Block b22 = loc.getBlock(location.getWorld(), -PLAYER_BOUNDINGBOX_ADD, -PLAYER_BOUNDINGBOX_ADD);
		if (b22.getType() != Material.AIR) {
			return b22;
		}
		return null;
	}
	
	private static class PlayerPosition {

		private double x;
		private int y;
		private double z;

		public PlayerPosition(double x, int y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public PlayerPosition(Location location) {
			this.x = location.getX();
			this.y = (int) location.getY();
			this.z = location.getZ();
		}

		public Block getBlock(World world, double addx, double addz) {
			return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
		}

	}
	
}
