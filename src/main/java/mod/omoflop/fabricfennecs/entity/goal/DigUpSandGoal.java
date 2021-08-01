package mod.omoflop.fabricfennecs.entity.goal;


import mod.omoflop.fabricfennecs.entity.FennecEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

import java.util.EnumSet;
import java.util.function.Predicate;

public class DigUpSandGoal extends Goal {
    private static final int MAX_TIMER = 400;
    private static final Predicate<BlockState> SAND_PREDICATE;
    private final FennecEntity mob;
    private final World world;
    private int timer;

    public DigUpSandGoal(FennecEntity mob) {
        this.mob = mob;
        this.world = mob.world;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    public boolean canStart() {
        if (this.mob.isSitting() || this.mob.isInSleepingPose() || this.mob.isInLove()) return false;

        if (this.mob.getRandom().nextInt(2000) != 0) {
            return false;
        } else {
            BlockPos blockPos = this.mob.getBlockPos();
            if (SAND_PREDICATE.test(this.world.getBlockState(blockPos)) || SAND_PREDICATE.test(this.world.getBlockState(blockPos.down())))
                return true;
            return world.getRandom().nextInt(8000) == 0;
        }
    }

    public void start() {
        this.timer = MAX_TIMER;
        this.world.sendEntityStatus(this.mob, (byte)10);
        this.mob.getNavigation().stop();
        this.mob.setDigging(100);
    }

    public void stop() {
        this.mob.setDigging(0);
        this.timer = 0;
    }

    public boolean shouldContinue() {
        return this.timer > 0;
    }

    public int getTimer() {
        return this.timer;
    }

    public void tick() {
        this.timer = Math.max(0, this.timer - 1);
        BlockPos blockPos = this.mob.getBlockPos();
        BlockState stoodOnBlock = world.getBlockState(blockPos.down());

        if (this.world.getTime() % 6 == 0 || this.world.getRandom().nextInt(100) == 1) {
            this.world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, blockPos, Block.getRawIdFromState(this.world.getBlockState(blockPos.down())));
        }

        if (this.timer == 4) {
            if (SAND_PREDICATE.test(stoodOnBlock) || SAND_PREDICATE.test(world.getBlockState(blockPos))) {
                world.spawnEntity(new ItemEntity(world, mob.getX(), mob.getY(), mob.getZ(), new ItemStack(Items.HANGING_ROOTS, 1)));
            }

        }
    }

    static {
        SAND_PREDICATE = BlockStatePredicate.forBlock(Blocks.SAND).or(BlockStatePredicate.forBlock(Blocks.RED_SAND));
    }
}
