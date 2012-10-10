package mc.alk.ctf;

import mc.alk.arena.objects.teams.Team;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class Flag {
	static int count = 0;
	final int id = count++; /// our id	
	
	Entity ent; /// What is our flag (item or carried by player)
	
	boolean home; /// is our flag at home
	
	Team team; /// which team this flag belongs to
	
	final ItemStack is; /// what type of item is our flag
	
	final Location homeLocation; /// our spawn location
	
	public Flag(Team team, ItemStack is, Location homeLocation){
		this.team = team;
		home = true;
		this.is = is;
		this.homeLocation = homeLocation;
	}

	public void setEntity(Entity entity) {this.ent = entity;}
	public Location getCurrentLocation() {return ent.getLocation();}
	public Location getHomeLocation() {return homeLocation;}

	public boolean sameFlag(ItemStack is2) {
		return is.getType() == is2.getType() && is.getDurability() == is2.getDurability();
	}

	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof Flag)) return false;
		return this.hashCode() == ((Flag) other).hashCode();
	}

	public int hashCode() { return id;}

	public Entity getEntity() {
		return ent;
	}

	public Team getTeam() {
		return team;
	}
}
