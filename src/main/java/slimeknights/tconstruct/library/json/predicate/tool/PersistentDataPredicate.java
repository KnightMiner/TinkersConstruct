package slimeknights.tconstruct.library.json.predicate.tool;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.tools.stack.ToolContext;

/** Predicate that checks if a key is present in persistent data */
public record PersistentDataPredicate(ResourceLocation key) implements ToolContextPredicate {
  public static final RecordLoadable<PersistentDataPredicate> LOADER = RecordLoadable.create(Loadables.RESOURCE_LOCATION.requiredField("key", PersistentDataPredicate::key), PersistentDataPredicate::new);

  @Override
  public boolean matches(ToolContext tool) {
    return tool.getPersistentData().contains(key);
  }

  @Override
  public RecordLoadable<PersistentDataPredicate> getLoader() {
    return LOADER;
  }
}
