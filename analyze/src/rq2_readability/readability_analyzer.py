from typing import List, Dict, Optional, Tuple
from collections import defaultdict
import logging
from data_models import ReadabilityMetric, RefactoringData, ReadabilityImpact, ProjectAnalysisResult
import statistics
import pandas as pd
import numpy as np

logger = logging.getLogger(__name__)


class ReadabilityAnalyzer:

    def __init__(self):
        self.readability_metrics: Dict[str, Dict[str, ReadabilityMetric]] = defaultdict(dict)
        self.refactoring_data: List[RefactoringData] = []

    def load_data(self, readability_metrics: List[ReadabilityMetric],
                  refactoring_data: List[RefactoringData]):
        for metric in readability_metrics:
            key = self._normalize_file_path(metric.file_path)
            self.readability_metrics[metric.commit_id][key] = metric

        self.refactoring_data = refactoring_data
        logger.info(f"Loaded {len(readability_metrics)} readability metrics and {len(refactoring_data)} refactoring records")

    def _normalize_file_path(self, file_path: str) -> str:
        if '/src/' in file_path:
            return file_path.split('/src/')[-1]
        elif 'src/' in file_path:
            return file_path.split('src/')[-1]
        return file_path.split('/')[-1] if '/' in file_path else file_path

    def analyze_refactoring_impact(self) -> List[ReadabilityImpact]:
        impacts = []

        for refactoring in self.refactoring_data:
            left_impact = self._analyze_file_impact(
                refactoring.refactoring_commit_id,
                refactoring.parent_commit_id,
                refactoring.left_file_path,
                refactoring.refactoring_name
            )
            if left_impact:
                impacts.append(left_impact)

            if refactoring.left_file_path != refactoring.right_file_path:
                right_impact = self._analyze_file_impact(
                    refactoring.refactoring_commit_id,
                    refactoring.parent_commit_id,
                    refactoring.right_file_path,
                    refactoring.refactoring_name
                )
                if right_impact:
                    impacts.append(right_impact)

        logger.info(f"Analyzed {len(impacts)} readability impacts")
        return impacts

    def _analyze_file_impact(self, after_commit: str, before_commit: str,
                             file_path: str, refactoring_name: str) -> Optional[ReadabilityImpact]:
        normalized_path = self._normalize_file_path(file_path)

        before_metric = self.readability_metrics.get(before_commit, {}).get(normalized_path)
        after_metric = self.readability_metrics.get(after_commit, {}).get(normalized_path)

        if not before_metric and not after_metric:
            return None

        before_score = before_metric.readability_score if before_metric else None
        after_score = after_metric.readability_score if after_metric else None

        impact = ReadabilityImpact(
            commit_id=after_commit,
            file_path=file_path,
            refactoring_name=refactoring_name,
            before_readability=before_score,
            after_readability=after_score,
            readability_change=None,
            is_test_file=self._is_test_file(file_path)
        )

        impact.calculate_change()
        return impact

    def _is_test_file(self, file_path: str) -> bool:
        return ('test' in file_path.lower() or
                'Test' in file_path or
                file_path.endswith('Test.java') or
                '/test/' in file_path)

    def analyze_by_project(self, impacts: List[ReadabilityImpact]) -> Dict[str, ProjectAnalysisResult]:
        project_impacts = defaultdict(list)

        for impact in impacts:
            project_name = self._extract_project_name(impact.file_path)
            project_impacts[project_name].append(impact)

        results = {}
        for project_name, project_impact_list in project_impacts.items():
            results[project_name] = self._calculate_project_result(project_name, project_impact_list)

        return results

    def _extract_project_name(self, file_path: str) -> str:
        parts = file_path.split('/')
        for i, part in enumerate(parts):
            if part in ['repos', 'x'] and i + 1 < len(parts):
                return parts[i + 1]
        return "unknown"

    def _calculate_project_result(self, project_name: str,
                                  impacts: List[ReadabilityImpact]) -> ProjectAnalysisResult:
        test_impacts = [i for i in impacts if i.is_test_file]
        production_impacts = [i for i in impacts if not i.is_test_file]

        test_changes = [i.readability_change for i in test_impacts if i.readability_change is not None]
        avg_test_change = statistics.mean(test_changes) if test_changes else None
        positive_test = len([c for c in test_changes if c > 0])
        negative_test = len([c for c in test_changes if c < 0])

        prod_changes = [i.readability_change for i in production_impacts if i.readability_change is not None]
        avg_prod_change = statistics.mean(prod_changes) if prod_changes else None
        positive_prod = len([c for c in prod_changes if c > 0])
        negative_prod = len([c for c in prod_changes if c < 0])

        return ProjectAnalysisResult(
            project_name=project_name,
            total_refactorings=len(impacts),
            test_refactorings=len(test_impacts),
            production_refactorings=len(production_impacts),
            avg_readability_change_test=avg_test_change,
            avg_readability_change_production=avg_prod_change,
            positive_impact_count_test=positive_test,
            positive_impact_count_production=positive_prod,
            negative_impact_count_test=negative_test,
            negative_impact_count_production=negative_prod
        )

    def get_summary_statistics(self, impacts: List[ReadabilityImpact]) -> Dict[str, float]:
        valid_changes = [i.readability_change for i in impacts if i.readability_change is not None]

        if not valid_changes:
            return {}

        return {
            'total_impacts': len(impacts),
            'valid_changes': len(valid_changes),
            'mean_change': statistics.mean(valid_changes),
            'median_change': statistics.median(valid_changes),
            'std_change': statistics.stdev(valid_changes) if len(valid_changes) > 1 else 0,
            'min_change': min(valid_changes),
            'max_change': max(valid_changes),
            'positive_changes': len([c for c in valid_changes if c > 0]),
            'negative_changes': len([c for c in valid_changes if c < 0]),
            'neutral_changes': len([c for c in valid_changes if c == 0])
        }

    def analyze_refactoring_type_impact(self, impacts: List[ReadabilityImpact]) -> Dict[str, Dict[str, float]]:
        type_impacts = {}

        for impact in impacts:
            if impact.readability_change is None:
                continue

            if impact.refactoring_name not in type_impacts:
                type_impacts[impact.refactoring_name] = {
                    'test': {'changes': [], 'count': 0},
                    'production': {'changes': [], 'count': 0}
                }

            category = 'test' if impact.is_test_file else 'production'
            type_impacts[impact.refactoring_name][category]['changes'].append(impact.readability_change)
            type_impacts[impact.refactoring_name][category]['count'] += 1

        results = {}
        for ref_type, data in type_impacts.items():
            results[ref_type] = {
                'test_mean': np.mean(data['test']['changes']) if data['test']['changes'] else None,
                'test_std': np.std(data['test']['changes']) if data['test']['changes'] else None,
                'test_count': data['test']['count'],
                'production_mean': np.mean(data['production']['changes']) if data['production']['changes'] else None,
                'production_std': np.std(data['production']['changes']) if data['production']['changes'] else None,
                'production_count': data['production']['count']
            }

        return results