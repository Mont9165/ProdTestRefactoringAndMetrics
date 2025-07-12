# src/run_analysis.py
import logging
import os
import re
# import pandas as pd
import fireducks.pandas as pd
from datetime import datetime

import numpy as np

from . import config, main_processor, statistical_analyzer, reporter


def setup_logging():
    log_dir = "logs"
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    log_filename = os.path.join(log_dir, f"analysis_log_{timestamp}.log")
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)
    if logger.hasHandlers():
        logger.handlers.clear()
    log_format = config.LOG_FORMAT
    formatter = logging.Formatter(log_format)
    file_handler = logging.FileHandler(log_filename, encoding='utf-8')
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)
    stream_handler = logging.StreamHandler()
    stream_handler.setFormatter(formatter)
    logger.addHandler(stream_handler)
    return logging.getLogger(__name__)


def get_smell_types_from_df(df: pd.DataFrame, suffix_pattern: str) -> list[str]:
    smell_types = set()
    regex_pattern = r'^(.*)_' + re.escape(suffix_pattern) + r'$'
    for col in df.columns:
        match = re.match(regex_pattern, col)
        if match:
            smell_types.add(match.group(1))
    return sorted(list(smell_types))

def format_stats_results(results: dict) -> str:
    if not results or results.get("p_value") is None:
        return "N/A (Analysis skipped or failed)"
    notes = results.get("notes")
    if notes:
        return f"N/A (Notes: {notes})"
    p_val_str = f"p={results['p_value']:.3f}"
    if results['p_value'] < 0.001:
        p_val_str += " ***"
    elif results['p_value'] < 0.01:
        p_val_str += " **"
    elif results['p_value'] < 0.05:
        p_val_str += " *"

    r_val = results.get('effect_size_r', float('nan'))
    d_val = results.get('effect_size_d', float('nan'))

    return (
        f"N={results.get('n_pairs', 'N/A')}, "
        f"W={results.get('statistic', float('nan')):.2f}, {p_val_str}, "
        f"Effect Size: r={r_val:.3f}, d={d_val:.3f}"
    )

def analyze_metric_or_smell(
        results_df: pd.DataFrame,
        item_name: str,
        code_type_filter: str,
        smell_suffix: str | None = None,
        logger=None
) -> list[dict]:
    analysis_results_list = []
    if results_df.empty:
        if logger: logger.info(f"DataFrame for {code_type_filter} code is empty. Skipping analysis for {item_name}.")
        return analysis_results_list

    log_item_type = "Metric"
    if smell_suffix == "smell":
        log_item_type = "Design Smell"
    elif smell_suffix == "impl_smell":
        log_item_type = "Implementation Smell"

    if logger: logger.info(
        f"\n{'=' * 20} Analyzing: {item_name} ({log_item_type}) for {code_type_filter.upper()} CODE {'=' * 20}")

    if smell_suffix:
        before_col = f"{item_name}_{smell_suffix}_before"
        after_col = f"{item_name}_{smell_suffix}_after"
        delta_col = f"{item_name}_{smell_suffix}_delta"
    else:
        before_col = f"{item_name}_before"
        after_col = f"{item_name}_after"
        delta_col = f"{item_name}_delta"

    if logger: logger.info(
        f"\n{'=' * 20} Analyzing: {item_name} ({'Smell' if log_item_type else 'Metric'}) for {code_type_filter.upper()} CODE {'=' * 20}")

    if before_col not in results_df.columns or after_col not in results_df.columns:
        if logger: logger.warning(f"Columns for item '{item_name}' ('{before_col}' or '{after_col}') not found. Skipping.")
        return analysis_results_list

    current_df = results_df.copy()
    current_df[before_col] = pd.to_numeric(current_df[before_col], errors='coerce').fillna(0)
    current_df[after_col] = pd.to_numeric(current_df[after_col], errors='coerce').fillna(0)

    if before_col in current_df.columns and after_col in current_df.columns:
        current_df[delta_col] = current_df[after_col] - current_df[before_col]
    else:
        if logger: logger.warning(f"Cannot create delta column for {item_name} as before/after columns are missing.")

    desc_stats = statistical_analyzer.calculate_descriptive_stats_delta(current_df, delta_col)

    if logger: logger.info(f"\n--- Paired Analysis for {item_name} ({code_type_filter.upper()} Overall) ---")
    overall_test_results = statistical_analyzer.perform_paired_analysis(
        current_df, before_col, after_col
    )
    if logger: logger.info(f"Overall change: {format_stats_results(overall_test_results)}")
    if overall_test_results and overall_test_results.get("p_value") is not None:
        analysis_results_list.append({
            "code_type": code_type_filter,
            "metric": item_name,
            "refactoring_type": "Overall",
            **overall_test_results,
            **desc_stats
        })

    if 'main_refactoring_name' not in current_df.columns and 'refactoring_names' in current_df.columns: # cite: src/run_analysis.py
        current_df['main_refactoring_name'] = current_df['refactoring_names'].apply(
            lambda x: x[0] if (isinstance(x, list) or isinstance(x, np.ndarray)) and len(x) > 0 else 'Unknown'
        )

    if 'main_refactoring_name' in current_df.columns:
        for ref_type, group in current_df.groupby('main_refactoring_name'):
            if logger: logger.info(f"\n--- Paired Analysis for {item_name} ({code_type_filter.upper()} - RefType: {ref_type}) ---")

            if group.empty:
                if logger: logger.info(f"Group for RefType '{ref_type}' is empty. Skipping.")
                continue

            ref_type_test_results = statistical_analyzer.perform_paired_analysis(
                group, before_col, after_col
            )
            desc_stats = statistical_analyzer.calculate_descriptive_stats_delta(group, delta_col)

            if logger: logger.info(f"{ref_type} change: {format_stats_results(ref_type_test_results)}")
            if ref_type_test_results and ref_type_test_results.get("p_value") is not None: # p_value が None でないことを確認
                analysis_results_list.append({
                    "code_type": code_type_filter,
                    "metric": item_name,
                    "refactoring_type": ref_type,
                    **ref_type_test_results,
                    **desc_stats
                })

    else:
        if logger: logger.warning("Column 'main_refactoring_name' not found for grouped analysis.")

    plot_output_dir = os.path.join("analysis_plots", code_type_filter)
    if delta_col in current_df.columns and 'main_refactoring_name' in current_df.columns:
        reporter.plot_metric_delta_boxplot(current_df, delta_col, 'main_refactoring_name', output_dir=plot_output_dir) # logger=logger

    return analysis_results_list


