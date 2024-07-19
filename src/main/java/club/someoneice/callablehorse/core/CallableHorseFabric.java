package club.someoneice.callablehorse.core;

import club.someoneice.callablehorse.mixin.AbstractHorseAccess;
import club.someoneice.callablehorse.network.C2SPayloadCallHorse;
import club.someoneice.callablehorse.network.C2SPayloadSetHorse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;

public class CallableHorseFabric implements ModInitializer {
    public static final String MODID = "callablehorsefabric";
    public static final Logger LOG = LogManager.getLogger(MODID);
    public static final SoundEvent whistle = SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "whistle"));
    // C2S
    public static final ResourceLocation CALL_HORSE_PACKAGE = new ResourceLocation(CallableHorseFabric.MODID, "call_horse_key");
    public static final CustomPacketPayload.Type<C2SPayloadCallHorse> CALL_HORSE_TYPE = new CustomPacketPayload.Type<>(CALL_HORSE_PACKAGE);
    public static final ResourceLocation SET_HORSE_PACKAGE = new ResourceLocation(CallableHorseFabric.MODID, "set_horse_key");
    public static final CustomPacketPayload.Type<C2SPayloadSetHorse> SET_HORSE_TYPE = new CustomPacketPayload.Type<>(SET_HORSE_PACKAGE);
    public static int rangeCanCall = 0;
    public static boolean canRespawnHorse = true;
    public static boolean canCallFromOtherWorld = true;
    public WorldHorseData data;

    private static void onCallHorse(MinecraftServer server, ServerPlayer player) {
        var nbt = player.getCompoundTag();

        if (!nbt.contains("player_horse_UUID")) {
            player.displayClientMessage(Component.translatable("no_horse_can_call.callablehorse.info").withStyle(ChatFormatting.RED), true);
            return;
        }

        String uuid = nbt.getString("player_horse_UUID");
        var data = WorldHorseData.getServerState((((ServerLevel) player.level()).getServer()));

        if (canRespawnHorse && data.horseShouldRespawn.contains(uuid)) {
            if (!respawnAndCallHorse(player)) return;
            data.horseShouldRespawn.remove(uuid);
            player.displayClientMessage(Component.translatable("success_call_house.callablehorse.info").withStyle(ChatFormatting.GREEN), true);
            return;

        }

        if (rangeCanCall > 0) {
            for (var horse : player.level().getEntitiesOfClass(AbstractHorse.class, new AABB(player.getX() - rangeCanCall, player.getY() - rangeCanCall, player.getZ() - rangeCanCall, player.getX() + rangeCanCall, player.getY() + rangeCanCall, player.getZ() + rangeCanCall))) {
                String uuidHorse = horse.getCompoundTag().getString("player_horse_UUID");
                if (!uuid.equals(uuidHorse)) continue;
                callHorse(horse, player);
            }
        } else if (canCallFromOtherWorld) {
            List<Entity> entities = Lists.newArrayList();

            for (ServerLevel w : server.getAllLevels()) entities.addAll(ImmutableList.copyOf(w.getAllEntities()));
            for (var entity : entities) {
                if (!(entity instanceof AbstractHorse horse)) continue;
                String uuidHorse = horse.getCompoundTag().getString("player_horse_UUID");
                if (!uuid.equals(uuidHorse)) continue;
                callHorse(horse, player);
                return;
            }

            if (!callFromUnloadAndCallHorse(player))
                player.displayClientMessage(Component.translatable("horse_cannot_call.callablehorse.info").withStyle(ChatFormatting.RED), true);

            data.horseShouldKill.add(uuid);
        } else {
            for (var entity : ((ServerLevel) player.level()).getAllEntities()) {
                if (!(entity instanceof AbstractHorse horse)) continue;
                String uuidHorse = horse.getCompoundTag().getString("player_horse_UUID");
                if (!uuid.equals(uuidHorse)) continue;
                callHorse(horse, player);
                return;
            }

            if (!callFromUnloadAndCallHorse(player))
                player.displayClientMessage(Component.translatable("horse_cannot_call.callablehorse.info").withStyle(ChatFormatting.RED), true);

            data.horseShouldKill.add(uuid);
        }

        player.displayClientMessage(Component.translatable("success_call_house.callablehorse.info").withStyle(ChatFormatting.GREEN), true);
    }

    private static void callHorse(AbstractHorse horse, ServerPlayer player) {
        horse.ejectPassengers();
        ServerLevel level = (ServerLevel) player.level();
        horse.teleportTo(level, player.getX(), player.getY(), player.getZ(), null, horse.getYRot(), horse.getXRot());

        level.playSound(player, player.getOnPos(), whistle, SoundSource.MASTER);

        /*
        if (horse.position().distanceTo(player.position()) > 32)
            horse.teleportTo((ServerLevel) player.level(), player.getX(), player.getY(), player.getZ(), null, horse.getYRot(), horse.getXRot());
        else {
            horse.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(48);
            horse.getMoveControl().setWantedPosition(player.getX(), player.getY(), player.getZ(), 1.8f);
            horse.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), 1.8f);
        }
        */
        player.displayClientMessage(Component.translatable("success_call_house.callablehorse.info").withStyle(ChatFormatting.GREEN), true);
    }

    private static boolean respawnAndCallHorse(ServerPlayer player) {
        var nbt = player.getCompoundTag().getCompound("player_horse_nbt");
        var deadHorse = EntityType.by(nbt).get().create(player.level());
        if (!(deadHorse instanceof AbstractHorse horse)) return false;
        horse.load(nbt);
        ((AbstractHorseAccess) horse).getInventory().clearContent();
        horse.getCompoundTag().putString("player_horse_UUID", player.getCompoundTag().getString("player_horse_UUID"));
        horse.setPos(player.position());
        player.level().addFreshEntity(horse);
        return true;
    }

    private static boolean callFromUnloadAndCallHorse(ServerPlayer player) {
        var nbt = player.getCompoundTag().getCompound("player_horse_nbt");
        var deadHorse = EntityType.by(nbt).get().create(player.level());
        if (!(deadHorse instanceof AbstractHorse horse)) return false;
        horse.load(nbt);
        horse.setPos(player.position());
        player.level().addFreshEntity(horse);

        String uuid = UUID.randomUUID().toString();
        horse.getCompoundTag().putString("player_horse_UUID", uuid);
        player.getCompoundTag().putString("player_horse_UUID", uuid);

        CompoundTag horseTag = new CompoundTag();
        horse.save(horseTag);
        player.getCompoundTag().put("player_horse_nbt", horseTag);
        player.getCompoundTag().putString("player_horse_type", horse.getType().toString());

        player.level().playSound(player, player.getOnPos(), whistle, SoundSource.MASTER);

        return true;
    }

    private static void onSetHorse(ServerPlayer player) {
        var entity = player.getVehicle();
        if (!(entity instanceof AbstractHorse horse)) {
            player.displayClientMessage(Component.translatable("no_horse_can_set.callablehorse.info").withStyle(ChatFormatting.RED), true);
            return;
        }

        String uuid = UUID.randomUUID().toString();
        horse.getCompoundTag().putString("player_horse_UUID", uuid);
        player.getCompoundTag().putString("player_horse_UUID", uuid);

        CompoundTag horseTag = new CompoundTag();
        horse.save(horseTag);
        player.getCompoundTag().put("player_horse_nbt", horseTag);
        player.getCompoundTag().putString("player_horse_type", horse.getType().toString());
        player.displayClientMessage(Component.translatable("success_set_house.callablehorse.info").withStyle(ChatFormatting.GREEN), true);
    }

    @Override
    public void onInitialize() {
        var flag = FabricLoader.getInstance().isModLoaded("pineapple_coffee");
        if (flag) new Config();

        ServerPlayNetworking.registerGlobalReceiver(CALL_HORSE_TYPE, (payload, context) -> onCallHorse(context.server(), context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SET_HORSE_TYPE, ((payload, context) -> onSetHorse(context.player())));

        Registry.register(BuiltInRegistries.SOUND_EVENT, whistle.getLocation(), whistle);

        ServerLifecycleEvents.SERVER_STARTED.register(it -> this.data = WorldHorseData.getServerState(it));

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (this.data == null) return;
            if (this.data.horseShouldKill.isEmpty()) return;

            for (var entity : world.getAllEntities()) {
                if (!(entity instanceof AbstractHorse horse)) return;

                var nbt = horse.getCompoundTag();
                if (!nbt.contains("player_horse_UUID")) return;
                String uuid = nbt.getString("player_horse_UUID");
                if (!this.data.horseShouldRespawn.contains(uuid) && this.data.horseShouldKill.contains(uuid))
                    horse.kill();

                this.data.horseShouldKill.remove(uuid);
            }
        });


        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (this.data == null) return;
            if (this.data.horseShouldKill.isEmpty()) return;

            for (var entity : world.getAllEntities()) {
                if (!(entity instanceof AbstractHorse horse)) return;

                var nbt = horse.getCompoundTag();
                if (!nbt.contains("player_horse_UUID")) return;
                String uuid = nbt.getString("player_horse_UUID");
                if (!this.data.horseShouldRespawn.contains(uuid) && this.data.horseShouldKill.contains(uuid))
                    horse.kill();

                this.data.horseShouldKill.remove(uuid);
            }
        });
    }

    /*
    private static void checkHorseState(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        var nbt = player.getCompoundTag();
        if (!nbt.contains("player_horse_UUID")) {
            player.displayClientMessage(Component.translatable("no_horse_can_call.callablehorse.info").withStyle(ChatFormatting.RED), true);
            return;
        }

        byte[] bytes = nbt.getCompound("player_horse_nbt").toString().getBytes();

        FriendlyByteBuf buff = PacketByteBufs.create();
        buff.setInt(0, bytes.length);
        buff.setBytes(0, bytes);

        ServerPlayNetworking.send(player, CallableHorseFabricClient.OPEN_GUI, buff);
    }
    */
}