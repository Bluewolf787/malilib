package fi.dy.masa.malilib.mixin;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.event.ServerHandler;
import fi.dy.masa.malilib.network.payload.PayloadTypeRegister;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class MixinIntegratedServer
{
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "setupServer", at = @At("RETURN"))
    private void malilib_setupServer(CallbackInfoReturnable<Boolean> cir)
    {
        if (cir.getReturnValue())
        {
            ((ServerHandler) ServerHandler.getInstance()).onServerIntegratedSetup(this.client.getServer());
        }
    }
    @Inject(method = "openToLan", at = @At("RETURN"))
    private void malilib_checkOpenToLan(GameMode gameMode, boolean cheatsAllowed, int port, CallbackInfoReturnable<Boolean> cir)
    {
        if (cir.getReturnValue())
        {
            ((ServerHandler) ServerHandler.getInstance()).onServerOpenToLan(this.client.getServer());
        }
    }
}
