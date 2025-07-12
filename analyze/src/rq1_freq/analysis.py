import pandas as pd
import numpy as np

def analyze_refactorings(file_path, project_list_path):
    try:
        with open(project_list_path, 'r') as f:
            allowed_projects = {line.strip().replace('/', '_') for line in f if line.strip()}
        if not allowed_projects:
            print(f"Error '{project_list_path}' ")
            return None
    except FileNotFoundError:
        print(f"Error: '{project_list_path}'")
        return None

    # Step 1: リファクタリングデータを読み込む
    try:
        df = pd.read_csv(file_path)
    except FileNotFoundError:
        print(f"Error: '{file_path}'")
        return None

    df = df[df['project_name'].isin(allowed_projects)].copy()

    if df.empty:
        print("df.empty")
        return pd.DataFrame()


    def get_code_type(path):
        if isinstance(path, str) and 'test' in path.lower():
            return 'Test'
        return 'Prod'
    df['code_type'] = df['left_file_path'].apply(get_code_type)

    project_stats = []
    for project_name, project_df in df.groupby('project_name'):

        commit_types = {}
        for commit_id, commit_df in project_df.groupby('refactoring_commit_id'):
            types_in_commit = set(commit_df['code_type'])
            if 'Prod' in types_in_commit and 'Test' in types_in_commit:
                commit_types[commit_id] = 'Co-occur. commits'
            elif 'Prod' in types_in_commit:
                commit_types[commit_id] = 'Prod. commits'
            else:
                commit_types[commit_id] = 'Test commits'

        commit_types_series = pd.Series(commit_types)
        commit_counts = commit_types_series.value_counts()

        prod_commits = commit_counts.get('Prod. commits', 0)
        test_commits = commit_counts.get('Test commits', 0)
        co_occur_commits = commit_counts.get('Co-occur. commits', 0)

        prod_files_df = project_df[project_df['code_type'] == 'Prod']
        test_files_df = project_df[project_df['code_type'] == 'Test']

        prod_files_count = len(pd.concat([prod_files_df['left_file_path'], prod_files_df['right_file_path']]).dropna().unique())
        test_files_count = len(pd.concat([test_files_df['left_file_path'], test_files_df['right_file_path']]).dropna().unique())

        prod_instances = prod_files_df['refactoring_hash'].nunique()
        test_instances = test_files_df['refactoring_hash'].nunique()

        project_stats.append({
            'Prod. commits': prod_commits,
            'Test commits': test_commits,
            'Co-occur. commits': co_occur_commits,
            'Refactored Prod. files': prod_files_count,
            'Refactored Test files': test_files_count,
            'Prod. instances': prod_instances,
            'Test instances': test_instances
        })

    if not project_stats:
        return pd.DataFrame()

    stats_df = pd.DataFrame(project_stats)

    summary = pd.DataFrame({
        'Mean': stats_df.mean(),
        'Median': stats_df.median(),
        'Sum': stats_df.sum()
    }).T

    summary_int = summary.astype(int)
    summary_with_pct = summary_int.copy().astype(str)
    sum_row = summary_int.loc['Sum']

    commit_sum = sum_row['Prod. commits'] + sum_row['Test commits'] + sum_row['Co-occur. commits']
    files_sum = sum_row['Refactored Prod. files'] + sum_row['Refactored Test files']
    instances_sum = sum_row['Prod. instances'] + sum_row['Test instances']

    if commit_sum > 0:
        summary_with_pct.loc['Sum', 'Prod. commits'] += f" ({(sum_row['Prod. commits'] / commit_sum * 100):.1f}%)"
        summary_with_pct.loc['Sum', 'Test commits'] += f" ({(sum_row['Test commits'] / commit_sum * 100):.1f}%)"
        summary_with_pct.loc['Sum', 'Co-occur. commits'] += f" ({(sum_row['Co-occur. commits'] / commit_sum * 100):.1f}%)"
    if files_sum > 0:
        summary_with_pct.loc['Sum', 'Refactored Prod. files'] += f" ({(sum_row['Refactored Prod. files'] / files_sum * 100):.1f}%)"
        summary_with_pct.loc['Sum', 'Refactored Test files'] += f" ({(sum_row['Refactored Test files'] / files_sum * 100):.1f}%)"
    if instances_sum > 0:
        summary_with_pct.loc['Sum', 'Prod. instances'] += f" ({(sum_row['Prod. instances'] / instances_sum * 100):.1f}%)"
        summary_with_pct.loc['Sum', 'Test instances'] += f" ({(sum_row['Test instances'] / instances_sum * 100):.1f}%)"

    summary_with_pct.columns = pd.MultiIndex.from_tuples([
        ('Refactoring commits', 'Prod. commits'),
        ('Refactoring commits', 'Test commits'),
        ('Refactoring commits', 'Co-occur. commits'),
        ('Refactored files', 'Prod. code'),
        ('Refactored files', 'Test code'),
        ('Refactoring instances', 'Prod. code'),
        ('Refactoring instances', 'Test code')
    ])
    summary_with_pct.index.name = "Type"

    return summary_with_pct