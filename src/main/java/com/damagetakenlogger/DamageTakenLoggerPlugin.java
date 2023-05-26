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
import java.time.Duration;
import java.time.Instant;
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

	@Provides
	DamageTakenLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DamageTakenLoggerConfig.class);
	}


	@Nullable
	private static Actor combatActor;

	private static int combatBeginXp;

	private static Instant combatTimer;

	/**
	 * Track hits, report when local player actually suffers damage.
	 */
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied damage)
	{
		if (damage.getActor() != client.getLocalPlayer()) return;

		int amount = damage.getHitsplat().getAmount();

		if (amount <= 0) return;

		if (config.showDamageTaken()) {
			String message = ColorUtil.wrapWithColorTag(
					String.format("You were hit for %d!", amount),
					config.damageTakenColor()
			);
			this.addGameMessage(message);
		}
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

		if (target !=  null) {
			combatTimer = Instant.now();
			log.debug(String.format("Combat begins with '%s'", target.getName()));
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
		if (ev.getActor() != combatActor) return;

		int combatXpGained = this.calculateGainedCombatXp();
		log.debug(String.format("Combat finished with '%s' for %d XP", ev.getActor().getName(), (int)combatXpGained));
		String xpOutput = this.getXpOutput(combatXpGained);

		if (config.showDamageTaken()) {
			String message = ColorUtil.wrapWithColorTag(
					String.format("You gained %s!", xpOutput),
					config.experienceGainedColor()
			);
			this.addGameMessage(message);
		}

		combatActor = null;
	}

	private void addGameMessage(String message)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
	}

	/**
	 * Calculate the sum of all skills that contribute to combat level.
	 *
	 * @return total XP
	 */
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

	private int calculateGainedCombatXp()
	{
		float combatXpGained = calculateTotalCombatXp() - combatBeginXp;
		Duration combatDuration = Duration.between(combatTimer, Instant.now());

		switch (config.ExperienceGainedAmount())
		{
			case PER_SECOND:
				return (int) (combatXpGained / combatDuration.getSeconds());
			default:
			case TOTAL:
				return (int) combatXpGained;
		}
	}

	private String getXpOutput(int combatXpGained)
	{
		switch (config.ExperienceGainedAmount())
		{
			case PER_SECOND:
				return String.format("%d XP/s", combatXpGained);
			default:
			case TOTAL:
				return String.format("%d XP", combatXpGained);
		}
	}
}
