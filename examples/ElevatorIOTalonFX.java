package frc.robot.subsystems.scoring;

import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.MotionMagicExpoTorqueCurrentFOC;
import com.ctre.phoenix6.controls.TorqueCurrentFOC;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.AngularVelocityUnit;
import edu.wpi.first.units.VoltageUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.Per;
import edu.wpi.first.units.measure.Voltage;
import frc.robot.subsystems.scoring.ElevatorConstants;
import org.littletonrobotics.junction.Logger;

public class ElevatorIOTalonFX implements ElevatorIO {
  MutAngle elevatorEncoderGoalAngle = Rotations.mutable(0.0);
  MutAngle elevatorEncoderSetpointPosition = Rotations.mutable(0.0);

  Current overrideCurrent;
  Voltage overrideVoltage;

  ElevatorOutputMode outputMode = ElevatorOutputMode.ClosedLoop;
  TalonFX leadMotor;
  
  TalonFX followerMotor;
  

  CANcoder elevatorEncoder;

  // Reuse the same talonFXConfiguration instead of making a new one each time.
  TalonFXConfiguration talonFXConfigs;

  boolean motorDisabled = false;

  private StatusSignal<Angle> elevatorEncoderPosition;
  private StatusSignal<AngularVelocity> elevatorEncoderVelocity;
  private StatusSignal<Current> leadMotorSupplyCurrent;
  private StatusSignal<Current> leadMotorStatorCurrent;

  
  private StatusSignal<Current> followerMotorSupplyCurrent;
  private StatusSignal<Current> followerMotorStatorCurrent;

  

  // Reuse the same motion magic request to avoid garbage collector having to clean them up.
  MotionMagicExpoTorqueCurrentFOC motionMagicExpoTorqueCurrentFOC =
      new MotionMagicExpoTorqueCurrentFOC(0.0);
  VoltageOut voltageOut = new VoltageOut(0.0);
  TorqueCurrentFOC currentOut = new TorqueCurrentFOC(0.0);

  public ElevatorIOTalonFX() {
    // Initialize TalonFXs  and CANcoders with their correct IDs
    leadMotor = new TalonFX(ElevatorConstants.synced.getObject().leadMotorId, "canivore");
    followerMotor = new TalonFX(ElevatorConstants.synced.getObject().followerMotorId, "canivore");

    elevatorEncoder =
        new CANcoder(ElevatorConstants.synced.getObject().elevatorEncoderID, "canivore");

    CANcoderConfiguration cancoderConfiguration = new CANcoderConfiguration();
    cancoderConfiguration.MagnetSensor.AbsoluteSensorDiscontinuityPoint =
        ElevatorConstants.synced.getObject().elevatorEncoderDiscontinuityPoint;

    // Update with large CANcoder direction and apply
    cancoderConfiguration.MagnetSensor.SensorDirection =
        ElevatorConstants.synced.getObject().elevatorEncoderDirection;
    cancoderConfiguration.MagnetSensor.MagnetOffset = ElevatorConstants.synced.getObject().elevatorEncoderMagnetOffset.in(Rotations);
    elevatorEncoder.getConfigurator().apply(cancoderConfiguration);

    // Cache status signals and refresh them when used
    elevatorEncoderPosition = elevatorEncoder.getPosition();
    elevatorEncoderVelocity = elevatorEncoder.getVelocity();

    leadMotorSupplyCurrent = leadMotor.getSupplyCurrent();
    leadMotorStatorCurrent = leadMotor.getStatorCurrent();

    followerMotorSupplyCurrent = followerMotor.getSupplyCurrent();
    followerMotorStatorCurrent = followerMotor.getStatorCurrent();

    BaseStatusSignal.setUpdateFrequencyForAll(50.0,
        leadMotorSupplyCurrent,
        leadMotorStatorCurrent,
        followerMotorSupplyCurrent,
        followerMotorStatorCurrent,
        elevatorEncoderPosition,
        elevatorEncoderVelocity
    );

    // Initialize talonFXConfigs to use FusedCANCoder and Motion Magic Expo and have correct PID
    // gains and current limits.
    talonFXConfigs =
        new TalonFXConfiguration()
            .withFeedback(
                new FeedbackConfigs()
                    .withFeedbackRemoteSensorID(elevatorEncoder.getDeviceID())
                    .withFeedbackSensorSource(FeedbackSensorSourceValue.FusedCANcoder)
                    .withSensorToMechanismRatio(
                        ElevatorConstants.synced.getObject().elevatorEncoderToMechanismRatio)
                    .withRotorToSensorRatio(
                        ElevatorConstants.synced.getObject().rotorToElevatorEncoderRatio))
            .withMotorOutput(new MotorOutputConfigs().withNeutralMode(NeutralModeValue.Coast))
            .withCurrentLimits(
                new CurrentLimitsConfigs()
                    .withStatorCurrentLimitEnable(true)
                    .withStatorCurrentLimit(
                        ElevatorConstants.synced.getObject().elevatorStatorCurrentLimit))
            .withSlot0(
                new Slot0Configs()
                    .withGravityType(GravityTypeValue.Elevator_Static)
                    .withKS(ElevatorConstants.synced.getObject().elevatorKS)
                    .withKV(ElevatorConstants.synced.getObject().elevatorKV)
                    .withKA(ElevatorConstants.synced.getObject().elevatorKA)
                    .withKG(ElevatorConstants.synced.getObject().elevatorKG)
                    .withKP(ElevatorConstants.synced.getObject().elevatorKP)
                    .withKI(ElevatorConstants.synced.getObject().elevatorKI)
                    .withKD(ElevatorConstants.synced.getObject().elevatorKD))
            .withMotionMagic(
                new MotionMagicConfigs()
                    .withMotionMagicCruiseVelocity(
                        ElevatorConstants.synced.getObject().elevatorAngularCruiseVelocityRotationsPerSecond)
                    .withMotionMagicExpo_kA(
                        ElevatorConstants.synced.getObject().elevatorMotionMagicExpo_kA)
                    .withMotionMagicExpo_kV(
                        ElevatorConstants.synced.getObject().elevatorMotionMagicExpo_kV));

    // Apply talonFX config to motors
    leadMotor.getConfigurator().apply(talonFXConfigs);
    followerMotor.getConfigurator().apply(talonFXConfigs);

    // Make follower motors permanently follow lead motor.
    followerMotor.setControl(
        new Follower(
            leadMotor.getDeviceID(),
            ElevatorConstants.synced.getObject().invertFollowerMotorFollowerRequest));
  }

