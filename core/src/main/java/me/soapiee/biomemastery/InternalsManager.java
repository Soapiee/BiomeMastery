package me.soapiee.biomemastery;

import lombok.Getter;
import me.soapiee.biomemastery.internals.*;
import me.soapiee.biomemastery.manager.MessageManager;
import me.soapiee.biomemastery.utils.CustomLogger;
import me.soapiee.biomemastery.utils.Message;
import me.soapiee.biomemastery.utils.Utils;

public class InternalsManager {

    @Getter private final PotionsProvider potionsProvider;
    @Getter private final TexturesProvider texturesProvider;
    @Getter private final BiomesProvider biomesProvider;

    private final BiomeMastery main;
    private final CustomLogger customLogger;
    private final MessageManager messageManager;

    public InternalsManager(BiomeMastery main) {
        this.main = main;
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();

        int majorVersion = Utils.getMajorVersion();
        int minorVersion = Utils.getMinorVersion();

        potionsProvider = getPotionsProvider(majorVersion, minorVersion);
        texturesProvider = getTexturesProvider(majorVersion, minorVersion);
        biomesProvider = getBiomesProvider(majorVersion);
    }

    private PotionsProvider getPotionsProvider(int majorVersion, int minorVersion) {
        PotionsProvider potionsProvider;

        try {
            String packageName = InternalsManager.class.getPackage().getName();
            String providerName = (majorVersion >= 20 && minorVersion > 3 ? "Potion_1_20_4" : "internals.Potion_1_16");

            potionsProvider = (PotionsProvider) Class.forName(packageName + "." + providerName).newInstance();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 ClassCastException | IllegalArgumentException ex) {
            customLogger.logToFile(ex, messageManager.get(Message.VERSIONSUPPORTERROR));
            potionsProvider = new Potion_1_16();
        }

        return potionsProvider;
    }

    private TexturesProvider getTexturesProvider(int majorVersion, int minorVersion) {
        TexturesProvider texturesProvider;

        try {
            String packageName = InternalsManager.class.getPackage().getName();
            String providerName = (majorVersion >= 21 && minorVersion > 3 ? "Textures_1_21_4" : "internals.TexturesUnsupported");

            texturesProvider = (TexturesProvider) Class.forName(packageName + "." + providerName).newInstance();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 ClassCastException | IllegalArgumentException ex) {
            customLogger.logToFile(ex, messageManager.get(Message.TEXTURESUNSUPPORTED));
            texturesProvider = new TexturesUnsupported();
        }

        texturesProvider.initialise(main);

        return texturesProvider;
    }

    private BiomesProvider getBiomesProvider(int majorVersion) {
        BiomesProvider biomesProvider;

        try {
            String packageName = InternalsManager.class.getPackage().getName();
            String providerName = (majorVersion >= 21 ? "Biomes_1_21_1" : "internals.Biomes_1_16");
//            if (majorVersion >= 26 && minorVersion >= 4) providerName = "Biomes_1_26_1";

            biomesProvider = (BiomesProvider) Class.forName(packageName + "." + providerName).newInstance();
            biomesProvider.initialise(main);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 ClassCastException | IllegalArgumentException ex) {
            customLogger.logToFile(ex, messageManager.get(Message.VERSIONSUPPORTERROR));
//            customLogger.logToFile(ex, "There is an version support error. Please give the log file to the developer");
            biomesProvider = new Biomes_1_16();
        }

        return biomesProvider;
    }
}
