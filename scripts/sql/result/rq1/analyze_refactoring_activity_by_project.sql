SELECT
    c.project,
    COUNT(DISTINCT c.commit_id) AS total_commits,
    COUNT(DISTINCT r.commit_id) AS refactoring_commits,
    COUNT(r.commit_id) AS total_refactoring_count,
    (COUNT(r.commit_id) * 1.0 / COUNT(DISTINCT c.commit_id)) AS refactorings_per_commit,
    (CASE WHEN COUNT(DISTINCT r.commit_id) > 0
              THEN COUNT(r.commit_id) * 1.0 / COUNT(DISTINCT r.commit_id)
          ELSE 0 END) AS refactorings_per_refactoring_commit
FROM
    commit.commit c
        LEFT JOIN
    refactoring.refactoring r ON c.project = r.project AND c.commit_id = r.commit_id
GROUP BY
    c.project
ORDER BY
    total_commits DESC;