package me.soapiee.common.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

public class DataManagerTest {
    private DataManager dataManager;

    @BeforeEach
    void beforeEach() {
        dataManager = mock(DataManager.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Test
    public void givenString_whenCreateBiomeWhitelist_thenReturnTrue() {
//        CommandSender sender = mock(CommandSender.class, Mockito.RETURNS_DEEP_STUBS);
//        List<String> stringList = new ArrayList<>();
//        stringList.add("PLAINS");
//
//        List<Biome> actualValue = dataManager.createBiomeWhitelist(sender, stringList);
//
//        Assertions.assertTrue(actualValue.contains(Biome.PLAINS));
    }

    @Test
    public void givenString_whenCreateBiomeWhitelist_thenReturnFalse() {

    }
}
