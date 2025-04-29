from argparse import Namespace
import json
import sys
from robotvibecoder import constants
from robotvibecoder.config import (
    MechanismConfig,
    MechanismKind,
    generate_config_from_data,
    load_json_config,
    validate_config,
)
from robotvibecoder.templating import generate_env
import os


def generate(args: Namespace) -> None:
    if args.stdin:
        print("Reading config from stdin.")
        data = json.load(sys.stdin)

        config: MechanismConfig = generate_config_from_data(data)
    else:
        if args.config is None:
            print(
                "Error: Config not specified: Either --stdin or --config [file] must be supplied to command."
            )
            sys.exit(1)
        config_path = os.path.join(args.folder, args.config)
        print(f"[{constants.Colors.title_str}] Reading config file at {config_path}")
        config = load_json_config(config_path)

    validate_config(config)

    env = generate_env()

    if config.kind == MechanismKind.FLYWHEEL:
        raise NotImplementedError("Flywheel Mechanisms are not implemented yet :(")

    template_to_output_map: dict[str, str] = {
        "Mechanism.java.j2": "{name}Mechanism.java",
        "MechanismIO.java.j2": "{name}IO.java",
        "MechanismIOTalonFX.java.j2": "{name}IOTalonFX.java",
        "MechanismConstants.java.j2": "{name}Constants.java",
        config.kind + "Sim.java.j2": "{name}IOSim.java",
    }

    if not args.stdin:
        print(
            f"{constants.Colors.fg_red}{constants.Colors.bold}WARNING{constants.Colors.reset}: This will create/overwrite files at the following paths:"
        )
        for file_template in template_to_output_map:
            output_path = os.path.join(
                args.folder,
                template_to_output_map[file_template].format(name=config.name),
            )
            print(f"  {output_path}")
        try:
            input("\n  Press Ctrl+C to cancel or [Enter] to continue")
        except KeyboardInterrupt:
            print("\nCancelled.")
            sys.exit(0)

    print("Templating files:")
    for file_template in template_to_output_map:
        output_path = os.path.join(
            args.folder, template_to_output_map[file_template].format(name=config.name)
        )

        if os.path.exists(output_path) and args.stdin:
            # stdin mode skips the warning prompt at the start, so files would be destroyed, necessitating this check
            print(
                f"Error: File {output_path} already exists. Please move/delete it and retry"
            )
            sys.exit(1)

        print(f"{constants.Colors.fg_cyan}➜{constants.Colors.reset} {output_path}")
        template_path = file_template
        template = env.get_template(template_path)

        output: str = template.render(config.__dict__)
        with open(output_path, "w+") as outfile:
            outfile.write(output)
