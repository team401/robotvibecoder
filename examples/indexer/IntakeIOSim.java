package frc.robot.subsystems.scoring;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import java.lang.Runnable;
import java.util.function.BooleanSupplier;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.constants.JsonConstants;
import org.littletonrobotics.junction.Logger;

import com.ctre.phoenix6.sim.CANrangeSimState;

public class IntakeIOSim extends IntakeIOTalonFX {
  private enum Action {
    None,
    Intaking,
    Ejecting,
    Outtaking,
  };

  boolean hasCoral = true; // TODO: Change this value to correctly reflect preloa

  // Supplier to manage where the robot can intake in sim
  private static BooleanSupplier coralAvailableSupplier = () -> false;
  // Callback for when a piece is ejected
  private static Runnable onEject = () -> {};
  // Callback for when a piece is outtaked
  private static Runnable onOuttake = () -> {};

  private MutVoltage outputVoltage = Volts.mutable(0.0);

  /** Keep track of simulated motor position */
  private MutAngle motorPos = Rotations.mutable(0.0);

  /* Timer for actions (intaking/outtaking/ejecting) */
  private Timer actionTimer = new Timer();

  private Action currentAction = Action.None;

  private CANrangeSimState coralCANRangeSimState = coralCANRange.getSimState();

  public IntakeIOSim() {
    updateSimState();
  }

  /**
   * Update the supplier that the Intake sim uses to determine if it can currently intake.
   */
  public static void setCoralAvailableSupplier(BooleanSupplier newSupplier) {
    coralAvailableSupplier = newSupplier;
  }

  /**
   * Update the on-eject callback.
   *
   * <p> This Runnable will be called whenever the Intake ejects/backs out a Coral
   */
  public static void setOnEject(Runnable newOnEject) {
    onEject = newOnEject;
  }

  /**
   * Update the on-outtake callback.
   *
   * <p> This Runnable will be called whenever the Intake outtakes/passes forward a Coral
   */
  public static void setOnOuttake(Runnable newOnOuttake) {
    onOuttake = newOnOuttake;
  }

  private void updateSimState() {
    if (!DriverStation.isEnabled()) {
      outputVoltage.mut_replace(Volts.zero());
    }

    boolean coralAvailable = coralAvailableSupplier.getAsBoolean();

    // v Volts * k Rotations/Second*Volt * s Seconds =  Rotations
    motorPos.mut_plus(
        RotationsPerSecond.of(
                outputVoltage.in(Volts) * IntakeConstants.Sim.synced.getObject().rotationsPerSecondPerVolt)
            .times(Seconds.of(0.02)));
    
    if (hasCoral) {
      // If the Intake currently has a Coral, check for eject/outtake
      if (Math.signum(outputVoltage.in(Volts)) ==  IntakeConstants.Sim.synced.getObject().ejectSignum) {
        if (currentAction != Action.Ejecting) {
          currentAction = Action.Ejecting;
          actionTimer.restart();
        }

        if (actionTimer.hasElapsed(IntakeConstants.Sim.synced.getObject().ejectTimeSeconds)) {
          currentAction = Action.None;
          hasCoral = false;
          actionTimer.restart();
          onEject.run();
        }
      } else if (Math.signum(outputVoltage.in(Volts)) ==  IntakeConstants.Sim.synced.getObject().outtakeSignum) {
        if (currentAction != Action.Outtaking) {
          currentAction = Action.Outtaking;
          actionTimer.restart();
        }

        if (actionTimer.hasElapsed(IntakeConstants.Sim.synced.getObject().outtakeTimeSeconds)) {
          currentAction = Action.None;
          hasCoral = false;
          actionTimer.restart();
          onOuttake.run();
        }
      }
    } else if (Math.signum(outputVoltage.in(Volts)) ==  IntakeConstants.Sim.synced.getObject().intakeSignum) {
      if (currentAction != Action.Intaking) {
        currentAction = Action.Intaking;
        actionTimer.restart();
      }

      if (actionTimer.hasElapsed(IntakeConstants.Sim.synced.getObject().intakeTimeSeconds)) {
        currentAction = Action.None;
        hasCoral = true;
        actionTimer.restart();
      }
    } else {
      actionTimer.reset();
    }

    coralCANRangeSimState.setDistance(
      hasCoral
        ? IntakeConstants.Sim.synced.getObject().distanceWithCoral
        : IntakeConstants.Sim.synced.getObject().distanceWithoutObject);

    Logger.recordOutput("intakeSim/outputVoltage", outputVoltage.in(Volts));
    Logger.recordOutput("intakeSim/hasCoral", hasCoral);
  }

  private final Current currentPushing = Amps.of(50);

  public void updateInputs(IntakeInputs inputs) {
    updateSimState();

    inputs.coralDetected = hasCoral;

    Current motorCurrent =
        (hasCoral || coralAvailableSupplier.getAsBoolean())
                && (outputVoltage.in(Volts) != 0.0)
            ? currentPushing
            : Amps.zero();

    inputs.intakeMotorPos.mut_replace(motorPos);
    inputs.intakeMotorStatorCurrent.mut_replace(motorCurrent);
    inputs.intakeMotorSupplyCurrent.mut_replace(motorCurrent);
  }

  public void applyOutputs(IntakeOutputs outputs) {
    outputs.intakeMotorAppliedVolts.mut_replace(outputVoltage);
  }

  public void setVoltage(Voltage volts) {
    outputVoltage.mut_replace(volts);
  }
}