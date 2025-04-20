from robotvibecoder_aidnem.config import MechanismConfig


DEFAULT_CONFIG: MechanismConfig = MechanismConfig(
    "subsystems.example",
    "Example",
    ["leftMotor", "rightMotor"],
    "leftMotor",
    "exampleEncoder",
)
