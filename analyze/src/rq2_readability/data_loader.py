import pandas as pd
import os
from pathlib import Path
from typing import List, Dict, Optional
from data_models import ReadabilityMetric, RefactoringData
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class DataLoader:

    def __init__(self, base_dir: str = "."):
        self.base_dir = Path(base_dir)
        self.readability_dir = self.base_dir / "outputs" / "metrics"
        self.refactoring_file = self.base_dir / "data" / "refactoring_analysis_results.csv"

    def load_readability_metrics(self, project_filter: Optional[str] = None) -> List[ReadabilityMetric]:
        metrics = []

        if not self.readability_dir.exists():
            logger.error(f"Readability directory not found: {self.readability_dir}")
            return metrics

        for csv_file in self.readability_dir.rglob("file_metrics.csv"):
            project_name = csv_file.parent.name

            if project_filter and project_filter not in project_name:
                continue

            try:
                df = pd.read_csv(csv_file)
                logger.info(f"Loading readability metrics from {csv_file}: {len(df)} records")

                for _, row in df.iterrows():
                    metric = ReadabilityMetric(
                        commit_id=str(row['commit_id']),
                        has_refactoring=bool(row['has_refactoring']),
                        file_path=str(row['file_path']),
                        readability_score=float(row['readability_score'])
                    )
                    metrics.append(metric)

            except Exception as e:
                logger.error(f"Error loading {csv_file}: {e}")

        logger.info(f"Total readability metrics loaded: {len(metrics)}")
        return metrics

    def load_refactoring_data(self, project_filter: Optional[str] = None) -> List[RefactoringData]:
        refactorings = []

        if not self.refactoring_file.exists():
            logger.error(f"Refactoring file not found: {self.refactoring_file}")
            return refactorings

        try:
            df = pd.read_csv(self.refactoring_file)
            logger.info(f"Loading refactoring data: {len(df)} records")

            for _, row in df.iterrows():
                project_name = str(row['project_name'])

                if project_filter and project_filter not in project_name:
                    continue

                refactoring = RefactoringData(
                    project_name=project_name,
                    refactoring_commit_id=str(row['refactoring_commit_id']),
                    parent_commit_id=str(row['parent_commit_id']),
                    refactoring_name=str(row['refactoring_name']),
                    refactoring_hash=int(row['refactoring_hash']),
                    left_file_path=str(row['left_file_path']),
                    left_start_line=float(row['left_start_line']),
                    left_end_line=float(row['left_end_line']),
                    right_file_path=str(row['right_file_path']),
                    right_start_line=float(row['right_start_line']),
                    right_end_line=float(row['right_end_line'])
                )
                refactorings.append(refactoring)

        except Exception as e:
            logger.error(f"Error loading refactoring data: {e}")

        logger.info(f"Total refactoring data loaded: {len(refactorings)}")
        return refactorings

    def get_available_projects(self) -> List[str]:
        projects = []

        if self.readability_dir.exists():
            for project_dir in self.readability_dir.iterdir():
                if project_dir.is_dir() and (project_dir / "file_metrics.csv").exists():
                    projects.append(project_dir.name)

        if self.refactoring_file.exists():
            try:
                df = pd.read_csv(self.refactoring_file)
                refactoring_projects = df['project_name'].unique().tolist()
                projects.extend([p for p in refactoring_projects if p not in projects])
            except Exception as e:
                logger.error(f"Error reading project names from refactoring data: {e}")

        return sorted(projects)

    def validate_data_consistency(self, readability_metrics: List[ReadabilityMetric],
                                  refactoring_data: List[RefactoringData]) -> Dict[str, int]:
        readability_commits = set(m.commit_id for m in readability_metrics)
        refactoring_commits = set()

        for r in refactoring_data:
            refactoring_commits.add(r.refactoring_commit_id)
            refactoring_commits.add(r.parent_commit_id)

        common_commits = readability_commits.intersection(refactoring_commits)

        return {
            'readability_commits': len(readability_commits),
            'refactoring_commits': len(refactoring_commits),
            'common_commits': len(common_commits),
            'readability_only': len(readability_commits - refactoring_commits),
            'refactoring_only': len(refactoring_commits - readability_commits)
        }