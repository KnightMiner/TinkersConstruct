package slimeknights.tconstruct.library.materials.stats;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.util.typed.TypedMapBuilder;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Packet sending material stats from server to client. */
@Internal
public record UpdateMaterialStatsPacket(Map<MaterialId, Collection<IMaterialStats>> materialToStats) implements ISimplePacket {
  public static final Type<UpdateMaterialStatsPacket> TYPE = new Type<>(TConstruct.getResource("update_material_stats"));
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateMaterialStatsPacket> CODEC = new Codec(MaterialRegistry.getInstance().getStatTypeLoader());

  @Override
  public Type<UpdateMaterialStatsPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    MaterialRegistry.updateMaterialStatsFromServer(this);
  }

  /** Stream codec instance. Setup to allow swapping in a different streamable for the stat types. */
  @VisibleForTesting
  record Codec(Streamable<MaterialStatType<?>> statTypeLoader) implements StreamCodec<RegistryFriendlyByteBuf,UpdateMaterialStatsPacket> {
    @Override
    public UpdateMaterialStatsPacket decode(RegistryFriendlyByteBuf buffer) {
      int materialCount = buffer.readInt();
      Map<MaterialId, Collection<IMaterialStats>> materialToStats = new HashMap<>(materialCount);
      for (int i = 0; i < materialCount; i++) {
        MaterialId id = MaterialId.create(buffer.readResourceLocation());
        int statCount = buffer.readInt();
        List<IMaterialStats> statList = new ArrayList<>();
        for (int j = 0; j < statCount; j++) {
          ResourceLocation statId = null;
          try {
            MaterialStatType<?> statType = statTypeLoader.decode(buffer);
            statId = statType.getId();
            statList.add(statType.getLoadable().decode(buffer, TypedMapBuilder.builder().put(MaterialStatType.CONTEXT_KEY, statType).build()));
          } catch (RuntimeException e) {
            TConstruct.LOG.error("Could not deserialize stat {} for material {}. Are client and server in sync?", statId, id, e);
            throw e;
          }
        }
        materialToStats.put(id, statList);
      }
      return new UpdateMaterialStatsPacket(materialToStats);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, UpdateMaterialStatsPacket packet) {
      buffer.writeInt(packet.materialToStats.size());
      packet.materialToStats.forEach((materialId, stats) -> {
        buffer.writeResourceLocation(materialId);
        buffer.writeInt(stats.size());
        for (IMaterialStats stat : stats) {
          encodeStat(buffer, stat, stat.getType(), materialId);
        }
      });
    }

    /**
     * Encodes a single material stat
     *
     * @param buffer     Buffer instance
     * @param stat       Stat to encode
     * @param material   Material being encoded
     */
    @SuppressWarnings("unchecked")
    private <T extends IMaterialStats> void encodeStat(RegistryFriendlyByteBuf buffer, IMaterialStats stat, MaterialStatType<T> type, MaterialId material) {
      try {
        MaterialStatsId.LOADABLE.encode(buffer, type.getId());
        type.getLoadable().encode(buffer, (T) stat);
      } catch (RuntimeException e) {
        TConstruct.LOG.error("Could not encode stat {} for material {}", stat.getIdentifier(), material, e);
        throw e;
      }
    }
  }
}
