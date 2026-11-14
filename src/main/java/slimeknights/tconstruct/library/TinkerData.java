package slimeknights.tconstruct.library;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.AttachmentTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.DataComponentTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredAttachment;
import slimeknights.mantle.registration.holder.DeferredDataComponent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.item.PiggybackHandler;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

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

  /* Tools */
  /** Holds a single modifier on an item. Used for modifier crystals */
  public static final DeferredDataComponent<ModifierHolder> MODIFIER = DATA_COMPONENTS.registerPersistent("modifier", ModifierHolder.CODEC, ModifierHolder.STREAM_CODEC);
}
