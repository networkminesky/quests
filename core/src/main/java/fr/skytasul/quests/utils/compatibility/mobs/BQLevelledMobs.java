package fr.skytasul.quests.utils.compatibility.mobs;

import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import fr.skytasul.quests.api.mobs.LeveledMobFactory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.mobs.EntityTypeGUI;
import fr.skytasul.quests.utils.XMaterial;
import me.lokka30.levelledmobs.LevelledMobs;

public class BQLevelledMobs extends BukkitEntityFactory implements LeveledMobFactory<EntityType> {

	@Override
	public String getID() {
		return "levelledMobs";
	}

	private ItemStack item = ItemUtils.item(XMaterial.CHICKEN_SPAWN_EGG, "§eLevelledMobs");

	@Override
	public ItemStack getFactoryItem() {
		return item;
	}

	@Override
	public void itemClick(Player p, Consumer<EntityType> run) {
		new EntityTypeGUI(run, x -> x != null && x.isAlive()).create(p);
	}

	@Override
	public double getMobLevel(EntityType type, Entity entity) {
		return JavaPlugin.getPlugin(LevelledMobs.class).levelInterface.getLevelOfMob((LivingEntity) entity);
	}

	@Override
	public void onEntityKilled(EntityDeathEvent e) {
		// do nothing as the original BukkitEntityFactory will manage the event itself
	}

}