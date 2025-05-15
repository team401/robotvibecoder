"""
Constants that will be reused/don't need to live in code, e.g. default config
"""

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
