package frc.robot.subsystems.scoring;

import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Volts;
import static edu.wpi.first.units.Units.VoltsPerRadianPerSecond;
import static edu.wpi.first.units.Units.VoltsPerRadianPerSecondSquared;

import coppercore.parameter_tools.LoggedTunableNumber;
import coppercore.wpilib_interface.UnitUtils;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.MutDistance;
import edu.wpi.first.units.measure.LinearVelocity;
import frc.robot.subsystems.scoring.ElevatorIO.ElevatorOutputMode;
import org.littletonrobotics.junction.Logger;

/**
 * A Mechanism to manage the Elevator
 *
 * <ul>
 *   <li>Uses closed-loop TorqueCurrentFOC control
 */
public class ElevatorMechanism {
  ElevatorIO io;
  ElevatorInputsAutoLogged inputs = new ElevatorInputsAutoLogged();
  ElevatorOutputsAutoLogged outputs = new ElevatorOutputsAutoLogged();

  MutDistance goalHeight = Rotations.mutable(0.0);
  MutDistance clampedGoalHeight = Rotations.mutable(0.0);

  MutDistance minDistance = ElevatorConstants.synced.getObject().elevatorMinMinDistance.mutableCopy();
  MutDistance maxDistance = ElevatorConstants.synced.getObject().elevatorMaxMaxDistance.mutableCopy();

  LoggedTunableNumber elevatorkP;
  LoggedTunableNumber elevatorkI;
  LoggedTunableNumber elevatorkD;

  LoggedTunableNumber elevatorkS;
  LoggedTunableNumber elevatorkV;
  LoggedTunableNumber elevatorkA;
  LoggedTunableNumber elevatorkG;

  LoggedTunableNumber elevatorCruiseVelocity;
  LoggedTunableNumber elevatorExpokV;
  LoggedTunableNumber elevatorExpokA;

  LoggedTunableNumber elevatorTuningSetpointRotations;
  LoggedTunableNumber elevatorTuningOverrideVolts;

  public ElevatorMechanism(ElevatorIO io) {
    elevatorkP =
        new LoggedTunableNumber("ElevatorTunables/elevatorkP", ElevatorConstants.synced.getObject().elevatorKP);
    elevatorkI =
        new LoggedTunableNumber("ElevatorTunables/elevatorkI", ElevatorConstants.synced.getObject().elevatorKI);
    elevatorkD =
        new LoggedTunableNumber("ElevatorTunables/elevatorkD", ElevatorConstants.synced.getObject().elevatorKD);

    elevatorkS =
        new LoggedTunableNumber("ElevatorTunables/elevatorkS", ElevatorConstants.synced.getObject().elevatorKS);
    elevatorkV =
        new LoggedTunableNumber("ElevatorTunables/elevatorkV", ElevatorConstants.synced.getObject().elevatorKV);
    elevatorkA =
        new LoggedTunableNumber("ElevatorTunables/elevatorkA", ElevatorConstants.synced.getObject().elevatorKA);
    elevatorkG =
        new LoggedTunableNumber("ElevatorTunables/elevatorkG", ElevatorConstants.synced.getObject().elevatorKG);

    elevatorCruiseVelocity =
        new LoggedTunableNumber(
            "ElevatorTunables/elevatorCruiseVelocity",
            ElevatorConstants.synced.getObject().elevatorAngularCruiseVelocityRotationsPerSecond);
    elevatorExpokV =
        new LoggedTunableNumber(
            "ElevatorTunables/elevatorExpokV", ElevatorConstants.synced.getObject().elevatorMotionMagicExpo_kV);
    elevatorExpokA =
        new LoggedTunableNumber(
            "ElevatorTunables/elevatorExpokA", ElevatorConstants.synced.getObject().elevatorMotionMagicExpo_kA);

    elevatorTuningSetpointRotations =
        new LoggedTunableNumber("ElevatorTunables/elevatorTuningSetpointRotations", 0.0);
    elevatorTuningOverrideVolts =
        new LoggedTunableNumber("ElevatorTunables/elevatorTuningOverrideVolts", 0.0);

    this.io = io;
  }

