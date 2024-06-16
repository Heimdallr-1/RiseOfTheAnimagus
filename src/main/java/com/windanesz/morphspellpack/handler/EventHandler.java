package com.windanesz.morphspellpack.handler;

import com.windanesz.morphspellpack.Settings;
import com.windanesz.morphspellpack.Utils;
import com.windanesz.morphspellpack.items.ItemSoulPhylactery;
import com.windanesz.morphspellpack.registry.MSItems;
import com.windanesz.morphspellpack.registry.MSPotions;
import com.windanesz.morphspellpack.registry.MSSpells;
import com.windanesz.morphspellpack.spell.ILichSpell;
import com.windanesz.morphspellpack.spell.SoulConjuration;
import com.windanesz.morphspellpack.spell.SpellTransformation;
import com.windanesz.wizardryutils.capability.SummonedCreatureData;
import com.windanesz.wizardryutils.event.MinionDeathEvent;
import com.windanesz.wizardryutils.integration.baubles.BaublesIntegration;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellMinion;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.Location;
import electroblob.wizardry.util.SpellModifiers;
import me.ichun.mods.morph.api.event.MorphAcquiredEvent;
import me.ichun.mods.morph.api.event.MorphEvent;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber
public final class EventHandler {

	@SubscribeEvent
	public static void onMorphAcquiredEvent(MorphAcquiredEvent event) {
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.isCancelable() && event.getEntityPlayer().isPotionActive(WizardryPotions.transience)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerInteractEvent(MorphEvent event) {
		if (event.isCancelable() && event.getEntityPlayer().isPotionActive(WizardryPotions.transience)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onLivingDeathEvent(LivingDeathEvent event) {
		// force demorph player
		if (event.getEntity() instanceof EntityPlayer && !event.getEntity().getEntityWorld().isRemote) {
			if (Morph.eventHandlerServer.morphsActive.containsKey(event.getEntity().getName())) {
				SpellTransformation.demorphPlayer((EntityLivingBase) event.getEntity());
			}
		} else {
			if (event.getSource() != null && event.getSource().getTrueSource() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();

				// Liches can refill phys from the hotbar
				if (LichHandler.isLich(player)) {
					String entity = EntityList.getKey(event.getEntityLiving()).toString();
					boolean foundEmpty = false;
					for (ItemStack stack : InventoryUtils.getHotbar((EntityPlayer) event.getSource().getTrueSource())) {
						if (stack.getItem() instanceof ItemSoulPhylactery) {
							if (ItemSoulPhylactery.getEntity(stack).equals(entity) && ItemSoulPhylactery.getPercentFilled(stack) < 1f) {
								ItemSoulPhylactery.addPercent(stack, Settings.generalSettings.soul_phylactery_percent_gain_per_kill);
								return;
							} else if (!ItemSoulPhylactery.hasEntity(stack)) {
								foundEmpty = true;
							}
						}
					}

					// find the first empty slot if no match was found
					if (foundEmpty) {
						for (ItemStack stack : InventoryUtils.getHotbar((EntityPlayer) event.getSource().getTrueSource())) {
							if (stack.getItem() instanceof ItemSoulPhylactery && !ItemSoulPhylactery.hasEntity(stack)) {
								ItemSoulPhylactery.setEntity(stack, entity);
								ItemSoulPhylactery.addPercent(stack, Settings.generalSettings.soul_phylactery_percent_gain_per_kill);
								return;
							}
						}
					}
				} else if (ItemArtefact.isArtefactActive(player, MSItems.charm_soul_phylactery)) {
					ItemStack stack = BaublesIntegration.getEquippedArtefactStacks(player, ItemArtefact.Type.CHARM).get(0);
					String entity = EntityList.getKey(event.getEntityLiving()).toString();
					if (!ItemSoulPhylactery.hasEntity(stack)) {
						ItemSoulPhylactery.setEntity(stack, entity);
						ItemSoulPhylactery.addPercent(stack, Settings.generalSettings.soul_phylactery_percent_gain_per_kill);
						BaublesIntegration.setArtefactToSlot(player, stack, ItemArtefact.Type.CHARM);
					} else if (ItemSoulPhylactery.getEntity(stack).equals(entity)) {
						ItemSoulPhylactery.addPercent(stack, Settings.generalSettings.soul_phylactery_percent_gain_per_kill);
						BaublesIntegration.setArtefactToSlot(player, stack, ItemArtefact.Type.CHARM);
					}
				} if (ItemArtefact.isArtefactActive(player, MSItems.charm_soul_gem)) {
					ItemStack stack = BaublesIntegration.getEquippedArtefactStacks(player, ItemArtefact.Type.CHARM).get(0);
					ItemSoulPhylactery.addPercent(stack, Settings.generalSettings.soul_phylactery_percent_gain_per_kill);
				}
			}
		}

		handleMinionDeath(event.getEntityLiving());
	}

	@SubscribeEvent
	public static void onMinionDeath(MinionDeathEvent event) {
		handleMinionDeath(event.getEntityLiving());
	}

	public static void handleMinionDeath(EntityLivingBase minion) {
		// Check if the dying entity is a conjured soul
		if (minion.isPotionActive(MSPotions.conjured_soul)) {
			SummonedCreatureData data = SummonedCreatureData.get(minion);

			// Check if the conjured soul has an owner and the owner is a player
			if (data != null && data.getCaster() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) data.getCaster();

				if (ItemArtefact.isArtefactActive(player, MSItems.charm_soul_phylactery)) {
					ItemStack stack = BaublesIntegration.getEquippedArtefactStacks(player, ItemArtefact.Type.CHARM).get(0);
					if (ItemSoulPhylactery.hasEntity(stack)) {
						float cost = MSSpells.soul_conjuration.getProperty(SoulConjuration.PERCENT_COST_PER_HP).floatValue() * minion.getMaxHealth();
						ItemSoulPhylactery.addPercent(stack, cost);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPotionApplicableEvent(PotionEvent.PotionApplicableEvent event) {
		// WizardryPotions.frostbite resistance if level 4 pyramid has Element.ICE blocks based on TileEntityLichPhialHolder
		if (event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			if (LichHandler.isLich(player)) {
				if (event.getPotionEffect().getPotion() == MobEffects.POISON || event.getPotionEffect().getPotion() == MobEffects.REGENERATION) {
					event.setResult(Event.Result.DENY);
				}

				List<Optional<Element>> elements = LichHandler.getCrystalPyramid(player);
				if (elements.get(4).filter(el -> el == Element.ICE).isPresent()) {
					if (event.getPotionEffect().getPotion() == WizardryPotions.frost) {
						event.setResult(Event.Result.DENY);
					}
				}
				if (elements.get(4).filter(el -> el == Element.NECROMANCY).isPresent()) {
					if (event.getPotionEffect().getPotion() == MobEffects.WITHER) {
						event.setResult(Event.Result.DENY);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onSpellCastEventPre(SpellCastEvent.Pre event) {
		if (event.getCaster() instanceof EntityPlayer && event.getSpell() instanceof SpellMinion
				&& ItemArtefact.isArtefactActive((EntityPlayer) event.getCaster(), MSItems.charm_shapeshifter_orb)) {
			SpellMinion spell = (SpellMinion) event.getSpell();
			Method meth = ReflectionHelper.findMethod(SpellMinion.class, "createMinion", "createMinion", World.class,
					EntityLivingBase.class, SpellModifiers.class);
			try {
				EntityLivingBase entity = (EntityLivingBase) meth.invoke(event.getSpell(), event.getWorld(), event.getCaster(), event.getModifiers()); // world, caster, modifiers
				String entityString = EntityList.getKey(entity).toString();
				List<String> bannedMobs = Arrays.asList(Settings.generalSettings.skinchanger_banned_mobs);
				if (bannedMobs.contains(entityString)) {
					Utils.sendMessage(event.getCaster(), "spell.morphspellpack:skinchanger.mob_not_allowed", true);
					return;
				}
				if (SpellTransformation.morphPlayer(event.getCaster(), entityString, (int) event.getModifiers().get(WizardryItems.duration_upgrade) / 3)) {
					if (event.getSource() == SpellCastEvent.Source.SCROLL) {
						ItemStack mainStack = event.getCaster().getHeldItem(EnumHand.MAIN_HAND);
						if (mainStack.getItem() instanceof ItemScroll && Spell.byMetadata(mainStack.getItemDamage()) instanceof SpellMinion) {
							mainStack.shrink(1);
						}
						event.setCanceled(true);
					}
				}

			}
			catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		if (event.getSpell() instanceof ILichSpell) {
			if (!(event.getCaster() instanceof EntityPlayer) || !LichHandler.isLich(event.getCaster())) {
				event.setCanceled(true);
				return;
			}
		}

		if (LichHandler.isLich(event.getCaster())) {
			if (event.getCaster().isPotionActive(MSPotions.phylacterys_grasp)) {
				event.getModifiers().set(SpellModifiers.COST, event.getModifiers().get(SpellModifiers.COST) * 0.5f, false);
				event.getModifiers().set(SpellModifiers.POTENCY, event.getModifiers().get(SpellModifiers.POTENCY) * 1.6f, true);
				event.getModifiers().set(WizardryItems.blast_upgrade, event.getModifiers().get(WizardryItems.blast_upgrade) * 1.75f, true);
				event.getModifiers().set(WizardryItems.range_upgrade, event.getModifiers().get(WizardryItems.range_upgrade) * 1.75f, true);
				event.getModifiers().set(WizardryItems.duration_upgrade, event.getModifiers().get(WizardryItems.duration_upgrade) * 1.75f, true);
				event.getModifiers().set(WizardryItems.cooldown_upgrade, event.getModifiers().get(WizardryItems.cooldown_upgrade) * 0.5f, true);
			}

			List<Optional<Element>> elements = LichHandler.getCrystalPyramid(event.getCaster());
			Element element = event.getSpell().getElement();
			if (elements.get(1).filter(el -> el == element).isPresent()) {
				event.getModifiers().set(SpellModifiers.POTENCY, event.getModifiers().get(SpellModifiers.POTENCY) * Settings.generalSettings.lich_soul_receptacle_pyramid_layer1_potency_bonus, true);
			}

			if (elements.get(2).filter(el -> el == element && el != Element.MAGIC).isPresent()) {
				EntityPlayer player = (EntityPlayer) event.getCaster();
				Location location = LichHandler.getReceptacleLocation(player);
				if (location.dimension == player.dimension) {
					float bonusPercent = (float) Math.max(0, (Settings.generalSettings.lich_soul_receptacle_layer_2_max_distance - player.getDistance(location.pos.getX(), location.pos.getY(), location.pos.getZ())) / Settings.generalSettings.lich_soul_receptacle_layer_2_max_distance);
					event.getModifiers().set(WizardryItems.blast_upgrade, event.getModifiers().get(WizardryItems.blast_upgrade) * (1f + (bonusPercent * (0.25f * 2))), true);
					event.getModifiers().set(WizardryItems.range_upgrade, event.getModifiers().get(WizardryItems.range_upgrade) * (1f + (bonusPercent * (0.25f * 2))), true);
					event.getModifiers().set(WizardryItems.duration_upgrade, event.getModifiers().get(WizardryItems.duration_upgrade) * (1f + (bonusPercent * (0.25f * 2))), true);
					event.getModifiers().set(WizardryItems.cooldown_upgrade, event.getModifiers().get(WizardryItems.cooldown_upgrade) * (1f - (bonusPercent * (0.15f * 2))), true);
					event.getModifiers().set(SpellModifiers.COST, event.getModifiers().get(SpellModifiers.COST) * (1f - (bonusPercent * (0.15f * 2))), false);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPotionExpiryEvent(PotionEvent.PotionExpiryEvent event) {
		if (event.getEntity() instanceof EntityPlayer && event.getPotionEffect().getPotion() == MSPotions.fleshcloak && LichHandler.isLich(event.getEntity())) {
			SpellTransformation.morphPlayer((EntityLivingBase) event.getEntity(), LichHandler.getLichString(), -1);
		}
	}

	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();

			// Liches can avoid death
			if (!player.world.isRemote && player.getHealth() - event.getAmount() <= 0 && LichHandler.isLich(player) && LichHandler.getReceptacleLocation(player) != null) {
				LichHandler.respawnPlayerOnDeath(event);
				return;
			}

			// Reduce damage taken by 25% if the player has both the soul ward and soul phylactery artefacts equipped
			if (event.getSource().getTrueSource() instanceof EntityLivingBase && ItemArtefact.isArtefactActive(player, MSItems.amulet_soul_ward)
					&& ItemArtefact.isArtefactActive(player, MSItems.charm_soul_phylactery)) {
				ItemStack stack = BaublesIntegration.getEquippedArtefactStacks(player, ItemArtefact.Type.CHARM).get(0);
				if (stack.getItem() instanceof ItemSoulPhylactery) {
					if (ItemSoulPhylactery.getPercentFilled(stack) == 1) {
						ResourceLocation morphEntityKey = EntityList.getKey(event.getSource().getTrueSource());
						if (morphEntityKey != null && morphEntityKey.toString().equals(ItemSoulPhylactery.getEntity(stack))) {
							event.setAmount(event.getAmount() * 0.75f);
						}
					}
				}
			}

			// Phoenix feather artefact effect
			if (!event.isCanceled() && player.getHealth() - event.getAmount() <= 0 && ItemArtefact.isArtefactActive(player, MSItems.charm_phoenix_feather)
					&& !player.getCooldownTracker().hasCooldown(MSItems.charm_phoenix_feather)) {
				event.setCanceled(true);
				player.heal(6);
				SpellTransformation.morphPlayer(player, "ebwizardry:phoenix", 360);
				SpellModifiers modifiers = new SpellModifiers();
				Spell spell = Spells.firestorm;
				if (spell.cast(player.world, player, EnumHand.MAIN_HAND, 0, modifiers)) {
					MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(SpellCastEvent.Source.COMMAND, spell, player, modifiers));
					if (spell.requiresPacket()) {
						// Sends a packet to all players in dimension to tell them to spawn particles.
						// Only sent if the spell succeeded, because if the spell failed, you wouldn't
						// need to spawn any particles!
						IMessage msg = new PacketCastSpell.Message(player.getEntityId(), null, spell, modifiers);
						WizardryPacketHandler.net.sendToDimension(msg, player.world.provider.getDimension());
					}
				}
				player.getCooldownTracker().setCooldown(MSItems.charm_phoenix_feather, 72000);
			}
		}
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayer) {
			resumeMorph(event.getEntity());
		}
	}

	public static void resumeMorph(Entity player) {
		if (player instanceof EntityPlayer && !player.world.isRemote) {

			WizardData data = WizardData.get((EntityPlayer) player);
			if (data != null) {
				String lastMorph = data.getVariable(SpellTransformation.LAST_MORPH);
				Integer duration = data.getVariable(SpellTransformation.MORPH_DURATION);

				if (lastMorph != null && duration != null && duration > 0 && !lastMorph.equals(LichHandler.getLichString())) {
					SpellTransformation.morphPlayer((EntityPlayer) player, lastMorph, duration);
				}
			}
		}
	}
}