  @Override
  public void updateInputs(ElevatorInputs inputs) {
    StatusCode refreshStatus = BaseStatusSignal.refreshAll(elevatorEncoderPosition, elevatorEncoderVelocity);

    inputs.elevatorEncoderPos.mut_replace(elevatorEncoderPosition.getValue());
    inputs.elevatorEncoderVel.mut_replace(elevatorEncoderVelocity.getValue());
    inputs.elevatorEncoderConnected = refreshStatus.isOK();

    refreshStatus = BaseStatusSignal.refreshAll(
        leadMotorSupplyCurrent,
        leadMotorStatorCurrent,
        followerMotorSupplyCurrent,
        followerMotorStatorCurrent
    );

    inputs.leadMotorSupplyCurrent.mut_replace(leadMotorSupplyCurrent.getValue());
    inputs.leadMotorStatorCurrent.mut_replace(leadMotorStatorCurrent.getValue());
    inputs.leadMotorConnected = leadMotor.isConnected();

    inputs.followerMotorSupplyCurrent.mut_replace(followerMotorSupplyCurrent.getValue());
    inputs.followerMotorStatorCurrent.mut_replace(followerMotorStatorCurrent.getValue());
    inputs.followerMotorConnected = followerMotor.isConnected();

    inputs.elevatorEncoderGoalPos.mut_replace(elevatorEncoderGoalAngle);
    inputs.elevatorEncoderSetpointPos.mut_replace(elevatorEncoderSetpointPosition);

    inputs.motionMagicError = leadMotor.getClosedLoopError().getValueAsDouble();

    inputs.elevatorVelocity.mut_replace(elevatorEncoder.getVelocity().getValue());
  }

