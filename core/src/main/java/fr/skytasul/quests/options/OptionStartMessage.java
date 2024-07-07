package fr.skytasul.quests.options;

import org.bukkit.entity.Player;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionString;
import com.cryptomorin.xseries.XMaterial;

public class OptionStartMessage extends QuestOptionString {
	
	@Override
	public void sendIndication(Player p) {
		Lang.WRITE_START_MESSAGE.send(p);
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.PAPER;
	}
	
	@Override
	public String getItemName() {
		return Lang.startMessage.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.startMessageLore.toString();
	}
	
}
