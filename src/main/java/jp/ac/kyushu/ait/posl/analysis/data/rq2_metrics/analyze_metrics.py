import pandas as pd
import json
import matplotlib.pyplot as plt
from io import StringIO

from scipy.stats import ttest_ind


# FIXME: Adjust the PWD variable to your working directory
PWD = "/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/src/main/java/jp/ac/kyushu/ait/posl/analysis/data/rq2_metrics_all/data"

def load_and_clean_csv(csv_file):
    with open(csv_file, "r") as file:
        lines = file.readlines()

    if not lines[-1].endswith("\n") or lines[-1].count("{") != lines[-1].count("}"):
        print("Skipping incomplete last line.")
        lines = lines[:-1]

    cleaned_data = "\n".join(lines)
    return pd.read_csv(StringIO(cleaned_data))


def convert_columns_to_json(df, columns):
    for col in columns:
        def safe_json_load(x):
            if pd.isna(x):
                return {}
            if isinstance(x, dict):
                return x
            try:
                json_string = x.replace("'", '"')
                return json.loads(json_string)
            except json.JSONDecodeError as e:
                print(f"Skipping malformed entry in column {col}: {x}\nError: {e}")
                return {}

        df[col] = df[col].apply(safe_json_load)
    return df


def preprocess_json_columns(df, columns):
    for col in columns:
        df[col] = df[col].apply(lambda x: x.replace("nan", "null") if isinstance(x, str) else x)
        df[col] = df[col].apply(lambda x: x.replace("None", "null") if isinstance(x, str) else x)
    return df


def validate_json_column(df, column_name):
    invalid_entries = []
    for idx, value in enumerate(df[column_name]):
        if not isinstance(value, (dict, str)):
            invalid_entries.append((idx, value, "Unexpected data type"))
        else:
            try:
                if isinstance(value, str):
                    json.loads(value)
            except Exception as e:
                invalid_entries.append((idx, value, str(e)))
    return invalid_entries


def calculate_statistics(df, column_name):
    if df[column_name].notna().any():
        return pd.DataFrame(df[column_name].tolist()).describe()
    else:
        print(f"No valid data in column {column_name} for statistics.")
        return pd.DataFrame()


def get_top_metrics(stats, top_n=10):
    if not stats.empty:
        return stats.loc['mean'].sort_values(ascending=False).head(top_n)
    else:
        return pd.Series(dtype='float64')


def plot_top_metrics(metric_data, title, ylabel):
    if not metric_data.empty:
        plt.figure(figsize=(10, 6))
        metric_data.plot(kind='bar', color='skyblue')
        plt.title(title)
        plt.ylabel(ylabel)
        plt.xlabel("Metrics")
        plt.xticks(rotation=45, ha='right')
        plt.tight_layout()
        plt.show()
    else:
        print(f"No data available to plot: {title}")


def add_type_column_based_on_path(df):
    def determine_type(row):
        if '/test' in str(row['left_filepath']).lower() or '/test' in str(row['right_filepath']).lower():
            return 'Test'
        else:
            return 'Production'

    df['Type'] = df.apply(determine_type, axis=1)
    return df


def calculate_p_values(df, column_name):
    test_data = df[df['Type'] == 'Test']
    production_data = df[df['Type'] == 'Production']

    p_values = {}

    test_metrics = pd.DataFrame(test_data[column_name].tolist())
    production_metrics = pd.DataFrame(production_data[column_name].tolist())

    common_columns = test_metrics.columns.intersection(production_metrics.columns)

    for metric in common_columns:
        test_values = test_metrics[metric].dropna()
        production_values = production_metrics[metric].dropna()

        if len(test_values) > 1 and len(production_values) > 1:
            _, p_value = ttest_ind(test_values, production_values, equal_var=False)
            p_values[metric] = p_value
        else:
            p_values[metric] = None

    return pd.Series(p_values, name="p_value")


def analyze_by_type(df, column_name):
    test_data = df[df['Type'] == 'Test']
    production_data = df[df['Type'] == 'Production']

    print("\nAnalyzing Test Data:")
    test_stats = calculate_statistics(test_data, column_name)
    test_stats.to_csv("./code_metrics_data/" + column_name + "_test_stats1.csv")
    print(test_stats)

    print("\nAnalyzing Production Data:")
    production_stats = calculate_statistics(production_data, column_name)
    production_stats.to_csv("./code_metrics_data/" + column_name + "_production_stats1.csv")
    print(production_stats)

    # top_test_metrics = get_top_metrics(test_stats)
    # top_production_metrics = get_top_metrics(production_stats)

    # print("\nTop Test Metrics by Mean Difference:")
    # print(top_test_metrics)
    # print("\nTop Production Metrics by Mean Difference:")
    # print(top_production_metrics)

    # plot_top_metrics(top_test_metrics, "Top Test Metrics by Mean Difference", "Mean Difference")
    # plot_top_metrics(top_production_metrics, "Top Production Metrics by Mean Difference", "Mean Difference")


def main():
    csv_file = f'{PWD}/refactoring_analysis_results_1.csv'

    df = load_and_clean_csv(csv_file)
    print("First 5 rows of the cleaned DataFrame:")
    print(df.head())
    print(df.columns)

    df = preprocess_json_columns(df, ['ck_diff', 'readability_diff'])
    df = convert_columns_to_json(df, ['ck_diff', 'readability_diff'])
    df = add_type_column_based_on_path(df)

    print("Validating readability_diff column...")
    invalid_entries = validate_json_column(df, 'readability_diff')
    if invalid_entries:
        print(f"Found {len(invalid_entries)} invalid entries in readability_diff:")
        for idx, value, error in invalid_entries:
            print(f"Index: {idx}, Value: {value}, Error: {error}")
        return

    print("\n### Analyzing CK Metrics by Type ###")
    analyze_by_type(df, 'ck_diff')

    print("\n### Analyzing Readability Metrics by Type ###")
    analyze_by_type(df, 'readability_diff')

    print(len(df))
    type_counts = df['Type'].value_counts()
    print("\nNumber of rows by Type:")
    print(type_counts)


if __name__ == "__main__":
    main()