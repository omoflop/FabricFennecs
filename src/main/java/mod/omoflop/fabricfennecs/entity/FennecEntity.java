package mod.omoflop.fabricfennecs.entity;

import mod.omoflop.fabricfennecs.FabricFennecs;
import mod.omoflop.fabricfennecs.entity.goal.DigUpSandGoal;
import mod.omoflop.fabricfennecs.entity.goal.FennecPickupItemGoal;
import mod.omoflop.fabricfennecs.entity.goal.FennecSleepGoal;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;
import java.util.function.Predicate;

public class FennecEntity extends TameableEntity implements IAnimatable {

    private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = (entity) -> !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity);
    private final AnimationFactory factory = new AnimationFactory(this);
    private FleeEntityGoal<FennecEntity> fleeGoal;
    private FollowOwnerGoal followOwnerGoal;

    public FennecEntity(EntityType<? extends Entity> entityType, World world) {
        super((EntityType<? extends TameableEntity>) entityType, world);
        this.ignoreCameraFrustum = true;
    }

    // Sounds
    @Nullable @Override protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_FOX_AMBIENT;
    }
    @Nullable @Override protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_FOX_HURT;
    }
    @Override public float getSoundPitch() {
        return 1.76f;
    }

    // Item things???
    @Override public Iterable<ItemStack> getArmorItems() {
        return super.getArmorItems();
    }
    @Override public ItemStack getEquippedStack(EquipmentSlot slot) {
        return super.getEquippedStack(slot);
    }
    @Override public void equipStack(EquipmentSlot slot, ItemStack stack) {
        super.equipStack(slot, stack);
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    // Default entity attributes
    public static DefaultAttributeContainer.Builder createEntityAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2);
    }

    // How many ticks the tail should wag
    private static final TrackedData<Integer> HAPPY_TICKS  = DataTracker.registerData(FennecEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public boolean isHappy() {
        return this.dataTracker.get(HAPPY_TICKS) > 1;
    }
    public void setHappy(int ticks) {
        this.dataTracker.set(HAPPY_TICKS, ticks);
    }

    // What color the collar is
    private static final TrackedData<Integer> COLLAR_COLOR = DataTracker.registerData(FennecEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public DyeColor getCollarColor() { return DyeColor.byId(this.dataTracker.get(COLLAR_COLOR)); }
    public void setCollarColor(DyeColor color) { this.dataTracker.set(COLLAR_COLOR, color.getId()); }

    // Is the collar glowing?
    private static final TrackedData<Boolean> COLLAR_GLOWING = DataTracker.registerData(FennecEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public boolean isCollarGlowing() { return this.dataTracker.get(COLLAR_GLOWING); }
    public void setCollarGlowing(boolean glowing) { this.dataTracker.set(COLLAR_GLOWING, glowing); }

    // If the fennec is sleeping
    private static final TrackedData<Boolean> SLEEPING_POSE = DataTracker.registerData(FennecEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public boolean isInSleepingPose() { return this.dataTracker.get(SLEEPING_POSE); }
    public void setSleepingPose(boolean sleepingPose) { this.dataTracker.set(SLEEPING_POSE, sleepingPose); }

    // How many ticks the fennec should dig
    private static final TrackedData<Integer> DIGGING_TICKS = DataTracker.registerData(FennecEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public boolean isDigging() {
        return this.dataTracker.get(DIGGING_TICKS) > 1;
    }
    public void setDigging(int ticks) {
        this.dataTracker.set(DIGGING_TICKS, ticks);
    }

    // How many ticks the fennec should eat for
    private static final TrackedData<Integer> EATING_TICKS = DataTracker.registerData(FennecEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public int getEatingTime() {
        return this.dataTracker.get(EATING_TICKS);
    }
    public void setEatingTime(int ticks) {
        this.dataTracker.set(EATING_TICKS, ticks);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HAPPY_TICKS, 0);
        this.dataTracker.startTracking(COLLAR_COLOR, DyeColor.LIGHT_BLUE.getId());
        this.dataTracker.startTracking(COLLAR_GLOWING, false);
        this.dataTracker.startTracking(SLEEPING_POSE, false);
        this.dataTracker.startTracking(DIGGING_TICKS, 0);
        this.dataTracker.startTracking(EATING_TICKS, 0);
    }

    @Override public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.getOwnerUuid() != null) {
            nbt.putUuid("Owner", this.getOwnerUuid());
            nbt.putByte("CollarColor", (byte)this.getCollarColor().getId());
            nbt.putBoolean("CollarGlow", this.isCollarGlowing());
        }

        nbt.putInt("HappyTicks", this.dataTracker.get(HAPPY_TICKS));
        nbt.putBoolean("Sitting", isSitting());
        nbt.putBoolean("Sleeping", isInSleepingPose());
        nbt.putInt("DiggingTicks", this.dataTracker.get(DIGGING_TICKS));
        nbt.putInt("EatingTicks", this.dataTracker.get(EATING_TICKS));
    }
    @Override public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Owner"))
            this.setOwnerUuid(nbt.getUuid("Owner"));
        if (nbt.contains("HappyTicks"))
            this.setHappy(nbt.getInt("HappyTicks"));
        if (nbt.contains("CollarColor", 99))
            this.setCollarColor(DyeColor.byId(nbt.getInt("CollarColor")));
        if (nbt.contains("CollarGlow"))
            this.setCollarGlowing(nbt.getBoolean("CollarGlow"));
        if (nbt.contains("Sleeping"))
            this.setSleepingPose(nbt.getBoolean("Sleeping"));
        if (nbt.contains("DiggingTicks"))
            this.setDigging(nbt.getInt("DiggingTicks"));
        if (nbt.contains("EatingTicks"))
            this.setEatingTime(nbt.getInt("EatingTicks"));
    }

    @Override public void tick() {
        super.tick();
        Random r = new Random();

        if (isHappy()) {
            setHappy(this.dataTracker.get(HAPPY_TICKS) - 1);
        } else {
            if (r.nextInt(1200) == 1 || this.isInLove()) {
                setHappy(r.nextInt(120));
            }
        }
    }
    @Override public void tickMovement() {
        super.tickMovement();
        if (!this.world.isClient && this.isAlive() && this.canMoveVoluntarily()) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (FabricFennecs.FENNEC_FOODS.contains(itemStack.getItem())) {
                setEatingTime(getEatingTime()+1);
                if (getEatingTime() > 600) {
                    ItemStack itemStack2 = itemStack.finishUsing(this.world, this);
                    itemStack2.decrement(1);
                    System.out.println(itemStack2);
                    if (!itemStack2.isEmpty()) {
                        this.equipStack(EquipmentSlot.MAINHAND, itemStack2);
                    }

                    setEatingTime(0);
                } else if (getEatingTime() > 560 && this.random.nextFloat() < 0.1F) {
                    this.playSound(this.getEatSound(itemStack), 1.0F, 1.0F);
                    this.world.sendEntityStatus(this, (byte) 45);
                }
            }
        }
    }

    public boolean canEquip(ItemStack stack) {
        EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
        if (!this.getEquippedStack(equipmentSlot).isEmpty()) {
            return false;
        } else {
            return equipmentSlot == EquipmentSlot.MAINHAND && super.canEquip(stack);
        }
    }
    public boolean canPickupItem(ItemStack stack) {
        Item item = stack.getItem();
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        return itemStack.isEmpty() || getEatingTime() > 0 && item.isFood() && !itemStack.getItem().isFood();
    }

    private void spit(ItemStack stack) {
        if (!stack.isEmpty() && !this.world.isClient) {
            ItemEntity itemEntity = new ItemEntity(this.world, this.getX() + this.getRotationVector().x, this.getY() + 1.0D, this.getZ() + this.getRotationVector().z, stack);
            itemEntity.setPickupDelay(40);
            itemEntity.setThrower(this.getUuid());
            this.playSound(SoundEvents.ENTITY_FOX_SPIT, 1.0F, 1.0F);
            this.world.spawnEntity(itemEntity);
        }
    }
    private void dropItem(ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), stack);
        this.world.spawnEntity(itemEntity);
    }
    @Override protected void loot(ItemEntity item) {
        ItemStack itemStack = item.getStack();
        if (this.canPickupItem(itemStack)) {
            int i = itemStack.getCount();
            if (i > 1) {
                this.dropItem(itemStack.split(i - 1));
            }

            this.spit(this.getEquippedStack(EquipmentSlot.MAINHAND));
            this.triggerItemPickedUpByEntityCriteria(item);
            this.equipStack(EquipmentSlot.MAINHAND, itemStack.split(1));
            this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 2.0F;
            this.sendPickup(item, itemStack.getCount());
            item.discard();
            setEatingTime(0);
        }

    }

    public void handleStatus(byte status) {
        if (status == 45) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (!itemStack.isEmpty()) {
                for(int i = 0; i < 8; ++i) {
                    Vec3d vec3d = (new Vec3d(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D)).rotateX(-this.getPitch() * 0.017453292F).rotateY(-this.getYaw() * 0.017453292F);
                    this.world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack), this.getX() + this.getRotationVector().x / 2.0D, this.getY(), this.getZ() + this.getRotationVector().z / 2.0D, vec3d.x, vec3d.y + 0.05D, vec3d.z);
                }
            }
        } else {
            super.handleStatus(status);
        }

    }

    private interface EventBooleanSupplier { boolean get(AnimationEvent event); }
    private AnimationController.IAnimationPredicate predicateOf(EventBooleanSupplier condition, String name, boolean shouldLoop) {
        return animationEvent -> {
            boolean b = condition.get(animationEvent);
            if (b) animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation(name, shouldLoop));
            return b ? PlayState.CONTINUE : PlayState.STOP;
        };
    }

    @Override public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(
                new AnimationController(this, "state_controller", 8,
                        (e) -> {
                            if (isInSleepingPose()) {
                                return PlayState.STOP;
                            } else if (isInSittingPose()) {
                                e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.fennec.sit", true));
                            } else if (e.isMoving()) {
                                e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.fennec.walk", true));
                            } else {
                                return PlayState.STOP;
                            }
                            return PlayState.CONTINUE;
                        }
                )
        );

        animationData.addAnimationController(
                new AnimationController(this, "wag_controller", 8,
                        predicateOf((e) -> isHappy(), "animation.fennec.wag", true)
                )
        );

        animationData.addAnimationController(
                new AnimationController(this, "sleep_controller", 24,
                        predicateOf((e) -> isInSleepingPose(), "animation.fennec.sleep", true)
                )
        );

        animationData.addAnimationController(
                new AnimationController(this, "dig_controller", 8,
                        predicateOf((e) -> isDigging(), "animation.fennec.dig", true)
                )
        );
    }
    @Override public AnimationFactory getFactory() { return this.factory; }

    @Override public void breed(ServerWorld world, AnimalEntity other) {
        super.breed(world, other);
        if (world.getRandom().nextInt(3)==0) {
            super.breed(world, other);
            if (world.getRandom().nextInt(3)==0) {
                super.breed(world, other);
                if (world.getRandom().nextInt(2)==0) {
                    super.breed(world, other);
                }
            }
        }
    }
    @Override public FennecEntity createChild(ServerWorld world, PassiveEntity passiveEntity) {
        FennecEntity fennec = FabricFennecs.FENNEC.create(world);
        if (passiveEntity instanceof FennecEntity) {
            if (this.isTamed()) {
                fennec.setOwnerUuid(this.getOwnerUuid());
                fennec.setTamed(true);
            }
        }
        return fennec;
    }

    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (this.world.isClient) {
            if (this.isTamed() && this.isOwner(player)) {
                return ActionResult.SUCCESS;
            } else {
                return !this.isBreedingItem(itemStack) || !(this.getHealth() < this.getMaxHealth()) && this.isTamed() ? ActionResult.PASS : ActionResult.SUCCESS;
            }
        } else {
            ActionResult actionResult;
            if (this.isTamed()) {
                if (this.isOwner(player)) {

                    if (item == Items.GLOW_INK_SAC) {
                        if (!isCollarGlowing()) {
                            this.playSound(SoundEvents.ITEM_GLOW_INK_SAC_USE, 0.5f, 1f);
                            setCollarGlowing(true);
                            return ActionResult.CONSUME;
                        }
                    } else if (item == Items.WATER_BUCKET) {
                        if (isCollarGlowing()) {
                            this.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 0.5f, 1.4f);
                            setCollarGlowing(false);
                            return ActionResult.SUCCESS;
                        }
                    }

                    if (!(item instanceof DyeItem)) {
                        if (item.isFood() && this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                            this.eat(player, hand, itemStack);
                            this.heal((float)item.getFoodComponent().getHunger());
                            return ActionResult.CONSUME;
                        }

                        actionResult = super.interactMob(player, hand);
                        if (!actionResult.isAccepted() || this.isBaby()) {
                            this.setSitting(!this.isSitting());
                        }

                        return actionResult;
                    }

                    DyeColor dyeColor = ((DyeItem)item).getColor();
                    if (dyeColor != this.getCollarColor()) {
                        this.setCollarColor(dyeColor);
                        if (!player.getAbilities().creativeMode) {
                            itemStack.decrement(1);
                        }

                        return ActionResult.SUCCESS;
                    }
                }
            } else if (this.isBreedingItem(itemStack)) {
                this.eat(player, hand, itemStack);
                if (this.random.nextInt(12) == 0) {
                    this.setOwner(player);
                    this.setSitting(true);
                    this.world.sendEntityStatus(this, (byte)7);
                } else {
                    this.world.sendEntityStatus(this, (byte)6);
                }

                this.setPersistent();
                return ActionResult.CONSUME;
            }

            actionResult = super.interactMob(player, hand);
            if (actionResult.isAccepted()) {
                this.setPersistent();
            }

            return actionResult;
        }
    }
    protected void onTamedChanged() {
        if (this.fleeGoal == null) {
            this.fleeGoal = makeFleeEntityGoal();
            this.followOwnerGoal = makeFollowOwnerGoal();
        }

        this.goalSelector.remove(this.fleeGoal);
        this.goalSelector.remove(this.followOwnerGoal);
        if (!this.isTamed()) {
            this.goalSelector.add(4, this.fleeGoal);
        } else {
            this.goalSelector.add(3, this.followOwnerGoal);
        }
    }

    @Override public ItemStack eatFood(World world, ItemStack stack) {
        this.setHappy(world.getRandom().nextInt(100));
        return super.eatFood(world, stack);
    }
    protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
        if (this.isBreedingItem(stack)) {
            this.playSound(SoundEvents.ENTITY_FOX_EAT, 1.0F, 1.5F);
        }
        super.eat(player, hand, stack);
    }
    @Override public boolean isBreedingItem(ItemStack stack) {
        return FabricFennecs.FENNEC_FOODS.contains(stack.getItem());
    }

    @Override protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(1, new WanderAroundGoal(this, 0.76d));
        this.goalSelector.add(3, new AnimalMateGoal(this, 1.0D));
        this.goalSelector.add(4, makeFleeEntityGoal());
        this.goalSelector.add(4, new FleeEntityGoal(this, WolfEntity.class, 8.0F, 1.6D, 1.4D, (entity) -> !((WolfEntity)entity).isTamed()));
        this.goalSelector.add(6, new FennecPickupItemGoal(this));
        this.goalSelector.add(7, new DigUpSandGoal(this));
        this.goalSelector.add(8, new FollowParentGoal(this, 1.25D));
        this.goalSelector.add(9, new FennecSleepGoal(this, 0.76d));
        this.goalSelector.add(10, new PounceAtTargetGoal(this, 0.15F));
        this.goalSelector.add(11, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(12, new LookAtEntityGoal(this, PlayerEntity.class, 24.0F));
    }
    private FleeEntityGoal makeFleeEntityGoal() {
        return new FleeEntityGoal(this, PlayerEntity.class, 16.0F, 1.6D, 1.4D,
                (entity) -> NOTICEABLE_PLAYER_FILTER.test((Entity) entity) && !this.isTamed());
    }
    private FollowOwnerGoal makeFollowOwnerGoal() {
        return new FollowOwnerGoal(this, 0.78d, 2.5f, 40f, false);
    }

}
