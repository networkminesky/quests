package fr.skytasul.quests.requirements.logical;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementList;

public class LogicalOrRequirement extends AbstractRequirement {
	
	private RequirementList requirements;
	
	public LogicalOrRequirement() {
		this(null, null, new RequirementList());
	}
	
	public LogicalOrRequirement(String customDescription, String customReason, RequirementList requirements) {
		super(customDescription, customReason);
		this.requirements = requirements;
	}
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		requirements.attachQuest(quest);
	}
	
	@Override
	public void detach() {
		super.detach();
		requirements.detachQuest();
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(Lang.requirements.format(requirements.size()));
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		QuestsAPI.getAPI().getRequirements().createGUI(QuestObjectLocation.OTHER, requirements -> {
			this.requirements = new RequirementList(requirements);
			event.reopenGUI();
		}, requirements).open(event.getPlayer());
	}
	
	@Override
	public boolean test(Player p) {
		return requirements.stream().anyMatch(x -> x.test(p));
	}
	
	@Override
	public AbstractRequirement clone() {
		return new LogicalOrRequirement(getCustomDescription(), getCustomReason(), new RequirementList(requirements));
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("requirements", requirements.serialize());
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		requirements = RequirementList.deserialize(section.getMapList("requirements"));
	}
	
}
