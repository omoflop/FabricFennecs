package mod.omoflop.fabricfennecs.entity.goal;

import mod.omoflop.fabricfennecs.FabricFennecs;
import mod.omoflop.fabricfennecs.entity.FennecEntity;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class FennecSleepGoal extends MoveToTargetPosGoal {
    private final FennecEntity fennec;

    public FennecSleepGoal(FennecEntity fennec, double speed) {
        super(fennec, speed, 8);
        this.fennec = fennec;
    }

    public boolean canStart() {
        return !this.fennec.isSitting() && super.canStart() && this.fennec.world.isNight();
    }

    public void start() {
        super.start();
        this.fennec.setSleepingPose(false);
    }

    public void stop() {
        super.stop();
        this.fennec.setSleepingPose(false);
    }

    public void tick() {
        super.tick();
        this.fennec.setSleepingPose(this.hasReached());
    }

    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        return IS_COMFY.test(world, world.getBlockState(pos), pos) || IS_COMFY.test(world, world.getBlockState(pos.up()), pos.up());
    }

    private static final ComfyPredicate IS_COMFY = (world, state, pos) -> {
        if (FabricFennecs.COMFY_BLOCKS.contains(state.getBlock()))
            return true;
        if (FabricFennecs.COMFY_WARM_BLOCKS.contains(state.getBlock())) {
            return (state.get(FurnaceBlock.LIT));
        }
        return false;
    };

    interface ComfyPredicate {
        boolean test(WorldView world, BlockState state, BlockPos pos);
    }
}
