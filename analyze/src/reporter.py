# src/reporter.py

# import pandas as pd
import fireducks.pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os
import logging

logger = logging.getLogger(__name__)

def generate_summary_table(results_df: pd.DataFrame, group_by_col: str, metric_deltas: list[str]) -> pd.DataFrame | None:
    if group_by_col not in results_df.columns:
        logger.error(f"Grouping column '{group_by_col}' not found in DataFrame.") # cite: 1
        return None

    summary_list = []
    for delta_col in metric_deltas:
        if delta_col not in results_df.columns:
            logger.warning(f"Delta column '{delta_col}' not found, skipping for summary table.") # cite: 1
            continue

        summary = results_df.groupby(group_by_col)[delta_col].agg(['mean', 'median', 'count']).reset_index() # cite: 1
        summary.rename(columns={
            'mean': f'{delta_col}_mean',
            'median': f'{delta_col}_median',
            'count': f'{delta_col}_N'
        }, inplace=True) # cite: 1
        summary_list.append(summary.set_index(group_by_col)) # cite: 1

    if not summary_list:
        logger.warning("No valid delta columns found for summary table.") # cite: 1
        return None

    final_summary = pd.concat(summary_list, axis=1).reset_index() # cite: 1
    logger.info(f"Generated summary table grouped by '{group_by_col}'.") # cite: 1
    return final_summary


def plot_metric_delta_boxplot(df: pd.DataFrame, delta_col: str, group_by_col: str, output_dir: str = "plots"):
    df = df.to_pandas()
    if delta_col not in df.columns or group_by_col not in df.columns:
        logger.error(f"Required columns ('{delta_col}', '{group_by_col}') not in DataFrame for boxplot.") # cite: 1
        return

    if not os.path.exists(output_dir):
        try:
            os.makedirs(output_dir) # cite: 1
            logger.info(f"Created directory for plots: {output_dir}") # cite: 1
        except Exception as e:
            logger.error(f"Could not create directory {output_dir}: {e}") # cite: 1
            return

    plt.style.use('seaborn-v0_8-whitegrid')

    num_groups = df[group_by_col].nunique()
    fig_width = max(12, num_groups * 0.5)

    plt.figure(figsize=(fig_width, 8))

    ax = sns.boxplot(x=group_by_col, y=delta_col, data=df) # cite: 1
    plt.title(f'Change in {delta_col} by {group_by_col}', fontsize=16) # cite: 1

    ax.set_xticklabels(ax.get_xticklabels(), rotation=90, ha="right", fontsize=10) # cite: 1

    plt.ylabel(delta_col, fontsize=12)
    plt.xlabel(group_by_col, fontsize=12)

    plt.subplots_adjust(bottom=0.4)

    plt.tight_layout()

    plot_filename = os.path.join(output_dir, f"{delta_col}_by_{group_by_col}_boxplot.png") # cite: 1
    try:
        plt.savefig(plot_filename) # cite: 1
        logger.info(f"Saved boxplot to {plot_filename}") # cite: 1
    except Exception as e:
        logger.error(f"Error saving boxplot {plot_filename}: {e}") # cite: 1
    plt.close() # cite: 1