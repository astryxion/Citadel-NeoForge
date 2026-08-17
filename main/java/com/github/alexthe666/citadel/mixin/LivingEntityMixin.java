package com.github.alexthe666.citadel.mixin;

import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.CitadelConstants;
import com.github.alexthe666.citadel.server.entity.ICitadelDataEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pre-26.1 Citadel stored custom NBT in {@link net.minecraft.network.syncher.SynchedEntityData}; NeoForge now rejects
 * {@link net.minecraft.network.syncher.SynchedEntityData#defineId} on entity classes touched by mod mixins. Use a
 * {@link net.neoforged.neoforge.attachment.AttachmentType} instead (see {@link Citadel#CITADEL_ENTITY_DATA}).
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ICitadelDataEntity {

    @Override
    public CompoundTag getCitadelEntityData() {
        LivingEntity self = (LivingEntity) (Object) this;
        AttachmentType<CompoundTag> type = Citadel.CITADEL_ENTITY_DATA.get();
        CompoundTag existing = self.getExistingDataOrNull(type);
        if (existing == null) {
            return new CompoundTag();
        }
        return existing.copy();
    }

    @Override
    public void setCitadelEntityData(CompoundTag nbt) {
        LivingEntity self = (LivingEntity) (Object) this;
        CompoundTag toStore = nbt == null ? new CompoundTag() : nbt.copy();
        self.setData(Citadel.CITADEL_ENTITY_DATA.get(), toStore);
    }

    /**
     * Worlds saved before the attachment migration still have {@code CitadelData} under entity load; copy into the attachment if needed.
     */
    @Inject(at = @At("TAIL"), remap = CitadelConstants.REMAPREFS, method = "readAdditionalSaveData")
    private void citadel_migrateLegacyCitadelData(ValueInput input, CallbackInfo ci) {
        input.read("CitadelData", CompoundTag.CODEC).ifPresent(legacy -> {
            LivingEntity self = (LivingEntity) (Object) this;
            AttachmentType<CompoundTag> type = Citadel.CITADEL_ENTITY_DATA.get();
            CompoundTag current = self.getExistingDataOrNull(type);
            if (current == null || current.isEmpty()) {
                self.setData(type, legacy.copy());
            }
        });
    }
}
