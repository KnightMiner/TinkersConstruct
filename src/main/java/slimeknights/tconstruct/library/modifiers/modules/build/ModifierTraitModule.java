package slimeknights.tconstruct.library.modifiers.modules.build;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.ToolContextPredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierTraitHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;

import java.util.List;

/**
 * Module for a modifier to have a nested modifier as a trait.
 * TODO 1.21: level range does not actually function when this module is applied across multiple sources, remove it.
 */
public record ModifierTraitModule(ModifierEntry modifier, boolean fixedLevel, IJsonPredicate<IToolContext> tool) implements ModifierTraitHook, ModifierModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierTraitModule>defaultHooks(ModifierHooks.MODIFIER_TRAITS);
  public static final RecordLoadable<ModifierTraitModule> LOADER = RecordLoadable.create(
    ModifierEntry.LOADABLE.directField(ModifierTraitModule::modifier),
    BooleanLoadable.INSTANCE.requiredField("fixed_level", ModifierTraitModule::fixedLevel),
    ToolContextPredicate.LOADER.defaultField("tool", ModifierTraitModule::tool),
    ModifierTraitModule::new);

  public ModifierTraitModule(ModifierId id, int level, boolean fixedLevel, IJsonPredicate<IToolContext> tool) {
    this(new ModifierEntry(id, level), fixedLevel, tool);
  }

  public ModifierTraitModule(ModifierId id, int level, boolean fixedLevel) {
    this(id, level, fixedLevel, ToolContextPredicate.ANY);
  }

  /** Common usecase of a modifier only applied to specifically tagged tools */
  public static ModifierTraitModule tagCondition(ModifierId id, TagKey<Item> tag) {
    return new ModifierTraitModule(id, 1, false, ToolContextPredicate.tag(tag));
  }

  @Override
  public void addTraits(IToolContext context, ModifierEntry self, TraitBuilder builder, boolean firstEncounter) {
    if (tool.matches(context)) {
      if (fixedLevel) {
        // fixed levels do not need to add again if already added
        if (firstEncounter) {
          builder.add(this.modifier);
        }
      // if just 1 level, can save some object creation
      } else if (self.getLevel() == 1) {
        builder.add(this.modifier);
      } else {
        // level of the trait is based on the level of the modifier, just multiply the two
        builder.add(this.modifier.withLevel(this.modifier.getLevel() * self.getLevel()));
      }
    }
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<ModifierTraitModule> getLoader() {
    return LOADER;
  }
}
