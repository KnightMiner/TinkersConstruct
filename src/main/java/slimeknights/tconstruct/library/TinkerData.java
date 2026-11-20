package slimeknights.tconstruct.library;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.AttachmentTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.DataComponentTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredAttachment;
import slimeknights.mantle.registration.holder.DeferredDataComponent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.item.PiggybackHandler;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.MultiplierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;

/** Handles registration of data components and data attachments added by Tinkers. */
public class TinkerData {
  private TinkerData() {}

  private static final DataComponentTypeDeferredRegister DATA_COMPONENTS = new DataComponentTypeDeferredRegister(Registries.DATA_COMPONENT_TYPE, TConstruct.MOD_ID);
  private static final AttachmentTypeDeferredRegister ATTACHMENTS = new AttachmentTypeDeferredRegister(TConstruct.MOD_ID);

  @Internal
  public static void register(IEventBus bus) {
    DATA_COMPONENTS.register(bus);
    ATTACHMENTS.register(bus);
  }

  /** Handles modifiers storing extra tool data on entities. */
  public static final DeferredAttachment<ModDataNBT> PERSISTENT_DATA = ATTACHMENTS.registerType("persistent_data", () -> AttachmentType.builder(ModDataNBT::new).serialize(ModDataNBT.CODEC).sync(ModDataNBT.STREAM_CODEC).copyOnDeath());
  /** Handles syncing the entity list for piggybackpacks */
  public static final DeferredAttachment<PiggybackHandler> PIGGYBACK_HANDLER = ATTACHMENTS.registerType("piggyback_handler", () -> AttachmentType.builder(PiggybackHandler::new));

  /* Parts and modifiers */
  /** Holds a single modifier on an item. Used for modifier crystals */
  public static final DeferredDataComponent<ModifierHolder> MODIFIER = DATA_COMPONENTS.registerPersistent("modifier", ModifierHolder.CODEC, ModifierHolder.STREAM_CODEC);
  /** Holds a single material on an item. Used for tool parts and other material items */
  public static final DeferredDataComponent<MaterialVariant> MATERIAL = DATA_COMPONENTS.registerPersistent("material", MaterialVariant.CODEC, MaterialVariant.STREAM_CODEC);

  /** Marker for tools and tool parts to indicate they are for display. Reduces the amount of data in the tooltip related to materials, which are likely render materials. */
  public static final DeferredDataComponent<Boolean> DISPLAY = DATA_COMPONENTS.registerPersistent("display", Codec.BOOL, ByteBufCodecs.BOOL);
  
  /* Tool */
  /** Holds a single material on an item. Used for tool parts and other material items */
  public static final DeferredDataComponent<MaterialNBT> MATERIALS = DATA_COMPONENTS.registerPersistent("materials", MaterialNBT.CODEC, MaterialNBT.STREAM_CODEC);
  /** Holds the list of modifiers from recipes, such as upgrades or abilities */
  public static final DeferredDataComponent<ModifierNBT> CRAFTED_MODIFIERS = DATA_COMPONENTS.registerPersistent("crafted_modifiers", ModifierNBT.CODEC, ModifierNBT.STREAM_CODEC);
  /** Holds the combined list of modifiers, including {@link #CRAFTED_MODIFIERS} and traits, sorted by priority. */
  public static final DeferredDataComponent<ModifierNBT> ALL_MODIFIERS = DATA_COMPONENTS.registerPersistent("all_modifiers", ModifierNBT.CODEC, ModifierNBT.STREAM_CODEC);
  /** Holds a cache of the current stats for the tool */
  public static final DeferredDataComponent<StatsNBT> TOOL_STATS = DATA_COMPONENTS.registerPersistent("tool_stats", StatsNBT.CODEC, StatsNBT.STREAM_CODEC);
  /** Holds a cache of stat multipliers on the tool */
  public static final DeferredDataComponent<MultiplierNBT> STAT_MULTIPLIERS = DATA_COMPONENTS.registerPersistent("stats_multipliers", MultiplierNBT.CODEC, MultiplierNBT.STREAM_CODEC);
}