def main():
    logger = setup_logging()

    logger.info("=================================================")
    logger.info("Starting Code Quality Refactoring Impact Analysis")
    logger.info("=================================================")

    results_df_all = None

    if not os.path.exists(config.PICKLE_FILEPATH):
        logger.error(f"Processed data file not found: '{config.PICKLE_FILEPATH}'")
        logger.error("Please run the data preprocessing script first to generate this file.")
        return

    logger.info(f"Loading preprocessed data from '{config.PICKLE_FILEPATH}'...")
    try:
        # results_df_all = pd.read_pickle(config.PICKLE_FILEPATH) # pickle
        results_df_all = pd.read_feather(config.PICKLE_FILEPATH) # Feather
        # results_df_all = pd.read_parquet(processed_data_filepath) # Parquet
        logger.info(f"Successfully loaded preprocessed data. Shape: {results_df_all.shape}")
    except Exception as e:
        logger.error(f"Failed to load preprocessed data from '{config.PICKLE_FILEPATH}': {e}", exc_info=True)
        return

    if results_df_all.empty:
        logger.warning("Preprocessed data is empty. Exiting.")
        return

    all_statistical_summaries = []

    production_df = results_df_all[results_df_all['code_type'] == 'production'].copy()
    test_df = results_df_all[results_df_all['code_type'] == 'test'].copy()

    logger.info(f"Total production code changes: {len(production_df)}")
    logger.info(f"Total test code changes: {len(test_df)}")

    type_metrics_to_analyze = config.TYPE_METRIC_COLUMNS_TO_ANALYZE
    design_smell_types_to_analyze = get_smell_types_from_df(results_df_all, "smell_before")
    implementation_smell_types_to_analyze = get_smell_types_from_df(results_df_all, "impl_smell_before")

    if not design_smell_types_to_analyze:
        logger.info("No design code smell columns found.")
    else:
        logger.info(f"Found design code smell types: {design_smell_types_to_analyze}")

    if not implementation_smell_types_to_analyze:
        logger.info("No implementation code smell columns found.")
    else:
        logger.info(f"Found implementation code smell types: {implementation_smell_types_to_analyze}")

    analysis_sets = []
    if not production_df.empty:
        analysis_sets.append({"name": "PRODUCTION", "df": production_df})
    else:
        logger.warning("Production DataFrame is empty, skipping analysis for production code.")

    if not test_df.empty:
        analysis_sets.append({"name": "TEST", "df": test_df})
    else:
        logger.warning("Test DataFrame is empty, skipping analysis for test code.")

    for analysis_set in analysis_sets:
        current_set_name = analysis_set["name"]
        current_results_df = analysis_set["df"]
        logger.info(f"\n\n{'#' * 20} STARTING ANALYSIS FOR: {current_set_name} CODE {'#' * 20}")

        for metric_name in type_metrics_to_analyze:
            metric_results = analyze_metric_or_smell(current_results_df, metric_name, current_set_name.lower(),
                                                     smell_suffix=None, logger=logger)
            all_statistical_summaries.extend(metric_results)

        for smell_name in design_smell_types_to_analyze:
            smell_results = analyze_metric_or_smell(current_results_df, smell_name, current_set_name.lower(),
                                                    smell_suffix="smell", logger=logger)
            all_statistical_summaries.extend(smell_results)

        for smell_name in implementation_smell_types_to_analyze:
            item_name_for_analysis = f"{smell_name}_impl"
            smell_results = analyze_metric_or_smell(current_results_df, item_name_for_analysis,
                                                    current_set_name.lower(),
                                                    smell_suffix="impl_smell", logger=logger)
            all_statistical_summaries.extend(smell_results)

    if all_statistical_summaries:
        summary_df = pd.DataFrame(all_statistical_summaries)
        summary_df = summary_df.round(3)
        if 'notes' in summary_df.columns:
            summary_df.drop(columns=['notes'], inplace=True)

        logger.info("\n\n" + "=" * 30 + " COMPREHENSIVE STATISTICAL SUMMARY " + "=" * 30)
        with pd.option_context('display.max_rows', None, 'display.max_columns', None, 'display.width', 200):
            logger.info(f"\n{summary_df.to_string()}")

        summary_filename = "statistical_summary_report_overview.csv"
        summary_df.to_csv(summary_filename, index=False)
        logger.info(f"\nComprehensive statistical summary saved to '{summary_filename}'")
    else:
        logger.info("No statistical analysis was performed or results to summarize.")

    logger.info("\n" + "=" * 30 + " Analysis Finished " + "=" * 30)
    logger.info(f"Full log has been saved to the '{os.path.join(os.getcwd(), 'logs')}' directory.")


if __name__ == "__main__":
    main()