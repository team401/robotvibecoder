"""
Utilities for printing warnings and errors and colors
"""

from dataclasses import dataclass
import sys
from typing import TextIO


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
    :type file: _type_, optional
    """
    print(f"{Colors.fg_red}{Colors.bold}Error{Colors.reset}: {message}", file=file)


def print_warning(message: str, file: TextIO = sys.stderr):
    """Print a warning message

    :param message: The warning message.
    :type message: str
    :param file: File to print the warning message to, defaults to sys.stderr
    :type file: _type_, optional
    """
    print(f"{Colors.fg_red}{Colors.bold}Warning{Colors.reset}: {message}", file=file)
