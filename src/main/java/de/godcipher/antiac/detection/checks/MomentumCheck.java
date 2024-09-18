package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.Check;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * MomentumCheck is a detection mechanism designed to analyze the rate of change in player click
 * patterns over a period of CPS. It measures the percentage slope (or steepness) of CPS activity to
 * detect potential irregularities.
 */
@Slf4j
public class MomentumCheck extends Check {

  private static final String PERCENTAGE_THRESHOLD_CONFIG = "percentage-threshold";
  private static final String CPS_THRESHOLD_CONFIG = "cps-threshold";

  private int CPSThreshold = -1; // Number of CPS to check
  private int percentageThreshold = -1; // Max percentage slope

  public MomentumCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  protected void onLoad() {
    setupDefaults();

    CPSThreshold =
        (Integer) getCheckConfiguration().getConfigOption(CPS_THRESHOLD_CONFIG).getValue();
    percentageThreshold =
        (Integer) getCheckConfiguration().getConfigOption(PERCENTAGE_THRESHOLD_CONFIG).getValue();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    List<CPS> playerCps = clickTracker.getCPSList(player.getUniqueId());
    List<CPS> setToProcess = trimList(playerCps, CPSThreshold);

    if (setToProcess.size() < CPSThreshold) {
      return false;
    }

    double slope = calculateSlope(setToProcess);
    double slopePercentage = Math.abs(slope * 100); // Take absolute value to handle negative slopes

    return slopePercentage > percentageThreshold;
  }

  /**
   * Calculate the slope based on the **average** CPS value and the time difference between the
   * first and last valid clicks in the CPS set.
   *
   * @param cpsSet the set of CPS instances
   * @return the calculated slope (rate of change in clicking)
   */
  private double calculateSlope(List<CPS> cpsSet) {
    LinkedList<CPS> validCps =
        cpsSet.stream()
            .filter(cps -> !cps.isEmpty() && cps.getCPS() > 0)
            .collect(Collectors.toCollection(LinkedList::new));

    if (validCps.isEmpty()) {
      return 0;
    }

    LinkedList<Long> timestamps =
        validCps.stream()
            .flatMap(cps -> cps.getClicks().stream())
            .map(Click::getTime)
            .sorted()
            .collect(Collectors.toCollection(LinkedList::new));

    if (timestamps.isEmpty()) {
      return 0;
    }

    long firstClick = timestamps.getFirst();
    long lastClick = timestamps.getLast();

    double timeSpan = (lastClick - firstClick) / 1000.0;

    if (timeSpan == 0) {
      return 0;
    }

    int initialCPS = validCps.getFirst().getCPS();
    double averageCPS = validCps.stream().mapToInt(CPS::getCPS).average().orElse(0);

    return (averageCPS - initialCPS) / timeSpan;
  }

  private void setupDefaults() {
    getCheckConfiguration()
        .addConfigOption(
            CPS_THRESHOLD_CONFIG, ConfigurationOption.ofInteger(20, "The number of CPS to check"));
    getCheckConfiguration()
        .addConfigOption(
            PERCENTAGE_THRESHOLD_CONFIG,
            ConfigurationOption.ofInteger(75, "The maximum percentage slope to trigger on"));
  }
}
