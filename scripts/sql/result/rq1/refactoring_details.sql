SELECT
    r.project AS project_name,
    r.commit_id AS refactoring_commit_id,
    p.parent_commit_id AS parent_commit_id,
    r.refactoring_type AS refactoring_name, -- ★★★ ここを修正しました ★★★
    r.hash AS refactoring_hash,

    lcr.file_path AS left_file_path,
    lcr.start_ AS left_start_line,
    lcr.end_ AS left_end_line,

    rcr.file_path AS right_file_path,
    rcr.start_ AS right_start_line,
    rcr.end_ AS right_end_line
FROM
    refactoring.refactoring r
        LEFT JOIN
    commit.parent p ON r.project = p.project AND r.commit_id = p.commit_id
        LEFT JOIN
    refactoring.left_code_range lcr ON r.project = lcr.project
        AND r.commit_id = lcr.commit_id
        AND r.hash = lcr.hash
        LEFT JOIN
    refactoring.right_code_range rcr ON r.project = rcr.project
        AND r.commit_id = rcr.commit_id
        AND r.hash = rcr.hash

ORDER BY
    r.project,
    r.commit_id,
    r.hash,
    refactoring_name,
    lcr.id,
    rcr.id;
