package slimeknights.tconstruct.shared.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Function;

/** Particle data for a fluid particle */
public record FluidParticleData(ParticleType<FluidParticleData> getType, FluidStack fluid) implements ParticleOptions {
  /** Particle type for a fluid particle */
  public static class Type extends ParticleType<FluidParticleData> {
    private final MapCodec<FluidParticleData> codec;
    private final StreamCodec<RegistryFriendlyByteBuf,FluidParticleData> streamCodec;
    public Type() {
      super(false);
      Function<FluidStack,FluidParticleData> constructor = fluid -> new FluidParticleData(this, fluid);
      // recreation of the fluid stack codec but without amount
      codec = RecordCodecBuilder.<FluidStack>mapCodec(instance -> instance.group(
        FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
        DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStack::getComponentsPatch)
      ).apply(instance, (fluid, components) -> new FluidStack(fluid, FluidType.BUCKET_VOLUME, components))).xmap(constructor, FluidParticleData::fluid);
      streamCodec = FluidStack.STREAM_CODEC.map(constructor, FluidParticleData::fluid);
    }

    @Override
    public MapCodec<FluidParticleData> codec() {
      return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, FluidParticleData> streamCodec() {
      return streamCodec;
    }
  }
}
