"""
Yfinance 1.7.0 Configuration and API Test Script

Demonstrates:
- Global config API with network, debug, and locale settings
- Retries with exponential backoff for reliability
- Different data fetching patterns
- Error handling
"""

import yfinance as yf
import time

print(f"yfinance version: {yf.__version__}")
print()

# ============================================================================
# CONFIGURATION SETUP - YFINANCE 1.7.0
# ============================================================================

print("="*70)
print("YFINANCE 1.7.0 CONFIGURATION SETUP")
print("="*70)

# Print default config
print("\n1. Default Config:")
print(yf.config)
print()

# Configure network settings
print("2. Network Configuration:")
print("   Setting up network retries with exponential backoff...")
yf.config.network.retries = 3
print(f"   ✓ Retries: {yf.config.network.retries}")
print(f"   → Exponential backoff: 1s, 2s, 4s...")

# Optional: Set proxy
# yf.config.network.proxy = "http://proxy.example.com:8080"

# Configure debug settings
print("\n3. Debug Configuration:")
yf.config.debug.logging = False  # Set to True for verbose logging
yf.config.debug.hide_exceptions = False
print(f"   ✓ Logging: {yf.config.debug.logging}")
print(f"   ✓ Hide exceptions: {yf.config.debug.hide_exceptions}")

# Check locale settings
print("\n4. Locale Settings:")
print(f"   Language: {yf.config.locale.lang}")
print(f"   Region: {yf.config.locale.region}")

print("\n" + "="*70)
print("API TESTS")
print("="*70 + "\n")


def test_single_ticker():
    """Test single ticker with detailed output"""
    print("\n--- Single Ticker: MSFT ---")
    try:
        dat = yf.Ticker("MSFT")
        
        print("\n1. Ticker Info:")
        if dat.info:
            print(f"   Company: {dat.info.get('longName', 'N/A')}")
            print(f"   Sector: {dat.info.get('sector', 'N/A')}")
            print(f"   Current Price: ${dat.info.get('currentPrice', 'N/A')}")
        else:
            print("   Info not available")
        
        print("\n2. Historical Data (1 Month):")
        history = dat.history(period='1mo')
        if not history.empty:
            print(f"   Records: {len(history)}")
            print(f"\n   Latest 3 records:")
            print(history.tail(3))
        else:
            print("   No data available")
        
        print("\n✓ Test passed")
            
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:100]}")


def test_multiple_tickers():
    """Test multiple tickers"""
    print("\n\n--- Multiple Tickers: MSFT, AAPL, GOOG ---")
    try:
        tickers = yf.Tickers('MSFT AAPL GOOG')
        
        print("\n1. Individual ticker info:")
        for symbol in ['MSFT', 'AAPL', 'GOOG']:
            ticker_obj = tickers.tickers[symbol]
            print(f"   {symbol}: Available")
        
        print("\n2. Batch download (1 Month):")
        data = yf.download(['MSFT', 'AAPL', 'GOOG'], period='1mo', progress=False)
        
        if not data.empty:
            print(f"   ✓ Downloaded {len(data)} records")
            print(f"   Columns: {list(data.columns)}")
            if 'Close' in data.columns:
                print(f"\n   Latest Close prices:")
                print(data[['Close']].tail(3))
        else:
            print("   No data available")
        
        print("\n✓ Test passed")
        
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:100]}")


def test_etf_data():
    """Test ETF/Fund data"""
    print("\n\n--- ETF Data: SPY ---")
    try:
        spy = yf.Ticker('SPY')
        
        print("\n1. SPY Historical Data (1 Month):")
        history = spy.history(period='1mo')
        
        if not history.empty:
            print(f"   Records: {len(history)}")
            print(f"\n   Latest 3 records:")
            print(history[['Close', 'Volume']].tail(3))
        else:
            print("   No data available")
        
        print("\n✓ Test passed")
            
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:100]}")


