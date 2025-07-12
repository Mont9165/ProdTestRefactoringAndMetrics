import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt

# FIXME: Adjust the CSV file path to your working directory
csv_file = ''

refactoring_col = 'refactoring_name'
prod_perc_col = 'Production Percentage'
test_perc_col = 'Test Percentage'

top_n = 40

output_filename = 'rq1_types_comparison.pdf'


try:
    df = pd.read_csv(csv_file)
except FileNotFoundError:
    print(f"Error: can't find '{csv_file}'")
    exit()

if refactoring_col in df.columns:
    df = df[df[refactoring_col] != 'SUM']
else:
    print(f"'{refactoring_col} ' column not found in the CSV file.")
    exit()

df['Total_Percentage'] = df[prod_perc_col] + df[test_perc_col]

top_df = df.nlargest(top_n, 'Total_Percentage')

columns_to_plot = [prod_perc_col, test_perc_col]
melted_df = top_df.melt(id_vars=refactoring_col, value_vars=columns_to_plot, var_name='Category', value_name='Percentage')


palette = {
    prod_perc_col: '#FF6666',
    test_perc_col: '#66B2FF'
}

plt.figure(figsize=(12, 14))
ax = sns.barplot(data=melted_df, x='Percentage', y=refactoring_col, hue='Category', palette=palette, order=top_df[refactoring_col])

ax.grid(True, which='both', linestyle='--', linewidth=0.5)
ax.tick_params(axis='both', which='major', labelsize=16, width=1.5)
ax.set_xlabel('Percentage [%]', fontsize=16)
ax.set_ylabel('')

handles, _ = ax.get_legend_handles_labels()
ax.legend(handles, ['Production Code', 'Test Code'], prop={'size': 16})

plt.tight_layout()
plt.savefig(output_filename)
