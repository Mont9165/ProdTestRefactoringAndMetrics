import csv
import os
from collections import defaultdict
import pandas as pd

# スキップするプロジェクト
SKIP_FILES = ['tencent_tencentkona-8', 'lwjgl_lwjgl3', 'grasscutters_grasscutter',
              'Potion-Studios_BYG', 'innoxia_liliths-throne-public', 'deeplearning4j_deeplearning4j',
              'ome_openmicroscopy', 'openjdk_jdk8u', 'opensourcebim_bimserver',
              'cinchapi_concourse', 'refinitiv_real-time-sdk', 'dreamoftheredchamber_system-design-interviews',
              'iluwatar_java-design-patterns']

# ディレクトリと出力ファイル
directory_path = '/output/refactoring/'
output_frequency_file = 'data/refactoring_frequency_statistics.csv'

# 結果を格納するリスト
frequency_data = []

# ディレクトリ内のCSVファイルを再帰的に処理
for root, _, files in os.walk(directory_path):
    for filename in files:
        if filename.endswith('.csv') and filename.split('.')[0] not in SKIP_FILES:
            project_name = filename.split('.')[0]
            file_path = os.path.join(root, filename)

            # カウント用変数
            test_refactoring_counts = defaultdict(int)
            prod_refactoring_counts = defaultdict(int)
            mix_refactoring_counts = defaultdict(int)
            mix_test_refactoring_counts = defaultdict(int)
            mix_prod_refactoring_counts = defaultdict(int)

            refactoring_commits = set()

            test_changed_files = set()
            prod_changed_files = set()
            mix_changed_files = set()
            mix_test_changed_files = set()
            mix_prod_changed_files = set()

            # コミットタイプをトラック
            commit_types = defaultdict(lambda: {"test": False, "production": False, "mix": False})

            # CSVを読み取る
            with open(file_path, 'r') as file:
                reader = csv.DictReader(file)
                for row in reader:
                    refactoring_type = row['refactoringType']
                    left_filepath = row['left_filepath']
                    right_filepath = row['right_filepath']
                    commit_id = row['commitId']

                    refactoring_commits.add(commit_id)

                    # テストかどうかを判定
                    is_test = '/test' in left_filepath.lower() or '/test' in right_filepath.lower()

                    # テストまたはプロダクションとしてフラグを設定
                    if is_test:
                        commit_types[commit_id]["test"] = True
                        test_changed_files.update([left_filepath, right_filepath])
                    else:
                        commit_types[commit_id]["production"] = True
                        prod_changed_files.update([left_filepath, right_filepath])

                    # Mixとして分類
                    if commit_types[commit_id]["test"] and commit_types[commit_id]["production"]:
                        commit_types[commit_id]["mix"] = True
                        for filepath in [left_filepath, right_filepath]:
                            if filepath in test_changed_files:
                                test_changed_files.discard(filepath)
                            if filepath in prod_changed_files:
                                prod_changed_files.discard(filepath)
                            mix_changed_files.add(filepath)

                        mix_refactoring_counts[refactoring_type] += (
                            test_refactoring_counts.get(refactoring_type, 0) + prod_refactoring_counts.get(refactoring_type, 0)
                        )
                        # test_refactoring_counts[refactoring_type] = 0
                        # prod_refactoring_counts[refactoring_type] = 0
                    else:
                        if is_test:
                            test_refactoring_counts[refactoring_type] += 1
                        else:
                            prod_refactoring_counts[refactoring_type] += 1

            # 各種カウントを計算
            total_refactorings = sum(test_refactoring_counts.values()) + sum(prod_refactoring_counts.values()) + sum(mix_refactoring_counts.values())
            test_refactorings = sum(test_refactoring_counts.values())
            prod_refactorings = sum(prod_refactoring_counts.values())
            mix_refactorings = sum(mix_refactoring_counts.values())
            avg_refactorings_per_commit_total = total_refactorings / len(refactoring_commits) if refactoring_commits else 0

            # コミットタイプのカウント
            test_only_commits = sum(1 for commit in commit_types.values() if commit["test"] and not commit["production"])
            production_only_commits = sum(1 for commit in commit_types.values() if commit["production"] and not commit["test"])
            mixed_commits = sum(1 for commit in commit_types.values() if commit["mix"])

            # プロジェクトごとの結果を格納
            frequency_data.append({
                'Project': project_name,
                'Refactoring Commits': len(refactoring_commits),
                'Total Refactorings': total_refactorings,
                'Test Refactorings': test_refactorings,
                'Production Refactorings': prod_refactorings,
                'Mixed Refactorings': mix_refactorings,
                'Avg Refactorings per Commit (Total)': avg_refactorings_per_commit_total,
                'Unique Test Files Changed': len(test_changed_files),
                'Unique Production Files Changed': len(prod_changed_files),
                'Unique Mixed Files Changed': len(mix_changed_files),
                'Test-Only Commits': test_only_commits,
                'Production-Only Commits': production_only_commits,
                'Mixed Commits': mixed_commits
            })

# 結果をデータフレームに変換してCSVに保存
frequency_df = pd.DataFrame(frequency_data)
os.makedirs(os.path.dirname(output_frequency_file), exist_ok=True)
frequency_df.to_csv(output_frequency_file, index=False, encoding='utf-8')
print(f"Refactoring frequency statistics saved to {output_frequency_file}")

