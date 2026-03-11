#!/usr/bin/env node

/**
 * Downloads a bundled JRE so users don't need Java installed.
 * Uses Eclipse Adoptium (Temurin) builds.
 * Skips download if JAVA_HOME is set or java is already on PATH.
 */

const { execSync } = require("child_process");
const https = require("https");
const fs = require("fs");
const path = require("path");
const os = require("os");
const { createWriteStream } = require("fs");

const LIB_DIR = path.join(__dirname, "..", "lib");
const JRE_DIR = path.join(LIB_DIR, "jre");
const JRE_VERSION = "21";

function javaAvailable() {
  if (process.env.JAVA_HOME) {
    const bin = path.join(process.env.JAVA_HOME, "bin", "java");
    if (fs.existsSync(bin)) return true;
  }
  try {
    execSync("java -version", { stdio: "ignore" });
    return true;
  } catch {
    return false;
  }
}

function adoptiumArch() {
  const arch = os.arch();
  if (arch === "x64" || arch === "amd64") return "x64";
  if (arch === "arm64" || arch === "aarch64") return "aarch64";
  return null;
}

function adoptiumOs() {
  const p = os.platform();
  if (p === "darwin") return "mac";
  if (p === "linux") return "linux";
  if (p === "win32") return "windows";
  return null;
}

function downloadFile(url) {
  return new Promise((resolve, reject) => {
    const follow = (u) => {
      https
        .get(u, (res) => {
          if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
            follow(res.headers.location);
            return;
          }
          if (res.statusCode !== 200) {
            reject(new Error("HTTP " + res.statusCode + " for " + u));
            return;
          }
          const tmpFile = path.join(os.tmpdir(), "algorilla-jre-" + Date.now() + ".tar.gz");
          const stream = createWriteStream(tmpFile);
          res.pipe(stream);
          stream.on("finish", () => {
            stream.close();
            resolve(tmpFile);
          });
        })
        .on("error", reject);
    };
    follow(url);
  });
}

async function main() {
  if (fs.existsSync(JRE_DIR)) {
    console.log("algorilla: bundled JRE already present, skipping download");
    return;
  }

  if (javaAvailable()) {
    console.log("algorilla: Java found on system, skipping JRE download");
    return;
  }

  const arch = adoptiumArch();
  const osName = adoptiumOs();
  if (!arch || !osName) {
    console.warn(
      "algorilla: unsupported platform " + os.platform() + "/" + os.arch() +
        " - install Java " + JRE_VERSION + "+ manually"
    );
    return;
  }

  const url =
    "https://api.adoptium.net/v3/binary/latest/" + JRE_VERSION +
    "/ga/" + osName + "/" + arch + "/jre/hotspot/normal/eclipse?project=jdk";

  console.log("algorilla: downloading JRE " + JRE_VERSION + " for " + osName + "/" + arch + "...");

  try {
    const archive = await downloadFile(url);
    fs.mkdirSync(JRE_DIR, { recursive: true });

    if (osName === "windows") {
      execSync(
        "powershell -command \"Expand-Archive -Path '" + archive + "' -DestinationPath '" + JRE_DIR + "'\"",
        { stdio: "ignore" }
      );
    } else {
      execSync('tar xzf "' + archive + '" --strip-components=1 -C "' + JRE_DIR + '"', {
        stdio: "ignore",
      });
    }

    fs.unlinkSync(archive);
    console.log("algorilla: JRE installed successfully");
  } catch (err) {
    console.warn("algorilla: failed to download JRE - " + err.message);
    console.warn("algorilla: install Java 21+ manually and ensure 'java' is on your PATH");
  }
}

main();
