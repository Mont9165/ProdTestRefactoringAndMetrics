import os
import pandas as pd
import subprocess

# Define directories
REFACTORING_DIRECTORY = '/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/output/refactoring/'
CK_DIRECTORY = '/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/output/ck/'
READABILITY_DIRECTORY = '/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/output/readability/'
OUTPUT_FILE = 'refactoring_analysis_results.csv'
REPO_DIRECTORY = '/Users/horikawa/Dev/sakigake/ICPC2025_ERA/Kashiwa_TestEffortEstimation/repos/x/'

def get_parent_commit_id(repo_path, commit_id):
    """
    Retrieve the parent commit ID for a given commit in a Git repository.
    """
    try:
        result = subprocess.run(
            ["git", "-C", repo_path, "log", "--format=%P", "-n", "1", commit_id],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=True
        )
        return result.stdout.strip().split()[0] if result.stdout.strip() else None
    except subprocess.CalledProcessError:
        return None

def load_metrics(file_path):
    """Load metrics from a given CSV file path."""
    if not os.path.exists(file_path) or os.stat(file_path).st_size == 0:
        return pd.DataFrame()
    try:
        return pd.read_csv(file_path)
    except pd.errors.EmptyDataError:
        return pd.DataFrame()

def analyze_refactoring_row(row, repo_path):
    """
    Analyze a single row of the refactoring file.
    """
    try:
        project_name = row['project']
        commit_id = row['commitId']
        left_filepath = row['left_filepath']
        right_filepath = row['right_filepath']

        if not isinstance(left_filepath, str) or not isinstance(right_filepath, str):
            return None

        # Load metrics for the current commit
        right_ck_class = load_metrics(os.path.join(CK_DIRECTORY, project_name, commit_id, "class.csv"))
        right_readability = load_metrics(os.path.join(READABILITY_DIRECTORY, project_name, commit_id, "readability.csv"))

        if right_ck_class.empty or right_readability.empty:
            return None

        # Get parent commit ID
        left_commit_id = get_parent_commit_id(repo_path, commit_id)
        if not left_commit_id:
            return None

        # Load metrics for the parent commit
        left_ck_class = load_metrics(os.path.join(CK_DIRECTORY, project_name, left_commit_id, "class.csv"))
        left_readability = load_metrics(os.path.join(READABILITY_DIRECTORY, project_name, left_commit_id, "readability.csv"))

        if left_ck_class.empty or left_readability.empty:
            return None

        # Filter metrics by file path
        left_ck_filtered = left_ck_class[left_ck_class['file'].str.endswith(os.path.basename(left_filepath))]
        right_ck_filtered = right_ck_class[right_ck_class['file'].str.endswith(os.path.basename(right_filepath))]

        if left_ck_filtered.empty or right_ck_filtered.empty:
            return None

        # Compute differences
        ck_diff = (right_ck_filtered.mean(numeric_only=True) / left_ck_filtered.mean(numeric_only=True)).to_dict()
        readability_diff = (
            right_readability.mean(numeric_only=True) / left_readability.mean(numeric_only=True)
        ).to_dict()

        ck_class = {'right_mean': right_ck_filtered.mean(numeric_only=True).to_dict(), 'right_median': right_ck_filtered.median(numeric_only=True).to_dict(), 'left_mean': left_ck_filtered.mean(numeric_only=True).to_dict() , 'left_median': left_ck_filtered.median(numeric_only=True).to_dict()}
        readability = {'right_mean': right_readability.mean(numeric_only=True).to_dict(), 'right_median': right_readability.median(numeric_only=True).to_dict(), 'left_mean': left_readability.mean(numeric_only=True).to_dict(), 'left_median': left_readability.median(numeric_only=True).to_dict()}

        return {
            "project": project_name,
            "commitId": commit_id,
            "left_filepath": left_filepath,
            "right_filepath": right_filepath,
            "refactoringType": row['refactoringType'],
            "ck": ck_class,
            "readability": readability,
            "ck_diff": ck_diff,
            "readability_diff": readability_diff
        }

    except Exception as e:
        return None

def analyze_refactoring_file(refactoring_file, repo_directory):
    """
    Analyze an entire refactoring file.
    """
    try:
        refactoring_df = pd.read_csv(refactoring_file)
        results = []
        project_name = os.path.basename(refactoring_file).split('.')[0]
        repo_path = os.path.join(repo_directory, project_name)

        for _, row in refactoring_df.iterrows():
            result = analyze_refactoring_row(row, repo_path)
            if result:
                results.append(result)
        return results
    except Exception:
        return []

def main():
    """
    Main entry point for the analysis script.
    """
    try:
        refactoring_files = [
            os.path.join(REFACTORING_DIRECTORY, f) for f in os.listdir(REFACTORING_DIRECTORY)
            if f.endswith('.csv')
        ]
        all_results = []
        total_files = len(refactoring_files)

        for idx, refactoring_file in enumerate(refactoring_files, start=1):
            print(f"Processing file {idx}/{total_files}: {refactoring_file}")
            file_results = analyze_refactoring_file(refactoring_file, REPO_DIRECTORY)
            all_results.extend(file_results)

            pd.DataFrame(all_results).to_csv(OUTPUT_FILE, mode='a', header=not os.path.exists(OUTPUT_FILE), index=False)

        print(f"Analysis complete. Results saved to {OUTPUT_FILE}.")
    except Exception as e:
        print(f"Unexpected error: {e}")

if __name__ == "__main__":
    main()