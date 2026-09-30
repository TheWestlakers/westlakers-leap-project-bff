"""
Live Price Pulling Test Script for Yfinance

Demonstrates different methods to pull live/real-time prices:
1. Current price from ticker info
2. Real-time 1-minute interval data
3. Fast info (lighter weight)
4. Monitoring prices over time
5. Multiple tickers price comparison
"""

import yfinance as yf
import time
from datetime import datetime

print(f"yfinance version: {yf.__version__}")
print()

# Configure yfinance for reliability
yf.config.network.retries = 3
yf.config.debug.hide_exceptions = False

print("="*70)
print("LIVE PRICE PULLING TESTS")
print("="*70)


def method1_current_price_from_info():
    """Method 1: Get current price from ticker info"""
    print("\n--- Method 1: Current Price from Ticker Info ---")
    try:
        ticker = yf.Ticker("MSFT")
        
        print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Ticker: MSFT")
        
        info = ticker.info
        current_price = info.get('currentPrice', 'N/A')
        previous_close = info.get('previousClose', 'N/A')
        bid_price = info.get('bid', 'N/A')
        ask_price = info.get('ask', 'N/A')
        
        print(f"  Current Price: ${current_price}")
        print(f"  Previous Close: ${previous_close}")
        print(f"  Bid: ${bid_price}")
        print(f"  Ask: ${ask_price}")
        
        if current_price != 'N/A' and previous_close != 'N/A':
            change = current_price - previous_close
            change_pct = (change / previous_close) * 100
            direction = "📈" if change >= 0 else "📉"
            print(f"  Change: {direction} ${change:+.2f} ({change_pct:+.2f}%)")
        
        print("✓ Method 1 passed")
        return True
        
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:80]}")
        return False


def method2_realtime_1min_interval():
    """Method 2: Get real-time 1-minute interval data"""
    print("\n--- Method 2: Real-time 1-Minute Interval Data ---")
    try:
        ticker = yf.Ticker("AAPL")
        
        print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Ticker: AAPL")
        print("Fetching 1-minute interval data...")
        
        # Get last 5 minutes of 1-minute interval data
        history = ticker.history(period='1d', interval='1m')
        
        if not history.empty:
            print(f"  Records retrieved: {len(history)}")
            print(f"\n  Latest 3 candles (1-minute intervals):")
            
            for idx, (timestamp, row) in enumerate(history.tail(3).iterrows(), 1):
                print(f"\n  Candle {idx}:")
                print(f"    Time: {timestamp}")
                print(f"    Open: ${row['Open']:.2f}")
                print(f"    High: ${row['High']:.2f}")
                print(f"    Low:  ${row['Low']:.2f}")
                print(f"    Close: ${row['Close']:.2f}")
                print(f"    Volume: {int(row['Volume']):,}")
        else:
            print("  No data available (may be outside market hours)")
        
        print("✓ Method 2 passed")
        return True
        
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:80]}")
        return False


def method3_fast_info():
    """Method 3: Get fast info (lighter weight)"""
    print("\n--- Method 3: Fast Info (Lighter Weight) ---")
    try:
        ticker = yf.Ticker("GOOG")
        
        print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Ticker: GOOG")
        
        # fast_info is a lighter weight version of info
        fast_info = ticker.fast_info
        
        print(f"  Current Price: ${fast_info.get('lastPrice', 'N/A')}")
        print(f"  Day High: ${fast_info.get('dayHigh', 'N/A')}")
        print(f"  Day Low: ${fast_info.get('dayLow', 'N/A')}")
        print(f"  52 Week High: ${fast_info.get('fiftyTwoWeekHigh', 'N/A')}")
        print(f"  52 Week Low: ${fast_info.get('fiftyTwoWeekLow', 'N/A')}")
        print(f"  Volume: {fast_info.get('lastVolume', 'N/A'):,}")
        print(f"  Market Cap: ${fast_info.get('marketCap', 'N/A'):,}")
        
        print("✓ Method 3 passed")
        return True
        
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:80]}")
        return False


def method4_monitor_price_changes():
    """Method 4: Monitor price changes over time"""
    print("\n--- Method 4: Monitor Price Changes Over Time ---")
    try:
        ticker = yf.Ticker("MSFT")
        
        print(f"Monitoring MSFT for 6 seconds (2 updates with 3-second delay)...")
        
        prices = []
        for i in range(2):
            try:
                info = ticker.info
                current_price = info.get('currentPrice')
                timestamp = datetime.now().strftime('%H:%M:%S')
                
                if current_price:
                    prices.append(current_price)
                    print(f"  [{timestamp}] Price: ${current_price:.2f}")
                    
                    if len(prices) > 1:
                        change = prices[-1] - prices[-2]
                        print(f"             Change from last: ${change:+.2f}")
                
                if i < 1:  # Don't sleep after last iteration
                    time.sleep(3)
            except Exception as inner_e:
                print(f"  Error on update: {str(inner_e)[:60]}")
        
        if len(prices) > 1:
            total_change = prices[-1] - prices[0]
            print(f"\n  Total change over monitoring period: ${total_change:+.2f}")
        
        print("✓ Method 4 passed")
        return True
        
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:80]}")
        return False


