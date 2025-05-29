package frc.robot.subsystems.scoring;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.AngularVelocityUnit;
import edu.wpi.first.units.VoltageUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.units.measure.MutCurrent;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Per;
import edu.wpi.first.units.measure.Voltage;
import org.littletonrobotics.junction.AutoLog;

public interface ElevatorIO {
  enum ElevatorOutputMode {
    ClosedLoop, // Not overriding, it should be closed loop
    Current, // Overriding, manually applying a current
    Voltage // Overriding, manually applying a voltage
  }

  @AutoLog
  public static class ElevatorInputs {
    public boolean leadMotorConnected = false;

    /** Stator current of the leadMotor */
    public MutCurrent leadMotorStatorCurrent = Amps.mutable(0.0);

    /** Supply current of the leadMotor */
    public MutCurrent leadMotorSupplyCurrent = Amps.mutable(0.0);
    
    public boolean followerMotorConnected = false;

    /** Stator current of the followerMotor */
    public MutCurrent followerMotorStatorCurrent = Amps.mutable(0.0);

    /** Supply current of the followerMotor */
    public MutCurrent followerMotorSupplyCurrent = Amps.mutable(0.0);
    

    public boolean elevatorEncoderConnected = false;

    /** Current position of the elevatorEncoder. This measures total rotation since power on, not absolute position */
    public MutAngle elevatorEncoderPos = Rotations.mutable(0.0);

    /** Current velocity reported by the elevatorEncoder */
    public MutAngularVelocity elevatorEncoderVel = RotationsPerSecond.mutable(0.0);

    
    /** The current closed-loop goal position of the name, in terms of the elevatorEncoder */
    public MutAngle elevatorEncoderGoalPos = Rotations.mutable(0.0);

    /** Profile setpoint goal position of the name, in terms of the elevatorEncoder */
    public MutAngle elevatorEncoderSetpointPos = Rotations.mutable(0.0);

    /**
     * Current closed-loop error (distance from setpoint position) as reported by the leadMotor
     * TalonFX, in rotations.
     */
    public double motionMagicError = 0.0;

    /** Velocity of the Elevator mechanism, as reported by the leadMotor TalonFX */
    public MutAngularVelocity elevatorVelocity = RotationsPerSecond.mutable(0.0);
  }

  @AutoLog
  public static class ElevatorOutputs {
    /** Are the motors currently disabled in software? */
    public boolean motorsDisabled = false;

    /** The current output mode of the Elevator */
    public ElevatorOutputMode outputMode = ElevatorOutputMode.ClosedLoop;

    /** The voltage currently applied to the motors */
    public MutVoltage elevatorAppliedVolts = Volts.mutable(0.0);

    /** The current closed-loop output from Motion Magic */
    public double elevatorClosedLoopOutput = 0.0;

    /** Contribution of the p-term to motor output */
    public MutVoltage pContrib = Volts.mutable(0.0);

    /** Contribution of the i-term to motor output */
    public MutVoltage iContrib = Volts.mutable(0.0);

    /** Contribution of the d-term to motor output */
    public MutVoltage dContrib = Volts.mutable(0.0);
  }

  /**
   * Updates an ElevatorInputs with the current information from sensors readings and from the
   * motors.
   *
   * @param inputs ElevatorInputs object to update with latest information
   */
  public void updateInputs(ElevatorInputs inputs);

  /**
   * Applies requests to motors and updates an ElevatorOutputs object with information about motor
   * output.
   *
   * @param outputs ElevatorOutputs object to update with latest applied outputs
   */
  public void applyOutputs(ElevatorOutputs outputs);

  /**
   * Set the goal position of elevatorEncoder which the Elevator will control to when it is not in
   * override mode
   */
  public void setElevatorEncoderGoalPos(Angle goalPos);

  /**
   * Set the position of the elevatorEncoder. This position is separate from absolute position and
   * can track multiple rotations.
   */
  public void setElevatorEncoderPosition(Angle newAngle);

  /**
   * Set the override voltage for the Elevator when in Voltage output mode
   *
   * @param volts The voltage to apply
   */
  public void setOverrideVoltage(Voltage volts);

  /**
   * Set the static current (because of FOC) that will be applied when the Elevator is in Current
   * output mode.
   */
  public void setOverrideCurrent(Current current);

  /**
   * Set whether the Elevator should use ClosedLoop control (default), voltage override, or current
   * override
   */
  public void setOutputMode(ElevatorOutputMode mode);

  /** Update PID gains for the Elevator */
  public void setPID(double p, double i, double d);

  /** Set profile constraints to be sent to Motion Magic Expo */
  public void setMaxProfile(
      AngularVelocity maxVelocity,
      Per<VoltageUnit, AngularAccelerationUnit> expo_kA,
      Per<VoltageUnit, AngularVelocityUnit> expo_kV);

  /** Set feedforward gains for closed-loop control */
  public void setFF(double kS, double kV, double kA, double kG);

  /** Set whether or not the motors should brake while idle */
  public void setBrakeMode(boolean brakeMode);

  /** Set the stator current limit for the Elevator motors */
  public void setStatorCurrentLimit(Current currentLimit);

  /** Set whether or not the motors on the Elevator should be disabled. */
  public void setMotorsDisabled(boolean disabled);
}