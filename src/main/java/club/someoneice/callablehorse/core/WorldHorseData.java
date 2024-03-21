package club.someoneice.callablehorse.core;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class WorldHorseData extends SavedData {
    public static final String KEY = "callable_horse_common_data";

    public List<String> horseShouldKill = Lists.newArrayList();
    public List<String> horseShouldRespawn = Lists.newArrayList();

    @Override @NotNull
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag tagListShouldKill = new ListTag();
        ListTag tagListShouldRespawn = new ListTag();

        horseShouldKill.forEach(it -> tagListShouldKill.add(StringTag.valueOf(it)));
        horseShouldRespawn.forEach(it -> tagListShouldRespawn.add(StringTag.valueOf(it)));

        compoundTag.put("horseShouldKill", tagListShouldKill);
        compoundTag.put("horseShouldRespawn", tagListShouldRespawn);

        return compoundTag;
    }

    public static WorldHorseData createFromNbt(CompoundTag tag) {
        WorldHorseData state = new WorldHorseData();
        var tagListShouldKill =    (ListTag) tag.getList("horseShouldKill", 8);
        var tagListShouldRespawn = (ListTag) tag.getList("horseShouldRespawn", 8);

        tagListShouldKill.forEach(it -> state.horseShouldKill.add(it.toString()));
        tagListShouldRespawn.forEach(it -> state.horseShouldRespawn.add(it.toString()));

        return state;
    }

    private static final Function<CompoundTag, WorldHorseData> type = compoundTag -> {
        if (compoundTag.contains("callable_horse_world_data")) return WorldHorseData.createFromNbt(compoundTag);
        else return new WorldHorseData();
    };

    public static WorldHorseData getServerState(MinecraftServer server) {
        DimensionDataStorage persistentStateManager = server.getLevel(Level.OVERWORLD).getDataStorage();

        WorldHorseData state = persistentStateManager.computeIfAbsent(type, WorldHorseData::new, CallableHorseFabric.MODID);

        state.setDirty();
        return state;
    }
}
