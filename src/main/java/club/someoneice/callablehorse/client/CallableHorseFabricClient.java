package club.someoneice.callablehorse.client;

import club.someoneice.callablehorse.core.CallableHorseFabric;
import club.someoneice.callablehorse.network.C2SPayloadCallHorse;
import club.someoneice.callablehorse.network.C2SPayloadSetHorse;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.glfw.GLFW;

public class CallableHorseFabricClient implements ClientModInitializer {
    // S2C
    public static ResourceLocation OPEN_GUI = new ResourceLocation(CallableHorseFabric.MODID, "open_horse_gui");
    public KeyMapping SET_HORSE = KeyBindingHelper.registerKeyBinding(new KeyMapping(keyStringKey("setHorse"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, keyStringKey("key." + CallableHorseFabric.MODID)));
    public KeyMapping CALL_HORSE = KeyBindingHelper.registerKeyBinding(new KeyMapping(keyStringKey("callHorse"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, keyStringKey("key." + CallableHorseFabric.MODID)));

    private String keyStringKey(String name) {
        return String.format("key.%s.%s", name, CallableHorseFabric.MODID);
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(it -> {
            if (CALL_HORSE.isDown()) {
                ClientPlayNetworking.send(new C2SPayloadCallHorse());
                it.level.playLocalSound(it.player.getOnPos(), CallableHorseFabric.whistle, SoundSource.MASTER, 32.0f, 16.0f, false);
            }
            if (SET_HORSE.isDown())
                ClientPlayNetworking.send(new C2SPayloadSetHorse());
        });
    }
}
