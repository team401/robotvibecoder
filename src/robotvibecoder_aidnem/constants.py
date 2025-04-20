from robotvibecoder_aidnem.config import MechanismConfig


DEFAULT_CONFIG: MechanismConfig = MechanismConfig(
    "subsystems.example",
    "Example",
    "canivore",
    ["leftMotor", "rightMotor"],
    "leftMotor",
    "exampleEncoder",
)
