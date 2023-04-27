package fr.skytasul.quests.api.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GuiManager {

	void open(@NotNull Player player, @NotNull CustomInventory inventory);

	void closeAndExit(@NotNull Player player);

	void closeWithoutExit(@NotNull Player player);

	void closeAll();

	boolean hasGuiOpened(@NotNull Player player);

	@Nullable
	CustomInventory getOpenedGui(@NotNull Player player);

}
