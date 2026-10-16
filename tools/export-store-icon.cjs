const fs=require('fs');
const path=require('path');
const sharp=require('./node-runtime.cjs')('sharp');
(async()=>{
 const root=path.resolve(__dirname,'..');
 const xml=fs.readFileSync(path.join(root,'app/src/main/res/drawable/ic_bitcoin.xml'),'utf8');
 const paths=[...xml.matchAll(/<path android:fillColor="([^"]+)" android:pathData="([^"]+)"/g)].map(m=>`<path fill="${m[1]}" d="${m[2]}"/>`).join('');
 if(!paths)throw Error('No icon paths found');
 const svg=`<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 108 108">${paths}</svg>`;
 const out=path.join(root,'deliverables/store-assets');fs.mkdirSync(out,{recursive:true});
 const target=path.join(out,'bitcoin-pulse-app-icon-512.png');
 await sharp(Buffer.from(svg)).ensureAlpha().png().toFile(target);
 const meta=await sharp(target).metadata();
 if(meta.width!==512||meta.height!==512||meta.channels!==4||fs.statSync(target).size>1024*1024)throw Error('Invalid Play icon');
 console.log('App icon: 512×512, RGBA PNG, '+fs.statSync(target).size+' bytes; exact native vector paths.');
})();
