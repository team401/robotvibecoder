import sys
from sys import argv
from robotvibecoder_aidnem.config import load_json_config
from robotvibecoder_aidnem.templating import generate_env


def usage(file=sys.stdout):
    print("Usage: robotvibecoder [config file]", file=file)
    print(
        "\tReads [config file] to generate a mechanism at the path specified", file=file
    )


def main() -> None:
    if len(argv) != 2:
        print("Error: Expected 2 arguments", file=sys.stderr)
        usage(sys.stderr)
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
