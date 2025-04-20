from argparse import Namespace
import json
import sys
from robotvibecoder_aidnem.config import (
    MechanismConfig,
    generate_config_from_data,
    load_json_config,
    validate_config,
)
from robotvibecoder_aidnem.templating import generate_env
import os


def generate(args: Namespace) -> None:
    print("[RobotVibeCoder] Generating a Mechanism")

    if args.stdin:
        print("Reading config from stdin.")
        data = json.load(sys.stdin)

        config = generate_config_from_data(data)
    else:
        if args.config is None:
            print(
                "Error: Config not specified: Either --stdin or --config [file] must be supplied to command."
            )
            sys.exit(1)
        print(f"[RobotVibeCoder] Reading config file at {args.config}")
        config = load_json_config(args.config)

        print("Successfully loaded config")

    validate_config(config)

    env = generate_env()

    file_templates = [
        "{name}IO.java",
        "{name}IOTalonFX.java",
        "{name}Constants.java",
    ]

    if not args.stdin:
        print("WARNING: This will create/overwrite files at the following paths:")
        for file_template in file_templates:
            output_path = file_template.format(name = config.name)
            print(f"  {output_path}")
        try:
            input("\n  Press Ctrl+C to cancel or [Enter] to continue")
        except KeyboardInterrupt:
            print("\nCancelled.")
            sys.exit(0)

    for file_template in file_templates:
        output_path = file_template.format(name = config.name)

        if os.path.exists(output_path) and args.stdin:
            # stdin mode skips the warning prompt at the start, so files would be destroyed, necessitating this check
            print(f"Error: File {output_path} already exists. Please move/delete it and retry")
            sys.exit(1)

        print(f"Templating {output_path}")
        template_path = file_template.format(name = "Mechanism") + ".j2" # E.g. MechanismIO.java.j2
        template = env.get_template(template_path)

        output: str = template.render(config.__dict__)
        print("Writing output... ", end="")
        with open(output_path, "w+") as outfile:
            outfile.write(output)
        print("Done")

    print("Done.")