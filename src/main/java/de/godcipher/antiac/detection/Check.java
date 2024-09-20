package de.godcipher.antiac.detection;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

@Slf4j
public abstract class Check {

  @Getter private final String name;
  @Getter private final CheckConfiguration checkConfiguration;

  private final PlayerFlaggingService playerFlaggingService;

  protected final ClickTracker clickTracker;

  @Getter @Setter private boolean activated;
  @Getter private boolean loaded;

  protected Check(ClickTracker clickTracker) {
    this.name = this.getClass().getSimpleName();
    this.clickTracker = clickTracker;

    this.checkConfiguration = new CheckConfiguration(name);
    this.playerFlaggingService = new PlayerFlaggingService(clickTracker, this);

    this.activated = checkConfiguration.isActivated();
  }

  // For cleanup when a player quits
  public void handlePlayerQuit(Player player) {

  }

  void load() {
    if (isLoaded() || !isActivated()) {
      log.error("Failed loading {}", name);
      return;
    }

    loaded = true;
    onLoad();
    checkConfiguration.loadConfig();
  }

  void unload() {
    if (!isLoaded()) {
      log.error("Failed unloading {}", name);
      return;
    }

    loaded = false;
    onUnload();
    checkConfiguration.saveConfiguration();
  }

  protected List<CPS> trimList(List<CPS> set, int size) {
    return set.stream().limit(size).collect(Collectors.toList());
  }

  protected final void onFlag(Player player) {
    log.debug("Player: {} - Flagged by {}", player.getName(), name);
    playerFlaggingService.flagPlayer(player);
  }

  protected abstract void onLoad();

  protected abstract void onUnload();

  public abstract boolean check(Player player);
}
