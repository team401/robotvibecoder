from robotvibecoder_aidnem.config import MechanismConfig, MechanismKind


DEFAULT_CONFIG: MechanismConfig = MechanismConfig(
    "subsystems.example",
    "Example",
    MechanismKind.Arm,
    "canivore",
    ["leftMotor", "rightMotor"],
    "leftMotor",
    "exampleEncoder",
)
