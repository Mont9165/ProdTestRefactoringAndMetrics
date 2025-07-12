#!/bin/bash

#SBATCH --job-name=my_parallel_job          # ジョブ名
#SBATCH --output=logs/job_%A_%a.out         # 標準出力のログファイル
#SBATCH --error=errors/job_%A_%a.err        # 標準エラーのログファイル
#SBATCH --array=0-468
#SBATCH --time=4-04:00:00                    # 実行時間の制限
#SBATCH --partition=cluster_long            # 使用するパーティション
#SBATCH --ntasks=1                          # 1タスク
#SBATCH --cpus-per-task=8                   # 各タスクに割り当てるCPU数
#SBATCH --mem=64G                           # 各タスクのメモリ

# Singularityモジュールをロード
module load singularity

# Singularityイメージを実行
singularity exec test-refactoring-analysis.sif java  --add-opens java.base/java.lang=ALL-UNNAMED -jar /usr/work/collect-refactoring.jar $SLURM_ARRAY_TASK_ID