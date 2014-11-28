package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_8_R1.EntityWither;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftWither;
import org.bukkit.entity.Wither;
import org.bukkit.util.Vector;

public class WitherController extends MobEntityController {
    public WitherController() {
        super(EntityWitherNPC.class);
    }

    @Override
    public Wither getBukkitEntity() {
        return (Wither) super.getBukkitEntity();
    }

    public static class EntityWitherNPC extends EntityWither implements NPCHolder {
        private int jumpTicks;
        private final CitizensNPC npc;

        public EntityWitherNPC(World world) {
            this(world, null);
        }

        public EntityWitherNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        protected String bn() {
            return npc == null ? super.bn() : npc.data().get(NPC.HURT_SOUND_METADATA, super.bn());
        }

        @Override
        protected String bo() {
            return npc == null ? super.bo() : npc.data().get(NPC.DEATH_SOUND_METADATA, super.bo());
        }

        @Override
        public boolean cb() {
            if (npc == null)
                return super.cb();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.cb();
            if (super.cb()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void collide(net.minecraft.server.v1_8_R1.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        protected void D() {
            if (npc == null) {
                super.D();
            }
        }

        @Override
        public void doTick() {
            super.doTick();
            if (npc != null) {
                npc.update();
            }
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new WitherNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        protected String z() {
            return npc == null || !npc.data().has(NPC.AMBIENT_SOUND_METADATA) ? super.z() : npc.data().get(
                    NPC.AMBIENT_SOUND_METADATA, super.z());
        }
    }

    public static class WitherNPC extends CraftWither implements NPCHolder {
        private final CitizensNPC npc;

        public WitherNPC(EntityWitherNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}