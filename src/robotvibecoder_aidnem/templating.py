from jinja2 import Environment, PackageLoader, select_autoescape


def article(word: str) -> str:
    return "an" if word[0].lower() in "aeiou" else "a"


def plural(words: list[str]) -> str:
    return "s" if len(words) > 1 else ""


def lowerfirst(word: str) -> str:
    return word[0].lower() + word[1:]

canIdMap = {}
nextId = 1

def hash_can_id(device: str) -> str:
    """
    Given a device name, generate a unique CAN Id that is tied to that device
    """
    global nextId

    if device not in canIdMap:
        canIdMap[device] = nextId
        print(f"Mapped device {device} to placeholder CAN ID {nextId}")
        nextId += 1

    return canIdMap[device]

def generate_env() -> Environment:
    env = Environment(
        loader=PackageLoader("robotvibecoder_aidnem"), autoescape=select_autoescape()
    )

    env.filters["article"] = article
    env.filters["plural"] = plural
    env.filters["lowerfirst"] = lowerfirst
    env.filters["hash_can_id"] = hash_can_id

    return env
