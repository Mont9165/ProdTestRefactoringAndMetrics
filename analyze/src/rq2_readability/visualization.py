"""
可視化クラス
分析結果をグラフやチャートで可視化
"""
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
from typing import List, Dict
import numpy as np
from pathlib import Path
from data_models import ReadabilityImpact, ProjectAnalysisResult
import logging

logger = logging.getLogger(__name__)



class ReadabilityVisualizer:

    def __init__(self, output_dir: str = "output_plots"):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(exist_ok=True)

        sns.set_style("whitegrid")
        plt.style.use('seaborn-v0_8')

    def plot_readability_change_distribution(self, impacts: List[ReadabilityImpact],
                                             title: str = "Readability Change Distribution"):
        valid_changes = [i.readability_change for i in impacts if i.readability_change is not None]

        if not valid_changes:
            logger.warning("No valid readability changes to plot")
            return

        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))

        ax1.hist(valid_changes, bins=30, alpha=0.7, edgecolor='black')
        ax1.axvline(x=0, color='red', linestyle='--', alpha=0.8, label='No Change')
        ax1.set_xlabel('Readability Change')
        ax1.set_ylabel('Frequency')
        ax1.set_title(f'{title} - Histogram')
        ax1.legend()
        ax1.grid(True, alpha=0.3)

        ax2.boxplot(valid_changes, vert=True)
        ax2.axvline(x=0, color='red', linestyle='--', alpha=0.8, label='No Change')
        ax2.set_ylabel('Readability Change')
        ax2.set_title(f'{title} - Box Plot')
        ax2.grid(True, alpha=0.3)

        plt.tight_layout()
        plt.savefig(self.output_dir / f'readability_change_distribution.png', dpi=300, bbox_inches='tight')
        plt.show()

    def plot_test_vs_production_comparison(self, impacts: List[ReadabilityImpact]):
        test_changes = [i.readability_change for i in impacts if i.is_test_file and i.readability_change is not None]
        prod_changes = [i.readability_change for i in impacts if not i.is_test_file and i.readability_change is not None]

        if not test_changes and not prod_changes:
            logger.warning("No data for test vs production comparison")
            return

        fig, axes = plt.subplots(2, 2, figsize=(15, 12))

        if test_changes:
            axes[0, 0].hist(test_changes, bins=20, alpha=0.7, label='Test Files', color='skyblue')
        if prod_changes:
            axes[0, 0].hist(prod_changes, bins=20, alpha=0.7, label='Production Files', color='lightcoral')
        axes[0, 0].axvline(x=0, color='red', linestyle='--', alpha=0.8)
        axes[0, 0].set_xlabel('Readability Change')
        axes[0, 0].set_ylabel('Frequency')
        axes[0, 0].set_title('Test vs Production - Histogram')
        axes[0, 0].legend()
        axes[0, 0].grid(True, alpha=0.3)

        data_to_plot = []
        labels = []
        if test_changes:
            data_to_plot.append(test_changes)
            labels.append('Test Files')
        if prod_changes:
            data_to_plot.append(prod_changes)
            labels.append('Production Files')

        if data_to_plot:
            axes[0, 1].boxplot(data_to_plot, labels=labels)
            axes[0, 1].axhline(y=0, color='red', linestyle='--', alpha=0.8)
            axes[0, 1].set_ylabel('Readability Change')
            axes[0, 1].set_title('Test vs Production - Box Plot')
            axes[0, 1].grid(True, alpha=0.3)

        stats_data = []
        if test_changes:
            stats_data.append(['Test Files', len(test_changes), np.mean(test_changes),
                               np.median(test_changes), np.std(test_changes)])
        if prod_changes:
            stats_data.append(['Production Files', len(prod_changes), np.mean(prod_changes),
                               np.median(prod_changes), np.std(prod_changes)])

        if stats_data:
            df_stats = pd.DataFrame(stats_data, columns=['Type', 'Count', 'Mean', 'Median', 'Std'])
            axes[1, 0].axis('tight')
            axes[1, 0].axis('off')
            table = axes[1, 0].table(cellText=df_stats.round(4).values,
                                     colLabels=df_stats.columns,
                                     cellLoc='center', loc='center')
            table.auto_set_font_size(False)
            table.set_fontsize(10)
            axes[1, 0].set_title('Statistical Summary')

        if test_changes or prod_changes:
            categories = []
            test_counts = []
            prod_counts = []

            if test_changes:
                test_improved = len([c for c in test_changes if c > 0])
                test_degraded = len([c for c in test_changes if c < 0])
                test_unchanged = len([c for c in test_changes if c == 0])
            else:
                test_improved = test_degraded = test_unchanged = 0

            if prod_changes:
                prod_improved = len([c for c in prod_changes if c > 0])
                prod_degraded = len([c for c in prod_changes if c < 0])
                prod_unchanged = len([c for c in prod_changes if c == 0])
            else:
                prod_improved = prod_degraded = prod_unchanged = 0

            categories = ['Improved', 'Degraded', 'Unchanged']
            test_counts = [test_improved, test_degraded, test_unchanged]
            prod_counts = [prod_improved, prod_degraded, prod_unchanged]

            x = np.arange(len(categories))
            width = 0.35

            axes[1, 1].bar(x - width/2, test_counts, width, label='Test Files', color='skyblue')
            axes[1, 1].bar(x + width/2, prod_counts, width, label='Production Files', color='lightcoral')
            axes[1, 1].set_xlabel('Change Category')
            axes[1, 1].set_ylabel('Count')
            axes[1, 1].set_title('Improvement vs Degradation Count')
            axes[1, 1].set_xticks(x)
            axes[1, 1].set_xticklabels(categories)
            axes[1, 1].legend()
            axes[1, 1].grid(True, alpha=0.3)

        plt.tight_layout()
        plt.savefig(self.output_dir / 'test_vs_production_comparison.png', dpi=300, bbox_inches='tight')
        plt.show()

    def plot_refactoring_type_analysis(self, impacts: List[ReadabilityImpact]):
        refactoring_impacts = {}

        for impact in impacts:
            if impact.readability_change is not None:
                refactoring_type = impact.refactoring_name
                if refactoring_type not in refactoring_impacts:
                    refactoring_impacts[refactoring_type] = []
                refactoring_impacts[refactoring_type].append(impact.readability_change)

        if not refactoring_impacts:
            logger.warning("No data for refactoring type analysis")
            return

        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))

        data_to_plot = list(refactoring_impacts.values())
        labels = list(refactoring_impacts.keys())

        box_plot = ax1.boxplot(data_to_plot, labels=labels, patch_artist=True)
        ax1.axhline(y=0, color='red', linestyle='--', alpha=0.8, label='No Change')
        ax1.set_xlabel('Refactoring Type')
        ax1.set_ylabel('Readability Change')
        ax1.set_title('Readability Change by Refactoring Type')
        ax1.tick_params(axis='x', rotation=45)
        ax1.grid(True, alpha=0.3)
        ax1.legend()

        colors = plt.cm.Set3(np.linspace(0, 1, len(box_plot['boxes'])))
        for patch, color in zip(box_plot['boxes'], colors):
            patch.set_facecolor(color)

        means = [np.mean(changes) for changes in refactoring_impacts.values()]
        bars = ax2.bar(labels, means, color=colors)
        ax2.axhline(y=0, color='red', linestyle='--', alpha=0.8, label='No Change')
        ax2.set_xlabel('Refactoring Type')
        ax2.set_ylabel('Average Readability Change')
        ax2.set_title('Average Readability Change by Refactoring Type')
        ax2.tick_params(axis='x', rotation=45)
        ax2.grid(True, alpha=0.3)
        ax2.legend()

        for bar, mean in zip(bars, means):
            height = bar.get_height()
            ax2.text(bar.get_x() + bar.get_width()/2., height + (0.01 if height >= 0 else -0.01),
                     f'{mean:.3f}', ha='center', va='bottom' if height >= 0 else 'top')

        plt.tight_layout()
        plt.savefig(self.output_dir / 'refactoring_type_analysis.png', dpi=300, bbox_inches='tight')
        plt.show()

    def plot_refactoring_type_comparison(self, impacts: List[ReadabilityImpact]):
        test_data = {}
        prod_data = {}

        for impact in impacts:
            if impact.readability_change is None:
                continue

            ref_type = impact.refactoring_name
            if impact.is_test_file:
                if ref_type not in test_data:
                    test_data[ref_type] = []
                test_data[ref_type].append(impact.readability_change)
            else:
                if ref_type not in prod_data:
                    prod_data[ref_type] = []
                prod_data[ref_type].append(impact.readability_change)

        if not test_data and not prod_data:
            logger.warning("No data for refactoring type comparison")
            return

        common_types = sorted(set(test_data.keys()) & set(prod_data.keys()))
        if not common_types:
            logger.warning("No common refactoring types between test and production code")
            return

        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))

        test_means = [np.mean(test_data[t]) for t in common_types]
        prod_means = [np.mean(prod_data[t]) for t in common_types]

        x = np.arange(len(common_types))
        width = 0.35

        ax1.bar(x - width/2, test_means, width, label='Test Files', color='skyblue')
        ax1.bar(x + width/2, prod_means, width, label='Production Files', color='lightcoral')
        ax1.axhline(y=0, color='red', linestyle='--', alpha=0.8, label='No Change')
        ax1.set_xlabel('Refactoring Type')
        ax1.set_ylabel('Average Readability Change')
        ax1.set_title('Average Readability Change by Refactoring Type')
        ax1.set_xticks(x)
        ax1.set_xticklabels(common_types, rotation=45)
        ax1.legend()
        ax1.grid(True, alpha=0.3)

        test_counts = [len(test_data[t]) for t in common_types]
        prod_counts = [len(prod_data[t]) for t in common_types]

        ax2.bar(x - width/2, test_counts, width, label='Test Files', color='skyblue')
        ax2.bar(x + width/2, prod_counts, width, label='Production Files', color='lightcoral')
        ax2.set_xlabel('Refactoring Type')
        ax2.set_ylabel('Number of Refactorings')
        ax2.set_title('Number of Refactorings by Type')
        ax2.set_xticks(x)
        ax2.set_xticklabels(common_types, rotation=45)
        ax2.legend()
        ax2.grid(True, alpha=0.3)

        plt.tight_layout()
        plt.savefig(self.output_dir / 'refactoring_type_comparison.png', dpi=300, bbox_inches='tight')
        plt.show()

    def plot_project_comparison(self, project_results: Dict[str, ProjectAnalysisResult]):
        """プロジェクト間の比較"""
        if not project_results:
            logger.warning("No project results to plot")
            return

        fig, axes = plt.subplots(2, 2, figsize=(16, 12))

        projects = list(project_results.keys())
        test_avg_changes = [r.avg_readability_change_test or 0 for r in project_results.values()]
        prod_avg_changes = [r.avg_readability_change_production or 0 for r in project_results.values()]

        x = np.arange(len(projects))
        width = 0.35

        axes[0, 0].bar(x - width/2, test_avg_changes, width, label='Test Files', color='skyblue')
        axes[0, 0].bar(x + width/2, prod_avg_changes, width, label='Production Files', color='lightcoral')
        axes[0, 0].axhline(y=0, color='red', linestyle='--', alpha=0.8)
        axes[0, 0].set_xlabel('Project')
        axes[0, 0].set_ylabel('Average Readability Change')
        axes[0, 0].set_title('Average Readability Change by Project')
        axes[0, 0].set_xticks(x)
        axes[0, 0].set_xticklabels(projects, rotation=45)
        axes[0, 0].legend()
        axes[0, 0].grid(True, alpha=0.3)

        test_counts = [r.test_refactorings for r in project_results.values()]
        prod_counts = [r.production_refactorings for r in project_results.values()]

        axes[0, 1].bar(x - width/2, test_counts, width, label='Test Files', color='skyblue')
        axes[0, 1].bar(x + width/2, prod_counts, width, label='Production Files', color='lightcoral')
        axes[0, 1].set_xlabel('Project')
        axes[0, 1].set_ylabel('Number of Refactorings')
        axes[0, 1].set_title('Number of Refactorings by Project')
        axes[0, 1].set_xticks(x)
        axes[0, 1].set_xticklabels(projects, rotation=45)
        axes[0, 1].legend()
        axes[0, 1].grid(True, alpha=0.3)

        test_positive = [r.positive_impact_count_test for r in project_results.values()]
        test_negative = [r.negative_impact_count_test for r in project_results.values()]

        axes[1, 0].bar(x, test_positive, label='Positive Impact', color='green', alpha=0.7)
        axes[1, 0].bar(x, test_negative, bottom=test_positive, label='Negative Impact', color='red', alpha=0.7)
        axes[1, 0].set_xlabel('Project')
        axes[1, 0].set_ylabel('Count')
        axes[1, 0].set_title('Test Files: Positive vs Negative Impact')
        axes[1, 0].set_xticks(x)
        axes[1, 0].set_xticklabels(projects, rotation=45)
        axes[1, 0].legend()
        axes[1, 0].grid(True, alpha=0.3)

        prod_positive = [r.positive_impact_count_production for r in project_results.values()]
        prod_negative = [r.negative_impact_count_production for r in project_results.values()]

        axes[1, 1].bar(x, prod_positive, label='Positive Impact', color='green', alpha=0.7)
        axes[1, 1].bar(x, prod_negative, bottom=prod_positive, label='Negative Impact', color='red', alpha=0.7)
        axes[1, 1].set_xlabel('Project')
        axes[1, 1].set_ylabel('Count')
        axes[1, 1].set_title('Production Files: Positive vs Negative Impact')
        axes[1, 1].set_xticks(x)
        axes[1, 1].set_xticklabels(projects, rotation=45)
        axes[1, 1].legend()
        axes[1, 1].grid(True, alpha=0.3)

        plt.tight_layout()
        plt.savefig(self.output_dir / 'project_comparison.png', dpi=300, bbox_inches='tight')
        plt.show()

    def create_summary_report(self, impacts: List[ReadabilityImpact],
                              project_results: Dict[str, ProjectAnalysisResult],
                              summary_stats: Dict[str, float]):
        fig, axes = plt.subplots(2, 3, figsize=(18, 12))

        stats_text = f"""
        Total Impacts: {summary_stats.get('total_impacts', 0)}
        Valid Changes: {summary_stats.get('valid_changes', 0)}
        Mean Change: {summary_stats.get('mean_change', 0):.4f}
        Median Change: {summary_stats.get('median_change', 0):.4f}
        Std Deviation: {summary_stats.get('std_change', 0):.4f}
        Min Change: {summary_stats.get('min_change', 0):.4f}
        Max Change: {summary_stats.get('max_change', 0):.4f}
        """

        axes[0, 0].text(0.1, 0.5, stats_text, fontsize=12, verticalalignment='center')
        axes[0, 0].set_title('Overall Statistics')
        axes[0, 0].axis('off')

        positive = summary_stats.get('positive_changes', 0)
        negative = summary_stats.get('negative_changes', 0)
        neutral = summary_stats.get('neutral_changes', 0)

        if positive + negative + neutral > 0:
            sizes = [positive, negative, neutral]
            labels = ['Improved', 'Degraded', 'No Change']
            colors = ['green', 'red', 'gray']

            axes[0, 1].pie(sizes, labels=labels, colors=colors, autopct='%1.1f%%', startangle=90)
            axes[0, 1].set_title('Overall Impact Distribution')

        test_impacts = [i for i in impacts if i.is_test_file and i.readability_change is not None]
        prod_impacts = [i for i in impacts if not i.is_test_file and i.readability_change is not None]

        comparison_data = {
            'File Type': ['Test Files', 'Production Files'],
            'Count': [len(test_impacts), len(prod_impacts)],
            'Mean Change': [
                np.mean([i.readability_change for i in test_impacts]) if test_impacts else 0,
                np.mean([i.readability_change for i in prod_impacts]) if prod_impacts else 0
            ],
            'Positive': [
                len([i for i in test_impacts if i.readability_change > 0]),
                len([i for i in prod_impacts if i.readability_change > 0])
            ]
        }

        df_comparison = pd.DataFrame(comparison_data)
        axes[0, 2].axis('tight')
        axes[0, 2].axis('off')
        table = axes[0, 2].table(cellText=df_comparison.round(4).values,
                                 colLabels=df_comparison.columns,
                                 cellLoc='center', loc='center')
        table.auto_set_font_size(False)
        table.set_fontsize(10)
        axes[0, 2].set_title('Test vs Production Summary')

        project_info = f"""
        Number of Projects: {len(project_results)}
        Projects Analyzed:
        {chr(10).join(f"• {name}" for name in project_results.keys())}
        """

        axes[1, 0].text(0.1, 0.5, project_info, fontsize=10, verticalalignment='center')
        axes[1, 0].set_title('Project Information')
        axes[1, 0].axis('off')

        refactoring_types = {}
        for impact in impacts:
            ref_type = impact.refactoring_name
            refactoring_types[ref_type] = refactoring_types.get(ref_type, 0) + 1

        if refactoring_types:
            types = list(refactoring_types.keys())
            counts = list(refactoring_types.values())

            axes[1, 1].bar(range(len(types)), counts, color='steelblue')
            axes[1, 1].set_xlabel('Refactoring Type')
            axes[1, 1].set_ylabel('Count')
            axes[1, 1].set_title('Refactoring Type Distribution')
            axes[1, 1].set_xticks(range(len(types)))
            axes[1, 1].set_xticklabels(types, rotation=45)
            axes[1, 1].grid(True, alpha=0.3)

        recommendations = []

        if summary_stats.get('mean_change', 0) > 0:
            recommendations.append("• Overall positive impact on readability")
        else:
            recommendations.append("• Consider reviewing refactoring practices")

        if len(test_impacts) > 0 and len(prod_impacts) > 0:
            test_mean = np.mean([i.readability_change for i in test_impacts])
            prod_mean = np.mean([i.readability_change for i in prod_impacts])
            if test_mean > prod_mean:
                recommendations.append("• Test file refactoring shows better results")
            else:
                recommendations.append("• Production file refactoring shows better results")

        recommendations.append(f"• {positive} improvements vs {negative} degradations")

        rec_text = "Recommendations:\n" + "\n".join(recommendations)
        axes[1, 2].text(0.1, 0.5, rec_text, fontsize=10, verticalalignment='center')
        axes[1, 2].set_title('Analysis Recommendations')
        axes[1, 2].axis('off')

        plt.tight_layout()
        plt.savefig(self.output_dir / 'summary_report.png', dpi=300, bbox_inches='tight')
        plt.show()

    def save_detailed_results(self, impacts: List[ReadabilityImpact],
                              project_results: Dict[str, ProjectAnalysisResult]):
        impact_data = []
        for impact in impacts:
            impact_data.append({
                'commit_id': impact.commit_id,
                'file_path': impact.file_path,
                'refactoring_name': impact.refactoring_name,
                'before_readability': impact.before_readability,
                'after_readability': impact.after_readability,
                'readability_change': impact.readability_change,
                'is_test_file': impact.is_test_file,
                'improvement_category': impact.get_improvement_category()
            })

        df_impacts = pd.DataFrame(impact_data)
        df_impacts.to_csv(self.output_dir / 'detailed_impacts.csv', index=False)

        project_data = []
        for project_name, result in project_results.items():
            project_data.append({
                'project_name': result.project_name,
                'total_refactorings': result.total_refactorings,
                'test_refactorings': result.test_refactorings,
                'production_refactorings': result.production_refactorings,
                'avg_readability_change_test': result.avg_readability_change_test,
                'avg_readability_change_production': result.avg_readability_change_production,
                'positive_impact_count_test': result.positive_impact_count_test,
                'positive_impact_count_production': result.positive_impact_count_production,
                'negative_impact_count_test': result.negative_impact_count_test,
                'negative_impact_count_production': result.negative_impact_count_production
            })

        df_projects = pd.DataFrame(project_data)
        df_projects.to_csv(self.output_dir / 'project_analysis_results.csv', index=False)

        logger.info(f"Detailed results saved to {self.output_dir}")