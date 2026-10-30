package com.github.alexthe668.citadel.server.world;

public interface ModifiableTickRateServer {

    void setGlobalTickLengthMs(long msPerTick);

    long getMasterMs();

    default void resetGlobalTickLengthMs(){
        setGlobalTickLengthMs(-1);
    }
}
