package nano.offtheshelf.block.entity;

import nano.offtheshelf.OffTheShelf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TieredShelfBlockEntity extends OffTheShelfBlockEntity {
    public TieredShelfBlockEntity(final BlockPos worldPosition, final BlockState blockState) {
        super(OffTheShelf.TIERED_SHELF_BLOCK_ENTITY, worldPosition, blockState);
    }

    @Override
    public int getInventorySize() {
        return 6;
    }
}