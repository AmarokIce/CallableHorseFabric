package club.someoneice.callablehorse.core;

import club.someoneice.pc.api.IPineappleConfig;
import club.someoneice.pc.config.ConfigBean;

public final class Config extends ConfigBean implements IPineappleConfig {
    public Config() {
        super(CallableHorseFabric.MODID);
        this.init();
    }

    @Override
    public void init() {
        CallableHorseFabric.canRespawnHorse = this.getBoolean("CanRespawnHorse", CallableHorseFabric.canRespawnHorse);
        CallableHorseFabric.canCallFromOtherWorld = this.getBoolean("CanCallHorseFromOtherWorld", CallableHorseFabric.canCallFromOtherWorld);
        CallableHorseFabric.rangeCanCall = this.getInteger("CallHorseRange", CallableHorseFabric.rangeCanCall);
        this.build();
    }

    @Override
    public IPineappleConfig reload() {
        this.readFileAndOverrideConfig();
        init();
        return this;
    }
}
