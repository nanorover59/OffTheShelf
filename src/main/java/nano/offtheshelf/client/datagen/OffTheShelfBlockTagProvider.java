package nano.offtheshelf.client.datagen;

import nano.offtheshelf.OffTheShelf;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OffTheShelfBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public static HashMap<TagKey, List<Block>> TAGS = new HashMap<>();
    static {
        // Mineable With Axe
        TAGS.put(BlockTags.MINEABLE_WITH_AXE,
                List.of(Blocks.OAK_PLANKS,
                        Blocks.SPRUCE_PLANKS,
                        Blocks.BIRCH_PLANKS,
                        Blocks.JUNGLE_PLANKS,
                        Blocks.ACACIA_PLANKS,
                        Blocks.DARK_OAK_PLANKS,
                        Blocks.MANGROVE_PLANKS,
                        Blocks.CHERRY_PLANKS,
                        Blocks.PALE_OAK_PLANKS,
                        Blocks.BAMBOO_PLANKS,
                        Blocks.CRIMSON_PLANKS,
                        Blocks.WARPED_PLANKS));
        // Mineable With Pickaxe
        TAGS.put(BlockTags.MINEABLE_WITH_PICKAXE,
                List.of(Blocks.STONE,
                        Blocks.STONE_BRICKS,
                        Blocks.POLISHED_GRANITE,
                        Blocks.POLISHED_DIORITE,
                        Blocks.POLISHED_ANDESITE,
                        Blocks.POLISHED_DEEPSLATE,
                        Blocks.DEEPSLATE_BRICKS,
                        Blocks.PRISMARINE_BRICKS,
                        Blocks.DARK_PRISMARINE,
                        Blocks.NETHER_BRICKS,
                        Blocks.POLISHED_BLACKSTONE_BRICKS,
                        Blocks.END_STONE_BRICKS,
                        Blocks.PURPUR_BLOCK,
                        Blocks.CUT_COPPER,
                        Blocks.IRON_BLOCK,
                        Blocks.GOLD_BLOCK));
        // Needs Stone Tool
        TAGS.put(BlockTags.NEEDS_STONE_TOOL,
                List.of(Blocks.CUT_COPPER,
                        Blocks.IRON_BLOCK));
        // Needs Iron Tool
        TAGS.put(BlockTags.NEEDS_IRON_TOOL,
                List.of(Blocks.GOLD_BLOCK));
    }

    public OffTheShelfBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        TAGS.forEach((tag, blocks) -> {
            Set<Block> blockSet = new HashSet<>();
            OffTheShelf.BASE_BLOCKS.forEach((block, baseBlock) -> {
                if(blocks.contains(baseBlock))
                    blockSet.add(block);
            });
            valueLookupBuilder(tag).addAll(blockSet).setReplace(false);
        });
    }
}