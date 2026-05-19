package nano.offtheshelf.block;

import com.mojang.serialization.MapCodec;
import nano.offtheshelf.block.entity.BookcaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class BookcaseBlock extends ModularShelfBlock {
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0));

    public BookcaseBlock(final BlockBehaviour.Properties properties) {
        super(properties);
        BlockState defaultState = this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(MODEL, 0);
        this.registerDefaultState(defaultState);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BookcaseBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BookcaseBlockEntity(blockPos, blockState);
    }

    @Override
    public VoxelShape getDirectionVoxelShape(BlockState state, Direction direction) {
        return SHAPES.get(direction);
    }

    @Override
    public int getMaxCount() {
        return 1;
    }

    @Override
    public SoundEvent getSoundPlaceItem(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
    }

    @Override
    public SoundEvent getSoundTakeItem(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
    }

    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block block, @Nullable final Orientation orientation, final boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);

        // Track whether the end slots of neighbor bookcases are empty.
        if(level.getBlockEntity(pos) instanceof BookcaseBlockEntity blockEntity) {
            blockEntity.checkLeftNeighbor();
            blockEntity.checkRightNeighbor();
        }
    }

    @Override
    public int getInteractionSlot(BlockState state, BlockHitResult hitResult) {
        Optional<Vec2> optionalVec = getInteractionVec2(hitResult);

        if(optionalVec.isEmpty() || hitResult.getDirection() != state.getValue(FACING))
            return -1;

        Vec2 vec = optionalVec.get();
        int model = state.getValue(MODEL);
        int columns = model == CENTER ? 8 : 7;
        float minX = model == SINGLE ? 0.0625f : (model == LEFT ? 0.125f : 0.0f);
        float maxX = model == SINGLE ? 0.9375f : (model == RIGHT ? 0.875f : 1.0f);
        float x = Mth.map(Mth.clamp(vec.x, minX, maxX), minX, maxX, 0.0f, 0.99f);
        return Mth.floor(x * columns) * 2 + (vec.y > 0.5f ? 1 : 0);
    }
}