package nano.offtheshelf.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class OffTheShelfDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(OffTheShelfModelProvider::new);
        pack.addProvider(OffTheShelfEnglishLanguageProvider::new);
        pack.addProvider(OffTheShelfLootTableProvider::new);
        pack.addProvider(OffTheShelfRecipeProvider::new);
    }
}