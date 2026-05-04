package nano.offtheshelf.client;

import nano.offtheshelf.OffTheShelf;
import nano.offtheshelf.client.renderer.blockentity.BookcaseRenderer;
import nano.offtheshelf.client.renderer.blockentity.TieredShelfRenderer;
import nano.offtheshelf.client.renderer.blockentity.WallShelfRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.Identifier;

public class OffTheShelfClient implements ClientModInitializer {
    public static final ModelLayerLocation BOOK_LAYER = mainLayer("book");

    @Override
    public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(BOOK_LAYER, BookcaseRenderer::getTexturedModelData);
        BlockEntityRenderers.register(OffTheShelf.BOOKCASE_BLOCK_ENTITY, BookcaseRenderer::new);
        BlockEntityRenderers.register(OffTheShelf.TIERED_SHELF_BLOCK_ENTITY, TieredShelfRenderer::new);
        BlockEntityRenderers.register(OffTheShelf.WALL_SHELF_BLOCK_ENTITY, WallShelfRenderer::new);
    }

    private static ModelLayerLocation mainLayer(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(OffTheShelf.MOD_ID, name), "main");
    }
}