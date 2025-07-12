import json
import pandas as pd
from io import StringIO
import pingouin as pg
import os

# FIXME: PWDをご自身の作業ディレクトリに合わせてください
PWD = "/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/src/main/java/jp/ac/kyushu/ait/posl/analysis/data/rq2_metrics/data"

# === データ読み込み・前処理関数 ===
def load_and_clean_csv(csv_file):
    with open(csv_file, "r", encoding="utf-8") as file:
        lines = file.readlines()
    if not lines or not lines[-1].strip():
        lines = lines[:-1]
    cleaned_data = "".join(lines)
    df = pd.read_csv(StringIO(cleaned_data))
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
        df[col] = df[col].astype(str).str.replace("nan|None|inf", "null", regex=True)
    return df

def convert_columns_to_json(df, columns):
    for col in columns:
        def safe_json_load(x):
            if pd.isna(x): return {}
            if isinstance(x, dict): return x
            try:
                return json.loads(x.replace("'", '"'))
            except json.JSONDecodeError:
                return {}
        df[col] = df[col].apply(safe_json_load)
    return df

# === 分析関数 (2種類) ===

def _run_analysis(df, column_name, output_dir, specific_metrics, analysis_type):
    """分析処理の共通部分を担う内部関数"""
    expanded_data = []
    for index, row in df.iterrows():
        data_dict = row[column_name]
        if not isinstance(data_dict, dict): continue
        for metric, value in data_dict.items():
            if pd.notna(value) and isinstance(value, (int, float)):
                expanded_data.append({"Type": row["Type"], "metric": metric, "value": value})

    if not expanded_data:
        print(f"No valid data found to process for column {column_name}.")
        return {}

    expanded_df = pd.DataFrame(expanded_data)
    results = {}

    for type_group in expanded_df['Type'].unique():
        group_data = expanded_df[expanded_df['Type'] == type_group]
        results[type_group] = {}

        for metric in group_data['metric'].unique():
            if metric not in specific_metrics: continue

            metric_data = group_data[group_data['metric'] == metric]['value'].dropna()

            desc = metric_data.describe()
            n_samples = int(desc.get('count', 0))

            stat, p_value, effect_size_r = None, None, None
            if n_samples > 0:
                data_for_test = metric_data
                if analysis_type == 'ratio':
                    data_for_test = metric_data - 1 # 比率の場合は1を引く

                if not data_for_test.round(10).eq(0).all():
                    try:
                        stats = pg.wilcoxon(data_for_test, alternative="two-sided")
                        stat, p_value, effect_size_r = stats['W-val'].iloc[0], stats['p-val'].iloc[0], stats['RBC'].iloc[0]
                    except Exception: pass

            results[type_group][metric] = {
                "N": n_samples, "mean": desc.get('mean'), "std_dev": desc.get('std'), "min": desc.get('min'),
                "25%_q1": desc.get('25%'), "median": desc.get('50%'), "75%_q3": desc.get('75%'), "max": desc.get('max'),
                "statistic": stat, "p_value": p_value, "effect_size_r": effect_size_r
            }

        if results[type_group]:
            output_file = f"{output_dir}/{type_group}_{column_name}_{analysis_type}_results.csv"
            pd.DataFrame.from_dict(results[type_group], orient="index").to_csv(output_file)
            print(f"Results for {type_group} in {column_name} saved to {output_file}")
    return results

def analyze_differences(df, column_name, output_dir, specific_metrics):
    """差 (right - left) を分析する関数。中央値が0と異なるか検定"""
    print(f"Analyzing '{column_name}' as DIFFERENCE (H0: median = 0)...")
    return _run_analysis(df, column_name, output_dir, specific_metrics, 'difference')

def analyze_ratios(df, column_name, output_dir, specific_metrics):
    """比率 (right / left) を分析する関数。中央値が1と異なるか検定"""
    print(f"Analyzing '{column_name}' as RATIO (H0: median = 1)...")
    return _run_analysis(df, column_name, output_dir, specific_metrics, 'ratio')


# === メイン処理 ===
def main():
    # specific_metrics のリストは長いので省略
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
        "New Synonym commented words AVG"
    ]

    csv_file = f'{PWD}/refactoring_analysis_results_1.csv'
    # 出力ディレクトリ名に 'ratio' を含めていることから、比率分析を意図していると判断
    output_dir = f'{PWD}/rq2_metrics_details_all_ratio'
    print(f"Output directory: {output_dir}")

    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    df = load_and_clean_csv(csv_file)
    df = add_type_column_based_on_path(df)
    df = preprocess_json_columns(df, ['ck_diff', 'readability_diff'])
    df = convert_columns_to_json(df, ['ck_diff', 'readability_diff'])

    # --- ★★★ ここで分析方法を指定 ★★★ ---
    # ご希望通り、両方の列を「比率」として分析します。
    analyze_ratios(df, 'ck_diff', output_dir, specific_metrics)
    analyze_ratios(df, 'readability_diff', output_dir, specific_metrics)

if __name__ == "__main__":
    main()