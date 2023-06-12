package fr.skytasul.quests.stages;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.utils.MessageUtils;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.items.ItemComparisonGUI;
import fr.skytasul.quests.gui.items.ItemsGUI;
import fr.skytasul.quests.utils.QuestUtils;

public class StageBringBack extends StageNPC{
	
	protected final ItemStack[] items;
	protected final String customMessage;
	protected final ItemComparisonMap comparisons;
	
	protected final Map<ItemStack, Integer> amountsMap = new HashMap<>();
	protected final String splitted;
	protected final String line;
	
	public StageBringBack(StageController controller, ItemStack[] items, String customMessage, ItemComparisonMap comparisons) {
		super(controller);
		this.customMessage = customMessage;
		this.comparisons = comparisons;
		
		this.items = items;
		for (ItemStack item : items) {
			int amount = (amountsMap.containsKey(item) ? amountsMap.get(item) : 0) + item.getAmount();
			amountsMap.put(item, amount);
		}

		String[] array = new String[items.length]; // create messages on beginning
		for (int i = 0; i < array.length; i++){
			array[i] = QuestsConfiguration.getConfig().getStageDescriptionConfig().getItemNameColor()
					+ Utils.getStringFromItemStack(items[i],
							QuestsConfiguration.getConfig().getStageDescriptionConfig().getItemAmountColor(),
							QuestsConfiguration.getConfig().getStageDescriptionConfig()
									.isAloneSplitAmountShown(DescriptionSource.FORCESPLIT));
		}
		splitted = QuestUtils.descriptionLines(DescriptionSource.FORCESPLIT, array);
		if (QuestsConfiguration.getConfig().getStageDescriptionConfig()
				.isAloneSplitAmountShown(DescriptionSource.FORCESPLIT)) {
			for (int i = 0; i < array.length; i++){
				array[i] = QuestsConfiguration.getConfig().getStageDescriptionConfig().getItemNameColor() + Utils
						.getStringFromItemStack(items[i],
								QuestsConfiguration.getConfig().getStageDescriptionConfig().getItemAmountColor(), false);
			}
		}
		line = QuestUtils.descriptionLines(DescriptionSource.FORCELINE, array);
	}
	
	public boolean checkItems(Player p, boolean msg){
		boolean done = true;
		for (Entry<ItemStack, Integer> en : amountsMap.entrySet()) {
			if (!comparisons.containsItems(p.getInventory(), en.getKey(), en.getValue())) {
				done = false;
				break;
			}
		}
		if (done) return true;
		if (msg) sendNeedMessage(p);
		return false;
	}

	public void sendNeedMessage(Player p) {
		String message = getMessage();
		if (message != null && !message.isEmpty() && !message.equals("none"))
			Lang.NpcText.sendWP(p, npcName(), MessageUtils.format(message, line), 1, 1);
	}
	
	public void removeItems(Player p){
		for(ItemStack is : items){
			comparisons.removeItems(p.getInventory(), is);
		}
		p.updateInventory();
	}
	
	public ItemStack[] getItems(){
		return items;
	}
	
	protected String getMessage() {
		return customMessage == null ? Lang.NEED_OBJECTS.toString() : customMessage;
	}

	@Override
	public String descriptionLine(PlayerAccount acc, DescriptionSource source){
		return MessageUtils.format(Lang.SCOREBOARD_ITEMS.toString() + " "
				+ (QuestsConfiguration.getConfig().getStageDescriptionConfig().getSplitSources().contains(source) ? splitted
						: line),
				npcName());
	}
	
	@Override
	public Object[] descriptionFormat(PlayerAccount acc, DescriptionSource source) {
		return new String[] {
				QuestsConfiguration.getConfig().getStageDescriptionConfig().getSplitSources().contains(source) ? splitted
						: line,
				npcName()};
	}

	@Override
	public void started(PlayerAccount acc) {
		super.started(acc);
		if (acc.isCurrent() && sendStartMessage())
			sendNeedMessage(acc.getPlayer());
	}
	
