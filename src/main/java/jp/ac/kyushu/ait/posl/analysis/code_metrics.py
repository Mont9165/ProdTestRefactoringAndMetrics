import os


import pandas as pd

# Define directories
REFACTORING_DIRECTORY = '/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/src/main/java/jp/ac/kyushu/ait/posl/analysis/output/refactoring/'
CK_DIRECTORY = '/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/src/main/java/jp/ac/kyushu/ait/posl/analysis/output/ck/'
READABILITY_DIRECTORY = '/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/src/main/java/jp/ac/kyushu/ait/posl/analysis/output/readability/'
OUTPUT_FILE = 'refactoring_analysis_results.csv'
REPO_DIRECTORY = '/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/repos/x/'

import subprocess


def get_parent_commit_id(repo_path, commit_id):
    """
    Retrieve the parent commit ID for a given commit in a Git repository.

    Parameters:
        repo_path (str): Path to the Git repository.
        commit_id (str): Commit ID for which the parent is required.

    Returns:
        str: Parent commit ID if found, else None.
    """
    try:
        # Run the git log command to get the parent commit
        result = subprocess.run(
            ["git", "-C", repo_path, "log", "--format=%P", "-n", "1", commit_id],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=True
        )
        # Extract the parent commit ID from the result
        parent_commit_id = result.stdout.strip()
        return parent_commit_id if parent_commit_id else None
    except subprocess.CalledProcessError as e:
        print(f"Error retrieving parent commit for {commit_id}: {e.stderr.strip()}")
        return None


def load_ck_metrics(project_name, commit_id, file_type):
    """Load CK metrics for a given project, commit, and file type."""
    file_path = os.path.join(CK_DIRECTORY, project_name, commit_id, f"{file_type}.csv")
    if not os.path.exists(file_path) or os.stat(file_path).st_size == 0:
        # print(f"CK metrics file missing or empty: {file_path}")
        return pd.DataFrame()
    try:
        return pd.read_csv(file_path)
    except pd.errors.EmptyDataError:
        # print(f"EmptyDataError for CK metrics file: {file_path}")
        return pd.DataFrame()


def load_readability_metrics(project_name, commit_id):
    """Load readability metrics for a given project and commit."""
    file_path = os.path.join(READABILITY_DIRECTORY, project_name, commit_id, "readability.csv")
    if not os.path.exists(file_path) or os.stat(file_path).st_size == 0:
        # print(f"Readability metrics file missing or empty: {file_path}")
        return pd.DataFrame()
    try:
        return pd.read_csv(file_path)
    except pd.errors.EmptyDataError:
        # print(f"EmptyDataError for Readability metrics file: {file_path}")
        return pd.DataFrame()


def analyze_refactoring_row(row):
    """Analyze a single row from the refactoring file."""
    project_name = row['project']
    commit_id = row['commitId']
    left_filepath = row['left_filepath']
    right_filepath = row['right_filepath']

    right_commit_id = row['commitId']  # Assuming this is the current commit ID

    # Load metrics
    right_ck_class = load_ck_metrics(project_name, commit_id, "class")
    right_readability = load_readability_metrics(project_name, commit_id)
    if right_readability.empty or right_ck_class.empty:
        return None
    else:
        repo_path = REPO_DIRECTORY + project_name
        left_commit_id = get_parent_commit_id(repo_path, commit_id)  # Assuming this is the parent commit ID

    left_ck_class = load_ck_metrics(project_name, left_commit_id, "class")
    left_readability = load_readability_metrics(project_name, left_commit_id)

    # Skip if metrics are missing
    if left_ck_class.empty or right_ck_class.empty or left_readability.empty or right_readability.empty:
        # print(f"Metrics missing for {commit_id}. Skipping.")
        return None
    else:
        print(f"Metrics found for {commit_id}. {left_ck_class}")
        print(f"Metrics found for {commit_id}. {right_ck_class}")
        print(f"Metrics found for {commit_id}. {left_readability}")
        print(f"Metrics found for {commit_id}. {right_readability}")

    # Filter metrics by file path
    left_ck_filtered = left_ck_class[
        left_ck_class['file'].apply(lambda x: os.path.basename(x)) == os.path.basename(left_filepath)]
    right_ck_filtered = right_ck_class[
        right_ck_class['file'].apply(lambda x: os.path.basename(x)) == os.path.basename(right_filepath)]
    left_readability_filtered = left_readability[
        left_readability['Filename'].apply(lambda x: os.path.basename(x)) == os.path.basename(left_filepath)]
    right_readability_filtered = right_readability[
        right_readability['Filename'].apply(lambda x: os.path.basename(x)) == os.path.basename(right_filepath)]

    print(f"Metrics filtered for {commit_id}. {left_ck_filtered.values}")
    print(f"Metrics filtered for {commit_id}. {right_ck_filtered.values}")
    print(f"Metrics filtered for {commit_id}. {left_readability_filtered.values}")
    print(f"Metrics filtered for {commit_id}. {right_readability_filtered.values}")

    # Compare metrics
    ck_diff = (right_ck_filtered.mean(numeric_only=True) - left_ck_filtered.mean(numeric_only=True)).to_dict()
    readability_diff = (right_readability_filtered.mean(numeric_only=True) -
                        left_readability_filtered.mean(numeric_only=True)).to_dict()

    print(f"CK diff: {ck_diff}")
    print(f"Readability diff: {readability_diff}")

    return {
        "project": project_name,
        "commitId": commit_id,
        "refactoringType": row['refactoringType'],
        "ck_diff": ck_diff,
        "readability_diff": readability_diff
    }


def analyze_refactoring_file(refactoring_file):
    """Analyze a single refactoring file."""
    refactoring_df = pd.read_csv(refactoring_file)
    results = []

    for _, row in refactoring_df.iterrows():
        result = analyze_refactoring_row(row)
        if result:
            results.append(result)

    return results


def main():
    """Main entry point."""
    # List all CSV files in the refactoring directory
    refactoring_files = [os.path.join(REFACTORING_DIRECTORY, f) for f in os.listdir(REFACTORING_DIRECTORY)
                         if f.endswith('.csv')]

    all_results = []

    # Analyze each refactoring file
    for refactoring_file in refactoring_files:
        print(f"Analyzing file: {refactoring_file}")
        file_results = analyze_refactoring_file(refactoring_file)
        all_results.extend(file_results)

    # Convert results to DataFrame and save
    results_df = pd.DataFrame(all_results)
    # os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)
    results_df.to_csv(OUTPUT_FILE, index=False)
    print(f"Analysis complete. Results saved to {OUTPUT_FILE}.")


if __name__ == "__main__":
    main()