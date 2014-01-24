package mc.alk.ctf;

import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.SerializerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class Flag {
	static int count = 0;
	final int id = count++; /// our id

	Entity ent; /// What is our flag (item or carried by player)

	boolean home; /// is our flag at home

	final ArenaTeam team; /// which team this flag belongs to

	final ItemStack is; /// what type of item is our flag

	final Location homeLocation; /// our spawn location

    static Method isValidMethod;
    static boolean isValid = true;

    /**
     * And a large workaround so that I can be compatible with 1.2.5 (aka Tekkit Servers)
     * who will not have the method Entity#isValid()
     */
    static {
        try {
            final String pkg = Bukkit.getServer().getClass().getPackage().getName();
            String version = pkg.substring(pkg.lastIndexOf('.') + 1);
            if (version.equalsIgnoreCase("craftbukkit")){
                isValidMethod = Entity.class.getMethod("isDead");
                isValid = false;
            } else {
                isValidMethod = Entity.class.getMethod("isValid");
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

	public Flag(ArenaTeam team, ItemStack is, Location homeLocation){
		this.team = team;
		this.home = true;
		this.is = is;
		this.homeLocation = homeLocation;
	}

	public void setEntity(Entity entity) {this.ent = entity;}
	public Location getCurrentLocation() {return ent.getLocation();}
	public Location getHomeLocation() {return homeLocation;}

	public boolean sameFlag(ItemStack is2) {
		return is.getType() == is2.getType() && is.getDurability() == is2.getDurability();
	}

	@SuppressWarnings("SimplifiableIfStatement")
    @Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof Flag)) return false;
		return this.hashCode() == other.hashCode();
	}

	@Override
	public int hashCode() { return id;}

	public Entity getEntity() {
		return ent;
	}

	public ArenaTeam getTeam() {
		return team;
	}

	public boolean isHome() {
		return home;
	}

	public void setHome(boolean home) {
		this.home = home;
	}

	@Override
	public String toString(){
		return String.format("[Flag %d: ent=%s, home=%s, team=%s, is=%s, homeloc=%s]",
				id,
                ent == null ? "null" :ent.getType(),home,
				team == null ? "null" : team.getId(),
				is == null ? "null" : InventoryUtil.getItemString(is),
				homeLocation==null? "null" : SerializerUtil.getLocString(homeLocation));
	}

    public boolean isValid() {
        try {
            return (Boolean) isValidMethod.invoke(ent);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
