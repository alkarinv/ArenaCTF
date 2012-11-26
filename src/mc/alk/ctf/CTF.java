package mc.alk.ctf;

import mc.alk.arena.BattleArena;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.util.Log;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class CTF extends JavaPlugin{

	static String name; 
	static String version;
	static CTF plugin;
	static ArenaSerializer arenaSerializer;

	@Override
	public void onEnable(){
		plugin = this;
		PluginDescriptionFile pdfFile = plugin.getDescription();
		name = pdfFile.getName();
		version = pdfFile.getVersion();
		BattleArena.registerEventType(this, "CaptureTheFlag", "ctf", CTFArena.class, new CTFExecutor());

		FileConfiguration config = this.getConfig();
		CTFArena.capturesToWin = config.getInt("capturesToWin", 3);
		saveDefaultConfig();

		Log.info("[" + getName()+ "] v" + getDescription().getVersion()+ " enabled!");
	}

	@Override
	public void onDisable(){
		Log.info("[" + name + "] v" + version + " stopping!");
	}
	public static CTF getSelf() {
		return plugin;
	}

}
