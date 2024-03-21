package club.someoneice.callablehorse.mixin;

import club.someoneice.callablehorse.api.IDataSaveHelper;
import club.someoneice.callablehorse.core.CallableHorseFabric;
import club.someoneice.callablehorse.core.WorldHorseData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntity.class)
public abstract class EntityMixin implements IDataSaveHelper {
    @Shadow public abstract void remove(Entity.RemovalReason reason);

    @Unique
    private CompoundTag callableHorseEntityDataTag;

    @Override
    public CompoundTag getCompoundTag() {
        this.callableHorseEntityDataTag = this.callableHorseEntityDataTag == null ? new CompoundTag() : callableHorseEntityDataTag;
        return this.callableHorseEntityDataTag;
    }

    @Override
    public void setCompoundTag(CompoundTag compoundTag) {
        this.callableHorseEntityDataTag = compoundTag;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void onSaveNBT(CompoundTag compound, CallbackInfo info) {
        if (callableHorseEntityDataTag != null) compound.put(WorldHorseData.KEY, callableHorseEntityDataTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void onLoadNBT(CompoundTag compound, CallbackInfo info) {
        callableHorseEntityDataTag = compound.contains(WorldHorseData.KEY) ? compound.getCompound(WorldHorseData.KEY) : new CompoundTag();
    }

    @Inject(method = "die", at = @At("HEAD"))
    public void onEntityDie(DamageSource damageSource, CallbackInfo ci) {
        if (!CallableHorseFabric.canRespawnHorse) return;
        if (damageSource.is(DamageTypes.GENERIC_KILL)) return;
        if (((LivingEntity) (Object) this) instanceof AbstractHorse horse) {
            if (horse.level().isClientSide) return;
            if (!horse.getCompoundTag().contains("player_horse_UUID")) return;
            var data = WorldHorseData.getServerState(((ServerLevel) horse.level()).getServer());
            data.horseShouldRespawn.add(horse.getCompoundTag().getString("player_horse_UUID"));
        }
    }
}
