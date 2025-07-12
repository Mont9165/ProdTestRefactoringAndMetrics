import json
import pandas as pd
from io import StringIO
# 修正: scipy.stats.wilcoxon の代わりに pingouin を使用
import pingouin as pg
import os

# FIXME: Adjust the PWD variable to your working directory
PWD = "/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/src/main/java/jp/ac/kyushu/ait/posl/analysis/data/rq2_metrics/data"

# load_and_clean_csv, add_type_column_based_on_path, preprocess_json_columns, convert_columns_to_json 関数は変更なし
def load_and_clean_csv(csv_file):
    with open(csv_file, "r", encoding="utf-8") as file:
        lines = file.readlines()
    if not lines[-1].strip():
        lines = lines[:-1]
    cleaned_data = "".join(lines)
    df = pd.read_csv(StringIO(cleaned_data))
    before_dropping = len(df)
    df = (
        df.groupby(["commitId", "left_filepath", "right_filepath"], as_index=False)
        .first()
    )
    after_dropping = len(df)
    if before_dropping > after_dropping:
        print(f"Consolidated {before_dropping - after_dropping} duplicate rows.")
    return df

def add_type_column_based_on_path(df):
    def determine_type(row):
        if '/test' in str(row['left_filepath']).lower() or '/test' in str(row['right_filepath']).lower():
            return 'Test'
        else:
            return 'Production'
    df['Type'] = df.apply(determine_type, axis=1)
    return df

def preprocess_json_columns(df, columns):
    for col in columns:
        df[col] = df[col].apply(lambda x: x.replace("nan", "null") if isinstance(x, str) else x)
        df[col] = df[col].apply(lambda x: x.replace("None", "null") if isinstance(x, str) else x)
        df[col] = df[col].apply(lambda x: x.replace("inf", "null") if isinstance(x, str) else x)
    return df

def convert_columns_to_json(df, columns):
    for col in columns:
        def safe_json_load(x):
            if pd.isna(x): return {}
            if isinstance(x, dict): return x
            try:
                json_string = x.replace("'", '"')
                return json.loads(json_string)
            except json.JSONDecodeError:
                return {}
        df[col] = df[col].apply(safe_json_load)
    return df


def calculate_wilcoxon_and_effect_size(df, column_name, output_dir, specific_metrics):
    expanded_data = []
    for index, row in df.iterrows():
        diff_data = row[column_name]
        if not isinstance(diff_data, dict):
            continue
        for metric, value in diff_data.items():
            if pd.notna(value) and isinstance(value, (int, float)):
                expanded_data.append({
                    "Type": row["Type"],
                    "metric": metric,
                    "diff": value,
                })

    if not expanded_data:
        print(f"No valid data found to process for column {column_name}.")
        return {}

    expanded_df = pd.DataFrame(expanded_data)
    results = {}

    for type_group in expanded_df['Type'].unique():
        group_data = expanded_df[expanded_df['Type'] == type_group]
        results[type_group] = {}

        for metric in group_data['metric'].unique():
            if metric not in specific_metrics:
                continue

            metric_data = group_data[group_data['metric'] == metric]['diff'].dropna()

            # ★★★ ここから修正・追加 ★★★

            # 記述統計量の計算
            if not metric_data.empty:
                desc = metric_data.describe()
                n_samples = int(desc.get('count', 0))
                mean_val = desc.get('mean', None)
                std_val = desc.get('std', None)
                min_val = desc.get('min', None)
                q1_val = desc.get('25%', None)
                median_val = desc.get('50%', None)
                q3_val = desc.get('75%', None)
                max_val = desc.get('max', None)

                # 変化の方向の割合
                pos_rate = (metric_data > 0).sum() / n_samples if n_samples > 0 else 0
                neg_rate = (metric_data < 0).sum() / n_samples if n_samples > 0 else 0
                zero_rate = (metric_data == 0).sum() / n_samples if n_samples > 0 else 0
            else:
                # データがない場合はすべてNone
                n_samples, mean_val, std_val, min_val, q1_val, median_val, q3_val, max_val = [None] * 8
                pos_rate, neg_rate, zero_rate = [None] * 3

            # 統計検定の計算
            stat, p_value, effect_size_r = None, None, None
            if n_samples > 0 and not all(metric_data == 0):
                try:
                    stats = pg.wilcoxon(metric_data, alternative="two-sided")
                    stat = stats['W-val'].iloc[0]
                    p_value = stats['p-val'].iloc[0]
                    effect_size_r = stats['RBC'].iloc[0]
                except Exception:
                    pass

            # 結果をすべて辞書に格納
            results[type_group][metric] = {
                # 記述統計量
                "N": n_samples,
                "mean": mean_val,
                "std_dev": std_val,
                "min": min_val,
                "25%_q1": q1_val,
                "median": median_val,
                "75%_q3": q3_val,
                "max": max_val,
                "positive_rate": pos_rate,
                "negative_rate": neg_rate,
                "zero_rate": zero_rate,
                # 統計検定
                "statistic": stat,
                "p_value": p_value,
                "effect_size_r": effect_size_r,
            }
            # ★★★ ここまで修正・追加 ★★★

        if results[type_group]:
            output_file = f"{output_dir}/{type_group}_{column_name}_wilcoxon_results.csv"
            # 結果をDataFrameに変換して保存
            output_df = pd.DataFrame.from_dict(results[type_group], orient="index")
            output_df.to_csv(output_file)
            print(f"Results for {type_group} in {column_name} saved to {output_file}")

    return results

