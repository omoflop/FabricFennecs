package mod.omoflop.fabricfennecs.render;

import mod.omoflop.fabricfennecs.entity.FennecEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class FennecCollarRenderer extends GeoEntityRenderer<FennecEntity> {
    public FennecCollarRenderer(EntityRendererFactory.Context ctx, AnimatedGeoModel<FennecEntity> modelProvider) {
        super(ctx, modelProvider);
    }
}
