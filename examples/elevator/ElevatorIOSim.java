package frc.robot.subsystems.scoring;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Kilograms;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Seconds;

import com.ctre.phoenix6.sim.CANcoderSimState;
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import frc.robot.constants.JsonConstants;
import frc.robot.constants.SimConstants;
import org.littletonrobotics.junction.Logger;

public class ElevatorIOSim extends ElevatorIOTalonFX {
  CANcoderSimState elevatorEncoderSimState = elevatorEncoder.getSimState();

  TalonFXSimState leadMotorSimState = leadMotor.getSimState();
  TalonFXSimState followerMotorSimState = followerMotor.getSimState();

    private final ElevatorSim elevatorSim =
      new ElevatorSim(
          DCMotor.getKrakenX60Foc(2),
          ElevatorConstants.synced.getObject().elevatorReduction,
          ElevatorConstants.Sim.synced.getObject().carriageMass.in(Kilograms),
          ElevatorConstants.Sim.synced.getObject().drumRadius.in(Meters),
          ElevatorConstants.synced.getObject().elevatorMinMinHeight.in(Meters),
          ElevatorConstants.synced.getObject().elevatorMaxMaxHeight.in(Meters),
          true,
          ElevatorConstants.Sim.synced.getObject().elevatorStartingHeight.in(Meters),
          ElevatorConstants.Sim.synced.getObject().positionStdDev,
          ElevatorConstants.Sim.synced.getObject().velocityStdDev);

  public ElevatorIOSim() {
    super();

    elevatorEncoderSimState.Orientation = ChassisReference.Clockwise_Positive;

    // Initialize sim state so that the first periodic runs with accurate data
    updateSimState();
  }

  private void updateSimState() {
    // Alias that JSON constant here for easier reuse in this method
    final double heightPerRotation = ElevatorConstants.synced.getObject().elevatorHeightPerElevatorEncoderRotationMeters;

    Distance elevatorHeight = Meters.of(elevatorSim.getPositionMeters());
    LinearVelocity elevatorVelocity = MetersPerSecond.of(elevatorSim.getVelocityMetersPerSecond());

    Logger.recordOutput("elevator/simElevatorHeightMeters", elevatorHeight.in(Meters));
    Logger.recordOutput(
        "elevator/simElevatorVelocityMetersPerSec", elevatorVelocity.in(MetersPerSecond));

    Angle elevatorEncoderAngle = Rotations.of(elevatorHeight.in(Meters) / heightPerRotation);

    Angle motorAngle = elevatorEncoderAngle.times(ElevatorConstants.synced.getObject().elevatorReduction);

    // Convert Elevator velocity (m/s) into angular velocity of elevatorEncoder
    // by dividing by height per rotation: (m/s) / (m/rot) = rot/s
    
    AngularVelocity elevatorEncoderVelocity =
        RotationsPerSecond.of(
            elevatorVelocity.in(MetersPerSecond)
                / heightPerRotation);

    // For motors, multiply encoder velocity by Elevator reduction, because the motors
    // will spin [reduction] times as many times as spool.
    // .times(ElevatorConstants.synced.getObject().elevatorReduction);
    AngularVelocity motorVelocity =
        elevatorEncoderVelocity.times(ElevatorConstants.synced.getObject().elevatorReduction);
    // TODO: Find out if/why sim breaks when multiplying motor velocity by motor reduction

    elevatorEncoderSimState.setRawPosition(elevatorEncoderAngle);
    elevatorEncoderSimState.setVelocity(elevatorEncoderVelocity);

    leadMotorSimState.setRawRotorPosition(motorAngle);
    leadMotorSimState.setRotorVelocity(motorVelocity);
    leadMotorSimState.setSupplyVoltage(RobotController.getBatteryVoltage());

    followerMotorSimState.setRawRotorPosition(motorAngle);
    followerMotorSimState.setRotorVelocity(motorVelocity);
    followerMotorSimState.setSupplyVoltage(RobotController.getBatteryVoltage());

    elevatorSim.setInputVoltage(leadMotorSimState.getMotorVoltage());

    elevatorSim.update(SimConstants.simDeltaTime.in(Seconds));
  }

  @Override
  public void updateInputs(ElevatorInputs inputs) {
    updateSimState();

    super.updateInputs(inputs);
  }
}