import fs from "node:fs/promises";
import path from "node:path";
import crypto from "node:crypto";
import sharp from "sharp";

// Original references. These are NOT screenshots of the running Android app.
const src="brending/screens";
const out="brending/google-play/reference-previews-NOT-PLAY-SCREENSHOTS";
const W=1080,H=1920;
await fs.mkdir(out,{recursive:true});
const names=(await fs.readdir(src)).filter(x=>/^\d\d-[\w-]+\.svg$/.test(x)).sort();
if(names.length!==37)throw new Error("Expected 37 reference SVGs, got "+names.length);
const background=Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1920"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#0D1F3F"/><stop offset="1" stop-color="#08111D"/></linearGradient></defs><rect width="1080" height="1920" fill="url(#g)"/></svg>');
const meta={type:"REFERENCE_CONCEPT_NOT_REAL_APP_SCREENSHOT",applicationId:"com.brendigo.systivra",target:[W,H],original:[390,844],screens:[]};
const tiles=[];
for(const name of names){
  const svg=await fs.readFile(path.join(src,name));
  const rendered=await sharp(svg,{density:192}).resize({height:H,fit:"inside"}).png().toBuffer();
  const m=await sharp(rendered).metadata();
  if(m.width>W||m.height>H)throw Error("Cannot fit "+name);
  const outfile=path.join(out,name.replace(".svg","-REFERENCE.png"));
  await sharp(background).composite([{input:rendered,left:Math.floor((W-m.width)/2),top:Math.floor((H-m.height)/2)}])
    .flatten({background:"#08111D"}).removeAlpha().png({compressionLevel:9,palette:false}).toFile(outfile);
  const verify=await sharp(outfile).metadata();
  if(verify.width!==W||verify.height!==H||verify.hasAlpha)throw Error("Wrong output for "+name+": "+JSON.stringify({width:verify.width,height:verify.height,channels:verify.channels,hasAlpha:verify.hasAlpha}));
  meta.screens.push({reference:name,preview:path.basename(outfile),sha256:crypto.createHash("sha256").update(svg).digest("hex")});
  const tile=await sharp(outfile).resize({width:162,height:288,fit:"contain"}).toBuffer();
  const i=tiles.length;
  tiles.push({input:tile,left:(i%7)*180+9,top:Math.floor(i/7)*316+9});
}
await sharp({create:{width:1260,height:Math.ceil(names.length/7)*316,channels:3,background:"#EAF1FF"}})
  .composite(tiles).png().toFile(path.join(out,"ALL-37-REFERENCES-contact-sheet.png"));
await fs.writeFile(path.join(out,"manifest.json"),JSON.stringify(meta,null,2)+"\n");
await fs.writeFile(path.join(out,"README.txt"),"SYSTIVRA 37 ORIGINAL REFERENCES. 1080x1920 PNGs preserve original 390x844 composition without cropping, only side letterboxing. THESE ARE DESIGN CONCEPTS, NOT REAL ANDROID SCREENSHOTS. DO NOT SUBMIT TO GOOGLE PLAY AS SCREEN CAPTURES. Real app screenshots must be taken from running com.brendigo.systivra and manually approved.\n");
console.log("Rendered "+names.length+" lossless-content reference previews at 1080x1920.");
