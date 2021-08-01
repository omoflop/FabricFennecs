package mod.omoflop.fabricfennecs.entity.goal;

import mod.omoflop.fabricfennecs.FabricFennecs;
import mod.omoflop.fabricfennecs.entity.FennecEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;
import java.util.List;

public class FennecPickupItemGoal extends Goal {
    private FennecEntity fennecEntity;

    public FennecPickupItemGoal(FennecEntity fennecEntity) {
        this.fennecEntity = fennecEntity;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    public boolean canStart() {
        if (!fennecEntity.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
            return false;
        } else if (fennecEntity.getTarget() == null && fennecEntity.getAttacker() == null) {
            if (fennecEntity.getRandom().nextInt(10) != 0) {
                return false;
            } else {
                List<ItemEntity> list = getItemList();
                return !list.isEmpty() && fennecEntity.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
            }
        } else {
            return false;
        }
    }

    private List<ItemEntity> getItemList() {
        return fennecEntity.world.getEntitiesByClass(ItemEntity.class, fennecEntity.getBoundingBox().expand(8.0D, 8.0D, 8.0D), (a) -> FabricFennecs.FENNEC_FOODS.contains(a.getStack().getItem()));
    }

    public void tick() {
        List<ItemEntity> list = getItemList();
        ItemStack itemStack = fennecEntity.getEquippedStack(EquipmentSlot.MAINHAND);
        if (itemStack.isEmpty() && !list.isEmpty()) {
            fennecEntity.getNavigation().startMovingTo(list.get(0), 1.2000000476837158D);
        }

    }

    public void start() {
        List<ItemEntity> list = getItemList();
        if (!list.isEmpty()) {
            fennecEntity.getNavigation().startMovingTo(list.get(0), 1.2000000476837158D);
        }
    }
}
