package com.windanesz.morphspellpack.handler;

import com.windanesz.morphspellpack.Settings;
import com.windanesz.morphspellpack.block.BlockLichPhialHolder;
import com.windanesz.morphspellpack.block.TileEntityLichPhialHolder;
import com.windanesz.morphspellpack.items.ItemSoulPhylactery;
import com.windanesz.morphspellpack.registry.MSPotions;
import com.windanesz.morphspellpack.spell.SpellTransformation;
import com.windanesz.wizardryutils.WizardryUtils;
import com.windanesz.wizardryutils.tools.WizardryUtilsTools;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.Location;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mod.EventBusSubscriber
public class LichHandler {

	private static final String LICH = "morphspellpack:lich";
	public static final IStoredVariable<Boolean> IS_LICH = IStoredVariable.StoredVariable.ofBoolean("isLich", Persistence.ALWAYS).setSynced();

	public static final IStoredVariable<Location> LOCATION_KEY = new IStoredVariable.StoredVariable<>("lichReceptacle",
			Location::toNBT, Location::fromNBT, Persistence.ALWAYS).setSynced();
	public static final IStoredVariable<Location> LAST_DEATH_LOCATION = new IStoredVariable.StoredVariable<>("lichLastDeathPosition",
			Location::toNBT, Location::fromNBT, Persistence.ALWAYS).setSynced();

	private LichHandler() {}

	public static void init() {
		WizardData.registerStoredVariables(IS_LICH, LOCATION_KEY, LAST_DEATH_LOCATION);
	}

	public static boolean isLich(Entity player) {
		if (player instanceof EntityPlayer) {
			WizardData data = WizardData.get((EntityPlayer) player);
			if (data != null) {
				Boolean lich = data.getVariable(IS_LICH);
				return lich != null && lich;
			}
		}
		return false;
	}

	public static void setLich(Entity player, boolean lich) {
		if (player instanceof EntityPlayer) {
			WizardData data = WizardData.get((EntityPlayer) player);
			if (data != null) {
				data.setVariable(IS_LICH, lich);
				data.sync();
			}
		}
	}

	public static void setLastDeathLocation(Entity player, Location location) {
		if (player instanceof EntityPlayer) {
			WizardData data = WizardData.get((EntityPlayer) player);
			if (data != null) {
				data.setVariable(LAST_DEATH_LOCATION, location);
				data.sync();
			}
		}
	}

