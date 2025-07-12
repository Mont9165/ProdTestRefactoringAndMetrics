import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt

# Load the CSV file
csv_file = '/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/src/main/java/jp/ac/kyushu/ait/posl/analysis/data/rq1_type/RQ1_refactoring_type.csv'
df = pd.read_csv(csv_file)

# Print the column names to debug
print(df.columns)

# Exclude the 'SUM' row
df = df[df['Refactoring Type'] != 'SUM']

# Extract the relevant columns (adjusted column names)
columns_to_plot = [
    'Perccentage_prod',  # Adjusted column name for "Production"
    'parcentage_test'    # Adjusted column name for "Test"
]

# Calculate the sum of the percentages for each refactoring type
df['Total'] = df[columns_to_plot].sum(axis=1)

# Get the top 35 refactoring types based on the total percentage
top_40_df = df.nlargest(40, 'Total')

# Melt the DataFrame for easier plotting with Seaborn
melted_df = top_40_df.melt(id_vars='Refactoring Type', value_vars=columns_to_plot, var_name='Category', value_name='Percentage')
melted_df['Percentage'] *= 100

# Define a custom color palette with improved brightness
palette = {
    'Perccentage_prod': '#FF6666',  # Light red for Production
    'parcentage_test': '#66B2FF'   # Light blue for Test
}

# Plot the data using Seaborn
plt.figure(figsize=(12, 14))
sns.barplot(data=melted_df, x='Percentage', y='Refactoring Type', hue='Category', palette=palette)

# Add grid lines
plt.grid(True, which='both', linestyle='--', linewidth=0.5)

# Adjust axis thickness
plt.tick_params(axis='both', which='major', width=1)
plt.tick_params(axis='both', which='minor', width=1.5)

plt.xlabel('Percentage [%]', fontsize=16)
plt.xticks(fontsize=16)
plt.yticks(fontsize=16)

plt.ylabel('')  # Hide y-axis label
plt.legend(prop={'size': 16})
handles, labels = plt.gca().get_legend_handles_labels()
labels = ['Production Code', 'Test Code']  # New labels
plt.legend(handles, labels, prop={'size': 16})

# Change the legend title
plt.tight_layout()
plt.savefig('rq1_types_2group.pdf')