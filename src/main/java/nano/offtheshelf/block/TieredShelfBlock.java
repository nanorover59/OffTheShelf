package nano.offtheshelf.block;

import com.mojang.serialization.MapCodec;
import nano.offtheshelf.block.entity.TieredShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class TieredShelfBlock extends ModularShelfBlock {
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.box(0.0, 0.0, 10.0, 16.0, 16.0, 16.0));

    public TieredShelfBlock(final BlockBehaviour.Properties properties) {
        super(properties);
        BlockState defaultState = this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(MODEL, 0);
        this.registerDefaultState(defaultState);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TieredShelfBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TieredShelfBlockEntity(blockPos, blockState);
    }

    @Override
    public VoxelShape getDirectionVoxelShape(BlockState state, Direction direction) {
        return SHAPES.get(direction);
    }

    @Override
    public int getInteractionSlot(BlockState state, BlockHitResult hitResult) {
        Optional<Vec2> optionalVec = getInteractionVec2(hitResult);

        if(optionalVec.isEmpty() || hitResult.getDirection() != state.getValue(FACING))
            return -1;

        int columns = 3;
        Vec2 vec = optionalVec.get();
        return Mth.floor(vec.x * columns) * 2 + (vec.y > 0.5f ? 1 : 0);
    }
}