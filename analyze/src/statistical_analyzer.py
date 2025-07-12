# src/statistical_analyzer.py
# import pandas as pd
import fireducks.pandas as pd
import numpy as np
import logging
import pingouin as pg

logger = logging.getLogger(__name__)


def perform_paired_analysis(df: pd.DataFrame, metric_col_before: str, metric_col_after: str):
    if metric_col_before not in df.columns or metric_col_after not in df.columns:
        logger.error(
            f"Missing one or both columns for paired analysis: '{metric_col_before}', '{metric_col_after}' in DataFrame with columns {df.columns.tolist()}")
        return {"statistic": None, "p_value": None, "n_pairs": 0, "notes": "Missing columns"}

    df_cleaned = df.copy()
    df_cleaned[metric_col_before] = pd.to_numeric(df_cleaned[metric_col_before], errors='coerce')
    df_cleaned[metric_col_after] = pd.to_numeric(df_cleaned[metric_col_after], errors='coerce')

    valid_pairs = df_cleaned[[metric_col_before, metric_col_after]].dropna(how='any')

    before_values = valid_pairs[metric_col_before]
    after_values = valid_pairs[metric_col_after]

    n_pairs = len(before_values)

    if n_pairs < 2:
        logger.info(  # Changed from warning to info as this is an expected skip
            f"Sample size for Wilcoxon test on '{metric_col_before}' vs '{metric_col_after}' "
            f"is {n_pairs}, too small to perform test. Skipping."
        )
        return {"statistic": None, "p_value": None, "n_pairs": n_pairs, "notes": "Sample size too small for test"}
    elif n_pairs < 5:
        logger.warning(
            f"Sample size for paired test on '{metric_col_before}' vs '{metric_col_after}' is small: {n_pairs}")

    try:
        # pingouin wilcoxon
        before_values_np = np.asarray(before_values)
        after_values_np = np.asarray(after_values)
        differences = after_values_np - before_values_np
        stats_results = pg.wilcoxon(differences, alternative='two-sided')
        # print(f"Wilcoxon test results for '{metric_col_before}' vs '{metric_col_after}':\n{stats_results}")
        if 'W-val' in stats_results.columns:
            stat = stats_results['W-val'].iloc[0]
        else:
            stat = np.nan
            logger.error(
                f"'W-val' column not found in Wilcoxon results for '{metric_col_before}' vs '{metric_col_after}'. Columns are: {stats_results.columns.tolist()}")

        if 'p-val' in stats_results.columns:
            p_value = stats_results['p-val'].iloc[0]
        else:
            p_value = np.nan
            logger.error(
                f"'p-val' column not found in Wilcoxon results for '{metric_col_before}' vs '{metric_col_after}'. Columns are: {stats_results.columns.tolist()}")

        if 'RBC' in stats_results.columns:
            effect_size_r = stats_results['RBC'].iloc[0]
        else:
            effect_size_r = np.nan
            logger.warning(
                f"Column 'RBC' not found in Wilcoxon results for '{metric_col_before}' vs '{metric_col_after}'. Columns are: {stats_results.columns.tolist()}")

        if 'CLES' in stats_results.columns:
            CLES = stats_results['CLES'].iloc[0]
        else:
            CLES = np.nan
            logger.warning(
                f"Column 'CLES' not found in Wilcoxon results for '{metric_col_before}' vs '{metric_col_after}'. Columns are: {stats_results.columns.tolist()}")

        if pd.isna(stat) or pd.isna(p_value):
            logger.error(
                f"Core statistics ('W-val' or 'p-val') missing or failed to parse from Wilcoxon results for '{metric_col_before}' vs '{metric_col_after}'.")
            return {"statistic": None, "p_value": None, "n_pairs": n_pairs, "effect_size_r": effect_size_r,
                    "CLES": CLES,
                    "notes": "Core stats missing/unparsable"}

        logger.info(
            f"Paired test ({metric_col_before} vs {metric_col_after}): "
            f"W={stat:.3f}, p={p_value:.3f}, N={n_pairs}, "
            f"Effect Size r={effect_size_r:.3f}, d={CLES:.3f}")

        return {
            "statistic": stat,
            "p_value": p_value,
            "n_pairs": n_pairs,
            "effect_size_r": effect_size_r,
            "CLES": CLES
        }

    except Exception as e:
        logger.error(
            f"Error during Wilcoxon test processing for '{metric_col_before}' vs '{metric_col_after}' (N={n_pairs}): {e}",
            exc_info=True)
        stats_cols_info = "stats_results not defined or no columns"
        if 'stats_results' in locals() and hasattr(stats_results, 'columns'):
            stats_cols_info = stats_results.columns.tolist()
        logger.error(f"Columns in stats_results at time of exception (if available): {stats_cols_info}")
        return {"statistic": None, "p_value": None, "n_pairs": n_pairs, "effect_size_r": None, "CLES": None,
                "notes": f"Exception: {e}"}

def calculate_descriptive_stats_delta(df: pd.DataFrame, delta_col: str) -> pd.Series | None:
    if delta_col not in df.columns:
        logger.error(f"Delta column '{delta_col}' not found for descriptive statistics.")
        return None

    try:
        desc_stats = df[delta_col].describe()
        logger.info(f"Descriptive statistics for '{delta_col}':\n{desc_stats}")
        return desc_stats
    except Exception as e:
        logger.error(f"Error calculating descriptive statistics for '{delta_col}': {e}")
        return None