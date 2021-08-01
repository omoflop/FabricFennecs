package mod.omoflop.fabricfennecs.render;

import mod.omoflop.fabricfennecs.entity.FennecEntity;
import mod.omoflop.fabricfennecs.model.FennecCollarModel;
import mod.omoflop.fabricfennecs.model.FennecModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.geo.raw.pojo.Cube;
import software.bernie.geckolib3.geo.raw.pojo.ModelProperties;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.AnimationUtils;

import java.awt.*;
import java.util.Optional;

public class FennecRenderer extends GeoEntityRenderer<FennecEntity> {
    public FennecRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new FennecModel());
        this.shadowRadius = 0.5f;
        this.addLayer(new FennecCollarFeatureRenderer(this, new FennecCollarRenderer(ctx, new FennecModel())));
    }

    @Override
    public void renderEarly(FennecEntity fennecEntity, MatrixStack stackIn, float ticks, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks) {
        super.renderEarly(fennecEntity, stackIn, ticks, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, partialTicks);
        float scale = fennecEntity.isBaby() ? 0.5F : 1F;
        stackIn.scale(scale, scale, scale);
    }


    @Override
    public void renderRecursively(GeoBone bone, MatrixStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("Head")) {
            stack.push();
            stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-75));
            stack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(0));
            stack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(0));
            stack.translate(0.4D,0.3D,0.6D);
            stack.scale(1.0f,1.0f,1.0f);
            MinecraftClient.getInstance().getItemRenderer().renderItem(mainHand, ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND, packedLightIn, packedOverlayIn, stack, this.rtb, 1);
            stack.pop();
            bufferIn = rtb.getBuffer(RenderLayer.getEntityTranslucent(whTexture));
        }

        super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
