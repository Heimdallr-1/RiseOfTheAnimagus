package com.windanesz.morphspellpack.client;

import com.windanesz.morphspellpack.CommonProxy;
import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.Settings;
import com.windanesz.morphspellpack.block.TileEntityLichPhialHolder;
import com.windanesz.morphspellpack.client.render.RenderDisguise;
import com.windanesz.morphspellpack.client.render.RenderLich;
import com.windanesz.morphspellpack.client.render.TileEntityLichPhialHolderRenderer;
import com.windanesz.morphspellpack.entity.construct.EntityStarfall;
import com.windanesz.morphspellpack.entity.living.EntityBatMinion;
import com.windanesz.morphspellpack.entity.living.EntityDisguise;
import com.windanesz.morphspellpack.entity.living.EntityLich;
import com.windanesz.morphspellpack.entity.living.EntityLightWisp;
import com.windanesz.morphspellpack.entity.living.EntityTemporaryRabbit;
import com.windanesz.morphspellpack.entity.projectile.EntityRadiantSpark;
import electroblob.wizardry.client.renderer.entity.RenderBlank;
import electroblob.wizardry.client.renderer.entity.RenderMagicArrow;
import electroblob.wizardry.spell.Mine;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderBat;
import net.minecraft.client.renderer.entity.RenderRabbit;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	public static KeyBinding KEY_ACTIVATE_MORPH_ABILITY;

	/**
	 * Called from preInit() in the main mod class to initialise the renderers.
	 */
	public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTemporaryRabbit.class, RenderRabbit::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBatMinion.class, RenderBat::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDisguise.class, RenderDisguise::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityLightWisp.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityStarfall.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityLich.class, RenderLich::new);

		//projectiles
		RenderingRegistry.registerEntityRenderingHandler(EntityRadiantSpark.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(MorphSpellPack.MODID, "textures/entity/radiant_spark.png"), true, 8.0, 2.0, 16, 5, false));

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLichPhialHolder.class, new TileEntityLichPhialHolderRenderer());
	}

	@Override
	public void init() {
		registerKeybindings();
	}

	@Override
	public void renderUmbralVeil(World world, double x, double y, double z) {
		if (Settings.generalSettings.umbral_veil_particles) {
			boolean firstPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView == 0;
			if (Minecraft.getMinecraft().player.getDistance(x, y, z) > 3 || !firstPerson) {
				for (int i = 0; i < 8; i++) {
					ParticleBuilder.create(ParticleBuilder.Type.FLASH).clr(1, 1, 1).pos(x, y - 0.3 + world.rand.nextFloat() * 0.5, z).time(20).spin(0.4, 0.05).spawn(world);
				}
			}
		}
	}

	@Override
	public void preInit() {
		//		MinecraftForge.EVENT_BUS.unregister(Morph.eventHandlerClient);
	}

	private void registerKeybindings() {
		// Initializing
		KEY_ACTIVATE_MORPH_ABILITY = new KeyBinding("key.morphspellpack.activate_morph_ability", Keyboard.KEY_K, "key.morphspellpack.category");
		ClientRegistry.registerKeyBinding(KEY_ACTIVATE_MORPH_ABILITY);
	}
}