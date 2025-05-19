"""
Utilities for printing warnings and errors and colors
"""

from dataclasses import dataclass
import sys
from typing import TextIO, List
import pick


@dataclass
class Colors:
    """
    ANSI Escape Codes for text colors and effects
    """

    fg_red = "\x1b[31m"
    fg_green = "\x1b[32m"
    fg_cyan = "\x1b[36m"
    bold = "\x1b[1m"
    reset = "\x1b[0m"

    title_str = f"{fg_cyan}RobotVibeCoder{reset}"


def print_err(message: str, file: TextIO = sys.stderr):
    """Print an error message

    :param message: The error message.
    :type message: str
    :param file: File to print the error message to, defaults to sys.stderr
    :type file: TextIO, optional
    """
    print(f"{Colors.fg_red}{Colors.bold}Error{Colors.reset}: {message}", file=file)


def print_warning(message: str, file: TextIO = sys.stderr):
    """Print a warning message

    :param message: The warning message.
    :type message: str
    :param file: File to print the warning message to, defaults to sys.stderr
    :type file: TextIO, optional
    """
    print(f"{Colors.fg_red}{Colors.bold}Warning{Colors.reset}: {message}", file=file)


def rvc_pick(options: List[str], title: str, spoof_input: bool = True) -> str:
    """Make the user select an option from options using arrow keys/enter

    This is a custom wrapper around the pick library.

    :param options: The options to select from
    :type options: list[str]
    :param title: The title message to print above the picker
    :type title: str
    :param spoof_input: Print a prompt with the selected option? Defaults to true.
    :type spoof_input: bool, optional
    """

    selection, _ = pick.pick(options, title)
    assert isinstance(
        selection, str
    ), """Pick selection wasn't a string. This is a robotvibecoder issue!
    Please report this on github: https://github.com/team401/robotvibecoder"""

    if spoof_input:
        print(f"{title}\n> {selection}")

    return selection
