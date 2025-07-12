# src/file_utils.py
import os
import logging
from . import config

logger = logging.getLogger(__name__)

def get_owner_repo_from_project_identifier(
        project_identifier_from_csv: str,
        valid_projects_set: set[tuple[str, str]]
) -> tuple[str | None, str | None]:
    parts = project_identifier_from_csv.split('_', 1)
    if len(parts) == 2:
        parsed_owner, parsed_repo = parts[0], parts[1]
        if (parsed_owner, parsed_repo) in valid_projects_set:
            return parsed_owner, parsed_repo
        else:
            logger.warning(
                f"Parsed owner/repo ('{parsed_owner}', '{parsed_repo}') from "
                f"'{project_identifier_from_csv}' not found in valid_projects_set."
            )
            return None, None
    else:
        logger.warning(
            f"Could not parse owner/repo from project_identifier: '{project_identifier_from_csv}'. "
            "Expected 'owner_repo' format."
        )
        return None, None


# src/file_utils.py


def extract_class_details(file_path: str) -> tuple[str | None, str | None]:
    if not file_path or not file_path.endswith(".java"):
        return None, None

    normalized_path = file_path.replace(os.sep, '/')
    path_no_suffix = normalized_path.removesuffix(".java")

    source_patterns = config.JAVA_SOURCE_PATTERNS + config.TEST_SOURCE_PATTERNS

    relative_path = None
    for pattern in source_patterns:
        index = normalized_path.find(pattern)
        if index != -1:
            start_index = index + len(pattern)
            relative_path = path_no_suffix[start_index:]
            logger.debug(f"Path '{file_path}' matched pattern '{pattern}'. Relative path: '{relative_path}'")
            break

    if relative_path is None:
        logger.warning(f"Path '{file_path}' did not match any source patterns. Cannot determine package structure.")
        return None, None

    fqn = relative_path.replace('/', '.')

    if '.' in fqn:
        package_name = fqn.rpartition('.')[0]
        type_name = fqn.rpartition('.')[2]
    else:
        package_name = ""
        type_name = fqn

    logger.debug(f"Extracted from '{file_path}': Pkg='{package_name}', Type='{type_name}'")
    return package_name, type_name


def is_test_file(file_path: str) -> bool:
    if not file_path:
        return False

    normalized_path = file_path.replace(os.sep, '/')
    filename = os.path.basename(normalized_path)

    for pattern in config.TEST_SOURCE_PATTERNS:
        if pattern in normalized_path:
            logger.debug(f"'{file_path}' matched TEST_SOURCE_PATTERN: '{pattern}'")
            return True

    for suffix in config.TEST_CODE_FILENAME_SUFFIXES:
        if filename.endswith(suffix):
            logger.debug(f"'{file_path}' matched TEST_CODE_FILENAME_SUFFIX: '{suffix}'")
            return True

    for prefix in config.TEST_CODE_FILENAME_PREFIXES:
        if filename.startswith(prefix):
            logger.debug(f"'{file_path}' matched TEST_CODE_FILENAME_PREFIX: '{prefix}'")
            return True

    return False