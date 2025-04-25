from jinja2 import Environment, PackageLoader, select_autoescape

from robotvibecoder_aidnem import constants


def article(word: str) -> str:
    return "an" if word[0].lower() in "aeiou" else "a"


def plural(words: list[str]) -> str:
    return "s" if len(words) > 1 else ""


def lowerfirst(word: str) -> str:
    return word[0].lower() + word[1:]


def upperfirst(word: str) -> str:
    return word[0].upper() + word[1:]


canIdMap: dict[str, int] = {}
nextId = 1


def hash_can_id(device: str) -> str:
    """
    Given a device name, generate a unique CAN Id that is tied to that device
    """
    global nextId

    if device not in canIdMap:
        canIdMap[device] = nextId
        print(f"  {constants.Colors.fg_green}➜{constants.Colors.reset} ", end="")
        print(
            f"Mapped device {constants.Colors.fg_cyan}{device}{constants.Colors.reset} to placeholder CAN ID {constants.Colors.fg_cyan}{nextId}{constants.Colors.reset}"
        )
        nextId += 1

    return str(canIdMap[device])


def pos_dimension(kind: str) -> str:
    """
    What dimension should be used for each kind of mechanism?
    """
    if kind == "Arm":
        return "Angle"
    elif kind == "Elevator":
        return "Distance"
    else:
        print(
            f"{constants.Colors.fg_red}Error:{constants.Colors.reset} Invalid kind {kind} passed to pos_dimension."
        )
        print(
            "This is a robotvibecoder issue, NOT a user error. Please report this on github!"
        )
        raise ValueError(f"Invalid kind {kind} passed to pos_dimension")
    # Flywheels won't have a position


def vel_dimension(kind: str) -> str:
    """
    What dimension should be used for the velocity of each kind of mechanism?
    """
    if kind == "Elevator":
        return "LinearVelocity"
    else:
        return "AngularVelocity"


def pos_unit(kind: str) -> str:
    """
    What measure should be used for each kind of mechanism?
    """
    if kind == "Arm":
        return "Rotations"
    elif kind == "Elevator":
        return "Meters"
    else:
        print(
            f"{constants.Colors.fg_red}Error:{constants.Colors.reset} Invalid kind {kind} passed to pos_unit."
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
    else:
        return "RotationsPerSecond"


def goal(kind: str) -> str:
    """
    What should the goal of each mechanism kind be described as?

    E.g. arm has a goalAngle, flywheel has a goalSpeed, elevator has a goalHeight
    """

    if kind == "Arm":
        return "Angle"
    elif kind == "Elevator":
        return "Height"
    else:
        return "Speed"


def goal_dimension(kind: str) -> str:
    """
    What unit is the goal of each mechanism kind represented as?

    E.g. arm uses Angle, elevator uses Distance, flywheel uses AngularVelocity
    """

    if kind == "Arm":
        return "Angle"
    elif kind == "Elevator":
        return "Distance"
    else:
        return "AngularVelocity"


def generate_env() -> Environment:
    env = Environment(
        loader=PackageLoader("robotvibecoder_aidnem"), autoescape=select_autoescape()
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
