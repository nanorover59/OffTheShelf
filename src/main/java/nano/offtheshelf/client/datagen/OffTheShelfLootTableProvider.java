package nano.offtheshelf.client.datagen;

import nano.offtheshelf.OffTheShelf;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class OffTheShelfLootTableProvider extends FabricBlockLootSubProvider {
    protected OffTheShelfLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        OffTheShelf.BOOKCASES.forEach(block -> this.dropSelf(block));
        OffTheShelf.TIERED_SHELVING.forEach(block -> this.dropSelf(block));
        OffTheShelf.WALL_SHELVING.forEach(block -> this.dropSelf(block));
    }
}