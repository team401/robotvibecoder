# CLI Usage

The robotvibecoder CLI provides two subcommands. Each of them have their own docs page:

- `new`: Generate a new config. It can be seen in more depth on the [config docs](config.md)
- `generate`: Generate mechanism code based on a config. See the [generate docs](generate.md)

## Specifying Output Folder

By default, the commands will generate the file in the current working directory. You can specify a different folder with `--folder [folder path]` (or `-f [folder path]`)

This argument must come before the subcommand, as shown:

```sh
robotvibecoder -f examples generate -c wristconfig.json
```
