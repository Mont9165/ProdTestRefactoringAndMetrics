from operator import index

import pandas as pd
import json
import matplotlib.pyplot as plt
from io import StringIO


# CSVファイルを読み込む関数
def load_and_clean_csv(csv_file):
    with open(csv_file, "r") as file:
        lines = file.readlines()

    # 最終行が不完全であればスキップ
    if not lines[-1].endswith("\n") or lines[-1].count("{") != lines[-1].count("}"):
        print("Skipping incomplete last line.")
        lines = lines[:-1]

    # データフレームに変換
    cleaned_data = "\n".join(lines)
    return pd.read_csv(StringIO(cleaned_data))

# 辞書型列を解析可能な形式に変換する関数
def convert_columns_to_json(df, columns):
    for col in columns:
        def safe_json_load(x):
            if pd.isna(x):
                return {}
            try:
                # シングルクォートをダブルクォートに変換
                json_string = x.replace("'", '"').replace("nan", "null")
                return json.loads(json_string)
            except json.JSONDecodeError as e:
                print(f"Skipping malformed entry in column {col}: {x}\nError: {e}")
                return {}
        df[col] = df[col].apply(safe_json_load)
    return df


# 統計情報を計算する関数
def calculate_statistics(df, column_name):
    if df[column_name].notna().any():
        return pd.DataFrame(df[column_name].tolist()).describe()
    else:
        # print(f"No valid data in column {column_name} for statistics.")
        return pd.DataFrame()


# 上位メトリクスを取得する関数
def get_top_metrics(stats, top_n=10):
    if not stats.empty:
        return stats.loc['mean'].sort_values(ascending=False).head(top_n)
    else:
        return pd.Series(dtype='float64')


# 棒グラフを作成して可視化する関数
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


def save_statistics_to_csv(stats, filename):
    """
    DataFrameの統計情報をCSVに保存する関数

    :param stats: 統計情報のDataFrame
    :param filename: 保存先のファイル名
    """
    if not stats.empty:
        stats.to_csv(filename, index=True)
        print(f"Statistics saved to {filename}")
    else:
        print(f"No data to save for {filename}")


# メイン処理
def main():
    csv_file = "input/refactoring_analysis_results.csv"

    # CSVを読み込んでクリーンアップ
    df = load_and_clean_csv(csv_file)
    # print("First 5 rows of the cleaned DataFrame:")
    # print(df.head())
    # print(df.columns)
    # print(df['commitId'])
    #
    # print(df['ck_diff'].head())
    # print(df['readability_diff'].head())

    # 辞書型列を変換
    df = convert_columns_to_json(df, ['ck_diff', 'readability_diff'])
    # print("First 5 rows of the DataFrame with JSON columns:")
    # print(df.head())

    # 統計情報を計算
    ck_diff_stats = calculate_statistics(df, 'ck_diff')
    readability_diff_stats = calculate_statistics(df, 'readability_diff')

    print("\nCK Metric Differences - Statistics:")
    print(ck_diff_stats)
    print("\nReadability Metric Differences - Statistics:")
    print(readability_diff_stats)

    # CSV保存用のコード
    ck_diff_filename = "ck_diff_statistics.csv"
    readability_diff_filename = "readability_diff_statistics.csv"

    # ck_diff_statsの保存
    save_statistics_to_csv(ck_diff_stats, ck_diff_filename)

    # readability_diff_statsの保存
    save_statistics_to_csv(readability_diff_stats, readability_diff_filename)

    # 上位メトリクスを抽出
    top_ck_metrics = get_top_metrics(ck_diff_stats)
    top_readability_metrics = get_top_metrics(readability_diff_stats)

    print("\nTop CK Metrics by Mean Difference:")
    print(top_ck_metrics)
    print("\nTop Readability Metrics by Mean Difference:")
    print(top_readability_metrics)

    # グラフをプロット
    plot_top_metrics(top_ck_metrics, "Top CK Metrics by Mean Difference", "Mean Difference")
    plot_top_metrics(top_readability_metrics, "Top Readability Metrics by Mean Difference", "Mean Difference")


if __name__ == "__main__":
    main()