from dataclasses import dataclass, fields
import json
import sys


@dataclass
class MechanismConfig:
    package: str
    name: str
    motors: list[str]
    lead_motor: str
    encoder: str


def generate_config_from_data(data: dict) -> MechanismConfig:
    for key in data:
        if key not in [field.name for field in fields(MechanismConfig)]:
            print(f"Error: Config contained unexpected field `{key}`", file=sys.stdout)
            sys.exit(1)

    for field in fields(MechanismConfig):
        if field.name not in data:
            print(
                f"Error: Config missing field `{field.name}`",
                file=sys.stdout,
            )
            sys.exit(1)

    config: MechanismConfig = MechanismConfig(**data)

    return config


def load_json_config(config_path: str) -> MechanismConfig:
    try:
        with open(config_path, "r") as config_file:
            data = json.load(config_file)
    except FileNotFoundError:
        print(f"Error: Specified config file {config_path} does not exist.")
        sys.exit(1)
    except json.JSONDecodeError:
        print(f"Error: Invalid JSON format in {config_path}")
        sys.exit(1)

    return generate_config_from_data(data)


def validate_config(config: MechanismConfig) -> None:
    """
    Validate a config and print any errors if they are found
    """

    if config.lead_motor not in config.motors:
        print(f"Error in config `{config.name}`: `lead_motor` must be one of the motors specified by `motors`")
        print(f"  Found `{config.lead_motor}` but expected one of {', '.join(['`' + motor + '`' for motor in config.motors])}")
        sys.exit(1)
