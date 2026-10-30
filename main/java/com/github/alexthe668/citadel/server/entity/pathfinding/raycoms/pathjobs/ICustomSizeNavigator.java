package com.github.alexthe668.citadel.server.entity.pathfinding.raycoms.pathjobs;

public interface ICustomSizeNavigator {

    boolean isSmallerThanBlock();
    float getXZNavSize();
    int getYNavSize();
}