  /**
   * Runs periodically when the robot is enabled
   *
   * <p>Does NOT run automatically! Must be called by the subsystem
   */
  public void periodic() {
    sendGoalHeightToIO();

    io.updateInputs(inputs);
    io.applyOutputs(outputs);

    Logger.processInputs("Elevator/inputs", inputs);
    Logger.processInputs("Elevator/outputs", outputs);
  }

  public void setBrakeMode(boolean brake) {
    io.setBrakeMode(brake);
  }

  /** This method must be called from the subsystem's test periodic! */
  public void testPeriodic() {
    if (false) { // TODO: Replace placeholder test if ElevatorTuning mode is active
      // switch (TestModeManager.getTestMode()) {
      // case ElevatorClosedLoopTuning:
        io.setOutputMode(ElevatorOutputMode.ClosedLoop);
        LoggedTunableNumber.ifChanged(
            hashCode(),
            (pid) -> {
              io.setPID(pid[0], pid[1], pid[2]);
            },
            elevatorkP,
            elevatorkI,
            elevatorkD);

        LoggedTunableNumber.ifChanged(
            hashCode(),
            (ff) -> {
              io.setFF(ff[0], ff[1], ff[2], ff[3]);
            },
            elevatorkS,
            elevatorkV,
            elevatorkA,
            elevatorkG);

        LoggedTunableNumber.ifChanged(
            hashCode(),
            (maxProfile) -> {
              io.setMaxProfile(
                  RadiansPerSecond.of(0.0),
                  VoltsPerRadianPerSecondSquared.ofNative(maxProfile[0]),
                  VoltsPerRadianPerSecond.ofNative(maxProfile[1]));
            },
            elevatorExpokA,
            elevatorExpokV);

        LoggedTunableNumber.ifChanged(
            hashCode(),
            (setpoint) -> {
              setGoalDistance(Rotations.of(setpoint[0]));
            },
            elevatorTuningSetpointRotations);
      /*  case ElevatorVoltageTuning:
          LoggedTunableNumber.ifChanged(
            hashCode(),
            (setpoint) -> {
              io.setOverrideVoltage(Volts.of(setpoint[0]));
            },
            elevatorTuningOverrideVolts);
          io.setOverrideMode(true);
          break;
        }
        */
    }
  }

  public void sendGoalHeightToIO() {
    updateClampedGoalHeight();
    // Convert goal height to encoder rotations
    Angle elevatorEncoderGoalAngle = elevatorHeightToElevatorEncoderAngle(clampedGoalHeight);

    io.setElevatorEncoderGoalPos(elevatorEncoderGoalAngle);
  }

  /**
   * Based on the bounds previously set, clamp the last set goal height to be between the bounds.
   *
   * <p>If the goal height is outside of the bounds and the bounds are expanded, this function will
   * still behave as expected, as the mechanism remembers its unclamped goal height and will attempt
   * to get there once it is allowed.
   */
  private void updateClampedGoalHeight() {
    clampedGoalHeight.mut_replace(UnitUtils.clampMeasure(goalDistance, minDistance, maxDistance));

    Logger.recordOutput("Elevator/clampedGoalHeight", clampedGoalHeight);
  }

  /**
   * Set the goal angle the elevator will to control about.
   *
   * <p>This goal angle will be clamped by the allowed range of motion
   *
   * @param goalDistance The new goal angle
   */
  public void setGoalDistance(Distance goalDistance) {
    this.goalDistance.mut_replace(goalDistance);

    Logger.recordOutput("Elevator/goalDistance", goalDistance);
  }
  /**
   * Sets the minimum and maximum allowed heights that the elevator may target.
   *
   * <p>When not in override mode, the goal height of the elevator will be clamped to be between these
   * values before it is sent to the IO. When these clamps change, the original goal height is
   * clamped to be within the new bounds.
   *
   * @param minHeight The minimum angle, which will be clamped between elevatorMinMinDistance and
   *     elevatorMaxMaxDistance before being applied
   * @param maxHeight The maximum angle, which will be clamped between elevatorMinMinDistance and
   *     elevatorMaxMaxDistance before being applied
   */
  public void setAllowedRangeOfMotion(Distance minHeight, Distance maxHeight) {
    setMinHeight(minDistance);
    setMaxHeight(maxDistance);
  }

