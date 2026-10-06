package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.service.PlayerSitService;
import io.canvasmc.canvas.event.EntityPostPortalAsyncEvent;
import io.canvasmc.canvas.event.EntityPostTeleportAsyncEvent;
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class CanvasPlayerSitEventHandler implements Listener {

    private final GSitMain gSitMain;

    public CanvasPlayerSitEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChairPreTeleport(EntityTeleportAsyncEvent event) {
        if(!(event.getEntity() instanceof Player chair)) return;
        PlayerSitService service = gSitMain.getPlayerSitService();
        if(!service.isPlayerBottomOfPlayerSitStack(chair)) return;
        if(!isPlayerSitStackStale(chair, service)) return;
        service.stopPlayerSit(chair, StopReason.TELEPORT, true, false, false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityPostTeleportAsync(EntityPostTeleportAsyncEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        handlePostMove(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityPostPortalAsync(EntityPostPortalAsyncEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        handlePostMove(player);
    }

    private void handlePostMove(Player player) {
        if(!player.isValid()) return;
        PlayerSitService service = gSitMain.getPlayerSitService();

        if(service.isPlayerTopOfPlayerSitStack(player)) {
            service.stopPlayerSit(player, StopReason.TELEPORT, false, true, true);
        }

        if(service.isPlayerBottomOfPlayerSitStack(player)) {
            if(!player.getPassengers().isEmpty()) return;
            reSeatRiderAfterTeleport(player, service);
        }
    }

    private void reSeatRiderAfterTeleport(Player chair, PlayerSitService service) {
        UUID riderUuid = service.getTopPlayerUuid(chair);

        service.stopPlayerSit(chair, StopReason.TELEPORT, true, false, false);

        if(riderUuid == null) return;
        Player rider = Bukkit.getPlayer(riderUuid);
        if(rider == null || !rider.isOnline() || !rider.isValid()) return;

        Location destination = chair.getLocation();

        rider.getScheduler().run(gSitMain, dismountTask -> {
            if(!rider.isValid() || !rider.isOnline()) return;
            if(rider.getVehicle() != null) rider.leaveVehicle();

            rider.teleportAsync(destination, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(success -> {
                if(Boolean.FALSE.equals(success)) return;
                chair.getScheduler().run(gSitMain, sitTask -> {
                    if(!chair.isValid() || !rider.isValid() || !rider.isOnline()) return;
                    service.sitOnPlayer(rider, chair);
                }, null);
            });
        }, null);
    }

    private static boolean isPlayerSitStackStale(Player chair, PlayerSitService service) {
        UUID riderUuid = service.getTopPlayerUuid(chair);
        if(riderUuid == null) return true;

        Player rider = Bukkit.getPlayer(riderUuid);
        if(rider == null || !rider.isOnline() || !rider.isValid()) return true;

        Entity riderVehicle = rider.getVehicle();
        if(riderVehicle == null) return true;

        return !riderVehicle.getScoreboardTags().contains(PlayerSitService.PLAYERSIT_ENTITY_TAG);
    }

}
