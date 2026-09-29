"""
Main entry point for ETL pipeline execution
Orchestrates all analytics pipelines:
1. Trading Volumes - Hourly aggregation by instrument, account, client
2. Active Instruments - Daily top instruments, volatility, unique clients
3. Client Activity Trends - Daily client engagement and trading patterns
4. Client Segment Analysis - Daily segment performance and retention
"""
import logging
import sys
from config import ETLConfig
from trading_volumes_etl import TradingVolumesETL
from active_instruments_pipeline import ActiveInstrumentsPipeline
from client_activity_trends_pipeline import ClientActivityTrendsPipeline
from client_segment_analysis_pipeline import ClientSegmentAnalysisPipeline  

# Configure logging
logging.basicConfig(
    level=ETLConfig.LOG_LEVEL,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(ETLConfig.LOG_FILE),
        logging.StreamHandler(sys.stdout)
    ]
)

logger = logging.getLogger(__name__)


def run_all_pipelines(days_back: int = 30) -> int:
    """
    Run all ETL pipelines in sequence
    
    Args:
        days_back: Number of days to look back for data (default: 30)
        
    Returns:
        int: Exit code (0 for success, 1 for failure)
    """
    pipelines = [
        ("Trading Volumes", TradingVolumesETL()),
        ("Active Instruments", ActiveInstrumentsPipeline()),
        ("Client Activity Trends", ClientActivityTrendsPipeline()),
        ("Client Segment Analysis", ClientSegmentAnalysisPipeline())
    ]
    
    failed_pipelines = []
    
    for pipeline_name, pipeline in pipelines:
        logger.info(f"\nRunning {pipeline_name} Pipeline...")
        if not pipeline.run(days_back):
            logger.error(f"{pipeline_name} Pipeline failed")
            failed_pipelines.append(pipeline_name)
        else:
            logger.info(f"{pipeline_name} Pipeline completed successfully")
    
    if failed_pipelines:
        logger.error(f"\nFailed pipelines: {', '.join(failed_pipelines)}")
        return 1
    else:
        logger.info("\nAll pipelines completed successfully")
        return 0


def run_single_pipeline(pipeline_name: str, days_back: int = 30) -> int:
    """
    Run a single pipeline by name
    
    Args:
        pipeline_name: Name of pipeline to run
        days_back: Number of days to look back for data
        
    Returns:
        int: Exit code (0 for success, 1 for failure)
    """
    pipelines = {
        "trading_volumes": TradingVolumesETL(),
        "active_instruments": ActiveInstrumentsPipeline(),
        "client_activity_trends": ClientActivityTrendsPipeline(),
        "client_segment_analysis": ClientSegmentAnalysisPipeline()
    }
    
    pipeline = pipelines.get(pipeline_name.lower())
    if not pipeline:
        logger.error(f"Unknown pipeline: {pipeline_name}")
        logger.info(f"Available pipelines: {', '.join(pipelines.keys())}")
        return 1
    
    logger.info(f"Running {pipeline_name} Pipeline...")
    success = pipeline.run(days_back)
    return 0 if success else 1


def main():
    """Main entry point"""
    import argparse
    
    parser = argparse.ArgumentParser(description="Analytics ETL Pipeline Orchestrator")
    parser.add_argument("--pipeline", type=str, help="Run specific pipeline by name")
    parser.add_argument("--all", action="store_true", help="Run all pipelines (default)")
    parser.add_argument("--days-back", type=int, default=30, help="Days to look back (default: 30)")
    
    args = parser.parse_args()
    
    logger.info("=" * 80)
    logger.info("ETL Pipeline Manager started")
    logger.info("=" * 80)
    
    if args.pipeline:
        exit_code = run_single_pipeline(args.pipeline, args.days_back)
    else:
        # Default: run all pipelines
        exit_code = run_all_pipelines(args.days_back)
    
    sys.exit(exit_code)


if __name__ == '__main__':
    main()
