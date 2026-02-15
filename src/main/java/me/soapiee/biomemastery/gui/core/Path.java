package me.soapiee.biomemastery.gui.core;

import lombok.Getter;

public enum Path {

    // General
    NEXT_PAGE("gui.generic_icons.next_page"),
    PREV_PAGE("gui.generic_icons.prev_page"),
    CLOSE("gui.generic_icons.close"),

    // Biome Page
    BIOME_TITLE("gui.main_page.title"),
    BIOME_SIZE("gui.main_page.size"),
    SPECIFIC_BIOME_DATA("biomes.{biome}"),
    DEFAULT_BIOME_ICON("gui.main_page.default_biome_icon"),
    BIOME_INFO("gui.main_page.info_icon"),
    BIOME_FILLER("gui.main_page.filler_icon"),


    // Reward Page
    REWARD_TITLE("gui.secondary_page.title"),
    REWARD_SIZE("gui.secondary_page.size"),
    REWARD_ICON("gui.secondary_page.default_reward_icon"),
    REWARD_INFO("gui.secondary_page.info_icon"),
    REWARD_FILLER("gui.secondary_page.filler_icon"),
    REWARD_NEXT_PAGE("gui.secondary_page.next_page_icon"),
    REWARD_PREV_PAGE("gui.secondary_page.prev_page_icon"),
    REWARD_CLOSE("gui.secondary_page.close_icon");

    @Getter private final String path;

    Path(String path) {
        this.path = path;
    }
}
