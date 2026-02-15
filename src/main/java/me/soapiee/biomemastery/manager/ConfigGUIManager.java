package me.soapiee.biomemastery.manager;

import lombok.Getter;
import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.gui.pages.main.BiomePageSettings;
import me.soapiee.biomemastery.gui.core.IconFactory;
import me.soapiee.biomemastery.gui.core.PageSettings;
import me.soapiee.biomemastery.gui.pages.secondary.RewardPageSettings;
import me.soapiee.biomemastery.logic.BiomeData;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigGUIManager {

    private final BiomeMastery main;
    @Getter private final IconFactory iconFactory;
    @Getter private final PageSettings pageSettings;
    @Getter private final BiomePageSettings biomePageSettings;
    @Getter private final RewardPageSettings rewardPageSettings;

    public ConfigGUIManager(BiomeMastery main) {
        this.main = main;
        checkConfigSection();
        iconFactory = new IconFactory(main);
        pageSettings = new PageSettings(iconFactory);
        biomePageSettings = new BiomePageSettings(main, iconFactory);
        rewardPageSettings = new RewardPageSettings(main, iconFactory);
    }

    private void checkConfigSection(){
        FileConfiguration config = main.getConfig();
        if (config.isSet("gui")) return;

        config.options().copyDefaults(true);
        main.saveConfig();
    }

    public void reload(CommandSender sender){
        iconFactory.reload();
        pageSettings.reload(sender);
        biomePageSettings.reload(sender);
        rewardPageSettings.reload(sender);

        for (BiomeData biomeData : main.getDataManager().getBiomeDataManager().getBiomeDataMap().values()){
            biomeData.setIcon(sender);
        }
    }
}
