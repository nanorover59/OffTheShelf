package nano.offtheshelf.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class BookcaseRenderState extends BlockEntityRenderState {
    public Direction direction = Direction.NORTH;
    public int model = 0;
    public int highlight = -1;
    public Component name = null;
    public Component author = null;
    public int[] colors;
    public int[] variants;
    public boolean[] flatten;
}