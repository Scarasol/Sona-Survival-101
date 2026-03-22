package com.scarasol.sona.mixin;

import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Unique
    private static final int sona$DIR_FORWARD = 0;
    @Unique
    private static final int sona$DIR_BACKWARD = 1;
    @Unique
    private static final int sona$DIR_LEFT = 2;
    @Unique
    private static final int sona$DIR_RIGHT = 3;

    @Unique
    private int sona$forwardMapping = sona$DIR_FORWARD;
    @Unique
    private int sona$backwardMapping = sona$DIR_BACKWARD;
    @Unique
    private int sona$leftMapping = sona$DIR_LEFT;
    @Unique
    private int sona$rightMapping = sona$DIR_RIGHT;
    @Unique
    private boolean sona$forwardPressed;
    @Unique
    private boolean sona$backwardPressed;
    @Unique
    private boolean sona$leftPressed;
    @Unique
    private boolean sona$rightPressed;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sona$confuseMovement(boolean slowDown, float amount, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !player.hasEffect(SonaMobEffects.CONFUSION.get()) || minecraft.screen != null || player.isSpectator()) {
            this.sona$resetConfusionMovement();
            return;
        }

        boolean[] currentPressed = new boolean[]{
                minecraft.options.keyUp.isDown(),
                minecraft.options.keyDown.isDown(),
                minecraft.options.keyLeft.isDown(),
                minecraft.options.keyRight.isDown()
        };
        boolean[] remappedPressed = new boolean[4];

        this.sona$applyConfusionKey(currentPressed[sona$DIR_FORWARD], remappedPressed, sona$DIR_FORWARD);
        this.sona$applyConfusionKey(currentPressed[sona$DIR_BACKWARD], remappedPressed, sona$DIR_BACKWARD);
        this.sona$applyConfusionKey(currentPressed[sona$DIR_LEFT], remappedPressed, sona$DIR_LEFT);
        this.sona$applyConfusionKey(currentPressed[sona$DIR_RIGHT], remappedPressed, sona$DIR_RIGHT);

        KeyboardInput input = (KeyboardInput) (Object) this;
        input.up = remappedPressed[sona$DIR_FORWARD];
        input.down = remappedPressed[sona$DIR_BACKWARD];
        input.left = remappedPressed[sona$DIR_LEFT];
        input.right = remappedPressed[sona$DIR_RIGHT];
        input.forwardImpulse = input.up == input.down ? 0.0F : (input.up ? 1.0F : -1.0F);
        input.leftImpulse = input.left == input.right ? 0.0F : (input.left ? 1.0F : -1.0F);
        if (slowDown) {
            input.forwardImpulse *= amount;
            input.leftImpulse *= amount;
        }
    }

    @Unique
    private void sona$resetConfusionMovement() {
        this.sona$forwardMapping = sona$DIR_FORWARD;
        this.sona$backwardMapping = sona$DIR_BACKWARD;
        this.sona$leftMapping = sona$DIR_LEFT;
        this.sona$rightMapping = sona$DIR_RIGHT;
        this.sona$forwardPressed = false;
        this.sona$backwardPressed = false;
        this.sona$leftPressed = false;
        this.sona$rightPressed = false;
    }

    @Unique
    private void sona$applyConfusionKey(boolean pressed, boolean[] remappedPressed, int source) {
        if (pressed) {
            if (!this.sona$isConfusionPressed(source)) {
                this.sona$setConfusionMapping(source, Minecraft.getInstance().player.getRandom().nextInt(4));
            }
            remappedPressed[this.sona$getConfusionMapping(source)] = true;
        } else {
            this.sona$setConfusionMapping(source, source);
        }
        this.sona$setConfusionPressed(source, pressed);
    }

    @Unique
    private int sona$getConfusionMapping(int source) {
        return switch (source) {
            case sona$DIR_FORWARD -> this.sona$forwardMapping;
            case sona$DIR_BACKWARD -> this.sona$backwardMapping;
            case sona$DIR_LEFT -> this.sona$leftMapping;
            default -> this.sona$rightMapping;
        };
    }

    @Unique
    private void sona$setConfusionMapping(int source, int mapping) {
        switch (source) {
            case sona$DIR_FORWARD -> this.sona$forwardMapping = mapping;
            case sona$DIR_BACKWARD -> this.sona$backwardMapping = mapping;
            case sona$DIR_LEFT -> this.sona$leftMapping = mapping;
            default -> this.sona$rightMapping = mapping;
        }
    }

    @Unique
    private boolean sona$isConfusionPressed(int source) {
        return switch (source) {
            case sona$DIR_FORWARD -> this.sona$forwardPressed;
            case sona$DIR_BACKWARD -> this.sona$backwardPressed;
            case sona$DIR_LEFT -> this.sona$leftPressed;
            default -> this.sona$rightPressed;
        };
    }

    @Unique
    private void sona$setConfusionPressed(int source, boolean pressed) {
        switch (source) {
            case sona$DIR_FORWARD -> this.sona$forwardPressed = pressed;
            case sona$DIR_BACKWARD -> this.sona$backwardPressed = pressed;
            case sona$DIR_LEFT -> this.sona$leftPressed = pressed;
            default -> this.sona$rightPressed = pressed;
        }
    }
}