def test_locale_configuration():
    """Test locale configuration"""
    print("\n\n" + "="*70)
    print("LOCALE CONFIGURATION TESTING")
    print("="*70)
    
    try:
        # US Locale (default)
        print("\n1. US Locale (Default):")
        yf.config.locale.lang = "en-US"
        yf.config.locale.region = "US"
        msft = yf.Ticker("MSFT")
        if msft.info:
            print(f"   MSFT: {msft.info.get('longName', 'N/A')}")
        
        # Japanese Locale
        print("\n2. Japanese Locale (ja-JP):")
        time.sleep(1)
        yf.config.locale.lang = "ja-JP"
        yf.config.locale.region = "JP"
        toyota = yf.Ticker("7203.T")
        if toyota.info:
            print(f"   Toyota (7203.T): {toyota.info.get('longName', 'N/A')}")
        
        # Hong Kong Chinese Locale
        print("\n3. Hong Kong Chinese Locale (zh-Hant-HK):")
        time.sleep(1)
        yf.config.locale.lang = "zh-Hant-HK"
        yf.config.locale.region = "HK"
        xiaomi = yf.Ticker("1810.HK")
        if xiaomi.info:
            print(f"   Xiaomi (1810.HK): {xiaomi.info.get('longName', 'N/A')}")
        
        # Reset to US
        yf.config.locale.lang = "en-US"
        yf.config.locale.region = "US"
        
        print("\n✓ Locale tests completed")
        
    except Exception as e:
        print(f"✗ Error: {type(e).__name__}: {str(e)[:100]}")


def show_config_reference():
    """Display configuration reference"""
    print("\n\n" + "="*70)
    print("CONFIGURATION REFERENCE FOR YFINANCE 1.7.0")
    print("="*70)
    
    print("""
NETWORK CONFIGURATION:
  - Set proxy:
    yf.config.network.proxy = "http://proxy.example.com:8080"
  
  - Set retries (exponential backoff: 1s, 2s, 4s...):
    yf.config.network.retries = 3

DEBUG CONFIGURATION:
  - Enable verbose logging:
    yf.config.debug.logging = True
  
  - Show exceptions (don't hide them):
    yf.config.debug.hide_exceptions = False

LOCALE CONFIGURATION:
  - Change language and region:
    yf.config.locale.lang = "ja-JP"
    yf.config.locale.region = "JP"
  
  Available locales:
    - en-US / US (English - United States) [default]
    - ja-JP / JP (Japanese)
    - zh-Hant-HK / HK (Traditional Chinese - Hong Kong)
    - de-DE / DE (German)
    - fr-FR / FR (French)
    - it-IT / IT (Italian)
    - es-ES / ES (Spanish)

USAGE EXAMPLE:
  import yfinance as yf
  
  # Configure once at session start
  yf.config.network.retries = 3
  yf.config.debug.logging = False
  yf.config.locale.lang = "en-US"
  yf.config.locale.region = "US"
  
  # All subsequent calls use the configuration
  dat = yf.Ticker("MSFT")
  history = dat.history(period='1mo')
  info = dat.info
""")


if __name__ == "__main__":
    # Run API tests
    test_single_ticker()
    time.sleep(2)
    
    test_multiple_tickers()
    time.sleep(2)
    
    test_etf_data()
    time.sleep(2)
    
    # Test locale configuration (optional)
    # Uncomment to test different locales
    # test_locale_configuration()
    
    # Show configuration reference
    show_config_reference()
    
    # Final summary
    print("\n" + "="*70)
    print("TEST COMPLETE")
    print("="*70)
    print(f"\nYfinance Version: {yf.__version__}")
    print("\nActive Configuration:")
    print(f"  Network Retries: {yf.config.network.retries}")
    print(f"  Debug Logging: {yf.config.debug.logging}")
    print(f"  Hide Exceptions: {yf.config.debug.hide_exceptions}")
    print(f"  Locale: {yf.config.locale.lang} / {yf.config.locale.region}")
    print("\n✓ Configuration and API testing completed!")
