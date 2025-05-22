"""RobotVibeCoder Config Module

Houses the MechanismConfig class, a dataclass for describing mechanisms.

Also contains utils for loading and generating configs.
"""

import json
import re
import sys
from dataclasses import dataclass
from enum import StrEnum
from typing import Union

from jsonschema import ValidationError, validate

from robotvibecoder.cli import print_err


class MechanismKind(StrEnum):
    """
    An enum for different types of mechanisms: arms, elevators, or flywheels

    This is called Kind and not Type because type is a keyword in python.
    """

    ARM = "Arm"
    ELEVATOR = "Elevator"
    FLYWHEEL = "Flywheel"
    INDEXER = "Indexer"

    @staticmethod
    def try_into(value: str) -> Union["MechanismKind", None]:
        """Try to parse a string into a MechanismKind.
        Returns none if the string isn't a valid MechanismKind.

        :param value: The string to convert
        :type value: str
        :return: A MechanismKind if value is a valid Mechanism Kind, otherwise none
        :rtype: MechanismKind | None
        """
        for kind in MechanismKind:
            if value == kind:
                return kind

        return None


class LimitSensingMethod(StrEnum):
    """
    An enum for different ways to detect that an indexer should stop
    """

    CURRENT = "Current"
    CANRANGE = "CANrange"
    CANDIS1 = "CANdiS1"
    CANDIS2 = "CANdiS2"

    @staticmethod
    def try_into(value: str) -> Union["LimitSensingMethod", None]:
        """Try to parse a string into a LimitSensingMethod.
        Returns none if the string isn't a valid LimitSensingMethod.

        :param value: The string to convert
        :type value: str
        :return: A LimitSensingMethod if value is a valid LimitSensingMethod, otherwise none
        :rtype: LimitSensingMethod | None
        """
        for kind in LimitSensingMethod:
            if value == kind:
                return kind

        return None


@dataclass
class MechanismConfig:
    """
    A dataclass to represent JSON configs. This dataclass is 1:1 with a config JSON file.
    """

    # pylint: disable=too-many-instance-attributes
    # This class has a lot of attributes; it's supposed to describe a whole mechanism in one object

    package: str
    name: str
    kind: MechanismKind
    canbus: str
    motors: list[str]
    lead_motor: str
    encoder: str = ""
    limit_sensing_method: LimitSensingMethod = LimitSensingMethod.CANRANGE
    limit_switch_name: str = ""


CONFIG_SCHEMA = {
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "title": "MechanismConfig",
    "description": "A configuration for a RobotVibeCoder mechanism",
    "type": "object",
    "properties": {
        "package": {"type": "string"},
        "name": {"type": "string"},
        "kind": {"type": "string", "enum": list(MechanismKind)},
        "canbus": {"type": "string"},
        "motors": {"type": "array", "items": {"type": "string"}},
        "lead_motor": {"type": "string"},
    },
    "required": ["package", "name", "canbus", "motors", "lead_motor"],
    "allOf": [
        {
            "if": {
                "properties": {"kind": {"pattern": "Elevator|Arm"}},
                "required": ["kind"],
            },
            "then": {
                "properties": {
                    "encoder": {
                        "type": "string",
                    }
                },
                "required": ["encoder"],
            },
        },
        {
            "if": {
                "properties": {"kind": {"const": "Indexer"}},
                "required": ["kind"],
            },
            "then": {
                "properties": {
                    "limit_sensing_method": {
                        "type": "string",
                        "enum": list(LimitSensingMethod),
                    },
                    "game_piece": {"type": "string"},
                },
                "if": {
                    "properties": {
                        "limit_sensing_method": {"pattern": "CANdiS1|CANdiS2|CANrange"},
                    },
                    "required": ["limit_sensing_method"],
                },
                "then": {
                    "properties": {"limit_switch_name": {"type": "string"}},
                    "required": ["limit_switch_name"],
                },
                "required": ["limit_sensing_method", "game_piece"],
            },
        },
    ],
    "unevaluatedProperties": False,
}


def generate_config_from_data(data: dict) -> MechanismConfig:
    """Given a data dict (e.g. raw JSON data), generate a MechanismConfig, printing errors and
    exiting when config is malformed.

    :param data: The JSON data to convert
    :type data: dict
    :return: A MechanismConfig generated from the dict
    :rtype: MechanismConfig
    """

    try:
        validate_json_config(data)
    except ValidationError as e:
        if e.validator == "unevaluatedProperties":
            # Manually calculate the additional properties because jsonschema doesn't expose it
            result = re.search(
                r"Unevaluated properties are not allowed \((.*) was unexpected\)",
                e.message,
            )

            field: str = (
                result.group(1) if result is not None else e.message
            )  # Safeguard against match fails

            print_err(f"Config contained unexpected field(s): {field}")
        elif e.validator == "required":
            print_err(f"Config missing required field: {e.message}")
        else:
            print_err(f"{e.validator}: {e.message}")
        sys.exit(1)

    # print_err(f"Config contained unexpected field `{key}`")

    # print_err(f"Config missing field `{field.name}`")

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
        with open(config_path, encoding="utf-8") as config_file:
            data = json.load(config_file)
    except FileNotFoundError:
        print_err(f"Specified config file {config_path} does not exist.")
        sys.exit(1)
    except json.JSONDecodeError:
        print_err(f"Invalid JSON format in {config_path}")
        sys.exit(1)

    return generate_config_from_data(data)


def validate_json_config(config: dict) -> None:
    """
    Validate a JSON config against the config schema and print any errors if they are found

    :param config: The JSON config object to validate
    :type config: dict
    """

    validate(config, CONFIG_SCHEMA)


def validate_config(config: MechanismConfig) -> None:
    """
    Validate a config and print any errors if they are found

    :param config: The config to validate
    :type config: MechanismConfig
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
