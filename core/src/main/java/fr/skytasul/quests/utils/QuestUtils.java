package fr.skytasul.quests.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.utils.MessageUtils;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.Utils;

public class QuestUtils {

	public static void runOrSync(Runnable run) {
		if (Bukkit.isPrimaryThread()) {
			run.run();
		} else
			Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), run);
	}

	public static <T> BiConsumer<T, Throwable> runSyncConsumer(Runnable run) {
		return (__, ___) -> runSync(run);
	}

	public static void runSync(Runnable run) {
		Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), run);
	}

	public static void runAsync(Runnable run) {
		Bukkit.getScheduler().runTaskAsynchronously(BeautyQuests.getInstance(), run);
	}

	public static void playPluginSound(Player p, String sound, float volume) {
		playPluginSound(p, sound, volume, 1);
	}

	public static void playPluginSound(Player p, String sound, float volume, float pitch) {
		if (!QuestsConfigurationImplementation.playSounds())
			return;
		if ("none".equals(sound))
			return;
		try {
			p.playSound(p.getLocation(), Sound.valueOf(sound), volume, pitch);
		} catch (Exception ex) {
			if (MinecraftVersion.MAJOR > 8)
				p.playSound(p.getLocation(), sound, volume, pitch);
		}
	}

	public static void playPluginSound(Location lc, String sound, float volume) {
		if (!QuestsConfigurationImplementation.playSounds())
			return;
		try {
			lc.getWorld().playSound(lc, Sound.valueOf(sound), volume, 1);
		} catch (Exception ex) {
			if (MinecraftVersion.MAJOR > 8)
				lc.getWorld().playSound(lc, sound, volume, 1);
		}
	}

	public static String descriptionLines(DescriptionSource source, String... elements) {
		if (elements.length == 0)
			return Lang.Unknown.toString();
		if (QuestsConfigurationImplementation.splitDescription(source) && (!QuestsConfigurationImplementation.inlineAlone() || elements.length > 1)) {
			return QuestsConfigurationImplementation.getDescriptionItemPrefix()
					+ Utils.buildFromArray(elements, 0, QuestsConfigurationImplementation.getDescriptionItemPrefix());
		}
		return MessageUtils.itemsToFormattedString(elements, QuestsConfigurationImplementation.getItemAmountColor());
	}

	public static void spawnFirework(Location lc, FireworkMeta meta) {
		if (!QuestsConfigurationImplementation.doFireworks() || meta == null)
			return;
		runOrSync(() -> {
			Consumer<Firework> fwConsumer = fw -> {
				fw.setMetadata("questFinish", new FixedMetadataValue(BeautyQuests.getInstance(), true));
				fw.setFireworkMeta(meta);
			};
			if (MinecraftVersion.MAJOR >= 12) {
				lc.getWorld().spawn(lc, Firework.class, fw -> fwConsumer.accept(fw));
				// much better to use the built-in since 1.12 method to do operations on entity
				// before it is sent to the players, as it will not create flickering
			} else {
				fwConsumer.accept(lc.getWorld().spawn(lc, Firework.class));
			}
		});
	}

}
