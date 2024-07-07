package fr.skytasul.quests.stages;

import fr.skytasul.quests.BeautyQuests;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class NPCMaster {

    public static ArrayList<StageNPC> stageNPCS = new ArrayList<>();

    // in-method to handle some things after
    public static void launchTask() {

        // just one runnable for everything
        new BukkitRunnable() {
            @Override
            public void run() {
                for(StageNPC s : stageNPCS) {
                    s.tickHere();
                }
            }
        }.runTaskTimer(BeautyQuests.getInstance(),80, 20);

    }

}
