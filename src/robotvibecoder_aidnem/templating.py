from jinja2 import Environment, PackageLoader, select_autoescape


def article(word: str) -> str:
    return "an" if word[0].lower() in "aeiou" else "a"


def plural(words: list[str]) -> str:
    return "s" if len(words) > 1 else ""


def generate_env() -> Environment:
    env = Environment(
        loader=PackageLoader("robotvibecoder_aidnem"), autoescape=select_autoescape()
    )

    env.filters["article"] = article
    env.filters["plural"] = plural

    return env
