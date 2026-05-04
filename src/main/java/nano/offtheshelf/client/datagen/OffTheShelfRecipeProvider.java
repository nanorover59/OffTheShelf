package nano.offtheshelf.client.datagen;

import nano.offtheshelf.OffTheShelf;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class OffTheShelfRecipeProvider extends FabricRecipeProvider {
    public static HashMap<Block, Tuple<ItemLike, ItemLike>> INGREDIENTS = new HashMap<>();
    static {
        // Wood Types
        INGREDIENTS.put(Blocks.OAK_PLANKS, new Tuple<>(Blocks.OAK_PLANKS, Blocks.OAK_SLAB));
        INGREDIENTS.put(Blocks.SPRUCE_PLANKS, new Tuple<>(Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB));
        INGREDIENTS.put(Blocks.BIRCH_PLANKS, new Tuple<>(Blocks.BIRCH_PLANKS, Blocks.BIRCH_SLAB));
        INGREDIENTS.put(Blocks.JUNGLE_PLANKS, new Tuple<>(Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_SLAB));
        INGREDIENTS.put(Blocks.ACACIA_PLANKS, new Tuple<>(Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB));
        INGREDIENTS.put(Blocks.DARK_OAK_PLANKS, new Tuple<>(Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB));
        INGREDIENTS.put(Blocks.MANGROVE_PLANKS, new Tuple<>(Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_SLAB));
        INGREDIENTS.put(Blocks.CHERRY_PLANKS, new Tuple<>(Blocks.CHERRY_PLANKS, Blocks.CHERRY_SLAB));
        INGREDIENTS.put(Blocks.PALE_OAK_PLANKS, new Tuple<>(Blocks.PALE_OAK_PLANKS, Blocks.PALE_OAK_SLAB));
        INGREDIENTS.put(Blocks.BAMBOO_PLANKS, new Tuple<>(Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_SLAB));
        INGREDIENTS.put(Blocks.CRIMSON_PLANKS, new Tuple<>(Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_SLAB));
        INGREDIENTS.put(Blocks.WARPED_PLANKS, new Tuple<>(Blocks.WARPED_PLANKS, Blocks.WARPED_SLAB));
        // Mineral Types
        INGREDIENTS.put(Blocks.STONE, new Tuple<>(Blocks.STONE, Blocks.STONE_SLAB));
        INGREDIENTS.put(Blocks.STONE_BRICKS, new Tuple<>(Blocks.STONE_BRICKS, Blocks.STONE_BRICK_SLAB));
        INGREDIENTS.put(Blocks.POLISHED_GRANITE, new Tuple<>(Blocks.POLISHED_GRANITE, Blocks.POLISHED_GRANITE_SLAB));
        INGREDIENTS.put(Blocks.POLISHED_DIORITE, new Tuple<>(Blocks.POLISHED_DIORITE, Blocks.POLISHED_DIORITE_SLAB));
        INGREDIENTS.put(Blocks.POLISHED_ANDESITE, new Tuple<>(Blocks.POLISHED_ANDESITE, Blocks.POLISHED_ANDESITE_SLAB));
        INGREDIENTS.put(Blocks.POLISHED_DEEPSLATE, new Tuple<>(Blocks.POLISHED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE_SLAB));
        INGREDIENTS.put(Blocks.DEEPSLATE_BRICKS, new Tuple<>(Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_BRICK_SLAB));
        INGREDIENTS.put(Blocks.PRISMARINE_BRICKS, new Tuple<>(Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICK_SLAB));
        INGREDIENTS.put(Blocks.DARK_PRISMARINE, new Tuple<>(Blocks.DARK_PRISMARINE, Blocks.DARK_PRISMARINE_SLAB));
        INGREDIENTS.put(Blocks.NETHER_BRICKS, new Tuple<>(Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_SLAB));
        INGREDIENTS.put(Blocks.RED_NETHER_BRICKS, new Tuple<>(Blocks.RED_NETHER_BRICKS, Blocks.RED_NETHER_BRICK_SLAB));
        INGREDIENTS.put(Blocks.POLISHED_BLACKSTONE_BRICKS, new Tuple<>(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB));
        INGREDIENTS.put(Blocks.END_STONE_BRICKS, new Tuple<>(Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICK_SLAB));
        INGREDIENTS.put(Blocks.PURPUR_BLOCK, new Tuple<>(Blocks.PURPUR_BLOCK, Blocks.PURPUR_SLAB));
        // Metal Types
        INGREDIENTS.put(Blocks.CUT_COPPER, new Tuple<>(Blocks.CUT_COPPER, Blocks.CUT_COPPER_SLAB));
        INGREDIENTS.put(Blocks.IRON_BLOCK, new Tuple<>(Items.IRON_INGOT, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE));
        INGREDIENTS.put(Blocks.GOLD_BLOCK, new Tuple<>(Items.GOLD_INGOT, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE));
    }

    public OffTheShelfRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

                OffTheShelf.BOOKCASES.forEach(block -> {
                    Block baseBlock = OffTheShelf.BASE_BLOCKS.get(block);

                    if(INGREDIENTS.containsKey(baseBlock)) {
                        Tuple<ItemLike, ItemLike> ingredients = INGREDIENTS.get(baseBlock);
                        shaped(RecipeCategory.DECORATIONS, block, 3)
                                .pattern("aba")
                                .pattern("aba")
                                .pattern("aba")
                                .define('a', ingredients.getA())
                                .define('b', ingredients.getB())
                                .group("bookcase")
                                .unlockedBy(getHasName(baseBlock), has(baseBlock))
                                .save(output);
                    }
                });

                OffTheShelf.TIERED_SHELVING.forEach(block -> {
                    Block baseBlock = OffTheShelf.BASE_BLOCKS.get(block);

                    if(INGREDIENTS.containsKey(baseBlock)) {
                        Tuple<ItemLike, ItemLike> ingredients = INGREDIENTS.get(baseBlock);
                        shaped(RecipeCategory.DECORATIONS, block, 3)
                                .pattern("aaa")
                                .pattern("aaa")
                                .pattern("aaa")
                                .define('a', ingredients.getB())
                                .group("tiered_shelf")
                                .unlockedBy(getHasName(baseBlock), has(baseBlock))
                                .save(output);
                    }
                });

                OffTheShelf.WALL_SHELVING.forEach(block -> {
                    Block baseBlock = OffTheShelf.BASE_BLOCKS.get(block);

                    if(INGREDIENTS.containsKey(baseBlock)) {
                        Tuple<ItemLike, ItemLike> ingredients = INGREDIENTS.get(baseBlock);
                        shaped(RecipeCategory.DECORATIONS, block, 3)
                                .pattern("aaa")
                                .define('a', ingredients.getB())
                                .group("wall_shelf")
                                .unlockedBy(getHasName(baseBlock), has(baseBlock))
                                .save(output);
                    }
                });
            }
        };
    }

    @Override
    public String getName() {
        return "OffTheShelfRecipeProvider";
    }
}