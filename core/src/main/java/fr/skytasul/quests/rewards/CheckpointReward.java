package fr.skytasul.quests.rewards;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import fr.skytasul.quests.api.rewards.RewardList;

public class CheckpointReward extends AbstractReward {
	
	private RewardList actions;
	
	public CheckpointReward() {
		this(null, new RewardList());
	}
	
	public CheckpointReward(String customDescription, RewardList actions) {
		super(customDescription);
		this.actions = actions;
	}
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		actions.attachQuest(quest);
	}
	
	@Override
	public void detach() {
		super.detach();
		actions.detachQuest();
	}
	
	@Override
	public List<String> give(Player p) {
		Lang.QUEST_CHECKPOINT.send(p);
		return null;
	}
	
	public void applies(Player p) {
		try {
			actions.giveRewards(p);
		} catch (InterruptingBranchException e) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Trying to interrupt branching in a checkpoint reward (useless). " + toString());
		}
	}
	
	@Override
	public AbstractReward clone() {
		return new CheckpointReward(getCustomDescription(), new RewardList(actions));
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(Lang.actions.format(actions.size()));
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		QuestsAPI.getAPI().getRewards().createGUI(Lang.INVENTORY_CHECKPOINT_ACTIONS.toString(), QuestObjectLocation.CHECKPOINT, rewards -> {
			actions = new RewardList(rewards);
			event.reopenGUI();
		}, actions, null).open(event.getPlayer());
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("actions", actions.serialize());
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		actions = RewardList.deserialize(section.getMapList("actions"));
	}
	
}
