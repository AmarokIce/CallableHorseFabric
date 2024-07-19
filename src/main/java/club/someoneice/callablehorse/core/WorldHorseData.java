package club.someoneice.callablehorse.core;

import com.google.common.collect.Lists;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WorldHorseData extends SavedData {
    public static final String KEY = "callable_horse_common_data";
    private static final SavedData.Factory<WorldHorseData> type = new Factory<>(WorldHorseData::new, (tag, data) -> {
        if (tag.contains("callable_horse_world_data")) return WorldHorseData.createFromNbt(tag);
        else return new WorldHorseData();
    }, DataFixTypes.SAVED_DATA_MAP_DATA);
    public List<String> horseShouldKill = Lists.newArrayList();
    public List<String> horseShouldRespawn = Lists.newArrayList();

    public static WorldHorseData createFromNbt(CompoundTag tag) {
        WorldHorseData state = new WorldHorseData();
        var tagListShouldKill = (ListTag) tag.getList("horseShouldKill", 8);
        var tagListShouldRespawn = (ListTag) tag.getList("horseShouldRespawn", 8);

        tagListShouldKill.forEach(it -> state.horseShouldKill.add(it.toString()));
        tagListShouldRespawn.forEach(it -> state.horseShouldRespawn.add(it.toString()));

        return state;
    }

    public static WorldHorseData getServerState(MinecraftServer server) {
        DimensionDataStorage persistentStateManager = server.getLevel(Level.OVERWORLD).getDataStorage();
        WorldHorseData state = persistentStateManager.computeIfAbsent(type, CallableHorseFabric.MODID);

        state.setDirty();
        return state;
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider registries) {
        ListTag tagListShouldKill = new ListTag();
        ListTag tagListShouldRespawn = new ListTag();

        horseShouldKill.forEach(it -> tagListShouldKill.add(StringTag.valueOf(it)));
        horseShouldRespawn.forEach(it -> tagListShouldRespawn.add(StringTag.valueOf(it)));

        compoundTag.put("horseShouldKill", tagListShouldKill);
        compoundTag.put("horseShouldRespawn", tagListShouldRespawn);

        return compoundTag;
    }
}
