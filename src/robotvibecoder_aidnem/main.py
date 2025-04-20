import argparse
import sys
from sys import argv
from robotvibecoder_aidnem.config import load_json_config
from robotvibecoder_aidnem.templating import generate_env
from robotvibecoder_aidnem.new import new


def main() -> None:
    parser = argparse.ArgumentParser(
        prog="RobotVibeCoder",
        description="Automatically generates boilerplate FRC mechanisms",
        epilog="For documentation or to open a ticket, visit https://github.com/aidnem/robotvibecoder",
    )

    subparsers = parser.add_subparsers(help="subcommand", required=True)

    parser_new = subparsers.add_parser("new", help="generate a new config json file")
    parser_new.add_argument(
        "-i",
        "--interactive",
        action="store_true",
        help="interactively fill fields with command-line utility",
    )
    parser_new.add_argument(
        "outfile", type=str, help="path to write the config file to"
    )
    parser_new.set_defaults(func=new)

    args = parser.parse_args()
    args.func(args)

    sys.exit(1)

    config_path: str = argv[1]

    print(f"[RobotVibeCoder] Reading config file at {config_path}")
    config = load_json_config(config_path)

    print("Successfully loaded config")

    env = generate_env()
    template = env.get_template("MechanismIO.java.jinja")

    output: str = template.render(config.__dict__)

    print("Templated output; writing file")

    with open(f"{config.name}IO.java", "w+") as outfile:
        outfile.write(output)

    print("Done.")


if __name__ == "__main__":
    main()
