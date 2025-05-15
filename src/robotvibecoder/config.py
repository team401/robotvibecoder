"""RobotVibeCoder Config Module

Houses the MechanismConfig class, a dataclass for describing mechanisms.

Also contains utils for loading and generating configs.
"""

from dataclasses import dataclass, fields
from enum import Enum
import json
import sys

from robotvibecoder.cli import print_err


class MechanismKind(str, Enum):
    """
    An enum for different types of mechanisms: arms, elevators, or flywheels

    This is called Kind and not Type because type is a keyword in python.
    """

    ARM = "Arm"
    ELEVATOR = "Elevator"
    FLYWHEEL = "Flywheel"


@dataclass
class MechanismConfig:
    """
    A dataclass to represent JSON configs. This dataclass is 1:1 with a config JSON file.
    """

    package: str
    name: str
    kind: MechanismKind
    canbus: str
    motors: list[str]
    lead_motor: str
    encoder: str


def generate_config_from_data(data: dict) -> MechanismConfig:
    """Given a data dict (e.g. raw JSON data), generate a MechanismConfig, printing errors and
    exiting when config is malformed.

    :param data: The JSON data to convert
    :type data: dict
    :return: A MechanismConfig generated from the dict
    :rtype: MechanismConfig
    """
    for key in data:
        if key not in [field.name for field in fields(MechanismConfig)]:
            print_err(f"Config contained unexpected field `{key}`")
            sys.exit(1)

    for field in fields(MechanismConfig):
        if field.name not in data:
            print_err(
                f"Config missing field `{field.name}`",
            )
            sys.exit(1)

    config: MechanismConfig = MechanismConfig(**data)

    return config


def load_json_config(config_path: str) -> MechanismConfig:
    """Given the path a JSON config file, parse the JSON and convert it to a MechanismConfig object.

    This will throw errors and exit the program if malformed JSON is written by the user.

    :param config_path: Path to the config file (e.g. './config.json')
    :type config_path: str
    :return: The generated MechanismConfig
    :rtype: MechanismConfig
    """
    try:
        with open(config_path, "r", encoding="utf-8") as config_file:
            data = json.load(config_file)
    except FileNotFoundError:
        print_err(f"Specified config file {config_path} does not exist.")
        sys.exit(1)
    except json.JSONDecodeError:
        print_err(f"Invalid JSON format in {config_path}")
        sys.exit(1)

    return generate_config_from_data(data)


def validate_config(config: MechanismConfig) -> None:
    """
    Validate a config and print any errors if they are found
    """

    if config.lead_motor not in config.motors:
        print_err(
            f"`{config.name}` config: `lead_motor` must be one of the motors listed in `motors`"  # pylint: disable=line-too-long
        )

        print(
            f"  Found `{config.lead_motor}` but expected one of {', '.join(['`' + motor + '`' for motor in config.motors])}"  # pylint: disable=line-too-long
        )
        sys.exit(1)

    if len(config.motors) != len(set(config.motors)):
        motors_seen: set[str] = set()

        for motor in config.motors:
            if motor not in motors_seen:
                motors_seen.add(motor)
            else:
                print_err(f"`{config.name}` config: Duplicate motor {motor}")

        sys.exit(1)
