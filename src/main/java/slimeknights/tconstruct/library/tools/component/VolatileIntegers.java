package slimeknights.tconstruct.library.tools.component;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.tools.entity.ThrownTool;

import static slimeknights.tconstruct.TConstruct.getResource;

/**
 * Contains resource locations for {@link TinkerData#VOLATILE_INTEGER}, used to prevent hardcoding some modifier checks.
 * Volatile integers do not need to be registered anywhere to be used; simply check for the key in relevant code.
 * TODO: consider replacing with integer components, might need a registry for that to work.
 * @see slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook
 * @see slimeknights.tconstruct.library.tools.definition.module.build.VolatileDataToolHook
 * @see slimeknights.tconstruct.library.tools.stack.BuilderKeys#INTEGERS
 */
public interface VolatileIntegers {
  // general
  /** Contains the amount a tool's range is expanded. */
  ResourceLocation EXPANDED = getResource("expanded");

  // ranged
  /** Maximum number of arrows to fire for a bow or crossbow. */
  ResourceLocation MULTISHOT = getResource("multishot");
  /** Changes the loyalty level on a {@link ThrownTool}. */
  ResourceLocation LOYALTY = getResource("loyalty");
  /** Changes the magnet level on a {@link ThrownTool}. */
  ResourceLocation MAGNET = getResource("magnet");

  // armor
  /** Duration to reflect projectiles in ticks. */
  ResourceLocation REFLECTING = getResource("reflecting");

  // inventory
  /** Stores the total inventory slot count across all modifiers. TODO: replace with a custom volatile object with more slot info? */
  ResourceLocation TOTAL_SLOTS = getResource("total_item_slots");


  /** Gets the value of an integer on the given tool. */
  static int get(DataComponentHolder holder, ResourceLocation key) {
    return ResourceMap.get(holder, TinkerData.VOLATILE_INTEGER).getOrDefault(key, 0);
  }

  /** Creates a new builder instance. Used in {@link slimeknights.tconstruct.library.tools.stack.BuilderKeys} */
  static Builder builder() {
    return new Builder();
  }

  class Builder {
    private final Object2IntMap<ResourceLocation> map = new Object2IntOpenHashMap<>();

    private Builder() {}

    /** Adds the given value into the builder. Will sum with any existing values. */
    public Builder add(ResourceLocation key, int value) {
      map.put(key, map.getOrDefault(key, 0) + value);
      return this;
    }

    /** Builds the final map. */
    public ResourceMap<Integer> build() {
      return ResourceMap.create(map);
    }
  }
}
