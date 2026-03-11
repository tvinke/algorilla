#!/usr/bin/env node

const { execFileSync } = require("child_process");
const path = require("path");
const fs = require("fs");

const libDir = path.join(__dirname, "..", "lib");
const jarPath = path.join(libDir, "algorilla.jar");

function findJava() {
  // 1. Bundled JRE (downloaded by postinstall)
  const jreDir = path.join(libDir, "jre");
  const jreBin = path.join(jreDir, "bin", "java");
  if (fs.existsSync(jreBin)) return jreBin;

  // 2. JAVA_HOME
  if (process.env.JAVA_HOME) {
    const jhBin = path.join(process.env.JAVA_HOME, "bin", "java");
    if (fs.existsSync(jhBin)) return jhBin;
  }

  // 3. System PATH
  return "java";
}

if (!fs.existsSync(jarPath)) {
  console.error(
    "algorilla: JAR not found at " +
      jarPath +
      "\nRun 'npm rebuild algorilla' or reinstall."
  );
  process.exit(1);
}

try {
  execFileSync(findJava(), ["-jar", jarPath, ...process.argv.slice(2)], {
    stdio: "inherit",
  });
} catch (err) {
  process.exit(err.status || 1);
}
