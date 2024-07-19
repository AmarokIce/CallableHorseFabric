package club.someoneice.callablehorse.network;

import club.someoneice.callablehorse.core.CallableHorseFabric;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class C2SPayloadCallHorse implements CustomPacketPayload {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return CallableHorseFabric.CALL_HORSE_TYPE;
    }
}
