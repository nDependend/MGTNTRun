package com.jbou.mgtntrun;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaType;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class IArena extends Arena {
	JavaPlugin plugin = null;
	PluginInstance pinstance;
	int playerTask;

    public IArena(JavaPlugin plugin, String name) {
        super(plugin, name, ArenaType.REGENERATION);
        this.plugin = plugin;
    }
    
	@Override
	public void started() {
		final IArena a = this;
		playerTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				plugin,
				new Runnable() {
					@Override
					public void run() {
						Main.RemoveBlocksUnderPlayer(a);
					}
				},
				0, 1
			);
	}
	
	@Override
	public void stop() {
		super.stop();
		Bukkit.getScheduler().cancelTask(playerTask);
	}
	
}