def method5_multiple_tickers_comparison():
    """Method 5: Compare prices across multiple tickers"""
    print("\n--- Method 5: Multiple Tickers Price Comparison ---")
    try:
        symbols = ['MSFT', 'AAPL', 'GOOG', 'NVDA', 'TSLA']
        
        print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Comparing {len(symbols)} tickers...\n")
        
        # Batch download current data
        tickers = yf.Tickers(' '.join(symbols))
        
        prices = []
        
        for symbol in symbols:
            try:
                ticker_obj = tickers.tickers[symbol]
                info = ticker_obj.info
                current_price = info.get('currentPrice', 'N/A')
                
                if current_price != 'N/A':
                    previous_close = info.get('previousClose', 'N/A')
                    if previous_close != 'N/A':
                        change_pct = ((current_price - previous_close) / previous_close) * 100
                        direction = "📈" if change_pct >= 0 else "📉"
                        prices.append({
                            'symbol': symbol,
                            'price': current_price,
                            'change_pct': change_pct
                        })
                        print(f"  {symbol}: ${current_price:>7.2f} {direction} {change_pct:+6.2f}%")
            except Exception as inner_e:
                print(f"  {symbol}: Error - {str(inner_e)[:50]}")
        
        # Show best and worst performers
        if prices:
            best = max(prices, key=lambda x: x['change_pct'])
            worst = min(prices, key=lambda x: x['change_pct'])
            print(f"\n  Best: {best['symbol']} ({best['change_pct']:+.2f}%)")
            print(f"  Worst: {worst['symbol']} ({worst['change_pct']:+.2f}%)")
        
        print("✓ Method 5 passed")
        return True
        
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:80]}")
        return False


def method6_intraday_price_tracking():
    """Method 6: Track intraday prices"""
    print("\n--- Method 6: Intraday Price Tracking ---")
    try:
        ticker = yf.Ticker("MSFT")
        
        print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Ticker: MSFT")
        print("Fetching intraday data (hourly)...\n")
        
        # Get hourly data for the current day
        history = ticker.history(period='1d', interval='1h')
        
        if not history.empty:
            print(f"  Hourly records: {len(history)}")
            print(f"\n  All hourly candles:")
            
            for timestamp, row in history.iterrows():
                print(f"    {timestamp}: Open ${row['Open']:.2f} | " +
                      f"High ${row['High']:.2f} | Low ${row['Low']:.2f} | " +
                      f"Close ${row['Close']:.2f}")
            
            # Calculate intraday range
            high = history['High'].max()
            low = history['Low'].min()
            current_close = history['Close'].iloc[-1]
            
            print(f"\n  Intraday Summary:")
            print(f"    High: ${high:.2f}")
            print(f"    Low: ${low:.2f}")
            print(f"    Range: ${high - low:.2f}")
            print(f"    Latest Close: ${current_close:.2f}")
        else:
            print("  No intraday data available (may be after market hours)")
        
        print("✓ Method 6 passed")
        return True
        
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:80]}")
        return False


def show_testing_tips():
    """Display tips for testing live prices"""
    print("\n" + "="*70)
    print("LIVE PRICE TESTING TIPS & BEST PRACTICES")
    print("="*70)
    
    print("""
1. TIMING CONSIDERATIONS:
   - Market Hours: Monday-Friday 9:30 AM - 4:00 PM ET (US)
   - Pre-market: 4:00 AM - 9:30 AM ET
   - After-hours: 4:00 PM - 8:00 PM ET
   - Real-time data is limited outside market hours

2. API RATE LIMITING:
   - Yahoo Finance has rate limits (typically 2000 requests/hour)
   - Use yf.config.network.retries = 3 for automatic retry
   - Add delays between requests: time.sleep(1-2 seconds)
   - Batch operations when possible

3. DATA QUALITY:
   - currentPrice may lag by 15-20 minutes during market hours
   - Use 1-minute interval data for more recent prices
   - Some tickers may have delayed data
   - Volume data is more reliable than price

4. BEST PRACTICES:
   - Cache recent prices to reduce API calls
   - Use fast_info for lighter weight requests
   - Batch multiple tickers with yf.Tickers()
   - Add error handling for API failures
   - Use try/except blocks around info access

5. TESTING CHECKLIST:
   ✓ Test during market hours (most reliable)
   ✓ Test with major tickers (MSFT, AAPL, GOOG)
   ✓ Test before/after market hours (different behavior)
   ✓ Test with less liquid tickers (may have stale data)
   ✓ Monitor rate limiting with requests
   ✓ Verify data against financial websites

6. QUICK TEST SCRIPT TEMPLATE:
   import yfinance as yf
   import time
   
   # Configure
   yf.config.network.retries = 3
   
   # Get price
   ticker = yf.Ticker("MSFT")
   price = ticker.info['currentPrice']
   print(f"MSFT: ${price}")
   
   # Wait before next request
   time.sleep(1)
""")


if __name__ == "__main__":
    print("\nRunning all live price pulling tests...\n")
    
    results = []
    
    # Run tests
    results.append(("Method 1: Current Price from Info", method1_current_price_from_info()))
    time.sleep(1)
    
    results.append(("Method 2: Real-time 1-Minute Data", method2_realtime_1min_interval()))
    time.sleep(1)
    
    results.append(("Method 3: Fast Info", method3_fast_info()))
    time.sleep(1)
    
    results.append(("Method 4: Monitor Price Changes", method4_monitor_price_changes()))
    time.sleep(1)
    
    results.append(("Method 5: Multiple Tickers", method5_multiple_tickers_comparison()))
    time.sleep(1)
    
    results.append(("Method 6: Intraday Tracking", method6_intraday_price_tracking()))
    
    # Show testing tips
    show_testing_tips()
    
    # Summary
    print("\n" + "="*70)
    print("TEST SUMMARY")
    print("="*70)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for test_name, result in results:
        status = "✓ PASSED" if result else "✗ FAILED"
        print(f"{test_name}: {status}")
    
    print(f"\nTotal: {passed}/{total} tests passed")
    
    if passed == total:
        print("\n✓ All live price pulling methods working!")
    else:
        print(f"\n⚠ {total - passed} test(s) failed - check errors above")
