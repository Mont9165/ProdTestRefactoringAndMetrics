# src/data_loader.py
# import pandas as pd
import fireducks.pandas as pd
import os
import logging
from . import config, file_utils

logger = logging.getLogger(__name__)

def load_projects_set(filepath: str) -> set[tuple[str, str]]:
    projects_set = set()
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith('#'):
                    continue
                parts = line.split('/', 1)
                if len(parts) == 2:
                    owner = parts[0].strip()
                    repo = parts[1].strip()
                    if owner and repo:
                        projects_set.add((owner, repo))
                    else:
                        logger.warning(f"Invalid owner/repo format in projects.txt line: '{line}'")
                else:
                    logger.warning(f"Invalid line format in projects.txt: '{line}' - expected 'owner/repo'")
        logger.info(f"Loaded {len(projects_set)} unique project owner/repo pairs from {filepath}")
    except FileNotFoundError:
        logger.error(f"Projects file not found: {filepath}")
    except Exception as e:
        logger.error(f"Error loading projects set from {filepath}: {e}")
    return projects_set


def load_refactoring_details(
    filepath: str,
    valid_projects_set: set[tuple[str, str]]
) -> pd.DataFrame | None:
    try:
        logger.info(f"Loading and filtering refactoring details from {filepath} using fireducks.pandas...")

        df = pd.read_csv(filepath)

        logger.info(f"Loaded refactoring details from {filepath}, shape before filtering: {df.shape}")
        if "project_name" not in df.columns:
            logger.error(f"Column 'project_name' not found in {filepath}. Cannot filter by project. Returning raw DataFrame.")
            return df

        def is_valid_project(project_identifier_csv: str) -> bool:
            if not isinstance(project_identifier_csv, str) or not project_identifier_csv.strip():
                return False
            owner, repo = file_utils.get_owner_repo_from_project_identifier(
                project_identifier_csv.strip(),
                valid_projects_set
            )
            return owner is not None and repo is not None

        logger.info("Filtering DataFrame by valid projects...")
        results_df = df[df["project_name"].apply(is_valid_project)].copy()

        logger.info(f"Successfully loaded and filtered refactoring details. Shape after filtering: {results_df.shape}")
        if results_df.empty:
            logger.warning(f"No valid projects found in {filepath} after filtering, or the file was empty.")
        else:
            logger.debug(f"First 5 rows of filtered refactorings_output.csv:\n{results_df.head().to_string()}")

        return results_df

    except FileNotFoundError:
        logger.error(f"Refactoring details file not found: {filepath}")
        return None
    except Exception as e:
        logger.error(f"Error loading or filtering refactoring details from {filepath}: {e}", exc_info=True)
        return None


def load_class_metrics(
        metrics_dir_path: str,
        project_repo_name: str,
        package_name: str,
        type_name: str
) -> dict | None:

    filepath = os.path.join(metrics_dir_path, config.TYPE_METRICS_FILENAME)
    try:
        df = pd.read_csv(filepath)
        class_data = df[
            (df["Project Name"] == project_repo_name) &
            (df["Package Name"] == package_name) &
            (df["Type Name"] == type_name)
            ]
        if not class_data.empty:
            metrics_dict = class_data.iloc[0].drop(
                ["Project Name", "Package Name", "Type Name"], errors='ignore'
            ).to_dict()
            logger.debug(f"Metrics loaded for {project_repo_name} - {package_name}.{type_name} from {filepath}")
            return metrics_dict
        else:
            logger.warning(f"Class {project_repo_name} - {package_name}.{type_name} not found in {filepath}")
            return None
    except FileNotFoundError:
        logger.warning(f"Metrics file not found: {filepath} (for class {project_repo_name} - {package_name}.{type_name})")
        return None
    except Exception as e:
        logger.error(f"Error loading metrics for {project_repo_name} - {package_name}.{type_name} from {filepath}: {e}")
        return None


def load_class_smells(
        metrics_dir_path: str,
        project_repo_name: str,
        package_name: str,
        type_name: str
) -> dict | None:

    filepath = os.path.join(metrics_dir_path, config.DESIGN_SMELLS_FILENAME)
    try:
        df = pd.read_csv(filepath)
        class_smells_df = df[
            (df["Project Name"] == project_repo_name) &
            (df["Package Name"] == package_name) &
            (df["Type Name"] == type_name)
            ]

        if not class_smells_df.empty:
            smell_counts = class_smells_df["Code Smell"].value_counts().to_dict()
            logger.debug(f"Smells loaded for {project_repo_name} - {package_name}.{type_name} from {filepath}: {smell_counts}")
            return smell_counts
        else:
            logger.debug(f"No smells recorded for class {project_repo_name} - {package_name}.{type_name} in {filepath}")
            return {}

    except FileNotFoundError:
        logger.warning(f"Smells file not found: {filepath} (for class {project_repo_name} - {package_name}.{type_name})")
        return None
    except Exception as e:
        logger.error(f"Error loading smells for {project_repo_name} - {package_name}.{type_name} from {filepath}: {e}")
        return None


def load_implementation_smells(
        metrics_dir_path: str,
        project_repo_name: str,
        package_name: str,
        type_name: str
) -> dict | None:

    filepath = os.path.join(metrics_dir_path, config.IMPLEMENTATION_SMELLS_FILENAME)
    try:
        df = pd.read_csv(filepath)
        class_smells_df = df[
            (df["Project Name"] == project_repo_name) &
            (df["Package Name"] == package_name) &
            (df["Type Name"] == type_name)
            ]

        if not class_smells_df.empty:
            smell_counts = class_smells_df["Code Smell"].value_counts().to_dict()
            logger.debug(
                f"Implementation smells loaded for {project_repo_name} - {package_name}.{type_name} from {filepath}: {smell_counts}")
            return smell_counts
        else:
            logger.debug(
                f"No implementation smells recorded for class {project_repo_name} - {package_name}.{type_name} in {filepath}")
            return {}

    except FileNotFoundError:
        logger.warning(
            f"Implementation smells file not found: {filepath} (for class {project_repo_name} - {package_name}.{type_name})")
        return None
    except Exception as e:
        logger.error(
            f"Error loading implementation smells for {project_repo_name} - {package_name}.{type_name} from {filepath}: {e}",
            exc_info=True)
        return None