  /**
   * Sets the minimum allowed angle that the elevator may target.
   *
   * <p>When not in override mode, the goal height of the elevator will be clamped to be between these
   * values before it is sent to the IO. When these clamps change, the original goal height is
   * clamped to be within the new bounds.
   *
   * @param minHeight The minimum angle, which will be clamped between elevatorMinMinHeight and
   *     elevatorMaxMaxHeight before being applied
   */
  public void setMinHeight(Distance minHeight) {
    this.minDistance.mut_replace(
        UnitUtils.clampMeasure(
            minDistance,
            ElevatorConstants.synced.getObject().elevatorMinMinHeight,
            ElevatorConstants.synced.getObject().elevatorMaxMaxHeight));

    Logger.recordOutput("Elevator/minHeight", minHeight);
  }

  /**
   * Sets the maximum allowed angle that the elevator may target.
   *
   * <p>When not in override mode, the goal height of the elevator will be clamped to be between these
   * values before it is sent to the IO. When these clamps change, the original goal angle is
   * clamped to be within the new bounds.
   *
   * @param maxHeight The maximum angle, which will be clamped between elevatorMinMinHeight and
   *     elevatorMaxMaxHeight before being applied
   */
  public void setMaxHeight(Distance maxHeight) {
    this.maxHeight.mut_replace(
        UnitUtils.clampMeasure(
            maxHeight,
            ElevatorConstants.synced.getObject().elevatorMaxMaxHeight,
            ElevatorConstants.synced.getObject().elevatorMaxMaxHeight));

    Logger.recordOutput("Elevator/maxHeight", maxHeight);
  }

  /**
   * Get the current height of the elevator
   *
   * @return
   */
  public Distance getElevatorHeight() {
  }

  /**
   * Get the current velocity of the elevator
   *
   * @return The current velocity of the elevator, according to the elevatorEncoder
   */
  public LinearVelocity getElevatorVelocity() {
    return inputs.elevatorEncoderVel;
  }

  /**
   * Check whether or not the elevatorEncoder is currently connected.
   *
   * <p>"Connected" means that last time the position and velocity status signals were refreshed, the status code
   * was OK
   *
   * @return True if connected, false if disconnected
   */
  public boolean isElevatorEncoderConnected() {
    return inputs.elevatorEncoderConnected;
  }

  /**
   * Get a reference to the elevator's IO. This should be used to update PID, motion profile, and feed
   * forward gains, and to set brake mode/disable motors. This method exists to avoid the need to
   * duplicate all of these functions between the mechanism and the IO.
   *
   * @return the elevator mechanism's IO
   */
  public ElevatorIO getIO() {
    return io;
  }

  /** Set whether or not the motor on the elevator should be disabled */
  public void setMotorsDisabled(boolean disabled) {
    io.setMotorsDisabled(disabled);
  }

  /** Get the current unclamped goal height of the elevator */
  public Height getElevatorGoalHeight() {
    return goalHeight;
  }

  /**
   * Convert an angle of the elevatorEncoder into Elevator height
   *
   * @param elevatorEncoderAngle The encoder angle to convert
   * @return How much the Elevator would move if the elevatorEncoder were rotated by elevatorEncoderAngle
   */
  public Distance elevatorEncoderAngleToElevatorHeight(Angle elevatorEncoderAngle) {
    return Meters.of(elevatorEncoderAngle.in(Rotations) * ElevatorConstants.synced.getObject().elevatorHeightPerElevatorEncoderRotationMeters);
  }

  /**
   * Convert Elevator height into elevatorEncoder angle
   *
   * @param ElevatorHeight The distance of the Elevator to convert
   * @return How much the elevatorEncoder would rotate if the Elevator were moved by ElevatorHeight
   */
   public Angle elevatorHeightToElevatorEncoderAngle(Distance elevatorHeight) {
    return Rotations.of(elevatorHeight.in(Meters) / ElevatorConstants.synced.getObject().elevatorHeightPerElevatorEncoderRotationMeters);
   }
}