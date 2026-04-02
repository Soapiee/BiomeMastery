//package me.soapiee.biomemastery.manager;
//
//import me.soapiee.biomemastery.BiomeMastery;
//import me.soapiee.biomemastery.gui.pages.main.BiomePageSettings;
//import me.soapiee.biomemastery.gui.core.PageSettings;
//import me.soapiee.biomemastery.gui.pages.secondary.RewardPageSettings;
//import me.soapiee.biomemastery.utils.CustomLogger;
//import org.bukkit.Bukkit;
//import org.bukkit.block.Biome;
//import org.bukkit.configuration.file.FileConfiguration;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class ConfigManagerTest {
//
//    private ConfigManager configManager;
//    private MockedStatic<Bukkit> mockedBukkit;
//
//    @BeforeEach
//    void beforeEach() {
//        BiomeMastery mockMain = mock(BiomeMastery.class);
//        FileConfiguration mockConfig = mock(FileConfiguration.class);
//        CustomLogger mockLogger = mock(CustomLogger.class);
//        PageSettings mockPageSettings = mock(PageSettings.class);
//        BiomePageSettings mockBiomeSettings = mock(BiomePageSettings.class);
//        RewardPageSettings mockRewardSettings = mock(RewardPageSettings.class);
//
//        // mock BiomeMastery behavior
//        when(mockMain.getConfig()).thenReturn(mockConfig);
//        when(mockMain.getCustomLogger()).thenReturn(mockLogger);
//        when(new PageSettings(mockMain)).thenReturn(mockPageSettings);
//        when(new BiomePageSettings(mockMain)).thenReturn(mockBiomeSettings);
//        when(new RewardPageSettings(mockMain)).thenReturn(mockRewardSettings);
//        when(mockMain.getConfig().isConfigurationSection("groups")).thenReturn(false);
//
//        mockedBukkit = Mockito.mockStatic(Bukkit.class);
//
//        configManager = new ConfigManager(mockMain);
//    }
//
//    @AfterEach
//    void afterEach() {
//        mockedBukkit.close();
//    }
//
//    @Test
//    void testDataManagerInitialization() {
//        assertNotNull(configManager);
//    }
//
//    @Test
//    void givenValidString_whenCreateBiomeWhitelist_thenReturnTrue() {
//        List<String> inputList = new ArrayList<>();
//        inputList.add("PLAINS");
//
//        List<Biome> actualValue = configManager.createBiomeWhitelist(inputList);
//
//        assertTrue(actualValue.contains(Biome.PLAINS));
//    }
//
//    @Test
//    void givenInvalidString_whenCreateBiomeWhitelist_thenReturnFalse() {
//        List<String> inputList = new ArrayList<>();
//        inputList.add("plainz");
//
//        List<Biome> actualValue = configManager.createBiomeWhitelist(inputList);
//
//        assertFalse(actualValue.contains(Biome.PLAINS));
//        assertTrue(actualValue.isEmpty());
//    }
//
//    @Test
//    void givenValidString_whenCreateBiomeBlacklist_thenReturnFalse() {
//        List<String> inputList = new ArrayList<>();
//        inputList.add("NETHER_WASTES");
//
//        List<Biome> actualValue = configManager.createBiomeBlacklist(inputList);
//
//        assertFalse(actualValue.contains(Biome.NETHER_WASTES));
//    }
//
//    @Test
//    void givenInvalidString_whenCreateBiomeBlacklist_thenReturnTrue() {
//        List<String> inputList = new ArrayList<>();
//        inputList.add("netherwastes");
//
//        List<Biome> actualValue = configManager.createBiomeBlacklist(inputList);
//
//        assertTrue(actualValue.contains(Biome.NETHER_WASTES));
//    }
//}
