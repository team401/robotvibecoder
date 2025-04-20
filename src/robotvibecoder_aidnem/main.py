from jinja2 import Environment, PackageLoader, select_autoescape


def main():
    print("Welcome to RobotVibeCoder.")
    print(
        "This is a stub to prove that Jinja2 installation and executable packaging work :)"
    )
    env = Environment(
        loader=PackageLoader("robotvibecoder_aidnem"), autoescape=select_autoescape()
    )

    template = env.get_template("hello.txt.jinja")

    print(template.render(name=input("Please enter your name\n> ")))


if __name__ == "__main__":
    main()
