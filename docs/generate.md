# Generating a Mechanism

Mechanisms are generated from a config file (see [config.md](config.md)).

To generate a Mechanism, use the `generate` subcommand:

```sh
robotvibecoder generate --config config.json
```

Configs can be supplied one of two ways:

## Using a config file

A config file can be specified after the `-c` or `--config` argument as shown:

```sh
robotvibecoder generate --config config.json
```

## Supplying a config through stdin

A config file can also be passed through stdin using `--stdin`. This can be useful for automatically generating configs without having to make a file (for example, if writing a GUI app to generate configs using this project as a backend)

Here's an example of how to pass a config file, although this would behave the same as the snippet above:

```sh
cat config.json | robotvibecoder generate --stdin
```

**Note**: Because --stdin mode expects only a config file in stdin, the normal warning about files that will be created/overwritten is skipped. Instead, the program will exit if a file exists in any of the paths it would write to. Therefore, it is necessary move/delete all files in conflicting paths before invoking the command.
