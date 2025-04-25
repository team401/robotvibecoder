# robotvibecoder

Automatically generate code stubs for mechanisms in FRC Robot Code. This project leverages WPILib, AdvantageKit, and CTRE's Phoenix-6 library to automatically generate boilerplate complete with hardware IO abstraction and simulation. RobotVibeCoder supports 3 mechanism types: elevators, single jointed arms, and flywheels.

---

## Table of Contents

- [robotvibecoder](#robotvibecoder)
  - [Table of Contents](#table-of-contents)
  - [Installation](#installation)
  - [Quickstart](#quickstart)
  - [License](#license)

## Installation

RobotVibeCoder is hosted on PyPI: [robotvibecoder](https://pypi.org/project/robotvibecoder/). It can be installed with pip:

```console
pip install robotvibecoder
```

As long as your PATH includes your python scripts folder, this will add the CLI to path and you will be able to run it as `robotvibecoder` from a command prompt/terminal.

## Quickstart

Here's a quick guide on usage; in-depth docs can be found in the `docs/` folder

1. Create a config:

A config specifies the name of the mechanism, its java package, and the name of its motors and encoder. A config can be generated interactively with:

```sh
robotvibecoder new -i config.json
```

2. Generate a mechanism using that config:

After the config has been edited to your satisfaction, generate Java IO code from that config:

```sh
robotvibecoder generate --config config.json
```

## License

`robotvibecoder` is distributed under the terms of the [GPL-3.0-only](https://spdx.org/licenses/GPL-3.0-only.html) license.