  @Override
  public void applyOutputs(ElevatorOutputs outputs) {
    outputs.motorsDisabled = motorDisabled;
    outputs.outputMode = outputMode;

    motionMagicExpoTorqueCurrentFOC.withPosition(elevatorEncoderGoalAngle);

    if (motorDisabled) {
      leadMotor.setControl(voltageOut.withOutput(0.0));
      outputs.elevatorAppliedVolts.mut_replace(Volts.of(0.0));
    } else {
      switch (outputMode) {
        case ClosedLoop:
          leadMotor.setControl(motionMagicExpoTorqueCurrentFOC);

          elevatorEncoderSetpointPosition.mut_setMagnitude(
              (leadMotor.getClosedLoopReference().getValue()));

          Logger.recordOutput(
              "elevator/referenceSlope",
              leadMotor.getClosedLoopReferenceSlope().getValueAsDouble());
          outputs.elevatorAppliedVolts.mut_replace(
              leadMotor.getMotorVoltage().getValue());
          outputs.elevatorClosedLoopOutput = leadMotor.getClosedLoopOutput().getValueAsDouble();
          outputs.pContrib.mut_replace(
              Volts.of(leadMotor.getClosedLoopProportionalOutput().getValueAsDouble()));
          outputs.iContrib.mut_replace(
              Volts.of(leadMotor.getClosedLoopIntegratedOutput().getValueAsDouble()));
          outputs.dContrib.mut_replace(
              Volts.of(leadMotor.getClosedLoopDerivativeOutput().getValueAsDouble()));
          break;
        case Voltage:
          leadMotor.setControl(new VoltageOut(overrideVoltage));
          outputs.elevatorAppliedVolts.mut_replace(overrideVoltage);
          break;
        case Current:
          leadMotor.setControl(currentOut.withOutput(overrideCurrent));
          outputs.elevatorAppliedVolts.mut_replace(leadMotor.getMotorVoltage().getValue());
          break;
      }
    }
  }

  @Override
  public void setElevatorEncoderGoalPos(Angle goalPos) {
    elevatorEncoderGoalAngle.mut_replace(goalPos);
  }

  @Override
  public void setElevatorEncoderPosition(Angle newAngle) {
    elevatorEncoder.setPosition(newAngle);
  }

  @Override
  public void setOutputMode(ElevatorOutputMode outputMode) {
    this.outputMode = outputMode;
  }

  @Override
  public void setOverrideVoltage(Voltage volts) {
    overrideVoltage = volts;
  }

  @Override
  public void setOverrideCurrent(Current current) {
    overrideCurrent = current;
  }

  @Override
  public void setPID(double p, double i, double d) {
    Slot0Configs configs = talonFXConfigs.Slot0;

    configs.kP = p;
    configs.kI = i;
    configs.kD = d;
    leadMotor.getConfigurator().apply(configs);
    followerMotor.getConfigurator().apply(configs);
  }

  @Override
  public void setMaxProfile(
      AngularVelocity maxVelocity,
      Per<VoltageUnit, AngularAccelerationUnit> expo_kA,
      Per<VoltageUnit, AngularVelocityUnit> expo_kV) {
    MotionMagicConfigs configs =
        talonFXConfigs
            .MotionMagic
            // .withMotionMagicCruiseVelocity(maxVelocity)
            .withMotionMagicExpo_kA(expo_kA)
            .withMotionMagicExpo_kV(expo_kV);
    leadMotor.getConfigurator().apply(configs);
    followerMotor.getConfigurator().apply(configs);
  }

  @Override
  public void setFF(double kS, double kV, double kA, double kG) {
    Slot0Configs configs = talonFXConfigs.Slot0;

    configs.kS = kS;
    configs.kV = kV;
    configs.kA = kA;
    configs.kG = kG;
    leadMotor.getConfigurator().apply(configs);
    followerMotor.getConfigurator().apply(configs);
  }

  @Override
  public void setBrakeMode(boolean brakeMode) {
    leadMotor.setNeutralMode(brakeMode ? NeutralModeValue.Brake : NeutralModeValue.Coast);
    followerMotor.setNeutralMode(brakeMode ? NeutralModeValue.Brake : NeutralModeValue.Coast);
  }

  @Override
  public void setStatorCurrentLimit(Current currentLimit) {
    talonFXConfigs.CurrentLimits.withStatorCurrentLimit(currentLimit);

    // Only apply current limit configs to avoid overwriting PID and FF values from tuning
    leadMotor.getConfigurator().apply(talonFXConfigs.CurrentLimits);
    followerMotor.getConfigurator().apply(talonFXConfigs.CurrentLimits);
  }

  @Override
  public void setMotorsDisabled(boolean disabled) {
    motorDisabled = disabled;
  }
}