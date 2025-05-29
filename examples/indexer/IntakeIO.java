package frc.robot.subsystems.scoring;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutCurrent;
import edu.wpi.first.units.measure.MutDistance;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Voltage;
import org.littletonrobotics.junction.AutoLog;

public interface IntakeIO {
  @AutoLog
  public static class IntakeInputs {
    /**
     * Whether or not the sensor detects a coral
     *
     * <p>This is left intentionally vague until we decide on CANrange vs. beam break vs. something
     * else
     */
    public boolean coralDetected = false;

    public double coralSignalStrength = 0.0;
    public MutDistance coralDistance = Meters.mutable(0.0);

    public boolean coralRangeConnected = false;

    public MutAngle intakeMotorPos = Rotations.mutable(0.0);

    /* Supply current of the Intake motor */
    public MutCurrent intakeMotorSupplyCurrent = Amps.mutable(0.0);

    /* Stator current of the Intake motor */
    public MutCurrent intakeMotorStatorCurrent = Amps.mutable(0.0);
  }

  @AutoLog
  public static class IntakeOutputs {
    /** The voltage applied to the intakeMotor */
    public MutVoltage intakeMotorAppliedVolts = Volts.mutable(0.0);
  }

  /**
   * Updates a IntakeInputs with the current information from sensors and motors.
   *
   * <p>Should be called by the IntakeMechanism periodically
   *
   * @param inputs The IntakeInputs to update with the latest information
   */
  public default void updateInputs(IntakeInputs inputs) {}

  /**
   * Applies requests to motors and updates a IntakeOutputs object with information about motor
   * output.
   *
   * <p>Should be called by the IntakeMechanism periodically
   *
   * @param outputs IntakeOutputs to update with the latest applied voltage.
   */
  public default void applyOutputs(IntakeOutputs outputs) {}

  /**
   * Set the voltage to run the Intake wheels
   *
   * @param volts The voltage to run the Intake wheels at
   */
  public default void setVoltage(Voltage volts) {}

  /**
   * Get the current position of the intakeMotor
   *
   * @return An Angle, the current position of the roller motor as reported by the TalonFX
   */
  public default Angle getIntakeMotorMotorPos() {
    return Rotations.zero();
  }

  /**
   * Set whether or not the mechanism should obey its hardware limit switch.
   * 
   * <p> This should be enabled when intaking and disabled when ejecting.
   *
   * @param obeyLimitSwitch a boolean, true if the limit switch should be obeyed
   */
  public default void setObeyLimitSwitch(boolean obeyLimitSwitch) {}
}