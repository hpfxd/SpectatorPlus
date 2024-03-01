package com.hpfxd.spectatorplus.fabric.client.config;

import com.hpfxd.spectatorplus.fabric.SpectatorMod;
import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.config.ServerConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;

public class ClothConfigIntegration {
    private ClothConfigIntegration() {
    }

    public static Screen getConfigScreen(Screen parent) {
        final ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("gui.spectatorplus.config.title"));

        setupClientConfig(builder);
        setupServerConfig(builder);

        builder.setSavingRunnable(() -> {
            try {
                SpectatorClientMod.configLoader.save(SpectatorClientMod.config);
                SpectatorMod.configLoader.save(SpectatorMod.config);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save configs", e);
            }
        });
        return builder.build();
    }

    private static void setupClientConfig(ConfigBuilder builder) {
        final ConfigCategory category = builder.getOrCreateCategory(Component.translatable("gui.spectatorplus.config.client.title"));
        final ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        final ClientConfig config = SpectatorClientMod.config;

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.renderStatus.name"), config.renderStatus)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.renderStatus.tooltip"))
                .setSaveConsumer(val -> config.renderStatus = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.renderStatusIfNoHotbar.name"), config.renderStatusIfNoHotbar)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.renderStatusIfNoHotbar.tooltip"))
                .setSaveConsumer(val -> config.renderStatusIfNoHotbar = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.renderHotbar.name"), config.renderHotbar)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.renderHotbar.tooltip"))
                .setSaveConsumer(val -> config.renderHotbar = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.renderArms.name"), config.renderArms)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.renderArms.tooltip"))
                .setSaveConsumer(val -> config.renderArms = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.showSpectators.name"), config.showSpectators)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.showSpectators.tooltip"))
                .setSaveConsumer(val -> config.showSpectators = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.highlightSpectators.name"), config.highlightSpectators)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.highlightSpectators.tooltip", Component.translatable("key.spectatorOutlines")))
                .setSaveConsumer(val -> config.highlightSpectators = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.teleportAutoSpectate.name"), config.teleportAutoSpectate)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.teleportAutoSpectate.tooltip"))
                .setSaveConsumer(val -> config.teleportAutoSpectate = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.openScreens.name"), config.openScreens)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.openScreens.tooltip"))
                .setSaveConsumer(val -> config.openScreens = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.hideTooltipUntilMouseMove.name"), config.hideTooltipUntilMouseMove)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.hideTooltipUntilMouseMove.tooltip"))
                .setSaveConsumer(val -> config.hideTooltipUntilMouseMove = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.client.screensNoOverride.name"), config.screensNoOverride)
                .setTooltip(Component.translatable("gui.spectatorplus.config.client.screensNoOverride.tooltip"))
                .setSaveConsumer(val -> config.screensNoOverride = val)
                .build());
    }

    private static void setupServerConfig(ConfigBuilder builder) {
        final ConfigCategory category = builder.getOrCreateCategory(Component.translatable("gui.spectatorplus.config.server.title"));
        final ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        final ServerConfig config = SpectatorMod.config;

        category.setDescription(new MutableComponent[]{Component.translatable("gui.spectatorplus.config.server.description")});

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.server.allowTransferBetweenLevels.name"), config.allowTransferBetweenLevels)
                .setTooltip(Component.translatable("gui.spectatorplus.config.server.allowTransferBetweenLevels.tooltip"))
                .setSaveConsumer(val -> config.allowTransferBetweenLevels = val)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gui.spectatorplus.config.server.autoUpdatePosition.name")
                        .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MC-148993"))), config.autoUpdatePosition)
                .setTooltip(Component.translatable("gui.spectatorplus.config.server.autoUpdatePosition.tooltip", Component.literal("MC-148993").withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE)))
                .setSaveConsumer(val -> config.autoUpdatePosition = val)
                .build());
    }
}
