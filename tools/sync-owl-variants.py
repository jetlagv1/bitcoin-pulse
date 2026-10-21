"""Generate import sizes from the canonical adaptive OWL program; no duplicated feature logic."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
source=(root/'app/src/main/assets/bitcoin-pulse.owl').read_text(encoding='utf-8')
for kind,size,width,height in [('mini','1x1',80,88),('compact','2x2',160,160)]:
    label='Mini 1×1' if kind=='mini' else 'Compact'
    variant=source.replace('widget "Bitcoin Pulse"','widget "Bitcoin Pulse · '+label+'"',1).replace('  size: 4x5','  size: '+size,1).replace('let initialWidth = 320','let initialWidth = '+str(width),1).replace('let initialHeight = 840','let initialHeight = '+str(height),1)
    (root/('app/src/main/assets/bitcoin-pulse-'+kind+'.owl')).write_text(variant,encoding='utf-8')
    print('Synchronized '+kind+' OWL variant')
