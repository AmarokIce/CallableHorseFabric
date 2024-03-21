package club.someoneice.callablehorse.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {

    @Inject(method = "mobInteract", at = @At("RETURN"))
    public void onPlayerUse(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        AbstractHorse horse = (AbstractHorse) (Object) this;
        CompoundTag horseTag = new CompoundTag();
        horse.save(horseTag);
        player.getCompoundTag().put("player_horse_nbt", horseTag);
    }
}
