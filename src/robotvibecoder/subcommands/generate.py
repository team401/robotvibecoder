"""
The 'generate' subcommand, for templating/generating a mechanism from a config
"""

from argparse import Namespace
import json
import os
import sys

from robotvibecoder import cli
from robotvibecoder.cli import print_err, print_warning
from robotvibecoder.config import (
    MechanismConfig,
    MechanismKind,
    generate_config_from_data,
    load_json_config,
    validate_config,
)
from robotvibecoder.templating import generate_env


def generate(args: Namespace) -> None:
    """
    Given a config (either a path or via stdin), template and generate mechanism boilerplate files
    """
    if args.stdin:
        print("Reading config from stdin.")
        data = json.load(sys.stdin)

        config: MechanismConfig = generate_config_from_data(data)
    else:
        if args.config is None:
            print_err(
                "Config not specified: Either --stdin or --config [file] must be supplied to command."  # pylint: disable=line-too-long
            )
            sys.exit(1)
        config_path = os.path.join(args.folder, args.config)
        print(f"[{cli.Colors.title_str}] Reading config file at {config_path}")
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
        print_warning(
            "This will create/overwrite files at the following paths:"  # pylint: disable=line-too-long
        )
        for file_template, file_output in template_to_output_map.items():
            output_path = os.path.join(
                args.folder,
                file_output.format(name=config.name),
            )
            print(f"  {output_path}")
        try:
            input("\n  Press Ctrl+C to cancel or [Enter] to continue")
        except KeyboardInterrupt:
            print("\nCancelled.")
            sys.exit(0)

    print("Templating files:")
    for file_template, file_output in template_to_output_map.items():
        output_path = os.path.join(args.folder, file_output.format(name=config.name))

        if os.path.exists(output_path) and args.stdin:
            # stdin mode skips the warning prompt at the start, so files would
            # be destroyed, necessitating this check
            print_err(
                f"File {output_path} already exists. Please move/delete it and retry"
            )
            sys.exit(1)

        print(f"{cli.Colors.fg_cyan}➜{cli.Colors.reset} {output_path}")
        template_path = file_template
        template = env.get_template(template_path)

        output: str = template.render(config.__dict__)
        with open(output_path, "w+", encoding="utf-8") as outfile:
            outfile.write(output)
