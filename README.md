# robotvibecoder

Automatically generate code stubs for mechanisms in FRC Robot Code. This project leverages WPILib, AdvantageKit, and CTRE's Phoenix-6 library to automatically generate boilerplate complete with hardware IO abstraction and simulation. RobotVibeCoder supports 3 mechanism types: elevators, single jointed arms, and flywheels.

---

## Table of Contents

- [robotvibecoder](#robotvibecoder)
  - [Table of Contents](#table-of-contents)
  - [Installation](#installation)
  - [License](#license)

## Installation

This project isn't done enough to be published, so just install it locally:

```console
git clone https://github.com/aidnem/robotvibecoder
cd robotvibecoder
pip install -e .
```

## Quickstart

Here's a quick guide on usage; in-depth docs can be found in the `docs/` folder

1. Create a config:

A config specifies the name of the mechanism, it's java package, and the name of its motors and encoder. A config can be generated interactively with:

```sh
robotvibecoder new -i config.json
```

2. Generate a mechanism using that config:

After the config has been editted to your satisfaction, generate Java IO code from that config:

```sh
robotvibecoder generate --config config.json
```

## License

`robotvibecoder-aidnem` is distributed under the terms of the [GPL-3.0-only](https://spdx.org/licenses/GPL-3.0-only.html) license.
