"""
Houses the main entrypoint for RobotVibeCoder CLI and argument parsing
"""

import argparse
from robotvibecoder import cli
from robotvibecoder.subcommands.new import new
from robotvibecoder.subcommands.generate import generate


def main() -> None:
    """
    Main entry point for the CLI. Parses args and invokes a subcommand.
    """

    parser = argparse.ArgumentParser(
        prog="RobotVibeCoder",
        description="Automatically generates boilerplate FRC mechanisms",
        epilog="For documentation or to open a ticket, visit https://github.com/team401/robotvibecoder",  # pylint: disable=line-too-long
    )
    parser.add_argument(
        "-f",
        "--folder",
        type=str,
        help="path to folder to generate/read config or generate IO files into",
        default="",
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

    parser_generate = subparsers.add_parser(
        "generate", help="generate a mechanism based on a config"
    )
    parser_generate.add_argument(
        "--stdin",
        action="store_true",
        help="accept config file from stdin instead of by a path",
    )
    parser_generate.add_argument(
        "-c", "--config", type=str, help="path to the config JSON file"
    )
    parser_generate.set_defaults(func=generate)

    # Parse argv
    args = parser.parse_args()
    # Call the default function defined by the subcommand
    args.func(args)

    print(f"{cli.Colors.fg_green}{cli.Colors.bold}Done.{cli.Colors.reset}")


if __name__ == "__main__":
    main()
