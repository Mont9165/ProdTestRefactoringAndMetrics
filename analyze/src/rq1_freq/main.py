from analysis import analyze_refactorings

DATA_FILE_PATH = 'data/refactorings_output.csv'
PROJECT_LIST_PATH = 'projects_1.txt'

def main():

    summary_table = analyze_refactorings(DATA_FILE_PATH, PROJECT_LIST_PATH)

    if summary_table is not None:
        if not summary_table.empty:
            print(summary_table.to_string())
    else:
        print("\nError")


if __name__ == '__main__':
    main()