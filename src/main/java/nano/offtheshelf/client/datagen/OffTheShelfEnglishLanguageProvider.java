package nano.offtheshelf.client.datagen;

import nano.offtheshelf.OffTheShelf;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.concurrent.CompletableFuture;

public class OffTheShelfEnglishLanguageProvider extends FabricLanguageProvider {
    protected OffTheShelfEnglishLanguageProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        OffTheShelf.BOOKCASES.forEach(block -> translationBuilder.add(block, this.convertID(BuiltInRegistries.BLOCK.getKey(block).getPath())));
        OffTheShelf.TIERED_SHELVING.forEach(block -> translationBuilder.add(block, this.convertID(BuiltInRegistries.BLOCK.getKey(block).getPath())));
        translationBuilder.add("creativeTab.offtheshelf", "Off The Shelf");
        translationBuilder.add("commands.shelf.mode", "Set %d shelves to %s");
    }

    /**
     * Convert the string ID to an English translation name.
     */
    private String convertID(String id) {
        String[] parts = id.split("_");
        StringBuilder name = new StringBuilder();

        for(String part : parts)
            name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");

        return name.toString().trim();
    }
}