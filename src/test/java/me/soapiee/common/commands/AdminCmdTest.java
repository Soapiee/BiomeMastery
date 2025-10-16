package me.soapiee.common.commands;

import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

public class AdminCmdTest {

    private static final PlayerData PLAYER_DATA = mock(PlayerData.class, Mockito.RETURNS_DEEP_STUBS);
    private static final BiomeLevel BIOME_LEVEL = mock(BiomeLevel.class, Mockito.RETURNS_DEEP_STUBS);

    @BeforeEach
    void beforeEach() {
    }

//    @Test
//    void testReadPlayerDataThatReturnsPlayerData() {
//        when(PLAYER_DATA.getBiomeData(eq(any(Biome.class)))).thenReturn(BIOME_LEVEL);

//        Set<OfflinePlayer> expectedPlayerList = new HashSet<>(Arrays.asList(offlinePlayers));
//        Set<OfflinePlayer> actualPlayerList = PLAYER_CACHE.getList();

//        assertEquals(actualPlayerList.size(), 2);
//        assertEquals(actualPlayerList, expectedPlayerList);
//    }
}
