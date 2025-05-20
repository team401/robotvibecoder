"""
Subcommand to generate a new config
"""

import json
import os
import sys
from argparse import Namespace
from typing import List

from prompt_toolkit import prompt
from prompt_toolkit.completion import Completer, Completion

import robotvibecoder.cli
from robotvibecoder import constants
from robotvibecoder.config import LimitSensingMethod, MechanismConfig, MechanismKind


class AppendCompleter(Completer):
    """
    An auto-completer that adds a string to the end of the input text.
    """

    def __init__(self, text: str):
        """
        Create an AppendCompleter

        :param text: The text to suggest completing
        :type text: str
        """
        self.text = text

    def get_completions(self, document, complete_event):
        yield Completion(self.text, start_position=0)


class WordFinisherCompleter(Completer):
    """
    An auto-completer that suggests a word based on the last letter typed
    """

    def __init__(self, words: List[str]):
        self.words: List[str] = words

    def get_completions(self, document, complete_event):
        for word in self.words:
            if document.char_before_cursor == word[0]:
                yield Completion(word[1:], display=word)


def new_config_interactive() -> MechanismConfig:
    """
    Prompt the user for each field of a config to generate one interactively
    """

    print(
        "Interactively generating new config. Please enter each field and press [Enter]."
    )
    print("Package: will come after frc.robot (e.g. `subsystems.scoring`)")
    package: str = prompt("> ", default="subsystems.")
    print(
        "Name: should be capitalized and should not end in Mechanism or Subsystem, as this is automatically added"  # pylint: disable=line-too-long
    )
    name: str = prompt("> ")

    kind_choices = [kind.value for kind in MechanismKind]
    kind: str = robotvibecoder.cli.rvc_pick(
        kind_choices, "Mechanism Kind: (Up/Down/K/J to move, Enter to select)"
    )

    kind_try = MechanismKind.try_into(kind)
    kind_enum: MechanismKind = (
        kind_try if kind_try is not None else MechanismKind.ELEVATOR
    )

    print("CAN Bus: whatever the name of the mechanism's bus is (e.g. `canivore`)")
    canbus: str = prompt("> ", default="canivore")

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
            prompt(
                f"Motor {i + 1} name: a camelcase motor name (e.g. leadMotor)\n> ",
                completer=AppendCompleter("Motor"),
                complete_while_typing=True,
            )
        )

    lead_motor: str = robotvibecoder.cli.rvc_pick(
        motors, "Lead Motor: (Up/Down/K/J to move, Enter to select)"
    )

    partial_config = MechanismConfig(
        package, name, kind_enum, canbus, motors, lead_motor
    )
    if kind_enum in (MechanismKind.ELEVATOR, MechanismKind.ARM):
        return finish_elevator_arm_config_interactive(partial_config)
    elif kind_enum == MechanismKind.INDEXER:
        return finish_indexer_config_interactive(partial_config)
    else:
        robotvibecoder.cli.print_err(
            f"Mechanism generation for kind {kind_enum} is not yet implemented."
        )
        print("  This is a robotvibecoder issue, not user error.")
        sys.exit(1)


def finish_elevator_arm_config_interactive(
    partial_config: MechanismConfig,
) -> MechanismConfig:
    """
    Given a config with all non-mechanism-kind-specific fields propagated, complete the config for
    elevators and arms.
    """

    encoder: str = prompt(
        "Encoder name: a camelcase encoder name (e.g. armEncoder)\n> ",
        completer=AppendCompleter("Encoder"),
        complete_while_typing=True,
    )

    partial_config.encoder = encoder

    return partial_config


def finish_indexer_config_interactive(
    partial_config: MechanismConfig,
) -> MechanismConfig:
    """
    Given a config with all non-mechanism-kind-specific fields propagated, complete the config for
    indexers.
    """

    method_choices = [method.value for method in LimitSensingMethod]
    method: str = robotvibecoder.cli.rvc_pick(
        method_choices, "Limit Sensing Method: (Up/Down/K/J to move, Enter to select)"
    )

    method_try = LimitSensingMethod.try_into(method)
    method_enum: LimitSensingMethod = (
        method_try if method_try is not None else LimitSensingMethod.CANDI
    )

    partial_config.limit_sensing_method = method_enum

    if method_enum in [LimitSensingMethod.CANDI, LimitSensingMethod.CANRANGE]:
        limit_switch_name: str = prompt(
            "Limit switch name: a sensor name (e.g. coralRange)\n> ",
            completer=WordFinisherCompleter(["Range", "Candi"]),
            complete_while_typing=True,
        )

        partial_config.limit_switch_name = limit_switch_name

    return partial_config


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
        # Filter unused values out of config before writing
        filtered_data = {}
        for key in config.__dict__:
            if config.__dict__[key] != "":
                filtered_data[key] = config.__dict__[key]

        json.dump(filtered_data, fp=outfile, indent=2)
