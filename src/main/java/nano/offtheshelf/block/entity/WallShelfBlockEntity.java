package nano.offtheshelf.block.entity;

import nano.offtheshelf.OffTheShelf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class WallShelfBlockEntity extends OffTheShelfBlockEntity {
    public WallShelfBlockEntity(final BlockPos worldPosition, final BlockState blockState) {
        super(OffTheShelf.WALL_SHELF_BLOCK_ENTITY, worldPosition, blockState);
    }

    @Override
    public int getInventorySize() {
        return 3;
    }
}