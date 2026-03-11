package me.soapiee.biomemastery.gui;

import me.soapiee.biomemastery.BiomeMastery;
import me.soapiee.biomemastery.gui.core.Icon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IconTest {

    private FileConfiguration mockConfig;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void beforeEach() {
        mockConfig = mock(FileConfiguration.class);
        BiomeMastery mockMain = mock(BiomeMastery.class);
        ItemFactory itemFactory = mock(ItemFactory.class);

        mockedBukkit = Mockito.mockStatic(Bukkit.class);
        when(mockMain.getConfig()).thenReturn(mockConfig);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        when(itemFactory.getItemMeta(any())).thenReturn(mock(ItemMeta.class));
    }

    @AfterEach
    void afterEach() {
        mockedBukkit.close();
    }

    @Test
    void givenMaterial_whenNewIcon_thenReturnCorrectMaterial() {
        ItemStack stack = new ItemStack(Material.GRASS_BLOCK, 1);
        Icon actualValue = new Icon(stack, new ArrayList<>(), 5);

        assertEquals(Material.GRASS_BLOCK, actualValue.getItemStack().getType());
        assertNotEquals(Material.DIAMOND_BLOCK, actualValue.getItemStack().getType());
    }

    @Test
    void givenAmount_whenCreateIcon_thenReturnCorrectAmount() {
        ItemStack stack = new ItemStack(Material.GRASS_BLOCK, 10);
        Icon actualValue = new Icon(stack, new ArrayList<>(), 5);

        assertEquals(10, actualValue.getItemStack().getAmount());
        assertNotEquals(7, actualValue.getItemStack().getAmount());
    }

//    @Test
//    void givenDisplayName_whenCreateIcon_thenReturnCorrectDisplayName() {
//        String displayName = "testDisplay Name";
//
//        ItemStack stack = new ItemStack(Material.GRASS_BLOCK, 1);
//        ItemMeta meta = stack.getItemMeta();
//        meta.setDisplayName(displayName);
//        stack.setItemMeta(meta);
//
//        Icon actualValue = new Icon(stack, new ArrayList<>(), 5);
//
//        assertEquals(displayName, actualValue.getItemStack().getItemMeta().getDisplayName());
//        assertNotEquals("abc", actualValue.getItemStack().getItemMeta().getDisplayName());
//        assertNotEquals("test display name", actualValue.getItemStack().getItemMeta().getDisplayName());
//    }

    @Test
    void givenLore_whenCreateLore_thenReturnCorrectLore() {
        List<String> lore = new ArrayList<>();
        lore.add("line 1");

        ItemStack stack = new ItemStack(Material.GRASS_BLOCK, 1);
        Icon actualValue = new Icon(stack, lore, 5);

        List<String> expectedLore = new ArrayList<>();
        expectedLore.add("line 1");

        assertEquals(expectedLore, actualValue.getLore());
    }

}
