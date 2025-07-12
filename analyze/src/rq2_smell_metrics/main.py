import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt

def analyze_and_plot_by_metric(dataframe, target_metric):
    """
    Filters data for a specific metric, categorizes refactorings,
    generates a box plot, and returns a summary DataFrame.

    Args:
        dataframe (pd.DataFrame): The input DataFrame.
        target_metric (str): The name of the metric to analyze.

    Returns:
        pd.DataFrame: A DataFrame containing summary statistics for the metric.
    """
    # Define category mapping
    cat1 = "1. Method"
    cat2 = "2. Class & Hierarchy"
    cat3 = "3. Variable & Attribute"
    cat4 = "4. Modifier & Metadata"
    cat5 = "5. Code Expression & Logic"
    mapping = {
        'ASSERT_THROWS': cat1, 'ASSERT_TIMEOUT': cat1, 'ADD_PARAMETER': cat1, 'REMOVE_PARAMETER': cat1, 'REORDER_PARAMETER': cat1, 'ADD_THROWN_EXCEPTION_TYPE': cat1, 'REMOVE_THROWN_EXCEPTION_TYPE': cat1, 'CHANGE_THROWN_EXCEPTION_TYPE': cat1, 'CHANGE_RETURN_TYPE': cat1, 'EXTRACT_METHOD': cat1, 'EXTRACT_OPERATION': cat1, 'INLINE_METHOD': cat1, 'INLINE_OPERATION': cat1, 'RENAME_METHOD': cat1, 'MERGE_METHOD': cat1, 'MERGE_OPERATION': cat1, 'SPLIT_METHOD': cat1, 'SPLIT_OPERATION': cat1, 'MOVE_METHOD': cat1, 'MOVE_OPERATION': cat1, 'MOVE_AND_RENAME_METHOD': cat1, 'MOVE_AND_RENAME_OPERATION': cat1, 'MOVE_AND_INLINE_METHOD': cat1, 'MOVE_AND_INLINE_OPERATION': cat1, 'PULL_UP_METHOD': cat1, 'PULL_UP_OPERATION': cat1, 'PUSH_DOWN_METHOD': cat1, 'PUSH_DOWN_OPERATION': cat1, 'EXTRACT_AND_MOVE_METHOD': cat1, 'EXTRACT_AND_MOVE_OPERATION': cat1, 'MOVE_CODE': cat1, 'PARAMETERIZE_TEST': cat1,
        'CHANGE_TYPE_DECLARATION_KIND': cat2, 'COLLAPSE_HIERARCHY': cat2, 'EXTRACT_CLASS': cat2, 'EXTRACT_INTERFACE': cat2, 'EXTRACT_SUBCLASS': cat2, 'EXTRACT_SUPERCLASS': cat2, 'MERGE_CLASS': cat2, 'SPLIT_CLASS': cat2, 'MOVE_CLASS': cat2, 'RENAME_CLASS': cat2, 'MOVE_RENAME_CLASS': cat2, 'MOVE_AND_RENAME_CLASS': cat2, 'MERGE_PACKAGE': cat2, 'SPLIT_PACKAGE': cat2, 'MOVE_PACKAGE': cat2, 'RENAME_PACKAGE': cat2, 'MOVE_SOURCE_FOLDER': cat2,
        'CHANGE_ATTRIBUTE_TYPE': cat3, 'CHANGE_PARAMETER_TYPE': cat3, 'CHANGE_VARIABLE_TYPE': cat3, 'ENCAPSULATE_ATTRIBUTE': cat3, 'EXTRACT_ATTRIBUTE': cat3, 'INLINE_ATTRIBUTE': cat3, 'EXTRACT_VARIABLE': cat3, 'INLINE_VARIABLE': cat3, 'LOCALIZE_PARAMETER': cat3, 'MERGE_ATTRIBUTE': cat3, 'MERGE_PARAMETER': cat3, 'MERGE_VARIABLE': cat3, 'MOVE_ATTRIBUTE': cat3, 'MOVE_RENAME_ATTRIBUTE': cat3, 'MOVE_AND_RENAME_ATTRIBUTE': cat3, 'PARAMETERIZE_ATTRIBUTE': cat3, 'PARAMETERIZE_VARIABLE': cat3, 'PULL_UP_ATTRIBUTE': cat3, 'PUSH_DOWN_ATTRIBUTE': cat3, 'RENAME_ATTRIBUTE': cat3, 'RENAME_PARAMETER': cat3, 'RENAME_VARIABLE': cat3, 'REPLACE_ATTRIBUTE': cat3, 'REPLACE_ATTRIBUTE_WITH_VARIABLE': cat3, 'REPLACE_VARIABLE_WITH_ATTRIBUTE': cat3, 'SPLIT_ATTRIBUTE': cat3, 'SPLIT_PARAMETER': cat3, 'SPLIT_VARIABLE': cat3,
        'ADD_ATTRIBUTE_ANNOTATION': cat4, 'REMOVE_ATTRIBUTE_ANNOTATION': cat4, 'MODIFY_ATTRIBUTE_ANNOTATION': cat4, 'ADD_CLASS_ANNOTATION': cat4, 'REMOVE_CLASS_ANNOTATION': cat4, 'MODIFY_CLASS_ANNOTATION': cat4, 'ADD_METHOD_ANNOTATION': cat4, 'REMOVE_METHOD_ANNOTATION': cat4, 'MODIFY_METHOD_ANNOTATION': cat4, 'ADD_PARAMETER_ANNOTATION': cat4, 'REMOVE_PARAMETER_ANNOTATION': cat4, 'MODIFY_PARAMETER_ANNOTATION': cat4, 'ADD_VARIABLE_ANNOTATION': cat4, 'REMOVE_VARIABLE_ANNOTATION': cat4, 'MODIFY_VARIABLE_ANNOTATION': cat4, 'ADD_ATTRIBUTE_MODIFIER': cat4, 'REMOVE_ATTRIBUTE_MODIFIER': cat4, 'ADD_CLASS_MODIFIER': cat4, 'REMOVE_CLASS_MODIFIER': cat4, 'ADD_METHOD_MODIFIER': cat4, 'REMOVE_METHOD_MODIFIER': cat4, 'CHANGE_ATTRIBUTE_ACCESS_MODIFIER': cat4, 'CHANGE_CLASS_ACCESS_MODIFIER': cat4, 'CHANGE_METHOD_ACCESS_MODIFIER': cat4, 'CHANGE_OPERATION_ACCESS_MODIFIER': cat4, 'ADD_PARAMETER_MODIFIER': cat4, 'REMOVE_PARAMETER_MODIFIER': cat4, 'ADD_VARIABLE_MODIFIER': cat4, 'REMOVE_VARIABLE_MODIFIER': cat4,
        'INVERT_CONDITION': cat5, 'MERGE_CATCH': cat5, 'MERGE_CONDITIONAL': cat5, 'REPLACE_ANONYMOUS_WITH_CLASS': cat5, 'REPLACE_ANONYMOUS_WITH_LAMBDA': cat5, 'REPLACE_LOOP_WITH_PIPELINE': cat5, 'REPLACE_PIPELINE_WITH_LOOP': cat5, 'REPLACE_GENERIC_WITH_DIAMOND': cat5, 'REPLACE_CONDITIONAL_WITH_TERNARY': cat5, 'SPLIT_CONDITIONAL': cat5, 'TRY_WITH_RESOURCES': cat5,
    }

    df_metric = dataframe[dataframe['metric'] == target_metric].copy()
    if df_metric.empty:
        print(f"Metric '{target_metric}' not found. Skipping.")
        return None

    df_metric['category'] = df_metric['refactoring_type'].map(mapping).fillna('Unknown')
    df_metric = df_metric[df_metric['refactoring_type'] != 'Overall']

    # --- Plotting ---
    plt.figure(figsize=(15, 8))
    category_order = [cat1, cat2, cat3, cat4, cat5, 'Unknown']
    plot_order = [c for c in category_order if c in df_metric['category'].unique()]
    sns.boxplot(x='category', y='mean', data=df_metric, order=plot_order)
    counts = df_metric['category'].value_counts()
    ax = plt.gca()
    for i, category_name_obj in enumerate(ax.get_xticklabels()):
        category_name = category_name_obj.get_text()
        count = counts.get(category_name, 0)
        ax.text(i, 0.97, f'n={count}', ha='center', va='top', transform=ax.get_xaxis_transform(), color='black', fontsize=11)
    plt.title(f'Distribution of {target_metric} Change by Refactoring Category', fontsize=16)
    plt.xlabel('Refactoring Category', fontsize=12)
    plt.ylabel(f'Change in {target_metric} (mean)', fontsize=12)
    plt.xticks(rotation=15, ha="right")
    plt.axhline(0, color='red', linestyle='--', linewidth=1)
    plt.tight_layout()
    output_filename = f'boxplot_{target_metric}_by_category.png'
    # plt.savefig(output_filename)
    plt.close()
    print(f"Generated plot: {output_filename}")

    # --- Generate Summary Statistics ---
    # The .describe() method calculates most of the needed stats
    summary = df_metric.groupby('category')['mean'].describe()
    return summary

