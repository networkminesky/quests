package fr.skytasul.quests.requirements.logical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class LogicalOrRequirement extends AbstractRequirement {
	
	private List<AbstractRequirement> requirements;
	
	public LogicalOrRequirement() {
		this(new ArrayList<>());
	}
	
	public LogicalOrRequirement(List<AbstractRequirement> requirements) {
		super("logicalOr");
		this.requirements = requirements;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { Lang.requirements.format(requirements.size()), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new QuestObjectGUI<>(Lang.INVENTORY_REQUIREMENTS.toString(), QuestObjectLocation.OTHER, QuestsAPI.requirements.values(), requirements -> {
			LogicalOrRequirement.this.requirements = requirements;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}, requirements).create(p);
	}
	
	@Override
	public boolean test(Player p) {
		return requirements.stream().anyMatch(x -> x.test(p));
	}
	
	@Override
	public AbstractRequirement clone() {
		return new LogicalOrRequirement(new ArrayList<>(requirements));
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("requirements", Utils.serializeList(requirements, AbstractRequirement::serialize));
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		requirements = Utils.deserializeList((List<Map<String, Object>>) savedDatas.get("requirements"), map -> {
			try {
				return AbstractRequirement.deserialize(map);
			}catch (ClassNotFoundException e) {
				BeautyQuests.getInstance().getLogger().severe("An exception occured while deserializing a quest object (class " + map.get("class") + ").");
				BeautyQuests.loadingFailure = true;
				e.printStackTrace();
			}
			return null;
		});
	}
	
}
