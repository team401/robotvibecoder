from robotvibecoder.config import MechanismConfig, MechanismKind


DEFAULT_CONFIG: MechanismConfig = MechanismConfig(
    "subsystems.example",
    "Example",
    MechanismKind.ARM,
    "canivore",
    ["leftMotor", "rightMotor"],
    "leftMotor",
    "exampleEncoder",
)


class Colors:
    fg_red = "\x1b[31m"
    fg_green = "\x1b[32m"
    fg_cyan = "\x1b[36m"
    bold = "\x1b[1m"
    reset = "\x1b[0m"

    title_str = f"{fg_cyan}RobotVibeCoder{reset}"
