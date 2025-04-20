package frc.robot.subsystems.scoring; // NOTE: This should be changed if you keep your constants in a separate package from your code

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.Pounds;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;
import static edu.wpi.first.units.Units.VoltsPerRadianPerSecond;
import static edu.wpi.first.units.Units.VoltsPerRadianPerSecondSquared;

import com.ctre.phoenix6.signals.SensorDirectionValue;
import coppercore.parameter_tools.json.JSONExclude;
import coppercore.parameter_tools.json.JSONSync;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.path_provider.EnvironmentHandler;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.AngularVelocityUnit;
import edu.wpi.first.units.DistanceUnit;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.units.VoltageUnit;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.units.measure.Mass;
import edu.wpi.first.units.measure.Per;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.Filesystem;

public final class ElevatorConstants {
  @JSONExclude
  public static final JSONSync<ElevatorConstants> synced =
      new JSONSync<ElevatorConstants>(
          new ElevatorConstants(),
          "ElevatorConstants.json",
          EnvironmentHandler.getEnvironmentHandler().getEnvironmentPathProvider(),
          new JSONSyncConfigBuilder().setPrettyPrinting(true).build());
  public final Integer leadMotorId = 1; // TODO: Replace placeholder CAN ID
  
  public final Integer followerMotorId = 2; // TODO: Replace placeholder CAN ID
  
  public final Boolean invertFollowerElevatorMotor = true;
    
    
    
  public final Boolean invertFollowermotorFollowerRequest = false;
    
    

  /**
   * What point in the sensor's range the discontinuity occurs. Results in a range of [1-x, x). For
   * example, a value of 1 gives a range of [0.0, 1).
   */
  public final Double elevatorEncoderDiscontinuityPoint = 1.0;

  public final Integer elevatorEncoderID = 3; // TODO: Replace placeholder CAN ID

  public final SensorDirectionValue elevatorEncoderDirection =
      SensorDirectionValue.Clockwise_Positive;

  /*
   * The elevatorEncoder is represented as the mechanism in our Phoenix configs.
   * This means that we are controlling to a goal in terms of large CANCoder angle.
   */
  @JSONExclude public final double elevatorEncoderToMechanismRatio = 1.0;

  @JSONExclude
  public final double rotorToelevatorEncoder Ratio = 1.0; // TODO: Replace placeholder value

  public final Double elevatorkP = 0.0;
  public final Double elevatorkI = 0.0;
  public final Double elevatorkD = 0.0;

  public final Double elevatorkS = 0.0;
  public final Double elevatorkV = 0.0;
  public final Double elevatorkA = 0.0;
  public final Double elevatorkG = 0.0;

  /** This is a Double until coppercore JSONSync supports RotationsPerSecond */
  public final Double elevatorAngularCruiseVelocityRotationsPerSecond = 1.0;

  /*
   * The Motion Magic Expo kV, measured in Volts per Radian per Second, but represented as a double so it can be synced by JSONSync
   *
   * <p> This kV is used by Motion Magic Expo to generate a motion profile. Dividing the supply voltage by
   * kV results in the maximum velocity of the system. Therefore, a higher profile kV results in a
   * lower profile velocity.
  */
  public final Double elevatorExpo_kV_raw = 0.0;

  /*
   * The Motion Magic Expo kA, measured in Volts per Radian per Second Squared, but represented as a double so it can be synced by JSONSync
  */
  public final Double elevatorExpo_kA_raw = 0.0;

  public final Current elevatorStatorCurrentLimit = Amps.of(80.0); // TODO: Replace placeholder current limit

  public static final class Sim {
    @JSONExclude
    public static final JSONSync<ElevatorConstants.Sim> synced =
        new JSONSync<ElevatorConstants.Sim>(
            new ElevatorConstants.Sim(),
            Filesystem.getDeployDirectory()
                .toPath()
                .resolve("constants/ElevatorConstants.Sim.json")
                .toString(),
            new JSONSyncConfigBuilder().build());

    /** Standard deviation passed to sim for the position measurement */
    public final Double positionStdDev = 0.0;

    /** Standard deviation passed to sim for the velocity measurement */
    public final Double velocityStdDev = 0.0;
  }
}