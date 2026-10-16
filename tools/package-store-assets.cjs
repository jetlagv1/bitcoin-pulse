const fs=require('fs');
const path=require('path');
const crypto=require('crypto');
const sharp=require('./node-runtime.cjs')('sharp');
const JSZip=require('./node-runtime.cjs')('jszip');
(async()=>{
 const root=path.resolve(__dirname,'..');
 const out=path.join(root,'deliverables/store-assets');
 const names=['01-dashboard.png','02-dagcandles.png','03-widgets.png','04-netwerkdetails.png'];
 const report=[];
 for(const name of names){
  const input=path.join(root,'app/build/reports/store-capture',name);
  const source=await sharp(input).raw().toBuffer({resolveWithObject:true});
  if(source.info.channels===4){for(let i=3;i<source.data.length;i+=4)if(source.data[i]!==255)throw Error('Unexpected transparency: '+name);}
  const target=path.join(out,'bitcoin-pulse-'+name);
  // Strip the unused opaque alpha channel; preserve every visible screenshot pixel.
  await sharp(input).removeAlpha().png().toFile(target);
  const metadata=await sharp(target).metadata();
  if(metadata.width!==1440||metadata.height!==2560||metadata.channels!==3)throw Error('Invalid screenshot: '+name);
  const bytes=fs.readFileSync(target);
  report.push({file:path.basename(target),width:1440,height:2560,mode:'RGB',bytes:bytes.length,sha256:crypto.createHash('sha256').update(bytes).digest('hex')});
 }
 const icon='bitcoin-pulse-app-icon-512.png';
 const iconMetadata=await sharp(path.join(out,icon)).metadata();
 if(iconMetadata.width!==512||iconMetadata.height!==512||iconMetadata.channels!==4)throw Error('Invalid icon');
 fs.writeFileSync(path.join(out,'ASSET-CHECKS.json'),JSON.stringify({icon:{file:icon,width:512,height:512,mode:'RGBA'},screenshots:report,origin:'Production native Android composables rendered with Robolectric native graphics and real public API responses captured 16 October 2026; no synthetic prices or web mockups. Not captured on a physical phone. The widget image shows the actual in-app preview.'},null,2));
 const zip=new JSZip();
 const files=[icon,...report.map(x=>x.file),'LEESMIJ.md','ASSET-CHECKS.json'];
 const featureFile='bitcoin-pulse-functieafbeelding-1024x500.png';
 if(fs.existsSync(path.join(out,featureFile))){
  const meta=await sharp(path.join(out,featureFile)).metadata();
  if(meta.width!==1024||meta.height!==500||meta.channels!==3)throw Error('Invalid feature graphic');
  files.push(featureFile);
  const checks=JSON.parse(fs.readFileSync(path.join(out,'ASSET-CHECKS.json'),'utf8'));
  checks.featureGraphic={file:featureFile,width:1024,height:500,mode:'RGB',sha256:crypto.createHash('sha256').update(fs.readFileSync(path.join(out,featureFile))).digest('hex')};
  fs.writeFileSync(path.join(out,'ASSET-CHECKS.json'),JSON.stringify(checks,null,2));
 }
 for(const file of files)zip.file(file,fs.readFileSync(path.join(out,file)));
 const target=path.join(root,'deliverables/Bitcoin-Pulse-Play-Store-Afbeeldingen.zip');
 fs.writeFileSync(target,await zip.generateAsync({type:'nodebuffer',compression:'DEFLATE'}));
 console.log('Validated icon and four RGB screenshots. ZIP: '+fs.statSync(target).size+' bytes.');
})();
