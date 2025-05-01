"""
Subcommand to generate a new config
"""

from argparse import Namespace
import json
import os
import sys

from robotvibecoder import constants
import robotvibecoder.cli
from robotvibecoder.config import MechanismConfig, MechanismKind


def new_config_interactive() -> MechanismConfig:
    """
    Prompt the user for each field of a config to generate one interactively
    """

    print(
        "Interactively generating new config. Please enter each field and press [Enter]."
    )
    print("Package: will come after frc.robot (e.g. `subsystems.scoring`)")
    package: str = input("> ")
    print(
        "Name: should be capitalized and should not end in Mechanism or Subsystem, as this is automatically added"  # pylint: disable=line-too-long
    )
    name: str = input("> ")

    print("Kind: Should be either 'Elevator', 'Arm', or 'Flywheel'")
    kind: str = ""
    while kind not in MechanismKind:
        kind = input("> ")
        if kind not in MechanismKind:
            print("Please input either Elevator, Arm, or Flywheel.")
    kind_enum: MechanismKind = MechanismKind[kind]

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
    """
    Generate a new config file, either with placeholder values or interactively in the CLI
    """
    config_path = os.path.join(args.folder, args.outfile)

    print(f"[{robotvibecoder.cli.Colors.title_str}] Creating a new config file")
    print("  ", end="", file=sys.stderr)  # Indent the warning on the line below
    robotvibecoder.cli.print_warning(
        f"This will create/overwrite a file at `{robotvibecoder.cli.Colors.fg_cyan}{config_path}{robotvibecoder.cli.Colors.reset}`"  # pylint: disable=line-too-long
    )
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

    print(f"[{robotvibecoder.cli.Colors.title_str}] Writing config file")
    with open(config_path, "w+", encoding="utf-8") as outfile:
        json.dump(config.__dict__, fp=outfile, indent=2)
