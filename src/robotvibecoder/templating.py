"""
Handles creation of a jinja2 environment and the definitions of custom filters.
"""

from jinja2 import Environment, PackageLoader, select_autoescape

from robotvibecoder import cli
from robotvibecoder.cli import print_err


def article(word: str) -> str:
    """
    Given a word, return 'an' if the word starts with vowel and 'a' otherwise
    """
    return "an" if word[0].lower() in "aeiou" else "a"


def plural(words: list[str]) -> str:
    """
    Given a list of words, return 's' if more than 1 word is present, otherwise ''
    """
    return "s" if len(words) > 1 else ""


def lowerfirst(word: str) -> str:
    """
    Lowercase the first letter of the input and return it
    """
    return word[0].lower() + word[1:]


def upperfirst(word: str) -> str:
    """
    Uppercase the first letter of the input and return it
    """
    return word[0].upper() + word[1:]


class GlobalTemplateState:  # pylint: disable=too-few-public-methods
    """
    Manages global state for templates:

    For example, CAN IDs should never be reused so this needs to be global
    """

    last_id = 0  # This value is initialized to zero because it is incremented before being used
    can_id_map: dict[str, int] = {}

    @staticmethod
    def new_id() -> int:
        """
        Generate a new CAN ID and increment the last ID counter
        """

        GlobalTemplateState.last_id += 1
        return GlobalTemplateState.last_id


def hash_can_id(device: str) -> str:
    """
    Given a device name, generate a unique CAN Id that is tied to that device
    """
    if device not in GlobalTemplateState.can_id_map:
        next_id = GlobalTemplateState.new_id()
        GlobalTemplateState.can_id_map[device] = next_id
        print(f"  {cli.Colors.fg_green}➜{cli.Colors.reset} ", end="")
        print(
            f"Mapped device {cli.Colors.fg_cyan}{device}{cli.Colors.reset} to placeholder CAN ID {cli.Colors.fg_cyan}{next_id}{cli.Colors.reset}"  # pylint: disable=line-too-long
        )

    return str(GlobalTemplateState.can_id_map[device])


def pos_dimension(kind: str) -> str:
    """
    What dimension should be used for each kind of mechanism?
    """
    if kind == "Arm":
        return "Angle"
    if kind == "Elevator":
        return "Distance"

    # Flywheels won't have a position
    print_err(
        f"Invalid kind {kind} passed to pos_dimension."  # pylint: disable=line-too-long
    )
    print(
        "This is a robotvibecoder issue, NOT a user error. Please report this on github!"
    )
    raise ValueError(f"Invalid kind {kind} passed to pos_dimension")


def vel_dimension(kind: str) -> str:
    """
    What dimension should be used for the velocity of each kind of mechanism?
    """
    if kind == "Elevator":
        return "LinearVelocity"

    return "AngularVelocity"


def pos_unit(kind: str) -> str:
    """
    What measure should be used for each kind of mechanism?
    """
    if kind == "Arm":
        return "Rotations"
    if kind == "Elevator":
        return "Meters"

    print_err(
        f"Invalid kind {kind} passed to pos_unit."  # pylint: disable=line-too-long
    )
    print(
        "This is a robotvibecoder issue, NOT user error. Please report this on github!"
    )
    raise ValueError(f"Invalid kind {kind} passed to pos_unit")


def vel_unit(kind: str) -> str:
    """
    What unit should be used for the velocity of each kind of mechanism?
    """
    if kind == "Elevator":
        return "MetersPerSecond"

    return "RotationsPerSecond"


def goal(kind: str) -> str:
    """
    What should the goal of each mechanism kind be described as?

    E.g. arm has a goalAngle, flywheel has a goalSpeed, elevator has a goalHeight
    """

    if kind == "Arm":
        return "Angle"
    if kind == "Elevator":
        return "Height"
    if kind == "Flywheel":
        return "Speed"

    print_err(f"Invalid kind {kind} passed to goal.")  # pylint: disable=line-too-long
    print(
        "This is a robotvibecoder issue, NOT a user error. Please report this on github!"
    )
    raise ValueError(f"Invalid kind {kind} passed to goal filter")


def goal_dimension(kind: str) -> str:
    """
    What unit is the goal of each mechanism kind represented as?

    E.g. arm uses Angle, elevator uses Distance, flywheel uses AngularVelocity
    """

    if kind == "Arm":
        return "Angle"
    if kind == "Elevator":
        return "Distance"
    if kind == "Flywheel":
        return "AngularVelocity"

    print_err(f"Invalid kind {kind} passed to goal_dimension.")
    print(
        "This is a robotvibecoder issue, NOT a user error. Please report this on github!"
    )
    raise ValueError(f"Invalid kind {kind} passed to goal_dimension")


def generate_env() -> Environment:
    """
    Generate a Jinja2 Environment using a PackageLoader loading from
    'robotvibecoder', add all custom filters, and return it.
    """

    env = Environment(
        loader=PackageLoader("robotvibecoder"), autoescape=select_autoescape()
    )

    env.filters["article"] = article
    env.filters["plural"] = plural
    env.filters["lowerfirst"] = lowerfirst
    env.filters["upperfirst"] = upperfirst
    env.filters["hash_can_id"] = hash_can_id
    env.filters["pos_dimension"] = pos_dimension
    env.filters["vel_dimension"] = vel_dimension
    env.filters["pos_unit"] = pos_unit
    env.filters["vel_unit"] = vel_unit
    env.filters["goal"] = goal
    env.filters["goal_dimension"] = goal_dimension

    return env
