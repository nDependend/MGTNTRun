package com.jbou.mgtntrun;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaType;

public class IArena extends Arena {
	JavaPlugin plugin = null;

    public IArena(JavaPlugin plugin, String name) {
        super(plugin, name, ArenaType.REGENERATION);
        this.plugin = plugin;
    }
	
}