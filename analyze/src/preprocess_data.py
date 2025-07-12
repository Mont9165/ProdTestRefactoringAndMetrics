# preprocess_data.py
from datetime import datetime

import pandas as pd
import os
import logging
from src import config, main_processor, data_loader


def setup_logging():
    log_dir = "logs"
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    log_filename = os.path.join(log_dir, f"analysis_log_{timestamp}.log")
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)
    if logger.hasHandlers():
        logger.handlers.clear()
    log_format = config.LOG_FORMAT
    formatter = logging.Formatter(log_format)
    file_handler = logging.FileHandler(log_filename, encoding='utf-8')
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)
    stream_handler = logging.StreamHandler()
    stream_handler.setFormatter(formatter)
    logger.addHandler(stream_handler)
    return logging.getLogger(__name__)


def generate_processed_data_pickle():
    logger = logging.getLogger(__name__)
    logging.basicConfig(level=config.LOG_LEVEL, format=config.LOG_FORMAT)

    logger.info("Starting data preprocessing to generate pickle file...")
    processed_df = main_processor.process_refactorings()

    if processed_df is not None and not processed_df.empty:
        logger.info(f"Saving processed data to '{config.PICKLE_FILEPATH}'... Shape: {processed_df.shape}")
        try:
            processed_df.to_pickle(config.PICKLE_FILEPATH) # cite: pickle
            # processed_df.to_feather(pickle_filepath) # Feather
            # processed_df.to_parquet(pickle_filepath) # Parquet
            logger.info(f"Successfully saved processed data to '{config.PICKLE_FILEPATH}'.")
        except Exception as e:
            logger.error(f"Failed to save processed data to '{config.PICKLE_FILEPATH}': {e}", exc_info=True)
    elif processed_df is not None and processed_df.empty:
        logger.warning("Processed data is empty. Pickle file not created.")
    else:
        logger.error("Data processing failed. Pickle file not created.")


if __name__ == "__main__":
    generate_processed_data_pickle()
    logger_main = logging.getLogger(__name__)
    logger_main.info("Data preprocessing script finished.")