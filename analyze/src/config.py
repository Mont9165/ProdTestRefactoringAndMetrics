# src/config.py
import os

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

DATA_DIR = os.path.join(PROJECT_ROOT, "data")
PROJECTS_TXT_PATH = os.path.join(DATA_DIR, "projects.txt")
REFACTORING_DETAILS_CSV_PATH = os.path.join(DATA_DIR, "refactoring_50000.csv")
PICKLE_FILEPATH = os.path.join(DATA_DIR, "processed_refactoring_data.feather")
CHUNK_SIZE = 500000

OUTPUTS_DIR = os.path.join(PROJECT_ROOT, "outputs")


JAVA_SOURCE_PATTERNS = [
    "src/main/java/",
    "src/java/"
]

TEST_SOURCE_PATTERNS = [
    "src/test/java/",
    "src/test/"
]

TEST_CODE_FILENAME_SUFFIXES = ["Test.java", "Tests.java"]
TEST_CODE_FILENAME_PREFIXES = ["Test"]


TYPE_METRICS_FILENAME = "typeMetrics.csv"
DESIGN_SMELLS_FILENAME = "designCodeSmells.csv"
IMPLEMENTATION_SMELLS_FILENAME = "implementationCodeSmells.csv"


TYPE_METRIC_COLUMNS_TO_ANALYZE = [
    "NOF", "NOPF", "NOM", "NOPM", "LOC", "WMC", "NC", "DIT", "LCOM", "FANIN", "FANOUT"
]

IMPLEMENTATION_SMELL_TYPES_TO_ANALYZE = [
    "Abstract Function Call From Constructor",
    "Complex Conditional",
    "Complex Method",
    "Empty catch clause",
    "Long Identifier",
    "Long Method",
    "Long Parameter List",
    "Long Statement",
    "Magic Number",
    "Missing default"
]

LOG_LEVEL = "INFO"
LOG_FORMAT = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"