# --- Execution Starts Here ---
if __name__ == "__main__":
    # FIXME: Adjust the file path to your working directory
    file_path = "../result_from_cluster/rq2_summary/statistical_summary_report_overview.csv"

    try:
        df = pd.read_csv(file_path)

        # This list contains all metrics you want to analyze
        metrics_list = [
            "Broken Hierarchy", "Broken Modularization", "Cyclic Hierarchy", "Cyclic-Dependent Modularization",
            "Deep Hierarchy", "Deficient Encapsulation", "Hub-like Modularization", "Imperative Abstraction",
            "Insufficient Modularization", "Missing Hierarchy", "Multifaceted Abstraction", "Multipath Hierarchy",
            "Rebellious Hierarchy", "Unexploited Encapsulation", "Unnecessary Abstraction", "Unutilized Abstraction",
            "Wide Hierarchy", "NOF", "NOPF", "NOM", "NOPM", "LOC", "WMC", "NC", "DIT", "LCOM", "FANIN", "FANOUT"
        ]

        available_metrics = df['metric'].unique()
        print("### Available Metrics in the Dataset ###")
        print(available_metrics)
        print("-" * 40)

        # List to hold summary DataFrames for each metric
        all_metrics_summary = []

        for metric in metrics_list:
            if metric in available_metrics:
                print(f"Analyzing metric: {metric}...")
                # The function now returns the summary data
                summary_df = analyze_and_plot_by_metric(df, metric)

                if summary_df is not None:
                    # Add the metric name as a column to identify the data
                    summary_df['metric'] = metric
                    all_metrics_summary.append(summary_df)
            else:
                print(f"Metric '{metric}' not found in the file, skipping.")

        # --- Combine all summaries and save to a single CSV ---
        if all_metrics_summary:
            # Concatenate all summary DataFrames into one
            final_summary_df = pd.concat(all_metrics_summary)

            # Reset index to make 'category' a column and reorder for clarity
            final_summary_df = final_summary_df.reset_index().rename(columns={'index': 'category'})
            final_summary_df = final_summary_df[['metric', 'category', 'count', 'mean', 'std', 'min', '25%', '50%', '75%', 'max']]

            # Save the combined data to a CSV file
            summary_filename = 'refactoring_analysis_summary.csv'
            final_summary_df.to_csv(summary_filename, index=False)
            print("-" * 40)
            print(f"All analysis results have been combined and saved to '{summary_filename}'")

    except FileNotFoundError:
        print(f"Error: File not found. Please check the path: {file_path}")