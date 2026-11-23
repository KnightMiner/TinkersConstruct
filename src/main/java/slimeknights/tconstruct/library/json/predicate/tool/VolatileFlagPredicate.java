package slimeknights.tconstruct.library.json.predicate.tool;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.tools.component.VolatileFlags;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

/** Predicate that checks if a volatile flag is set */
public record VolatileFlagPredicate(ResourceLocation key) implements ToolStackPredicate {
  public static final RecordLoadable<VolatileFlagPredicate> LOADER = RecordLoadable.create(Loadables.RESOURCE_LOCATION.requiredField("key", VolatileFlagPredicate::key), VolatileFlagPredicate::new);

  @Override
  public boolean matches(ToolStackView tool) {
    return VolatileFlags.hasFlag(tool, key);
  }

  @Override
  public RecordLoadable<VolatileFlagPredicate> getLoader() {
    return LOADER;
  }
}
