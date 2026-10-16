"""Cache current public production responses for native store-asset rendering, without invented values."""
from pathlib import Path
import json
import time
import urllib.request

root=Path(__file__).resolve().parents[1]
out=root.parent/'.tools/android-build/store-capture-data'
out.mkdir(exist_ok=True)
endpoints={
    'prices':'https://api.kraken.com/0/public/Ticker?pair=XBTEUR,XBTUSD',
    'fees':'https://mempool.space/api/v1/fees/recommended',
    'mempool':'https://mempool.space/api/mempool',
    'block':'https://mempool.space/api/v1/blocks',
    'difficulty':'https://mempool.space/api/v1/difficulty-adjustment',
}
for period,interval in [('4h',240),('1d',1440),('1w',10080)]:
    endpoints[f'candles-EUR-{period}']=f'https://api.kraken.com/0/public/OHLC?pair=XBTEUR&interval={interval}'
for key,url in endpoints.items():
    request=urllib.request.Request(url,headers={'User-Agent':'BitcoinPulse/1.0 (Android)'})
    with urllib.request.urlopen(request,timeout=30) as response: raw=response.read().decode('utf-8')
    data=json.loads(raw)
    if 'error' in data: assert not data['error']
    (out/f'{key}.json').write_text(raw,encoding='utf-8')
    (out/f'{key}.time').write_text(str(int(time.time()*1000)))
    print(key+': public response captured')
    time.sleep(1.1)
