package mod.omoflop.fabricfennecs.model;

import mod.omoflop.fabricfennecs.entity.FennecEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class FennecCollarModel extends AnimatedGeoModel<FennecEntity> {

    @Override
    public Identifier getModelLocation(FennecEntity object) {
        return new Identifier("fabricfennecs:geo/fennec.geo.json");
    }

    @Override
    public Identifier getTextureLocation(FennecEntity object) {
        return new Identifier("fabricfennecs:textures/entity/fennec/collar.png");
    }

    @Override
    public Identifier getAnimationFileLocation(FennecEntity animatable) {
        return new Identifier("fabricfennecs:animations/fennec.animation.json");
    }

    @Override
    public void setLivingAnimations(FennecEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);

        IBone head = this.getAnimationProcessor().getBone("Head");
        EntityModelData extra = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        if (head != null) {
            head.setRotationX((float) Math.toRadians(extra.headPitch + (entity.isInSittingPose() ? -27.5f : 0)));
            head.setRotationY((float) Math.toRadians(extra.netHeadYaw));
        }

        IBone tail = this.getAnimationProcessor().getBone("Tail");
        if (tail != null) {
            tail.setRotationX((float) Math.toRadians(entity.isInSittingPose() ? -40d : 0d));
        }

        IBone ears = this.getAnimationProcessor().getBone("Ears");
        if (ears != null) {

        }

        if (extra.isChild) {
            head.setScaleX(2.0f);
            head.setScaleY(2.0f);
            head.setScaleZ(2.0f);
        } else {
            head.setScaleX(1.0f);
            head.setScaleY(1.0f);
            head.setScaleZ(1.0f);
        }
    }
}
