# rq2_types/main.py

import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

def create_refactoring_comparison_graph(csv_path, output_image_path, output_csv_path, top_n=None):
    plt.style.use('seaborn-v0_8-whitegrid')
    plt.rcParams['font.family'] = 'sans-serif'

    try:
        df = pd.read_csv(csv_path)
    except FileNotFoundError:
        print(f"Error: missing the data file '{csv_path}'")
        return

    df['code_type'] = np.where(
        df['left_file_path'].str.contains('test', case=False, na=False),
        'Test Code',
        'Production Code'
    )

    df = df.drop_duplicates(subset=['refactoring_hash']).copy()

    counts = df.groupby(['refactoring_name', 'code_type']).size().unstack(fill_value=0)
    total_prod = counts['Production Code'].sum()
    total_test = counts['Test Code'].sum()

    if total_prod == 0 and total_test == 0:
        print("Error")
        return

    if total_prod > 0:
        counts['Production Percentage'] = (counts['Production Code'] / total_prod) * 100
    else:
        counts['Production Percentage'] = 0

    if total_test > 0:
        counts['Test Percentage'] = (counts['Test Code'] / total_test) * 100
    else:
        counts['Test Percentage'] = 0

    counts_sorted = counts.sort_values(by='Production Percentage', ascending=False)

    if top_n is not None and len(counts_sorted) > top_n:
        counts_to_plot = counts_sorted.head(top_n)
        plot_title = f'Top {top_n} Refactoring Types in Production vs. Test Code'
    else:
        counts_to_plot = counts_sorted
        plot_title = 'Comparison of Refactoring Types in Production vs. Test Code'

    counts_to_plot = counts_to_plot.iloc[::-1]

    try:
        counts_sorted.to_csv(output_csv_path)
        print(f"Success '{output_csv_path}'")
    except Exception as e:
        print(f"Fail to save: {output_csv_path}, Error: {e}")

    refactoring_types = counts_to_plot.index
    prod_percentages = counts_to_plot['Production Percentage']
    test_percentages = counts_to_plot['Test Code']

    y_pos = np.arange(len(refactoring_types))
    bar_height = 0.4

    fig_height = max(6, len(refactoring_types) * 0.4)
    fig, ax = plt.subplots(figsize=(10, fig_height))

    bar_prod = ax.barh(y_pos - bar_height / 2, prod_percentages, height=bar_height,
                       label='Production Code', color='lightcoral', align='center')
    bar_test = ax.barh(y_pos + bar_height / 2, test_percentages, height=bar_height,
                       label='Test Code', color='cornflowerblue', align='center')

    ax.bar_label(bar_prod, fmt='%.2f%%', padding=3, color='dimgray', fontsize=8)
    ax.bar_label(bar_test, fmt='%.2f%%', padding=3, color='dimgray', fontsize=8)

    ax.set_yticks(y_pos)
    ax.set_yticklabels(refactoring_types, fontsize=10)
    ax.set_xlabel('Percentage [%]', fontsize=11)
    ax.set_title(plot_title, pad=20, fontsize=14, weight='bold')
    ax.legend(fontsize=10)

    ax.set_xlim(right=ax.get_xlim()[1] * 1.15)

    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    ax.spines['left'].set_visible(True)
    ax.spines['bottom'].set_visible(True)

    plt.tight_layout(pad=1.5)

    plt.savefig(output_image_path, dpi=300, bbox_inches='tight')


if __name__ == '__main__':
    INPUT_CSV = '../data/refactorings_output.csv'
    OUTPUT_IMAGE = 'refactoring_types_comparison_v2.png'
    OUTPUT_CSV = 'refactoring_types_summary.csv'

    TOP_N_TO_SHOW = 40
    create_refactoring_comparison_graph(INPUT_CSV, OUTPUT_IMAGE, OUTPUT_CSV, top_n=TOP_N_TO_SHOW)