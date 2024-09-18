package de.godcipher.antiac.detection.reliability;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.config.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class TPSChecker {

  private final Configuration configuration;

  @Getter private double tps;

  private int reliableTpsThreshold = 18;

  private byte tick;
  private double lastFinish;

  public void start() {
    reliableTpsThreshold = (int) configuration.getConfigOption("tps-protection").getValue();

    new BukkitRunnable() {
      @Override
      public void run() {
        tick();
      }
    }.runTaskTimer(AntiAC.getInstance(), 1, 1);
  }

  public boolean isReliable() {
    return tps > reliableTpsThreshold;
  }

  private void tick() {
    tick++;
    if (tick == 20) {
      tps = calculateTPS();
      tick = 0;
      lastFinish = System.currentTimeMillis();
    }
  }

  private double calculateTPS() {
    double currentTime = System.currentTimeMillis();
    double elapsedTime = currentTime - lastFinish;
    return elapsedTime > 1000 ? 20.0 / (elapsedTime / 1000) : 20.0;
  }
}
