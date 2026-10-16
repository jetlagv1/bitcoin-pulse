"""Read-only smoke test of the exact public APIs used by the Android app."""
import json
import urllib.request

URLS = {
    "fees": "https://mempool.space/api/v1/fees/recommended",
    "mempool": "https://mempool.space/api/mempool",
    "blocks": "https://mempool.space/api/blocks",
    "prices": "https://api.kraken.com/0/public/Ticker?pair=XBTEUR,XBTUSD",
}

def fetch(url):
    request = urllib.request.Request(url, headers={"User-Agent": "BitcoinPulse/1.0 (API smoke test)"})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)

def check(name, value):
    if name == "fees":
        assert all(isinstance(value[k], (int, float)) and value[k] >= 0 for k in ("fastestFee", "halfHourFee", "hourFee"))
    elif name == "mempool":
        assert value["count"] >= 0 and value["vsize"] >= 0
    elif name == "blocks":
        assert value and all(value[0][k] >= 0 for k in ("height", "timestamp", "tx_count"))
    else:
        assert value["error"] == [], value["error"]
        for key in ("XXBTZEUR", "XXBTZUSD"):
            quote = value["result"][key]
            assert all(float(v) > 0 for v in (quote["c"][0], quote["o"], quote["h"][1], quote["l"][1]))

if __name__ == "__main__":
    for name, url in URLS.items():
        check(name, fetch(url))
        print(f"PASS: {name} response matches Android data contract")
