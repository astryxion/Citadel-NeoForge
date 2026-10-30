package com.github.alexthe668.citadel.server.entity;

import net.minecraft.nbt.CompoundTag;
/**
 * @author Alexthe668
 * @since 1.7.0
 */
public interface ICitadelDataEntity {

    CompoundTag getCitadelEntityData();

    void setCitadelEntityData(CompoundTag nbt);
}
