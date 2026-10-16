"""Read-only compatibility check against the public production endpoints."""
import json
import time
import urllib.request
from pathlib import Path

def get(url):
    request=urllib.request.Request(url,headers={"User-Agent":"BitcoinPulse/1.1 compatibility check"})
    with urllib.request.urlopen(request,timeout=30) as response:
        return json.load(response)

report=[]
for currency in ("EUR","USD"):
    for interval in (240,1440,10080):
        data=get(f"https://api.kraken.com/0/public/OHLC?pair=XBT{currency}&interval={interval}")
        assert not data["error"],data["error"]
        rows=data["result"][f"XXBTZ{currency}"]
        assert rows and all(len(r)>=8 for r in rows)
        for r in rows[-36:]:
            o,h,l,c=map(float,r[1:5]); assert h>=max(o,c) and l<=min(o,c) and l>0
        report.append(f"Kraken BTC/{currency} {interval} min: PASS, {len(rows)} candles; latest open {rows[-1][0]}")
        time.sleep(1.1)
blocks=get("https://mempool.space/api/v1/blocks")
b=blocks[0]; e=b["extras"]
assert b["height"]>0 and b["tx_count"]>=0 and e["medianFee"]>=0
report.append(f"Latest block: PASS, height {b['height']}, tx {b['tx_count']}, pool {e.get('pool',{}).get('name','Unknown')}, median {e['medianFee']} sat/vB, weight {b['weight']}")
d=get("https://mempool.space/api/v1/difficulty-adjustment")
assert 0<=d["progressPercent"]<=100 and d["remainingBlocks"]>=0
report.append(f"Difficulty: PASS, estimate {d['difficultyChange']}%, remaining {d['remainingBlocks']} blocks")
report.insert(0,time.strftime("Checked %Y-%m-%d %H:%M:%S %Z"))
output="\n".join(report)
Path("deliverables/live-api-check-1.1.txt").write_text(output,encoding="utf-8")
print(output)