# サマリー統計を計算
summary_stats = {
    "Metric": [
        "Refactoring Commits",
        "Test-Only Commits",
        "Production-Only Commits",
        "Mixed Commits",
        "Total Refactorings",
        "Test Refactorings",
        "Production Refactorings",
        "Mixed Refactorings",
        "Avg Refactorings per Commit (Total)",
        "Unique Test Files Changed",
        "Unique Production Files Changed",
        "Unique Mixed Files Changed"
    ],
    "sum": [
        frequency_df['Refactoring Commits'].sum(),
        frequency_df['Test-Only Commits'].sum(),
        frequency_df['Production-Only Commits'].sum(),
        frequency_df['Mixed Commits'].sum(),
        frequency_df['Total Refactorings'].sum(),
        frequency_df['Test Refactorings'].sum(),
        frequency_df['Production Refactorings'].sum(),
        frequency_df['Mixed Refactorings'].sum(),
        frequency_df['Avg Refactorings per Commit (Total)'].mean(),
        frequency_df['Unique Test Files Changed'].sum(),
        frequency_df['Unique Production Files Changed'].sum(),
        frequency_df['Unique Mixed Files Changed'].sum()
    ],
    "Mean": [
        frequency_df['Refactoring Commits'].mean(),
        frequency_df['Test-Only Commits'].mean(),
        frequency_df['Production-Only Commits'].mean(),
        frequency_df['Mixed Commits'].mean(),
        frequency_df['Total Refactorings'].mean(),
        frequency_df['Test Refactorings'].mean(),
        frequency_df['Production Refactorings'].mean(),
        frequency_df['Mixed Refactorings'].mean(),
        frequency_df['Avg Refactorings per Commit (Total)'].mean(),
        frequency_df['Unique Test Files Changed'].mean(),
        frequency_df['Unique Production Files Changed'].mean(),
        frequency_df['Unique Mixed Files Changed'].mean()
    ],
    "Median": [
        frequency_df['Refactoring Commits'].median(),
        frequency_df['Test-Only Commits'].median(),
        frequency_df['Production-Only Commits'].median(),
        frequency_df['Mixed Commits'].median(),
        frequency_df['Total Refactorings'].median(),
        frequency_df['Test Refactorings'].median(),
        frequency_df['Production Refactorings'].median(),
        frequency_df['Mixed Refactorings'].median(),
        frequency_df['Avg Refactorings per Commit (Total)'].median(),
        frequency_df['Unique Test Files Changed'].median(),
        frequency_df['Unique Production Files Changed'].median(),
        frequency_df['Unique Mixed Files Changed'].median()
    ],
    "Variance": [
        frequency_df['Refactoring Commits'].var(),
        frequency_df['Test-Only Commits'].var(),
        frequency_df['Production-Only Commits'].var(),
        frequency_df['Mixed Commits'].var(),
        frequency_df['Total Refactorings'].var(),
        frequency_df['Test Refactorings'].var(),
        frequency_df['Production Refactorings'].var(),
        frequency_df['Mixed Refactorings'].var(),
        frequency_df['Avg Refactorings per Commit (Total)'].var(),
        frequency_df['Unique Test Files Changed'].var(),
        frequency_df['Unique Production Files Changed'].var(),
        frequency_df['Unique Mixed Files Changed'].var()
    ],
    "Standard Deviation": [
        frequency_df['Refactoring Commits'].std(),
        frequency_df['Test-Only Commits'].std(),
        frequency_df['Production-Only Commits'].std(),
        frequency_df['Mixed Commits'].std(),
        frequency_df['Total Refactorings'].std(),
        frequency_df['Test Refactorings'].std(),
        frequency_df['Production Refactorings'].std(),
        frequency_df['Mixed Refactorings'].std(),
        frequency_df['Avg Refactorings per Commit (Total)'].std(),
        frequency_df['Unique Test Files Changed'].std(),
        frequency_df['Unique Production Files Changed'].std(),
        frequency_df['Unique Mixed Files Changed'].std()
    ],
    "Min": [
        frequency_df['Refactoring Commits'].min(),
        frequency_df['Test-Only Commits'].min(),
        frequency_df['Production-Only Commits'].min(),
        frequency_df['Mixed Commits'].min(),
        frequency_df['Total Refactorings'].min(),
        frequency_df['Test Refactorings'].min(),
        frequency_df['Production Refactorings'].min(),
        frequency_df['Mixed Refactorings'].min(),
        frequency_df['Avg Refactorings per Commit (Total)'].min(),
        frequency_df['Unique Test Files Changed'].min(),
        frequency_df['Unique Production Files Changed'].min(),
        frequency_df['Unique Mixed Files Changed'].min()
    ],
    "Max": [
        frequency_df['Refactoring Commits'].max(),
        frequency_df['Test-Only Commits'].max(),
        frequency_df['Production-Only Commits'].max(),
        frequency_df['Mixed Commits'].max(),
        frequency_df['Total Refactorings'].max(),
        frequency_df['Test Refactorings'].max(),
        frequency_df['Production Refactorings'].max(),
        frequency_df['Mixed Refactorings'].max(),
        frequency_df['Avg Refactorings per Commit (Total)'].max(),
        frequency_df['Unique Test Files Changed'].max(),
        frequency_df['Unique Production Files Changed'].max(),
        frequency_df['Unique Mixed Files Changed'].max()
    ]
}

# DataFrameに変換
summary_df = pd.DataFrame(summary_stats)

# サマリー統計の保存
output_summary_file = '../refactoring_summary_statistics.csv'
os.makedirs(os.path.dirname(output_summary_file), exist_ok=True)
summary_df.to_csv(output_summary_file, index=False, encoding='utf-8')
print(f"Summary statistics saved to {output_summary_file}")
