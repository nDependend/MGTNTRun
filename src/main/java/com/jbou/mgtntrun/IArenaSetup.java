package com.jbou.mgtntrun;

import com.comze_instancelabs.minigamesapi.*;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class IArenaSetup extends ArenaSetup {

	@Override
	public Arena saveArena(JavaPlugin plugin, String arenaname) {
		if (!Validator.isArenaValid(plugin, arenaname)) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Arena " + arenaname + " appears to be invalid.");
			return null;
		}
		// TODO arena saving (to file too)
		PluginInstance pli = MinigamesAPI.getAPI().pinstances.get(plugin);
		if (pli.getArenaByName(arenaname) != null) {
			pli.removeArenaByName(arenaname);
		}
		IArena a = Main.initArena(arenaname);
		if(a.getArenaType() == ArenaType.REGENERATION){
			if(Util.isComponentForArenaValid(plugin, arenaname, "bounds")){
				Util.saveArenaToFile(plugin, arenaname);
			}else{
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not save arena to file because boundaries were not set up.");
			}
		}
		this.setArenaVIP(plugin, arenaname, false);
		pli.addArena(a);
		return a;
	}
	
}