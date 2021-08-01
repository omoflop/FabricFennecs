package mod.omoflop.fabricfennecs;

import mod.omoflop.fabricfennecs.entity.FennecEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationContextImpl;
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import software.bernie.geckolib3.GeckoLib;

public class FabricFennecs implements ModInitializer {

	public static final EntityType<FennecEntity> FENNEC = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("fabricfennecs:fennec"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE,
					FennecEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.4f)).build()
	);

	public static final Item FENNEC_SPAWN_EGG = new SpawnEggItem(FENNEC, 0xb29476, 0xf1ebdb, new Item.Settings().group(ItemGroup.MISC));

	public static Tag<Item> FENNEC_FOODS;
	public static Tag<Block> COMFY_BLOCKS;
	public static Tag<Block> COMFY_WARM_BLOCKS;

	@Override
	public void onInitialize() {
		GeckoLib.initialize();

		FENNEC_FOODS = TagRegistry.item(new Identifier("fabricfennecs:fennec_foods"));
		COMFY_BLOCKS = TagRegistry.block(new Identifier("fabricfennecs:comfy"));
		COMFY_WARM_BLOCKS = TagRegistry.block(new Identifier("fabricfennecs:comfy_when_warm"));

		BiomeModifications.addSpawn(
				selection -> selection.getBiome().getCategory() == Biome.Category.DESERT,
				SpawnGroup.CREATURE,
				FENNEC,
				3, 1, 3
		);

		FabricDefaultAttributeRegistry.register(FENNEC, FennecEntity.createEntityAttributes());
		Registry.register(Registry.ITEM, new Identifier("fabricfennecs:fennec_spawn_egg"), FENNEC_SPAWN_EGG);
	}
}
