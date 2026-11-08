package slimeknights.tconstruct.common.data.model;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.entity.FrameType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Generates static block state models outside Neo's builder. Used for item frames notably. */
public class SimpleBlockStateProvider extends GenericDataProvider {
  public SimpleBlockStateProvider(PackOutput output) {
    super(output, PackOutput.Target.RESOURCE_PACK, "blockstates");
  }

  /** Creates a model object */
  private static JsonObject model(ResourceLocation name) {
    JsonObject model = new JsonObject();
    model.addProperty("model", name.toString());
    return model;
  }

  @Override
  public CompletableFuture<?> run(CachedOutput output) {
    Map<ResourceLocation, JsonObject> models = new HashMap<>();

    // add block states for all fancy item frames, directing to with and without map
    for (FrameType type : FrameType.values()) {
      String name = type == FrameType.REVERSED_GOLD ? FrameType.GOLD.getSerializedName() : type.getSerializedName();
      JsonObject variants = new JsonObject();
      variants.add("map=false", model(TConstruct.getResource("block/frame/" + name)));
      variants.add("map=true", model(TConstruct.getResource("block/frame/" + name + "_map")));
      JsonObject blockstate = new JsonObject();
      blockstate.add("variants", variants);
      models.put(TConstruct.getResource(type.getSerializedName() + "_item_frame"), blockstate);
    }

    // save the models
    return allOf(models.entrySet().stream().map(entry -> saveJson(output, entry.getKey(), entry.getValue())));
  }

  @Override
  public String getName() {
    return "Tinkers Simple Block State Provider";
  }
}
