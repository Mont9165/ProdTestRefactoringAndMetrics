
import argparse
import logging
import sys
from pathlib import Path
from typing import Optional
import json

from data_loader import DataLoader
from readability_analyzer import ReadabilityAnalyzer
from visualization import ReadabilityVisualizer
# from report_generator import ReportGenerator

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('analysis.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)


def main():
    parser = argparse.ArgumentParser(description='Refactoring and Readability Analysis')
    parser.add_argument('--base-dir', type=str, default='.',
                        help='Base directory containing data files')
    parser.add_argument('--project-filter', type=str, default=None,
                        help='Filter analysis by project name (partial match)')
    parser.add_argument('--output-dir', type=str, default='analysis_output',
                        help='Output directory for results')
    parser.add_argument('--generate-plots', action='store_true',
                        help='Generate visualization plots')
    parser.add_argument('--generate-report', action='store_true',
                        help='Generate detailed report')
    parser.add_argument('--verbose', action='store_true',
                        help='Enable verbose logging')

    args = parser.parse_args()

    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)

    logger.info("Starting Refactoring and Readability Analysis")
    logger.info(f"Base directory: {args.base_dir}")
    logger.info(f"Output directory: {args.output_dir}")

    try:
        logger.info("Loading data...")
        loader = DataLoader(args.base_dir)

        available_projects = loader.get_available_projects()
        logger.info(f"Available projects: {available_projects}")

        readability_metrics = loader.load_readability_metrics(args.project_filter)
        refactoring_data = loader.load_refactoring_data(args.project_filter)

        if not readability_metrics:
            logger.error("No readability metrics loaded. Check data files.")
            return 1

        if not refactoring_data:
            logger.error("No refactoring data loaded. Check data files.")
            return 1

        consistency = loader.validate_data_consistency(readability_metrics, refactoring_data)
        logger.info(f"Data consistency check: {consistency}")

        logger.info("Performing readability analysis...")
        analyzer = ReadabilityAnalyzer()
        analyzer.load_data(readability_metrics, refactoring_data)

        impacts = analyzer.analyze_refactoring_impact()
        logger.info(f"Analyzed {len(impacts)} refactoring impacts")

        if not impacts:
            logger.warning("No refactoring impacts found. Check data alignment.")
            return 1

        project_results = analyzer.analyze_by_project(impacts)
        logger.info(f"Analyzed {len(project_results)} projects")

        summary_stats = analyzer.get_summary_statistics(impacts)
        logger.info("Summary statistics calculated")

        output_dir = Path(args.output_dir)
        output_dir.mkdir(exist_ok=True)

        save_basic_results(impacts, project_results, summary_stats, output_dir)

        if args.generate_plots:
            logger.info("Generating visualizations...")
            visualizer = ReadabilityVisualizer(str(output_dir / 'plots'))
            visualizer.plot_readability_change_distribution(impacts)
            visualizer.plot_test_vs_production_comparison(impacts)
            visualizer.plot_refactoring_type_analysis(impacts)
            visualizer.plot_refactoring_type_comparison(impacts)
            visualizer.plot_project_comparison(project_results)
            visualizer.create_summary_report(impacts, project_results, summary_stats)
            visualizer.save_detailed_results(impacts, project_results)

            logger.info("Visualizations completed")

        refactoring_type_analysis = analyzer.analyze_refactoring_type_impact(impacts)
        with open(output_dir / 'refactoring_type_analysis.json', 'w') as f:
            json.dump(refactoring_type_analysis, f, indent=2)

        # detail report generation (commented out for now)
        # if args.generate_report:
        #     logger.info("Generating detailed report...")
        #     report_generator = ReportGenerator(str(output_dir))
        #     report_generator.generate_comprehensive_report(
        #         impacts, project_results, summary_stats, consistency
        #     )
        #     logger.info("Report generation completed")

        print_summary(impacts, project_results, summary_stats)

        logger.info("Analysis completed successfully")
        return 0

    except Exception as e:
        logger.error(f"Analysis failed: {e}", exc_info=True)
        return 1


def save_basic_results(impacts, project_results, summary_stats, output_dir):
    import pandas as pd

    with open(output_dir / 'summary_statistics.json', 'w') as f:
        json.dump(summary_stats, f, indent=2)

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
    df_impacts.to_csv(output_dir / 'readability_impacts.csv', index=False)

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
    df_projects.to_csv(output_dir / 'project_results.csv', index=False)


def print_summary(impacts, project_results, summary_stats):
    print("\n" + "="*60)
    print("REFACTORING AND READABILITY ANALYSIS SUMMARY")
    print("="*60)

    print(f"\nOverall Statistics:")
    print(f"  Total Impacts Analyzed: {summary_stats.get('total_impacts', 0)}")
    print(f"  Valid Changes: {summary_stats.get('valid_changes', 0)}")
    print(f"  Mean Readability Change: {summary_stats.get('mean_change', 0):.4f}")
    print(f"  Median Readability Change: {summary_stats.get('median_change', 0):.4f}")
    print(f"  Standard Deviation: {summary_stats.get('std_change', 0):.4f}")

    print(f"\nImpact Distribution:")
    print(f"  Positive Changes: {summary_stats.get('positive_changes', 0)}")
    print(f"  Negative Changes: {summary_stats.get('negative_changes', 0)}")
    print(f"  No Changes: {summary_stats.get('neutral_changes', 0)}")

    print(f"\nProject Analysis:")
    print(f"  Total Projects: {len(project_results)}")

    for project_name, result in project_results.items():
        print(f"\n  {project_name}:")
        print(f"    Total Refactorings: {result.total_refactorings}")
        print(f"    Test Files: {result.test_refactorings}")
        print(f"    Production Files: {result.production_refactorings}")
        if result.avg_readability_change_test is not None:
            print(f"    Avg Test Change: {result.avg_readability_change_test:.4f}")
        if result.avg_readability_change_production is not None:
            print(f"    Avg Production Change: {result.avg_readability_change_production:.4f}")

    print("\n" + "="*60)


if __name__ == "__main__":
    sys.exit(main())