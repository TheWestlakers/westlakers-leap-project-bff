"""Smoke test for yfinance's asynchronous price websocket."""

import argparse
import asyncio
from datetime import datetime
from typing import Any, Dict, List, Set

import yfinance as yf


DEFAULT_TICKERS = ["AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "MU", "NFLX", "NVDA", "AMD", "BTC-USD"]


async def run_websocket(tickers: List[str], duration: float) -> Set[str]:
    """Subscribe to tickers, print messages, and return symbols received."""
    websocket = yf.AsyncWebSocket(verbose=False)
    received: Set[str] = set()
    message_count = 0

    def handle_message(message: Dict[str, Any]) -> None:
        nonlocal message_count
        ticker = message.get("id")
        if not ticker:
            return

        received.add(ticker)
        message_count += 1
        print(
            f"[{datetime.now().strftime('%H:%M:%S')}] "
            f"{ticker}: price={message.get('price')}"
        )

    listener = None
    try:
        print(f"Subscribing to: {', '.join(tickers)}")
        await websocket.subscribe(tickers)
        listener = asyncio.create_task(
            websocket.listen(message_handler=handle_message)
        )
        await asyncio.sleep(duration)
    finally:
        if listener is not None:
            listener.cancel()
            await asyncio.gather(listener, return_exceptions=True)
        await websocket.close()

    print(f"\nMessages received: {message_count}")
    return received


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--duration",
        type=float,
        default=10.0,
        help="Seconds to listen for messages (default: 10)",
    )
    parser.add_argument(
        "--tickers",
        default=",".join(DEFAULT_TICKERS),
        help="Comma-separated ticker symbols",
    )
    args = parser.parse_args()

    if args.duration <= 0:
        parser.error("--duration must be greater than zero")

    tickers = [ticker.strip().upper() for ticker in args.tickers.split(",") if ticker.strip()]
    if not tickers:
        parser.error("At least one ticker is required")

    try:
        received = asyncio.run(run_websocket(tickers, args.duration))
    except KeyboardInterrupt:
        print("\nStopped by user.")
        return

    missing = sorted(set(tickers) - received)
    print(f"Received symbols: {sorted(received)}")
    print(f"Coverage: {len(received)}/{len(tickers)}")
    if missing:
        print(f"Missing symbols: {missing}")
        raise SystemExit(1)

    print("Websocket test passed: all requested symbols received.")


if __name__ == "__main__":
    main()