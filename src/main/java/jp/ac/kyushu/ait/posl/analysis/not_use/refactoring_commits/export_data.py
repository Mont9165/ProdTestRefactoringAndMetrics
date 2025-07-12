import pandas as pd
from sqlalchemy import create_engine
from sqlalchemy.exc import SQLAlchemyError

# --- データベース接続情報 (ご自身の環境に合わせて変更してください) ---
# 例: PostgreSQL
db_user = 'horikawa'
db_password = 'Hishikawa3'
db_host = 'mussel'
db_port = '5432'
db_name = 'icpc'

# データベース接続文字列を作成
# PostgreSQLの場合: 'postgresql+psycopg2://user:password@host:port/dbname'
# MySQLの場合: 'mysql+mysqlconnector://user:password@host:port/dbname'
# SQLiteの場合: 'sqlite:///path/to/your/database.db'
DATABASE_URI = f'postgresql+psycopg2://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}'

# --- 実行したいSQLクエリ ---
sql_query = """
SELECT
    r.project,
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND (f_sub.new_path ILIKE '%%/test/%%' OR f_sub.old_path ILIKE '%%/test/%%' OR
                   f_sub.new_path ILIKE '%%test%%' OR f_sub.old_path ILIKE '%%test%%')
              AND (f_sub.new_path ILIKE '%%.java' OR f_sub.old_path ILIKE '%%.java')
        ) AND EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND (f_sub.new_path ILIKE '%%.java' OR f_sub.old_path ILIKE '%%.java')
              AND NOT (f_sub.new_path ILIKE '%%/test/%%' OR f_sub.old_path ILIKE '%%/test/%%' OR
                       f_sub.new_path ILIKE '%%test%%' OR f_sub.old_path ILIKE '%%test%%')
        ) AND EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND f_sub.new_path NOT ILIKE '%%.java' AND f_sub.old_path NOT ILIKE '%%.java'
        )
        THEN 'Test and Production and Other Files'

        WHEN EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND (f_sub.new_path ILIKE '%%/test/%%' OR f_sub.old_path ILIKE '%%/test/%%' OR
                   f_sub.new_path ILIKE '%%test%%' OR f_sub.old_path ILIKE '%%test%%')
              AND (f_sub.new_path ILIKE '%%.java' OR f_sub.old_path ILIKE '%%.java')
        ) AND EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND (f_sub.new_path ILIKE '%%.java' OR f_sub.old_path ILIKE '%%.java')
              AND NOT (f_sub.new_path ILIKE '%%/test/%%' OR f_sub.old_path ILIKE '%%/test/%%' OR
                       f_sub.new_path ILIKE '%%test%%' OR f_sub.old_path ILIKE '%%test%%')
        )
        THEN 'Test and Production Code'

        WHEN EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND (f_sub.new_path ILIKE '%%/test/%%' OR f_sub.old_path ILIKE '%%/test/%%' OR
                   f_sub.new_path ILIKE '%%test%%' OR f_sub.old_path ILIKE '%%test%%')
              AND (f_sub.new_path ILIKE '%%.java' OR f_sub.old_path ILIKE '%%.java')
        ) AND EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND f_sub.new_path NOT ILIKE '%%.java' AND f_sub.old_path NOT ILIKE '%%.java'
        )
        THEN 'Test Code and Other Files'

        WHEN EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND (f_sub.new_path ILIKE '%%.java' OR f_sub.old_path ILIKE '%%.java')
              AND NOT (f_sub.new_path ILIKE '%%/test/%%' OR f_sub.old_path ILIKE '%%/test/%%' OR
                       f_sub.new_path ILIKE '%%test%%' OR f_sub.old_path ILIKE '%%test%%')
        ) AND EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND f_sub.new_path NOT ILIKE '%%.java' AND f_sub.old_path NOT ILIKE '%%.java'
        )
        THEN 'Production Code and Other Files'

        WHEN EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND (f_sub.new_path ILIKE '%%/test/%%' OR f_sub.old_path ILIKE '%%/test/%%' OR
                   f_sub.new_path ILIKE '%%test%%' OR f_sub.old_path ILIKE '%%test%%')
              AND (f_sub.new_path ILIKE '%%.java' OR f_sub.old_path ILIKE '%%.java')
        )
        THEN 'Test Code Only'

        WHEN EXISTS (
            SELECT 1
            FROM commit.file f_sub
            WHERE f_sub.project = c.project AND f_sub.commit_id = c.commit_id
              AND (f_sub.new_path ILIKE '%%.java' OR f_sub.old_path ILIKE '%%.java')
              AND NOT (f_sub.new_path ILIKE '%%/test/%%' OR f_sub.old_path ILIKE '%%/test/%%' OR
                       f_sub.new_path ILIKE '%%test%%' OR f_sub.old_path ILIKE '%%test%%')
        )
        THEN 'Production Code Only'

        ELSE 'Other Files Only'
    END AS commit_type,
    COUNT(DISTINCT r.commit_id) AS refactoring_commit_count,
    COUNT(r.commit_id) AS total_refactoring_count
FROM
    refactoring.refactoring r
JOIN
    commit.commit c ON r.project = c.project AND r.commit_id = c.commit_id
GROUP BY
    r.project, commit_type
ORDER BY
    r.project, commit_type;
"""

# --- 出力するCSVファイル名 ---
output_csv_file = 'refactoring_analysis.csv'

def execute_query_and_save_to_csv(db_uri, query, output_file):
    """
    データベースに接続し、クエリを実行して結果をCSVに保存する
    """
    try:
        print("データベースに接続しています...")
        engine = create_engine(db_uri)

        print("SQLクエリを実行しています... (データ量によっては時間がかかる場合があります)")
        # pandasのread_sql_queryを使用して、クエリ結果を直接DataFrameに読み込む
        df = pd.read_sql_query(query, engine)

        print(f"クエリの実行が完了しました。{len(df)}行のデータを取得しました。")

        # DataFrameをCSVファイルに保存 (インデックスは保存しない)
        df.to_csv(output_file, index=False)

        print(f"結果が '{output_file}' に正常に保存されました。")

    except SQLAlchemyError as e:
        print(f"データベースエラーが発生しました: {e}")
    except Exception as e:
        print(f"予期せぬエラーが発生しました: {e}")

# 関数を実行
execute_query_and_save_to_csv(DATABASE_URI, sql_query, output_csv_file)