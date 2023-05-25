package com.damagetakenlogger;

import com.google.inject.Provides;

import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "Damage Taken Logger",
	description = "Displays damage taken in chat, as well as total XP gained per kill."
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
		combatActor = null;
		combatBeginXp = 0;
		log.info("Damage taken logger stopped!");
	}

	@Provides
	DamageTakenLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DamageTakenLoggerConfig.class);
	}



	@Nullable
	private static Actor combatActor;
	private static int combatBeginXp;



	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
//		log.debug(String.format("%s was hit for %d",
//				hitsplatApplied.getActor().getName(),
//				hitsplatApplied.getHitsplat().getAmount()
//		));

		if (hitsplatApplied.getActor() != client.getLocalPlayer()) return;

		int damage = hitsplatApplied.getHitsplat().getAmount();
		if (damage <= 0) return;

		String message = ColorUtil.wrapWithColorTag(
				String.format("You were hit for %d!", damage),
				new Color(255, 0, 0)
		);

		this.addGameMessage(message);
	}

	/**
	 * Update combat actor when attacking new target.
	 * Reset time and accumulated XP.
	 */
	@Subscribe
	public void onInteractingChanged(InteractingChanged interact)
	{
		if (interact.getSource() != client.getLocalPlayer()) return;

		Actor target = interact.getTarget();

		if (target == null) {
			log.debug("TODO: HANDLE IT OR REMOVE AS REDUNDANT");
		} else {
			log.debug("COMBAT START");
			combatBeginXp = calculateTotalCombatXp();
		}

		combatActor = target;
	}

	/**
	 * On kill, log accumulated XP.
	 */
	@Subscribe
	public void onActorDeath(ActorDeath ev)
	{
		if (ev.getActor() == combatActor) {
			int combatAfterXp = calculateTotalCombatXp();
			log.debug("COMBAT SUCCESSFUL");

			String message = ColorUtil.wrapWithColorTag(
					String.format("Gained %d XP!", combatAfterXp - combatBeginXp),
					new Color(0, 166, 255) // Todo: get from config
			);
			this.addGameMessage(message);
			combatBeginXp = combatAfterXp; // Just in case.
			combatActor = null;
		}
	}

	private void addGameMessage(String message)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
	}

	private int calculateTotalCombatXp()
	{
		int[] combatSkills = {
				client.getSkillExperience(Skill.ATTACK),
				client.getSkillExperience(Skill.DEFENCE),
				client.getSkillExperience(Skill.HITPOINTS),
				client.getSkillExperience(Skill.MAGIC),
				client.getSkillExperience(Skill.RANGED),
				client.getSkillExperience(Skill.STRENGTH),
		};

		return Arrays.stream(combatSkills).sum();
	}
}
