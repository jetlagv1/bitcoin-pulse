// Resolve optional export dependencies from a local npm install or the bundled workspace runtime.
const path=require('path');
const os=require('os');
module.exports=function loadDependency(name){
 try { return require(name); }
 catch(error){
  if(error.code!=='MODULE_NOT_FOUND')throw error;
  const modules=process.env.CODEX_DEPENDENCY_NODE_MODULES||path.join(os.homedir(),'.cache','codex-runtimes','codex-primary-runtime','dependencies','node','node_modules');
  try {return require(path.join(modules,name));}
  catch {throw new Error(`Optional asset dependency ${name} is missing. Install it locally or set CODEX_DEPENDENCY_NODE_MODULES.`);}
 }
};
