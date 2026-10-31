package com.github.alexthe668.citadel.server.entity;

import com.github.alexthe668.citadel.server.tick.modifier.TickRateModifier;

public interface IModifiesTime {

    boolean isTimeModificationValid(TickRateModifier modifier);

}
