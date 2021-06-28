package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@Feat(name = "FastAnvilDupe", category = FeatureCategory.MISC, description = "Speeds up the current anvil dupe")
public class FastAnvilDupe extends Feature {

    @Op(name = "Mark Damaged")
    public boolean markedDamage = true;

    private boolean pickedUp;
    private boolean alertedXPEmpty;
    private boolean alertedInventoryNotFull;

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets) {
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (Wrapper.INSTANCE.getLocalPlayer().age % 2 != 0)
                    return;
                if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AnvilScreen anvilScreen) {
                    if (!InventoryHelper.INSTANCE.isInventoryFull()) {
                        if (!alertedInventoryNotFull) {
                            ChatHelper.INSTANCE.addClientMessage("Inventory is not full! You must have a full inventory to do this!.");
                            alertedInventoryNotFull = true;
                        }
                        return;
                    }
                    AnvilScreenHandler anvilScreenHandler = anvilScreen.getScreenHandler();
                    if (pickedUp) {
                        InventoryHelper.INSTANCE.windowClick(anvilScreenHandler, 0, SlotActionType.PICKUP);
                        pickedUp = false;
                    } else if (anvilScreenHandler.getSlot(2).getStack().getItem() != Items.AIR) {
                        InventoryHelper.INSTANCE.windowClick(anvilScreenHandler, 2, SlotActionType.PICKUP);
                        pickedUp = true;
                    } else if (anvilScreenHandler.getSlot(0).getStack().getItem() != Items.AIR) {
                        if (Wrapper.INSTANCE.getLocalPlayer().experienceLevel < 1) {
                            if (!alertedXPEmpty) {
                                ChatHelper.INSTANCE.addClientMessage("Out of XP! Can not continue dupe.");
                                alertedXPEmpty = true;
                            }
                            pickedUp = false;
                            return;
                        }
                        alertedXPEmpty = false;
                        alertedInventoryNotFull = false;
                        String currentName = anvilScreenHandler.getSlot(0).getStack().getName().getString();
                        NetworkHelper.INSTANCE.sendPacket(new RenameItemC2SPacket(currentName.equalsIgnoreCase("dupe") ? "dupe-1" : "dupe"));
                        anvilScreenHandler.updateResult();
                    }
                }
            }
        } else if (event instanceof EventRender3D eventRender3D && markedDamage) {
            for (int x = -4; x < 4; x++) {
                for (int y = -4; y < 4; y++) {
                    for (int z = -4; z < 4; z++) {
                        BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                        if (WorldHelper.INSTANCE.getBlock(blockPos) == Blocks.DAMAGED_ANVIL) {
                            Vec3d vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                            int color = new Color(0, 174, 255, 255).getRGB();
                            Box box = new Box(vec3d.x, vec3d.y, vec3d.z, vec3d.x + 1, vec3d.y + 1.2f, vec3d.z + 1);
                            Render3DHelper.INSTANCE.setup3DRender(true);
                            Render3DHelper.INSTANCE.drawFadeBox(eventRender3D.getMatrixStack(), box, color & 0xa9ffffff);
                            Render3DHelper.INSTANCE.end3DRender();
                        }
                    }
                }
            }
        }
    }

}