	@SubscribeEvent
	public static void onInteractItem(PlayerInteractEvent.RightClickItem event) {
		if (event.getItemStack().getItemUseAction() == EnumAction.EAT && isLich(event.getEntityPlayer())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		// liches doesn't like cakes
		if (event.getEntityPlayer().world.getBlockState(event.getPos()).getBlock() == Blocks.CAKE && isLich(event.getEntity())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayer && isLich(event.getEntity()) && !((EntityPlayer) event.getEntity()).isPotionActive(MSPotions.curse_of_transformation)
				&& !((EntityPlayer) event.getEntity()).isPotionActive(MSPotions.fleshcloak)
				&& !PlayerMorphHandler.getInstance().hasMorph(event.getEntity().getName(), Side.SERVER)) {
			SpellTransformation.morphPlayer((EntityPlayer) event.getEntity(), LichHandler.getLichString(), -1);
		}
	}

	public static String getLichString() {
		return LICH;
	}

	public static void checkReceptacleIntegrity(EntityPlayer player) {
		Location location = getReceptacleLocation(player);
		if (location != null) {
			World world = player.world;
			if (player.dimension != location.dimension) {
				MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
				if (server != null) {
					world = server.getWorld(location.dimension);
				}
			}
			// Check if the block at the location still exists
			if (world.getBlockState(location.pos).getBlock() instanceof BlockLichPhialHolder) {
				// Check if the player's soul phylactery is still present
				TileEntity tileEntity = world.getTileEntity(location.pos); // Get the tile entity again after changing the block state
				if (tileEntity instanceof TileEntityLichPhialHolder) {
					TileEntityLichPhialHolder tileLichPhialHolder = (TileEntityLichPhialHolder) tileEntity;
					ItemStack phylactery = tileLichPhialHolder.getPhylactery();
					if (!phylactery.isEmpty() && ItemSoulPhylactery.getEntity(phylactery).equals(player.getName())) {
						return;
					}
				}
			}
		}
		setReceptacleLocation(player, null);
	}

	public static Location getReceptacleLocation(Entity player) {
		if (player instanceof EntityPlayer) {
			WizardData data = WizardData.get((EntityPlayer) player);
			if (data != null) {
				return data.getVariable(LOCATION_KEY);
			}
		}
		return null;
	}

	public static Location getLastDeathLocation(Entity player) {
		if (player instanceof EntityPlayer) {
			WizardData data = WizardData.get((EntityPlayer) player);
			if (data != null) {
				return data.getVariable(LAST_DEATH_LOCATION);
			}
		}
		return null;
	}

	public static void setReceptacleLocation(Entity player, Location location) {
		if (player instanceof EntityPlayer) {
			WizardData data = WizardData.get((EntityPlayer) player);
			if (data != null) {
				data.setVariable(LOCATION_KEY, location);
				data.sync();
			}
		}
	}

	public static void respawnPlayerOnDeath(LivingHurtEvent event) {
		EntityPlayer player = (EntityPlayer) event.getEntityLiving();

		Location location = LichHandler.getReceptacleLocation(player);

		LichHandler.setLastDeathLocation(player, new Location(player.getPosition(), player.dimension));

		if (!player.getActivePotionEffects().isEmpty()) {
			ItemStack milk = new ItemStack(Items.MILK_BUCKET);

			for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) { // Get outta here, CMEs
				// The PotionEffect version (as opposed to Potion) does not call cleanup callbacks
				if (effect.isCurativeItem(milk)) {
					player.removePotionEffect(effect.getPotion());
				}
			}
		}

		// Get the TileEntityLichPhialHolder at the receptacle location
		TileEntity tileEntity = player.world.getTileEntity(location.pos);
		if (tileEntity instanceof TileEntityLichPhialHolder) {
			TileEntityLichPhialHolder lichPhialHolder = (TileEntityLichPhialHolder) tileEntity;
			// Consume phylactery charges
			lichPhialHolder.consumePercent(Settings.generalSettings.soul_receptacle_percent_lost_on_death);

			int skulls = 0;
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			if (server != null) {
				World world = server.getWorld(location.dimension);
				for (BlockPos blockPos : BlockUtils.getBlockSphere(location.pos, 4)) {
					if (world.getBlockState(blockPos).getBlock() == Blocks.SKULL) {
						skulls++;
					}
				}
				if (skulls >= 4) {
					return;
				}
			}

			event.setAmount(0);
			player.setHealth(2);
			if (lichPhialHolder.getPercentFilled() < 0.1f) {
				WizardryUtilsTools.sendMessage(player, "You are too weak to form a body. You must wait till you regain enough strength.", false);
				player.setGameType(GameType.SPECTATOR);
				player.addPotionEffect(new PotionEffect(WizardryPotions.containment, 140, 0));
			}
			if (lichPhialHolder.getPercentFilled() > 0.1f) {
				player.addPotionEffect(new PotionEffect(WizardryPotions.transience, 200, 3));
				player.addPotionEffect(new PotionEffect(WizardryPotions.containment, 300, 3));
				player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 300, 2));
			}
		}

		teleportEntity(location.dimension, location.pos.getX() + 1, location.pos.getY(), location.pos.getZ(), true, player);

		player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 120, 2));
		if (ForgeRegistries.POTIONS.containsKey(new ResourceLocation("ancientspellcraft:magical_exhaustion"))) {
			player.addPotionEffect(new PotionEffect(Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(
					new ResourceLocation("ancientspellcraft:magical_exhaustion"))), 400, 3));
		}
		player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 120, 2));
		player.addExhaustion(200);

	}

	public static void teleportEntity(int targetDim, double x, double y, double z, boolean causeBlindness, EntityPlayer entity) {

		if (!DimensionManager.isDimensionRegistered(targetDim) || entity == null || entity.isBeingRidden() || entity.isRiding()) {
			return;
		}

		EntityPlayerMP player = (entity instanceof EntityPlayerMP) ? (EntityPlayerMP) entity : null;

		boolean sameDim = (player.dimension == targetDim);

		if (!player.world.isRemote && causeBlindness) {
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 70, 0));

			if (targetDim == -1) { // nether teleport
				//avoid falling through the void
				if (y <= 32) {
					y = 35;
				} else if (y >= 120) { // and TP-ing too high in the nether
					y = 120;
				}
			}

			if (!sameDim) {
				if (ForgeHooks.onTravelToDimension(player, targetDim)) {
					teleportPlayerToDimension(player, targetDim, new BlockPos(x, y, z), player.cameraYaw);
				} else {
					// cancelled TP
				}
			} else {
				player.connection.setPlayerLocation(
						x + 0.5d,
						y,
						z + 0.5d,
						player.cameraYaw,
						player.rotationPitch);
			}

			if (player.dimension == -1) { // nether teleport

				// avoid lava and fire
				BlockPos blockPos = (new BlockPos(player.posX, player.posY, player.posZ));
				IBlockState currState = player.world.getBlockState(blockPos);
				Material material = currState.getMaterial();
				while ((material == Material.LAVA || material == Material.FIRE) && y <= 100) {
					blockPos = blockPos.add(0, 3, 0);
					currState = player.world.getBlockState(blockPos);
					material = currState.getMaterial();
					y = y + 3;
				}

				for (BlockPos currPos : BlockPos.getAllInBox(blockPos.add(-2, -1, -2), blockPos.add(2, -1, 2))) {
					if (player.world.isAirBlock(currPos) || player.world.getBlockState(currPos).getBlock() == Blocks.LAVA || player.world.getBlockState(currPos).getBlock() == Blocks.FLOWING_LAVA) {
						player.world.setBlockState(currPos, Blocks.NETHERRACK.getDefaultState());
					}
				}

				if (player.world.isAreaLoaded(player.getPosition(), 1)) {
					player.world.setBlockToAir(player.getPosition());
					player.world.setBlockToAir(player.getPosition().up());
				}

			}

			// TP to overworld
			if (targetDim == 0) {
				BlockPos blockPos = (new BlockPos(x, y, z));
				if (!(player.world.isAirBlock(blockPos) && player.world.isAirBlock(blockPos.up()) && !player.world.isAirBlock(blockPos.down()))) {
					while (player.world.isAirBlock(blockPos.down())) {
						blockPos = blockPos.add(0, -1, 0);
						y--;
					}
				}
			}

			//		player.world.getMinecraftServer().getPlayerList().transferPlayerToDimension((EntityPlayerMP) player, targetDim, new SpellTeleporter(x + 0.5, y, z + 0.5));

			// experminental
			//				player.setPositionAndUpdate(x + 0.5, y, z + 0.5);
			//				IMessage msg = new PacketTransportation.Message();
			//				WizardryPacketHandler.net.sendToDimension(msg, player.world.provider.getDimension());
			// experminental

		}
	}

	private static void teleportPlayerToDimension(EntityPlayerMP player, int dimension, BlockPos destination, float yaw) {
		int startDimension = player.dimension;
		MinecraftServer server = player.getServer();
		PlayerList playerList = server.getPlayerList();
		WorldServer startWorld = server.getWorld(startDimension);
		WorldServer destinationWorld = server.getWorld(dimension);

		player.dimension = dimension;
		player.connection.sendPacket(new SPacketRespawn(
				dimension,
				destinationWorld.getDifficulty(),
				destinationWorld.getWorldInfo().getTerrainType(),
				player.interactionManager.getGameType()));

		playerList.updatePermissionLevel(player);
		startWorld.removeEntityDangerously(player);
		player.isDead = false;

		player.setLocationAndAngles(
				destination.getX() + 0.5d,
				destination.getY(),
				destination.getZ() + 0.5d,
				yaw,
				player.rotationPitch);

		destinationWorld.spawnEntity(player);
		destinationWorld.updateEntityWithOptionalForce(player, false);
		player.setWorld(destinationWorld);

		playerList.preparePlayer(player, startWorld);
		player.connection.setPlayerLocation(
				destination.getX() + 0.5d,
				destination.getY(),
				destination.getZ() + 0.5d,
				yaw,
				player.rotationPitch);

		player.interactionManager.setWorld(destinationWorld);
		player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
		playerList.updateTimeAndWeatherForPlayer(player, destinationWorld);
		playerList.syncPlayerInventory(player);

		// Reapply potion effects

		for (PotionEffect potionEffect : player.getActivePotionEffects()) {
			player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potionEffect));
		}

		// Resend player XP otherwise the XP bar won't show up until XP is either gained or lost

		player.connection.sendPacket(new SPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));

		// Remove the ender dragon hp bar when porting out of the End, otherwise if the dragon is still alive
		// the hp bar won't go away and if you then reenter the End, you will have multiple boss hp bars.

		FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, startDimension, dimension);
	}

	public static List<Optional<Element>> getCrystalPyramid(EntityLivingBase player) {
		if (player instanceof EntityPlayer && isLich(player)) {
			WizardData data = WizardData.get((EntityPlayer) player);
			Location loc = getReceptacleLocation(player);
			if (loc != null) {
				World world = DimensionManager.getWorld(loc.dimension);
				if (world != null) {
					TileEntity tile = world.getTileEntity(loc.pos);
					if (tile instanceof TileEntityLichPhialHolder) {
						return ((TileEntityLichPhialHolder) tile).getCrystalPyramid();
					}
				}
			}
		}
		// Return an empty list of 5 elements
		return new ArrayList<>(Collections.nCopies(5, Optional.empty()));
	}
}
