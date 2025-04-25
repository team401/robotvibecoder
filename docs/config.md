# Config JSON Files

A config file provides the basic specifications of the template mechanism that will be generated.

Here's an example config:

```json
{
  "package": "subsystems.scoring",
  "name": "Elevator",
  "kind": "Elevator",
  "motors": ["leadMotor", "followerMotor"],
  "lead_motor": "leadMotor",
  "encoder": "elevatorEncoder"
}
```

## Creating a config file

`robotvibecoder` can automatically generate a config file, either through a template interactively

To generate a template config file, just run

```sh
robotvibecoder new config.json
```

The output file path (in this case `config.json`) can be any path where the config should be generated.

### Interactively generate a config

To interactively generate a config with a CLI wizard, add the `-i` or `--interactive` flag:

```sh
$ robotvibecoder new -i config.json
```

That will launch a wizard: here's an example usage:

```
[RobotVibeCoder] Creating a new config file
  This will create/overwrite a file at `output.json`
  Press Ctrl+C to cancel or [Enter] to continue
Interactively generating new config. Please enter each field and press [Enter].
Package: will come after frc.robot (e.g. `subsystems.scoring`)
> subsystems.scoring
Name: should be capitalized and should not end in Mechanism or Subsystem, as this is automatically added
> Elevator
Kind: Should be either 'Elevator', 'Arm', or 'Flywheel'
> Elevator
CAN Bus: whatever the name of the mechanism's bus is (e.g. `canivore`)
> canivore
Number of motors (an integer, >= 1)
> 2
Motor 1 name: a camelcase motor name (e.g. leadMotor)
> leadMotor
Motor 2 name: a camelcase motor name (e.g. leadMotor)
> followerMotor
Lead motor (must be one of previously defined motors)
> leadMotor
Encoder name: a camelcase encoder name (e.g. armEncoder)
> elevatorEncoder
[RobotVibeCoder] Writing config file at `output.json`
```

This process would produce the following JSON config:

```json
{
  "package": "subsystems.example",
  "name": "Elevator",
  "kind": "Elevator",
  "motors": ["leadMotor", "followerMotor"],
  "lead_motor": "leadMotor",
  "encoder": "elevatorEncoder"
}
```

## Config file fields

- ### `package`

  The package field is a string determining what package the resulting java files will be in. This should be lowercase, and starts after `frc.robot`. For example, the package string `subsystems.scoring` would add the following line to the top of the generated files:

  ```java
  package frc.robot.subsystems.scoring;
  ```

- ### `name`

  The name field is a string determining the name of the mechanism. This should be capitalized, and should not end with "mechanism" or "subsystem". For instance, the name string `Elevator` would generate IOs named `ElevatorIO.java`, `ElevatorIOTalonFX.java`, etc.

- ### `kind`

  The kind field is a string determining what kind of mechanism to generate. This should be either `Arm`, `Elevator`, or `Flywheel`. The kind (named kind instead of type because type is a reserved keyword in python) determines what type of sim to generate:
  
  - `Arm`: [`SingleJointedArmSim`](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/wpilibj/simulation/SingleJointedArmSim.html)
  - `Elevator`: [`ElevatorSim`](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/wpilibj/simulation/ElevatorSim.html)
  - `Flywheel`: [`FlywheelSim`](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/wpilibj/simulation/FlywheelSim.html)

  **Note**: Currently, `Arm` is the only mechanism kind that is supported. The other two mechanisms will be added over the next few days.

- ### `canbus`

  The canbus field is a string field determining the name of the CAN bus that the devices will be on. For example, the default CAN bus when using a canivore is `canivore`

- ### `motors`

  The motors field is an array of strings defining the names of the motors of the mechanism. It is recommended for these names to be camel-cased (e.g. `leadMotor`). Each motor will be a field in the inputs object for whether or not it is connected, as well as fields for supply and stator current.

- ### `lead_motor`

  The lead_motor field is a string determining which of these motors should be treated as the "lead motor". In TalonFX IOs, this motor will be commanded with the Closed-Loop request while all other motors are commanded with follower requests to follow the lead motor. This value must be one of `motors`.

- ### `encoder`

  The encoder field is a string determining the name of the encoder that will be used for closed-loop control. This name is recommended for these names to be camel-cased (e.g. `armEncoder`). This field determines the name of the variable holding the encoder as well as its name in the IO objects.
