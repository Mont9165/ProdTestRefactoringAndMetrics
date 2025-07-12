# src/main_processor.py
# import pandas as pd
import fireducks.pandas as pd
import os
import logging
from . import config, data_loader, file_utils

logger = logging.getLogger(__name__)


def process_refactorings() -> pd.DataFrame | None:
    logger.info("Starting refactoring processing pipeline...")

    valid_projects_set = data_loader.load_projects_set(config.PROJECTS_TXT_PATH)  # cite: src/main_processor.py
    if not valid_projects_set:
        logger.error("No valid projects loaded from projects.txt. Aborting.")  # cite: src/main_processor.py
        return None

    ref_df = data_loader.load_refactoring_details(
        config.REFACTORING_DETAILS_CSV_PATH,
        valid_projects_set
    )  # cite: src/data_loader.py

    if ref_df is None:
        logger.error("Failed to load refactoring details. Aborting.")  # cite: src/main_processor.py
        return None

    if ref_df.empty:
        logger.warning("Refactoring details DataFrame is empty after project filtering. No data to process.")
        return pd.DataFrame()

    logger.debug(
        f"DEBUG: DataFrame columns are: {ref_df.columns.tolist()}")

    all_intermediate_data = []

    logger.info(f"Processing {len(ref_df)} rows from filtered refactoring details...")
    for index, row in ref_df.iterrows():  # cite: src/main_processor.py
        project_identifier_csv = row.get("project_name")
        if project_identifier_csv is None:
            logger.warning(f"Row {index}: Column 'project_name' not found. Skipping.")  # cite: src/main_processor.py
            continue

        project_identifier_csv_str = str(project_identifier_csv).strip()
        if not project_identifier_csv_str:
            logger.warning(f"Row {index}: 'project_name' is empty. Skipping.")
            continue

        owner, repo = file_utils.get_owner_repo_from_project_identifier(
            project_identifier_csv_str,
            valid_projects_set
        )
        if not owner or not repo:
            logger.warning(
                f"Row {index}: Skipping due to invalid or unidentifiable project_identifier "
                f"'{project_identifier_csv_str}' (should have been filtered by data_loader)."
            )
            continue

        left_file_path_str = str(row.get("left_file_path", "")).strip()
        right_file_path_str = str(row.get("right_file_path", "")).strip()

        left_pkg, left_type = file_utils.extract_class_details(left_file_path_str)
        right_pkg, right_type = file_utils.extract_class_details(right_file_path_str)

        if left_type is None:
            logger.warning(
                f"Chunk {index}, original index {index}: Skipping row for project '{owner}/{repo}' as left_file_path "
                f"('{left_file_path_str}') could not be parsed."
            )
            continue

        is_left_test = file_utils.is_test_file(left_file_path_str)
        code_type = "test" if is_left_test else "production"

        parent_commit_id_val = str(row["parent_commit_id"]).strip()
        refactoring_commit_id_val = str(row["refactoring_commit_id"]).strip()

        all_intermediate_data.append({
            "original_index": index,
            "project_identifier_csv": project_identifier_csv_str,
            "owner": owner,
            "repo": repo,
            "parent_commit_id": parent_commit_id_val,
            "refactoring_commit_id": refactoring_commit_id_val,
            "refactoring_name": row["refactoring_name"],
            "refactoring_hash": row["refactoring_hash"],
            "left_pkg": left_pkg,
            "left_type": left_type,
            "right_pkg": right_pkg,
            "right_type": right_type,
            "code_type": code_type,
            "left_file_path": left_file_path_str,
        })

    if not all_intermediate_data:
        logger.warning("No processable entries after processing all chunks from refactoring details.")
        return pd.DataFrame()

    logger.info(f"Converting {len(all_intermediate_data)} processed entries to DataFrame...")
    processed_df = pd.DataFrame(all_intermediate_data)
    logger.info(f"Initial processed_df shape: {processed_df.shape}")

    all_class_level_changes = []
    group_cols = [
        "owner", "repo",
        "parent_commit_id", "refactoring_commit_id",
        "left_pkg", "left_type",
        "right_pkg", "right_type"
    ]

    filler_val = "_NA_GRP_KEY_"
    temp_group_df = processed_df.copy()
    for col in ["left_pkg", "left_type", "right_pkg", "right_type"]:
        if col in temp_group_df.columns:
            temp_group_df[col] = temp_group_df[col].fillna(filler_val)
        else:
            logger.warning(f"Grouping column '{col}' not found in processed_df. Skipping fillna for it.")

    logger.info(f"Grouping by class transitions...")
    grouped = temp_group_df.groupby(group_cols, dropna=False)
    logger.info(f"Processing {len(grouped)} unique class transitions...")

    for name_tuple, group_df_from_temp in grouped:
        original_group_indices = group_df_from_temp.index
        group_df = processed_df.loc[original_group_indices]

        current_owner = group_df["owner"].iloc[0]
        current_repo = group_df["repo"].iloc[0]
        current_parent_commit_id = group_df["parent_commit_id"].iloc[0]
        current_ref_commit_id = group_df["refactoring_commit_id"].iloc[0]

        current_left_pkg = group_df["left_pkg"].iloc[0]
        current_left_type = group_df["left_type"].iloc[0]
        current_right_pkg = group_df["right_pkg"].iloc[0]
        current_right_type = group_df["right_type"].iloc[0]
        current_code_type = group_df["code_type"].iloc[0]

        ref_names_in_group = list(group_df["refactoring_name"].unique())
        ref_hashes_in_group = list(group_df["refactoring_hash"].unique())

        parent_metrics_dir = os.path.join(config.OUTPUTS_DIR, current_owner, current_repo, current_parent_commit_id)
        ref_metrics_dir = os.path.join(config.OUTPUTS_DIR, current_owner, current_repo, current_ref_commit_id)
        project_repo_name_for_csv = current_repo

        metrics_before = data_loader.load_class_metrics(
            parent_metrics_dir, project_repo_name_for_csv, current_left_pkg, current_left_type
        )
        if metrics_before is None:
            logger.warning(
                f"Could not load 'before' metrics for class transition: "
                f"{current_left_pkg}.{current_left_type} in {current_owner}/{current_repo} commit {current_parent_commit_id}. Skipping this transition."
            )
            continue

        smells_before = data_loader.load_class_smells(
            parent_metrics_dir, project_repo_name_for_csv, current_left_pkg, current_left_type
        )
        impl_smells_before = data_loader.load_implementation_smells(
            parent_metrics_dir, project_repo_name_for_csv, current_left_pkg, current_left_type
        )

        if smells_before is None: smells_before = {}
        if impl_smells_before is None: impl_smells_before = {}


        metrics_after, smells_after, impl_smells_after = {}, {}, {}

        if current_right_type is not None:
            metrics_after = data_loader.load_class_metrics(
                ref_metrics_dir, project_repo_name_for_csv, current_right_pkg, current_right_type
            )
            smells_after = data_loader.load_class_smells(
                ref_metrics_dir, project_repo_name_for_csv, current_right_pkg, current_right_type
            )
            impl_smells_after = data_loader.load_implementation_smells(
                ref_metrics_dir, project_repo_name_for_csv, current_right_pkg, current_right_type
            )

        if metrics_after is None: metrics_after = {}
        if smells_after is None: smells_after = {}
        if impl_smells_after is None: impl_smells_after = {}

        change_record = {
            "project_identifier_csv": group_df["project_identifier_csv"].iloc[0],
            "owner": current_owner,
            "repo": current_repo,
            "parent_commit_id": current_parent_commit_id,
            "refactoring_commit_id": current_ref_commit_id,
            "refactoring_names": ref_names_in_group,
            "refactoring_hashes": ref_hashes_in_group,
            "left_package_name": current_left_pkg,
            "left_type_name": current_left_type,
            "right_package_name": current_right_pkg,
            "right_type_name": current_right_type,
            "code_type": current_code_type,
            "left_file_path_debug": group_df["left_file_path"].iloc[0]
        }

        for k, v in metrics_before.items(): change_record[f"{k}_before"] = v
        for k, v in smells_before.items(): change_record[f"{k}_smell_before"] = v
        for k, v in impl_smells_before.items(): change_record[f"{k}_impl_smell_before"] = v

        for k, v in metrics_after.items(): change_record[f"{k}_after"] = v
        for k, v in smells_after.items(): change_record[f"{k}_smell_after"] = v
        for k, v in impl_smells_after.items(): change_record[f"{k}_impl_smell_after"] = v

        all_class_level_changes.append(change_record)

    if not all_class_level_changes:
        logger.warning("No class level changes were processed after grouping and metric loading.")
        return pd.DataFrame()

    results_df = pd.DataFrame(all_class_level_changes)
    logger.info(f"Successfully processed {len(results_df)} class level changes into DataFrame.")
    if not results_df.empty:
        logger.info(f"Final DataFrame shape: {results_df.shape}")
        logger.info(f"Code type distribution:\n{results_df['code_type'].value_counts(dropna=False)}")

    logger.info("Refactoring processing pipeline finished.")
    return results_df