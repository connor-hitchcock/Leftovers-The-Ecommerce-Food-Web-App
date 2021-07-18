"""
    This script checks the tags for a given commit
    Checking if the tag matches the format sprint_X.Y
    Where X is the sprint number
    And Y is the revision number

    It will throw an error and fail the CI build if it does not match that format.
"""

import sys
import re

def exit(error_message):
    """exits the script giving an error this is so that the build can fail"""
    sys.exit(error_message)

class RegexMatcher():
    """Custom matcher for regular expressions"""
    def __init__(self):
        sprint_format_regex = '^sprint_[0-9]+.[0-9]+$'
        special_format_regex = '^special_[A-Za-z0-9]+$'
        self.regex_patterns = [sprint_format_regex, special_format_regex]


    def has_match(self, tag):
        """checks if the tag matches one of the expected patterns: sprint_x.y or special_..."""
        matches = [re.search(regex, tag) for regex in self.regex_patterns]
        has_match = any(matches)
        return has_match


def get_tags_from_args():
    """gets a list of tags passed in via the cli"""
    tags = sys.argv[1:]

    if len(tags) == 0:
        # this shouldn't happen as the yml file should only run when there are tags
        exit('no commit tags provided')

    return tags


def main():
    matcher = RegexMatcher()
    tags = get_tags_from_args()

    for tag in tags:
        if not matcher.has_match(tag):
            error_message = "Your tag '{}' is not in the form of 'sprint_X.Y' or 'special_...', please change your tag".format(
                tag)
            exit(error_message)

if __name__ == "__main__":
    main()