	@Override
	protected void initDialogRunner() {
		super.initDialogRunner();
		
		getNPC().addStartablePredicate((p, acc) -> {
			return canUpdate(p, false) && checkItems(p, false);
		}, this);
		
		dialogRunner.addTest(p -> {
			if (getNPC().canGiveSomething(p)) {
				// if the NPC can offer something else to the player
				// AND the stage cannot complete right now,
				// the click will not be handled by this stage
				// to let the plugin handle the NPC event (and give
				// another quest/complete something else to the player).
				if (!canUpdate(p, true) || !checkItems(p, false)) return false;
			}
			return true;
		});
		dialogRunner.addTestCancelling(p -> checkItems(p, true));
		
		dialogRunner.addEndAction(this::removeItems);
	}
	
	@Override
	public void unload() {
		super.unload();
		if (getNPC() != null) getNPC().removeStartablePredicate(this);
	}
	
	@Override
	public void serialize(ConfigurationSection section) {
		super.serialize(section);
		section.set("items", items);
		if (customMessage != null) section.set("customMessage", customMessage);
		if (!comparisons.getNotDefault().isEmpty()) section.createSection("itemComparisons", comparisons.getNotDefault());
	}
	
	public static StageBringBack deserialize(ConfigurationSection section, StageController controller) {
		ItemStack[] items = section.getList("items").toArray(new ItemStack[0]);
		String customMessage = section.getString("customMessage", null);
		ItemComparisonMap comparisons;
		if (section.contains("itemComparisons")) {
			comparisons = new ItemComparisonMap(section.getConfigurationSection("itemComparisons"));
		}else comparisons = new ItemComparisonMap();
		StageBringBack st = new StageBringBack(controller, items, customMessage, comparisons);
		st.loadDatas(section);
		return st;
	}

	public abstract static class AbstractCreator<T extends StageBringBack> extends StageNPC.AbstractCreator<T> {
		
		private static final ItemStack stageItems = ItemUtils.item(XMaterial.CHEST, Lang.stageItems.toString());
		private static final ItemStack stageMessage = ItemUtils.item(XMaterial.PAPER, Lang.stageItemsMessage.toString());
		private static final ItemStack stageComparison = ItemUtils.item(XMaterial.PRISMARINE_SHARD, Lang.stageItemsComparison.toString());

		protected List<ItemStack> items;
		protected String message = null;
		protected ItemComparisonMap comparisons = new ItemComparisonMap();
		
		protected AbstractCreator(@NotNull StageCreationContext<T> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);
			
			line.setItem(5, stageItems, event -> {
				new ItemsGUI(items -> {
					setItems(items);
					event.reopen();
				}, items).open(event.getPlayer());
			});
			line.setItem(9, stageMessage, event -> {
				new TextEditor<String>(event.getPlayer(), event::reopen, x -> {
					setMessage(x);
					event.reopen();
				}).passNullIntoEndConsumer().start();
			});
			line.setItem(10, stageComparison, event -> {
				new ItemComparisonGUI(comparisons, () -> {
					setComparisons(comparisons);
					event.reopen();
				}).open(event.getPlayer());
			});
		}
		
		public void setItems(List<ItemStack> items) {
			this.items = Utils.combineItems(items);
			getLine().refreshItemLore(5, Lang.optionValue.format(Lang.AmountItems.format(this.items.size())));
		}
		
		public void setMessage(String message) {
			this.message = message;
			getLine().refreshItemLore(9,
					message == null
							? Lang.optionValue.format(Lang.NEED_OBJECTS.toString()) + " " + Lang.defaultValue.toString()
							: Lang.optionValue.format(message));
		}
		
		public void setComparisons(ItemComparisonMap comparisons) {
			this.comparisons = comparisons;
			getLine().refreshItemLore(10,
					Lang.optionValue.format(Lang.AmountComparisons.format(this.comparisons.getEffective().size())));
		}
		
		@Override
		public void start(Player p) {
			new ItemsGUI(items -> {
				setItems(items);
				super.start(p);
			}, Collections.emptyList()).open(p);
		}

		@Override
		public void edit(T stage) {
			super.edit(stage);
			setItems(Arrays.asList(stage.items));
			setMessage(stage.customMessage);
			setComparisons(stage.comparisons.clone());
		}
		
	}
	
	public static class Creator extends AbstractCreator<StageBringBack> {
		
		public Creator(@NotNull StageCreationContext<StageBringBack> context) {
			super(context);
		}

		@Override
		protected StageBringBack createStage(StageController controller) {
			return new StageBringBack(controller, items.toArray(new ItemStack[0]), message, comparisons);
		}
		
	}

}
