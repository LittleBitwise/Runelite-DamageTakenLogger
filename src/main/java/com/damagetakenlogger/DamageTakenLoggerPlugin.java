package com.damagetakenlogger;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

@Slf4j
@PluginDescriptor(
	name = "Logs the damage you've taken to chat."
)
public class DamageTakenLoggerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private DamageTakenLoggerConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Damage taken logger started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Damage taken logger stopped!");
	}

	@Provides
	DamageTakenLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DamageTakenLoggerConfig.class);
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		log.debug(String.format("%s was hit for %d",
				hitsplatApplied.getActor().getName(),
				hitsplatApplied.getHitsplat().getAmount()
		));

		if (hitsplatApplied.getActor() != client.getLocalPlayer()) return;

		int damage = hitsplatApplied.getHitsplat().getAmount();
		if (damage <= 0) return;

		String message = ColorUtil.wrapWithColorTag(
				String.format("You were hit for %d!", damage),
				new Color(255, 0, 0)
		);

		this.addGameMessage(message);
	}

	private void addGameMessage(String message)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
	}
}
