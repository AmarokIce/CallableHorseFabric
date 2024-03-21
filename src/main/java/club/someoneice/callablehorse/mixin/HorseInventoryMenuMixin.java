package club.someoneice.callablehorse.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.HorseInventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseInventoryMenu.class)
public abstract class HorseInventoryMenuMixin {
    @Shadow private @Final AbstractHorse horse;

    @Inject(method = "removed", at = @At("TAIL"))
    public void onClose(Player player, CallbackInfo ci) {
        CompoundTag horseTag = new CompoundTag();
        horse.save(horseTag);
        player.getCompoundTag().put("player_horse_nbt", horseTag);
    }
}
