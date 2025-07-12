from dataclasses import dataclass
from typing import Optional, List
from enum import Enum

# FIXME: Define the work directory where the data files are stored
WORK_DIR = ''

class RefactoringType(Enum):
    EXTRACT_AND_MOVE_OPERATION = "EXTRACT_AND_MOVE_OPERATION"
    EXTRACT_METHOD = "EXTRACT_METHOD"
    MOVE_METHOD = "MOVE_METHOD"
    RENAME_METHOD = "RENAME_METHOD"
    INLINE_METHOD = "INLINE_METHOD"


@dataclass
class ReadabilityMetric:
    commit_id: str
    has_refactoring: bool
    file_path: str
    readability_score: float

    def get_normalized_path(self) -> str:
        parts = self.file_path.split('/')
        if 'src' in parts:
            src_index = parts.index('src')
            return '/'.join(parts[src_index:])
        return self.file_path


@dataclass
class RefactoringData:
    project_name: str
    refactoring_commit_id: str
    parent_commit_id: str
    refactoring_name: str
    refactoring_hash: int
    left_file_path: str
    left_start_line: float
    left_end_line: float
    right_file_path: str
    right_start_line: float
    right_end_line: float

    def get_normalized_left_path(self) -> str:
        return self.left_file_path.replace(WORK_DIR, '')

    def get_normalized_right_path(self) -> str:
        return self.right_file_path.replace(WORK_DIR, '')

    def is_test_file(self) -> bool:
        return ('test' in self.left_file_path.lower() or
                'test' in self.right_file_path.lower())


@dataclass
class ReadabilityImpact:
    commit_id: str
    file_path: str
    refactoring_name: str
    before_readability: Optional[float]
    after_readability: Optional[float]
    readability_change: Optional[float]
    is_test_file: bool

    def calculate_change(self) -> Optional[float]:
        if self.before_readability is not None and self.after_readability is not None:
            self.readability_change = self.after_readability - self.before_readability
            return self.readability_change
        return None

    def get_improvement_category(self) -> str:
        if self.readability_change is None:
            return "Unknown"
        elif self.readability_change > 0.1:
            return "Significant Improvement"
        elif self.readability_change > 0.05:
            return "Moderate Improvement"
        elif self.readability_change > -0.05:
            return "No Change"
        elif self.readability_change > -0.1:
            return "Moderate Degradation"
        else:
            return "Significant Degradation"


@dataclass
class ProjectAnalysisResult:
    project_name: str
    total_refactorings: int
    test_refactorings: int
    production_refactorings: int
    avg_readability_change_test: Optional[float]
    avg_readability_change_production: Optional[float]
    positive_impact_count_test: int
    positive_impact_count_production: int
    negative_impact_count_test: int
    negative_impact_count_production: int