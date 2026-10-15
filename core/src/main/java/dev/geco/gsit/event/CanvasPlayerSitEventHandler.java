package dev.geco.gsit.event;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.service.PlayerSitService;
import io.canvasmc.canvas.event.EntityPostPortalAsyncEvent;
import io.canvasmc.canvas.event.EntityPostTeleportAsyncEvent;
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CanvasPlayerSitEventHandler implements Listener {

    private final GSitMain gSitMain;

    public CanvasPlayerSitEventHandler(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPreTeleport(EntityTeleportAsyncEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        PlayerSitService service = gSitMain.getPlayerSitService();
        service.stopPlayerSit(player, StopReason.TELEPORT);
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
        service.stopPlayerSit(player, StopReason.TELEPORT);
    }

}
