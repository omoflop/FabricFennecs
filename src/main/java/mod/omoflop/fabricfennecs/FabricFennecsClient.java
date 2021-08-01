package mod.omoflop.fabricfennecs;

import mod.omoflop.fabricfennecs.entity.FennecEntity;
import mod.omoflop.fabricfennecs.render.FennecRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class FabricFennecsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(FabricFennecs.FENNEC, ctx -> new FennecRenderer(ctx));
    }
}
