package frc.robot.subsystems.scoring;

import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import coppercore.parameter_tools.LoggedTunableNumber;
import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import frc.robot.TestModeManager;
import frc.robot.constants.JsonConstants;
import frc.robot.subsystems.scoring.states.IntakeState;
import org.littletonrobotics.junction.Logger;

public class IntakeMechanism {
  private IntakeIO io;
  private IntakeInputsAutoLogged inputs = new IntakeInputsAutoLogged();
  private IntakeOutputsAutoLogged outputs = new IntakeOutputsAutoLogged();

  private LoggedTunableNumber manualTuningVolts;

  public IntakeMechanism(IntakeIO io) {
    manualTuningVolts = new LoggedTunableNumber("IntakeTunables/clawManualVolts", 0.0);

    this.io = io;
  }

  /**
   * Return the IntakeMechanism's IntakeIO instance
   *
   * <p>NOTE: Be careful to only call methods from where they're allowed to be called (according to IO method docstrings)!
   *
   * @return a IntakeIO
   */
  public IntakeIO getIO() {
    return io;
  }

  /**
   * This method must be called from the subsystem's periodic! Mechanism periodics don't run
   * automatically!
   */
  public void periodic() {
    io.updateInputs(inputs);
    io.applyOutputs(outputs);

    Logger.processInputs("intake/inputs", inputs);
    Logger.processInputs("intake/outputs", outputs);
  }

  /** This method must be called from the subsystem's test periodic! */
  public void testPeriodic() {
    switch (TestModeManager.getTestMode()) {
      case IntakeTuning:
        LoggedTunableNumber.ifChanged(
            hashCode(),
            (volts) -> {
              io.setVoltage(Volts.of(volts[0]));
            },
            manualTuningVolts);
        break;
      default:
        break;
    }
  }

  /**
   * Check whether the Intake currently detects a Coral
   *
   * @return True if detected, false if not
   */
  public boolean isCoralDetected() {
    return inputs.coralDetected;
  }

  /**
   * Intake at a specified voltage until a Coral is detected.
   * 
   * @param voltage A Voltage, the voltage to apply to the motors until that piece is detected.
   */
  public void intake(Voltage voltage) {
    io.setObeyLimitSwitch(true);
    io.setVoltage(voltage)
  }

  /**
   * Run motors at a certain voltage, regardless of whether a Coral is detected.
   *
   * @param voltage A voltage, the voltage to apply to the motors until otherwise specified.
   */
  public void eject(Voltage voltage) {
    io.setVoltage(voltage);
  }

  /**
   * Stop the motors (apply zero volts)
   */
  public void stop() {
    io.setVoltage(Volts.zero())
  }
}