package nano.offtheshelf.block;

import com.mojang.serialization.MapCodec;
import nano.offtheshelf.OffTheShelf;
import nano.offtheshelf.block.entity.OffTheShelfBlockEntity;
import nano.offtheshelf.block.entity.WallShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class WallShelfBlock extends ModularShelfBlock {
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.box(0.0, 6.0, 10.0, 16.0, 9.0, 16.0));
    private static final Map<Direction, VoxelShape> SHAPES_INTERACTION = Shapes.rotateHorizontal(Block.box(0.0, 6.0, 10.0, 16.0, 16.0, 16.0));
    private static final Map<Direction, VoxelShape> BOTTOM_SHAPES = Shapes.rotateHorizontal(Block.box(0.0, 0.0, 10.0, 16.0, 3.0, 16.0));
    private static final Map<Direction, VoxelShape> BOTTOM_SHAPES_INTERACTION = Shapes.rotateHorizontal(Block.box(0.0, 0.0, 10.0, 16.0, 8.0, 16.0));
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public WallShelfBlock(final Properties properties) {
        super(properties);
        BlockState defaultState = this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BOTTOM, false);
        this.registerDefaultState(defaultState);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ModularShelfBlock.FACING);
        builder.add(BOTTOM);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(WallShelfBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WallShelfBlockEntity(blockPos, blockState);
    }

    @Override
    public VoxelShape getDirectionVoxelShape(BlockState state, Direction direction) {
        if(state.getValue(BOTTOM))
            return BOTTOM_SHAPES.get(direction);
        else
            return SHAPES.get(direction);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction direction = state.getValue(FACING);

        if(state.getValue(BOTTOM))
            return BOTTOM_SHAPES_INTERACTION.get(direction);
        else
            return SHAPES_INTERACTION.get(direction);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        boolean bottom = context.getClickedFace() == Direction.UP || !(context.getClickLocation().y - context.getClickedPos().getY() > 0.5);
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(BOTTOM, bottom);
    }

    @Override
    public int getInteractionSlot(BlockState state, BlockHitResult hitResult) {
        Optional<Vec2> optionalVec = getInteractionVec2(hitResult);

        if(!optionalVec.isEmpty() && (hitResult.getDirection() == state.getValue(FACING) || hitResult.getDirection() == Direction.UP)) {
            Vec2 vec = optionalVec.get();
            return Mth.floor(vec.x * 3);
        }

        return -1;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, OffTheShelf.WALL_SHELF_BLOCK_ENTITY, OffTheShelfBlockEntity::tick);
    }
}