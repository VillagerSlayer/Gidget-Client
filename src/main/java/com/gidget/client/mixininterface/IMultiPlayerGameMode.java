package com.gidget.client.mixininterface;

/** Exposes {@code MultiPlayerGameMode}'s private destroy-delay counter for {@code FastBreakModule}. */
public interface IMultiPlayerGameMode {
    void gidget$setDestroyDelay(int delay);
}
