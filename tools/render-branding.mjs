import fs from "node:fs/promises";
import path from "node:path";
import sharp from "sharp";

// Generates concept images, not real device screenshots.
const sourceDir="brending/screens", output="brending/rendered";
await fs.mkdir(output,{recursive:true});
const screens=(await fs.readdir(sourceDir)).filter(n=>n.endsWith(".svg")).sort();
if(screens.length!==37)throw new Error("Expected 37 screen references; got "+screens.length);
for(const name of screens){
  const source=await fs.readFile(path.join(sourceDir,name));
  await sharp(source,{density:192}).resize({width:780})
    .png({compressionLevel:9}).toFile(path.join(output,name.replace(".svg",".png")));
}
await sharp(await fs.readFile("brending/logo.svg")).resize({width:1400}).png()
  .toFile(path.join(output,"systivra-wordmark.png"));
await sharp(await fs.readFile("brending/app-icon.svg")).resize(1024,1024).png()
  .toFile(path.join(output,"systivra-app-icon.png"));
const cols=7,width=195,height=435,composite=[];
for(let i=0;i<screens.length;i++){
  const input=await sharp(path.join(output,screens[i].replace(".svg",".png")))
    .resize({width:180,height:390,fit:"contain"}).toBuffer();
  composite.push({input,left:(i%cols)*width+7,top:Math.floor(i/cols)*height+8});
}
await sharp({create:{width:cols*width,height:Math.ceil(screens.length/cols)*height,channels:4,background:"#eaf1ff"}})
  .composite(composite).png({compressionLevel:9})
  .toFile(path.join(output,"systivra-37-screen-contact-sheet.png"));
console.log("SYSTIVRA: 37 screen previews and brand images exported");
