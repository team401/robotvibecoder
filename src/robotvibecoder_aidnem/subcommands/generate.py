from argparse import Namespace
import json
import sys
from robotvibecoder_aidnem.config import (
    MechanismConfig,
    generate_config_from_data,
    load_json_config,
)
from robotvibecoder_aidnem.templating import generate_env


def generate(args: Namespace) -> None:
    print(f"[RobotVibeCoder] Generating a Mechanism")

    if args.stdin:
        print("Reading config from stdin.")
        data = json.load(sys.stdin)

        config = generate_config_from_data(data)
    else:
        if args.config is None:
            print(
                "Error: Config not specified: Either --stdin or --config [file] must be supplied to command."
            )
        print(f"[RobotVibeCoder] Reading config file at {args.config}")
        config = load_json_config(args.config)

        print("Successfully loaded config")

    env = generate_env()
    template = env.get_template("MechanismIO.java.jinja")

    output: str = template.render(config.__dict__)

    print("Templated output; writing file")

    outpath = f"{config.name}IO.java"

    print(f"Writing IO to {outpath}")

    with open(f"{config.name}IO.java", "w+") as outfile:
        outfile.write(output)

    print("Done.")
