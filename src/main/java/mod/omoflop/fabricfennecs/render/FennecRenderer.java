package mod.omoflop.fabricfennecs.render;

import mod.omoflop.fabricfennecs.entity.FennecEntity;
import mod.omoflop.fabricfennecs.model.FennecCollarModel;
import mod.omoflop.fabricfennecs.model.FennecModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FoxEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
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

    protected Optional<GeoBone> boneReference = Optional.empty();

    public FennecRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new FennecModel());
        this.shadowRadius = 0.5f;
        this.addLayer(new FennecCollarFeatureRenderer(this, new FennecCollarRenderer(ctx, new FennecModel())));
    }

    @Override
    public void renderEarly(FennecEntity fennecEntity, MatrixStack stack, float ticks, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks) {
        super.renderEarly(fennecEntity, stack, ticks, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, partialTicks);
        float scale = fennecEntity.isBaby() ? 0.5F : 1F;
        stack.scale(scale, scale, scale);

        if(boneReference.isPresent()) {
            boolean bl = fennecEntity.isSleeping();
            boolean bl2 = fennecEntity.isBaby();
            stack.push();
            float n;
            if (bl2) {
                n = 0.75F;
                stack.scale(0.75F, 0.75F, 0.75F);
                stack.translate(0.0D, 0.5D, 0.20937499403953552D);
            }

            stack.translate(boneReference.get().getPivotX()/16f,boneReference.get().getPivotY()/16f,boneReference.get().getPivotZ()/16f);
            stack.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(0));
            stack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float) Math.toDegrees(boneReference.get().getRotationY())));
            stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion((float) Math.toDegrees(boneReference.get().getRotationX())));
            if (fennecEntity.isBaby()) {
                if (bl) {
                    stack.translate(0.4000000059604645D, 0.25999999046325684D, 0.15000000596046448D);
                } else {
                    stack.translate(0.05999999865889549D, 0.25999999046325684D, -0.5D);
                }
            } else if (bl) {
                stack.translate(0.46000000834465027D, 0.25999999046325684D, 0.2199999988079071D);
            } else {
                stack.translate(0D, -0.08000001072883606D, -0.7D);
            }

            stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
            if (bl) {
                stack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90.0F));
            }

            ItemStack itemStack = fennecEntity.getEquippedStack(EquipmentSlot.MAINHAND);
            MinecraftClient.getInstance().getHeldItemRenderer().renderItem(fennecEntity, itemStack, ModelTransformation.Mode.GROUND, false, stack, renderTypeBuffer, packedLightIn);
            stack.pop();
        }
    }


    @Override
    public void renderRecursively(GeoBone bone, MatrixStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("Head")) {
            this.boneReference = Optional.of(bone);
        }

        super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
