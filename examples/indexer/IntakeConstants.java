package frc.robot.subsystems.scoring;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Centimeters;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.signals.InvertedValue;
import coppercore.parameter_tools.json.JSONExclude;
import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.path_provider.EnvironmentHandler;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.Filesystem;

public class IntakeConstants {
  @JSONExclude
  public static final JSONSync<IntakeConstants> synced =
      new JSONSync<IntakeConstants>(
          new IntakeConstants(),
          "IntakeConstants.json",
          EnvironmentHandler.getEnvironmentHandler().getEnvironmentPathProvider(),
          new JSONSyncConfigBuilder().build());

  public final Integer coralCANRangeID = 1;

  
  public final Integer intakeMotorID = 2; // TODO: Replace placeholder CAN ID
  public final InvertedValue kIntakeMotorInverted = InvertedValue.Clockwise_Positive;

  public final Current supplyCurrentLimit = Amps.of(40);
  public final Current statorCurrentLimit = Amps.of(40);


  public final Distance coralCANRangeProximityThreshold = Centimeters.of(8.0);
  public final Distance coralCANRangeProximityHysteresis = Centimeters.of(0.5);
  public final Double coralCANRangeMinSignalStrengthForValidMeasurement = 2500.0;

  public final Voltage coralIntakeVoltage = Volts.of(3.0);
  public final Voltage coralVoltage = Volts.of(3.0);

  public static final class Sim {
    @JSONExclude
    public static final JSONSync<IntakeConstants.Sim> synced =
        new JSONSync<IntakeConstants.Sim>(
            new IntakeConstants.Sim(),
            Filesystem.getDeployDirectory()
                .toPath()
                .resolve("constants/IntakeConstants.Sim.json")
                .toString(),
            new JSONSyncConfigBuilder().build());
    
    /**
     * Simulated motor speed per volt applied
     * 
     * <p> (e.g. 12V = 12 rotations per second at 1 rotation per second per volt)
     */
    public final Double rotationsPerSecondPerVolt = 5.0;

    /**
     * The signum the motor voltage must match for intakeTimeSeconds to intake.
     */
    public final Double intakeSignum = 1.0;

    /**
     * How long the motor voltage signum must equal intakeSignum to intake
     */
    public final Double intakeTimeSeconds = 0.5;

    /**
     * The signum the motor voltage must match for ejectTimeSeconds to eject.
     *
     * <p> Ejecting means sending the game piece backward through the mechanism.
     */
    public final Double ejectSignum = 1.0;

    /**
     * How long the motor voltage signum must equal ejectSignum to eject
     */
    public final Double ejectTimeSeconds = 0.8;

    // TODO: Ensure that eject/outtaking logic matches real-life mechanism
    /**
     * The signum the motor voltage must match for outtakeTimeSeconds to outtake.
     *
     * <p> Outtaking is passing the piece through the mechanism forward.
     */
    public final Double outtakeSignum = 1.0;

    /**
     * How long the motor voltage signum must equal outtakeSignum to outtake
     */
    public final Double outtakeTimeSeconds = 1.0;

    public final Distance distanceWithCoral = Centimeters.of(1.0);
    public final Distance distanceWithoutObject = Centimeters.of(50.0);
  }
}