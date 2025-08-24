package frc.robot.subsystems.scoring;

import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.CANrangeConfiguration;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.HardwareLimitSwitchConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.ProximityParamsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.CANrange;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Voltage;
import frc.robot.constants.JsonConstants;

public class IntakeIOTalonFX implements IntakeIO {
  CANrange coralCANRange = new CANrange(IntakeConstants.synced.getObject().coralCANRangeID, "canivore");

  /** Should the hardware limit switch be enabled currently */
  private boolean obeyLimitSwitch = false;
  TalonFX intakeMotor = new TalonFX(IntakeConstants.synced.getObject().intakeMotorID, "canivore");

  private MutVoltage outputVoltage = Volts.mutable(0.0);
  private VoltageOut voltageRequest = new VoltageOut(outputVoltage);
  private StatusSignal<Boolean> coralCANRangeDetected = coralCANRange.getIsDetected();
  private StatusSignal<Current> intakeMotorSupplyCurrent;
  private StatusSignal<Current> intakeMotorStatorCurrent;

  
  public IntakeIOTalonFX() {
    TalonFXConfiguration talonFXConfigs =
        new TalonFXConfiguration()
            .withMotorOutput(
                new MotorOutputConfigs()
                    .withInverted(IntakeConstants.synced.getObject().kIntakeMotorInverted)
                    .withNeutralMode(NeutralModeValue.Brake))
            .withHardwareLimitSwitch(
              new HardwareLimitSwitchConfigs()
                .withForwardLimitRemoteCANrange(coralCANRange))
            .withCurrentLimits(
                new CurrentLimitsConfigs()
                    .withSupplyCurrentLimit(IntakeConstants.synced.getObject().supplyCurrentLimit)
                    .withStatorCurrentLimit(IntakeConstants.synced.getObject().statorCurrentLimit));

    intakeMotor.getConfigurator().apply(talonFXConfigs);

    CANrangeConfiguration coralCANRangeConfigs =
        new CANrangeConfiguration()
            .withProximityParams(
                new ProximityParamsConfigs()
                    .withMinSignalStrengthForValidMeasurement(
                        IntakeConstants.synced.getObject().coralCANRangeMinSignalStrengthForValidMeasurement)
                    .withProximityThreshold(IntakeConstants.synced.getObject().coralCANRangeProximityThreshold)
                    .withProximityHysteresis(IntakeConstants.synced.getObject().coralCANRangeProximityHysteresis));

    coralCANRange.getConfigurator().apply(coralCANRangeConfigs);

    BaseStatusSignal.setUpdateFrequencyForAll(50.0,
        intakeMotorSupplyCurrent,
        intakeMotorStatorCurrent,
        coralCANRangeDetected
    );
  }

  public void updateInputs(IntakeInputs inputs) {
    BaseStatusSignal.refreshAll(
        intakeMotorSupplyCurrent,
        intakeMotorStatorCurrent,
        coralCANRangeDetected
    );
    inputs.coralDetected = coralCANRangeDetected.getValue();

    inputs.coralSignalStrength = coralRange.getSignalStrength().getValueAsDouble();
    inputs.coralDistance.mut_replace(coralRange.getDistance().getValue());

    inputs.coralRangeConnected =
        coralRange.isConnected()
            && StatusSignal.isAllGood(
                coralRange.getIsDetected(),
                coralRange.getSignalStrength(),
                coralRange.getDistance());

    inputs.intakeMotorPos.mut_replace(intakeMotor.getPosition().getValue());

    inputs.intakeMotorStatorCurrent.mut_replace(intakeMotor.getStatorCurrent().getValue());
    inputs.intakeMotorSupplyCurrent.mut_replace(intakeMotor.getSupplyCurrent().getValue());
  }

  public void applyOutputs(ClawOutputs outputs) {
    voltageRequest.withIgnoreHardwareLimits(!obeyLimitSwitch);

    intakeMotor.setControl(voltageRequest.withOutput(outputVoltage));

    outputs.clawAppliedVolts.mut_replace(intakeMotor.getMotorVoltage());
  }

  public void setVoltage(Voltage volts) {
    outputVoltage.mut_replace(volts);
  }

  public Angle getClawMotorPos() {
    return intakeMotor.getPosition().getValue();
  }

  public void setObeyLimitSwitch(boolean obeyLimitSwitch) {
    this.obeyLimitSwitch = obeyLimitSwitch;
  }
}