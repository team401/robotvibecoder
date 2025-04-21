"""
Subcommand to generate a new config
"""

from argparse import Namespace
import json
import sys

from robotvibecoder_aidnem import constants
from robotvibecoder_aidnem.config import MechanismConfig, MechanismKind


def new_config_interactive() -> MechanismConfig:
    print(
        "Interactively generating new config. Please enter each field and press [Enter]."
    )
    print("Package: will come after frc.robot (e.g. `subsystems.scoring`)")
    package: str = input("> ")
    print(
        "Name: should be capitalized and should not end in Mechanism or Subsystem, as this is automatically added"
    )
    name: str = input("> ")

    print("Kind: Should be either 'Elevator', 'Arm', or 'Flywheel'")
    kind: str = ""
    while kind not in MechanismKind:
        kind = input("> ")
        if kind not in MechanismKind:
            print("Please input either Elevator, Arm, or Flywheel.")
    kind_enum: MechanismKind = kind

    print("CAN Bus: whatever the name of the mechanism's bus is (e.g. `canivore`)")
    canbus: str = input("> ")

    num_motors: int = -1
    while num_motors == -1:
        try:
            raw_num = int(input("Number of motors (an integer, >= 1)\n> "))
            if raw_num < 1:
                continue
            num_motors = raw_num
        except ValueError:
            print("Please input a valid integer.")

    motors: list[str] = []
    for i in range(num_motors):
        motors.append(
            input(f"Motor {i + 1} name: a camelcase motor name (e.g. leadMotor)\n> ")
        )

    lead_motor: str = ""

    while lead_motor not in motors:
        lead_motor = input("Lead motor (must be one of previously defined motors)\n> ")

        if lead_motor not in motors:
            print("Please select one of the previously defined motors:")
            for motor in motors:
                print(f" - {motor}")

    encoder: str = input("Encoder name: a camelcase encoder name (e.g. armEncoder)\n> ")

    return MechanismConfig(
        package, name, kind_enum, canbus, motors, lead_motor, encoder
    )


def new(args: Namespace) -> None:
    print("[RobotVibeCoder] Creating a new config file")
    print(f"  This will create/overwrite a file at `{args.outfile}`")
    try:
        input("  Press Ctrl+C to cancel or [Enter] to continue")
    except KeyboardInterrupt:
        print("\nCancelled.")
        sys.exit(0)

    config: MechanismConfig
    if args.interactive:
        config = new_config_interactive()
    else:
        config = constants.DEFAULT_CONFIG

    print(f"[RobotVibeCoder] Writing config file at `{args.outfile}`")
    with open(args.outfile, "w+") as outfile:
        json.dump(config.__dict__, fp=outfile, indent=2)
