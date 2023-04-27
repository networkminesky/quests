package fr.skytasul.quests.options;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.checkers.MaterialParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.creation.FinishGUI;

public class OptionQuestItem extends QuestOption<ItemStack> {
	
	@Override
	public void setValue(ItemStack value) {
		if (value == null || value.getType() == Material.AIR) {
			value = XMaterial.BOOK.parseItem();
		}else {
			value = ItemUtils.clearVisibleAttributes(value.clone());
		}
		super.setValue(value);
	}
	
	@Override
	public Object save() {
		return getValue();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(config.isItemStack(key) ? config.getItemStack(key) : XMaterial.valueOf(config.getString(key)).parseItem());
	}
	
	@Override
	public ItemStack cloneValue(ItemStack value) {
		return value.clone();
	}
	
	private String[] getLore() {
		String description = formatDescription(Lang.customMaterialLore.toString());
		if (!hasCustomValue()) return new String[] { description, "", Lang.defaultValue.toString() };
		return new String[] { description };
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.nameAndLore(getValue().clone(), Lang.customMaterial.toString(), getLore());
	}

	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		Lang.QUEST_MATERIAL.send(p);
		new TextEditor<>(p, () -> gui.reopen(p), obj -> {
			if (obj == null) {
				resetValue();
			}else {
				setValue(obj.parseItem());
			}
			gui.inv.setItem(slot, ItemUtils.nameAndLore(getValue().clone(), Lang.customMaterial.toString(), getLore()));
			ItemStack setItem = gui.inv.getItem(slot);
			if (setItem == null || setItem.getType() == Material.AIR) {
				// means that the material cannot be treated as an inventory item (ex: fire)
				resetValue();
				Lang.INVALID_ITEM_TYPE.send(p);
				gui.inv.setItem(slot, ItemUtils.nameAndLore(getValue().clone(), Lang.customMaterial.toString(), getLore()));
			}
			gui.reopen(p);
		}, MaterialParser.ANY_PARSER).passNullIntoEndConsumer().start();
	}
	
	@Override
	public boolean clickCursor(FinishGUI gui, Player p, ItemStack item, ItemStack cursor, int slot) {
		Utils.runSync(() -> player.setItemOnCursor(null));
		setValue(cursor);
		ItemUtils.nameAndLore(cursor, Lang.customMaterial.toString(), getLore());
		return false;
	}
	
}