def main():
    specific_metrics = [
      "Dorn Areas Keywords/Identifiers",
      "BW Avg number of identifiers",
      "BW Max line length",
      "Dorn Areas Operators",
      "Dorn Areas Literals/Comments",
      "New Identifiers words MIN",
      "BW Avg Identifiers Length",
      "BW Avg conditionals",
      "BW Avg loops",
      "BW Avg periods",
      "Posnett lines",
      "Dorn align extent",
      "BW Avg indentation length",
      "New Number of senses AVG",
      "BW Max words",
      "Dorn Areas Operators/Identifiers",
      "New Method chains MIN",
      "Dorn DFT Spaces",
      "Dorn DFT Comparisons",
      "New Method chains MAX",
      "Dorn DFT Numbers",
      "Dorn Visual X Operators",
      "Dorn Areas Operators/Comments",
      "BW Max char",
      "Posnett entropy",
      "Dorn Areas Identifiers",
      "BW Avg line length",
      "Dorn align blocks",
      "New Expression complexity MAX",
      "Dorn Areas Strings/Identifiers",
      "Dorn DFT Indentations",
      "BW Max Identifiers Length",
      "Dorn DFT Keywords",
      "New Abstractness words MAX",
      "BW Avg blank lines",
      "Dorn Areas Keywords/Comments",
      "Dorn Visual Y Operators",
      "BW Max keywords",
      "BW Avg keywords",
      "Dorn Areas Keywords",
      "Dorn Areas Literals",
      "Dorn Areas Literals/Keywords",
      "New Expression complexity MIN",
      "Dorn Visual Y Comments",
      "New Abstractness words MIN",
      "Dorn Areas Literals/Strings",
      "Dorn DFT Conditionals",
      "Dorn DFT Periods",
      "BW Avg comparisons",
      "New Text Coherence MIN",
      "Dorn Areas Operators/Keywords",
      "BW Avg parenthesis",
      "New Commented words MAX",
      "Dorn DFT Loops",
      "New Semantic Text Coherence Standard",
      "Dorn Visual X Strings",
      "Dorn Areas Comments",
      "Dorn Visual Y Numbers",
      "New Number of senses MAX",
      "New Text Coherence MAX",
      "New Identifiers words AVG",
      "BW Avg comments",
      "Dorn Areas Strings/Numbers",
      "Posnett volume",
      "Dorn Areas Operators/Literals",
      "New Synonym commented words MAX",
      "BW Avg operators",
      "BW Avg commas",
      "Dorn Areas Numbers/Keywords",
      "Dorn Areas Numbers",
      "BW Max numbers",
      "New Method chains AVG",
      "Dorn Visual Y Strings",
      "Dorn Visual X Keywords",
      "Dorn DFT Comments",
      "Dorn Areas Identifiers/Comments",
      "Dorn Visual X Literals",
      "BW Max indentation",
      "Dorn Areas Operators/Numbers",
      "Dorn Areas Literals/Identifiers",
      "BW Max number of identifiers",
      "Dorn DFT Parenthesis",
      "Dorn Areas Strings/Comments",
      "Dorn Visual X Numbers",
      "Dorn Visual Y Literals",
      "Dorn DFT Assignments",
      "Dorn Visual Y Identifiers",
      "Dorn Visual Y Keywords",
      "Dorn Visual X Identifiers",
      "New Commented words AVG",
      "BW Avg Assignment",
      "Dorn Areas Numbers/Comments",
      "Dorn Areas Numbers/Identifiers",
      "Dorn Areas Strings",
      "BW Avg numbers",
      "Dorn Areas Literals/Numbers",
      "Dorn DFT Identifiers",
      "Dorn Areas Strings/Keywords",
      "New Comments readability",
      "BW Avg spaces",
      "New Expression complexity AVG",
      "New Abstractness words AVG",
      "Dorn DFT Operators",
      "Dorn DFT LineLengths",
      "New Text Coherence AVG",
      "New Semantic Text Coherence Normalized",
      "Dorn DFT Commas",
      "Dorn Visual X Comments",
      "Dorn Areas Operators/Strings",
      "New Synonym commented words AVG",
]

    csv_file = f'{PWD}/refactoring_analysis_results.csv'
    output_dir = f'{PWD}/rq2_metrics_details_diff'
    print(f"Output directory: {output_dir}")
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    df = load_and_clean_csv(csv_file)
    df = add_type_column_based_on_path(df)
    df = preprocess_json_columns(df, ['ck_diff', 'readability_diff'])
    df = convert_columns_to_json(df, ['ck_diff', 'readability_diff'])

    # ★修正点: 新しい関数を呼び出す
    calculate_wilcoxon_and_effect_size(df, 'ck_diff', output_dir, specific_metrics)
    calculate_wilcoxon_and_effect_size(df, 'readability_diff', output_dir, specific_metrics)


if __name__ == "__main__":
    main()