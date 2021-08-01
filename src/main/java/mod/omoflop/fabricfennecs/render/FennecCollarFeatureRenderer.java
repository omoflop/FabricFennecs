package mod.omoflop.fabricfennecs.render;

import mod.omoflop.fabricfennecs.entity.FennecEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class FennecCollarFeatureRenderer extends GeoLayerRenderer<FennecEntity> {
    private final FennecCollarRenderer fennecCollarRenderer;
    private final Identifier COLLAR_TEXTURE = new Identifier("fabricfennecs:textures/entity/fennec/collar.png");
    private final Identifier GEO = new Identifier("fabricfennecs:geo/fennec.geo.json");

    public FennecCollarFeatureRenderer(IGeoRenderer<FennecEntity> entityRendererIn, FennecCollarRenderer fennecCollarRenderer) {
        super(entityRendererIn);
        this.fennecCollarRenderer = fennecCollarRenderer;
    }


    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, FennecEntity fennecEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (fennecEntity.isTamed()) {
            float colors[] = fennecEntity.getCollarColor().getColorComponents();
            RenderLayer layer = fennecEntity.isCollarGlowing() ? RenderLayer.getEyes(COLLAR_TEXTURE) : RenderLayer.getEntityCutout(COLLAR_TEXTURE);
            fennecCollarRenderer.render(getEntityModel().getModel(GEO),
            fennecEntity,
            partialTicks,
            layer,
            matrixStackIn,
            bufferIn,
            bufferIn.getBuffer(layer),
            packedLightIn, OverlayTexture.DEFAULT_UV, colors[0], colors[1], colors[2], 1);
        }
    }
}
