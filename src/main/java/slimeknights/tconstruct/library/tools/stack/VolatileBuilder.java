package slimeknights.tconstruct.library.tools.stack;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.tools.component.CollectionComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Builder for volatile data used in {@link slimeknights.tconstruct.library.tools.definition.module.build.VolatileDataToolHook} and {@link slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook} */
public class VolatileBuilder {
  private final Map<BuilderKey<?,?>,Object> builders = new HashMap<>();

  /** Gets the builder for the given key */
  @SuppressWarnings("unchecked")
  public <B> B get(BuilderKey<?,B> key) {
    if (Config.COMMON.logBadToolStackComponents.doesLog() && !ToolStack.isVolatile(key.component())) {
      Config.COMMON.logBadToolStackComponents.log("Attempting to use builder for " + key.component() + " but it is not a volatile key.");
    }
    return (B) builders.computeIfAbsent(key, key);
  }

  /** Handles generics for adding to the tool */
  private <C,B> void addToTool(MutableDataComponentHolder holder, BuilderKey<C,B> key) {
    // will technically create the builder if absent, but we always have one if the key exists
    C value = key.createComponent(get(key));
    if (value != null) {
      holder.set(key.component(), value);
    }
  }

  /** Adds all built data to the given object */
  public void addToTool(MutableDataComponentHolder holder) {
    for (BuilderKey<?,?> key : builders.keySet()) {
      if (ToolStack.isVolatile(key.component())) {
        addToTool(holder, key);
      }
    }
  }

  /** Key for the builder */
  public interface BuilderKey<C,B> extends Function<BuilderKey<?,?>,B> {
    /** Creates a new builder instance */
    B createBuilder();

    /** Gets the component type to save the data */
    DataComponentType<C> component();

    /** Creates the final component, or returns null if nothing to save */
    @Nullable
    C createComponent(B builder);

    /** @apiNote override {@link #createBuilder()} */
    @Override
    @NonExtendable
    default B apply(BuilderKey<?,?> key) {
      return createBuilder();
    }
  }

  /** {@link BuilderKey} for {@link CollectionComponent} */
  public interface CollectionBuilderKey<C extends CollectionComponent, B> extends BuilderKey<C,B> {
    /** Creates the final component. Automatically skips serializing if {@link CollectionComponent#isEmpty()} returns true. */
    C createCollection(B builder);

    @Nullable
    @Override
    default C createComponent(B builder) {
      C collection = createCollection(builder);
      return collection.isEmpty() ? null : collection;
    }
  }
}
