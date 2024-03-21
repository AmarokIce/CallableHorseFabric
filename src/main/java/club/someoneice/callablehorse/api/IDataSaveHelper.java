package club.someoneice.callablehorse.api;

import net.minecraft.nbt.CompoundTag;

public interface IDataSaveHelper {
    default CompoundTag getCompoundTag() {
        return new CompoundTag();
    }

    default void setCompoundTag(CompoundTag compoundTag) {
    }
}
