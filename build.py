#!/usr/bin/env python3
# build and install npltz for android.
# works on linux, macos and windows, as long as the requirements are
# installed: jdk 17+, the android sdk (platform 35, build-tools 35.0.0,
# platform-tools) and python 3.
#
# usage:
#   python3 build.py              build the apk and run the calendar tests
#   python3 build.py --test       run the calendar tests only
#   python3 build.py --install    build, then install on a connected phone

import os
import platform
import shutil
import subprocess
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
APK = os.path.join("dist", "npltz-android.apk")


def java_exec():
    return "java.exe" if platform.system() == "Windows" else "java"


def sdk_candidates():
    home = os.path.expanduser("~")
    system = platform.system()
    if system == "Windows":
        local = os.environ.get("LOCALAPPDATA", "")
        return [
            os.environ.get("ANDROID_HOME", ""),
            os.environ.get("ANDROID_SDK_ROOT", ""),
            os.path.join(local, "Android", "Sdk"),
            os.path.join(home, "Android", "Sdk"),
        ]
    if system == "Darwin":
        return [
            os.environ.get("ANDROID_HOME", ""),
            os.environ.get("ANDROID_SDK_ROOT", ""),
            os.path.join(home, "Library", "Android", "sdk"),
            os.path.join(home, "Android", "Sdk"),
        ]
    return [
        os.environ.get("ANDROID_HOME", ""),
        os.environ.get("ANDROID_SDK_ROOT", ""),
        "/opt/android-sdk",
        os.path.join(home, "Android", "Sdk"),
        os.path.join(home, "android-sdk"),
    ]


def find_sdk():
    for candidate in sdk_candidates():
        if candidate and os.path.isdir(os.path.join(candidate, "platforms")) \
            and os.path.isdir(os.path.join(candidate, "build-tools")):
            return candidate
    return ""


def check_java():
    # prefer java_home when set, so gradle and this check agree on one jdk.
    java_home = os.environ.get("JAVA_HOME", "")
    if java_home and os.path.isfile(os.path.join(java_home, "bin", java_exec())):
        os.environ["PATH"] = os.path.join(java_home, "bin") \
            + os.pathsep + os.environ["PATH"]
    if shutil.which("java") is None:
        print("java not found. install jdk 17 or newer and set JAVA_HOME.")
        sys.exit(1)
    version = subprocess.run(
        ["java", "-version"], capture_output=True, text=True
    ).stderr.splitlines()[0]
    for part in version.split('"'):
        if part and part[0].isdigit():
            if int(part.split(".")[0]) < 17:
                print("jdk 17 or newer is required, found: " + version)
                sys.exit(1)
            break
    return version


def run_gradle(tasks):
    wrapper = "gradlew.bat" if platform.system() == "Windows" else "./gradlew"
    env = dict(os.environ)
    if os.environ.get("JAVA_HOME"):
        env["JAVA_HOME"] = os.environ["JAVA_HOME"]
    result = subprocess.run([wrapper] + tasks, cwd=ROOT, env=env)
    if result.returncode != 0:
        sys.exit(result.returncode)


def install():
    system = platform.system()
    adb = os.path.join(os.environ.get("ANDROID_HOME", ""),
                       "platform-tools", "adb")
    if system == "Windows":
        adb += ".exe"
    if not os.path.isfile(adb):
        adb = shutil.which("adb") or ""
    if not adb:
        print("adb not found; install android platform-tools.")
        sys.exit(1)
    if not os.path.isfile(APK):
        print("apk not found: " + APK)
        sys.exit(1)
    print()
    print("==> installing")
    result = subprocess.run([adb, "install", "-r", APK], cwd=ROOT)
    if result.returncode != 0:
        sys.exit(result.returncode)


def main():
    install_phone = "--install" in sys.argv
    test_only = "--test" in sys.argv

    sdk = find_sdk()
    if not sdk and not test_only:
        print("android sdk not found.", file=sys.stderr)
        print()
        print("set ANDROID_HOME to the sdk root (the folder containing")
        print("platforms/ and build-tools/), for example:")
        print()
        print("  linux/arch   paru -S android-sdk android-sdk-build-tools"
              " android-sdk-platform-tools")
        print("               or use android studio's sdk manager")
        print("  windows      %LOCALAPPDATA%\\Android\\Sdk via android studio")
        print("  macos        ~/Library/Android/sdk via android studio")
        print()
        print("  export ANDROID_HOME=/path/to/android-sdk")
        sys.exit(1)
    if sdk:
        os.environ["ANDROID_HOME"] = sdk
        os.environ["ANDROID_SDK_ROOT"] = sdk

    java = check_java()
    if sdk:
        print("sdk:   " + sdk)
    print("java:  " + java)
    print()

    tasks = ["coreTest"] if test_only else ["coreTest", "apk"]
    run_gradle(tasks)

    if test_only:
        return

    size = os.path.getsize(os.path.join(ROOT, APK))
    print()
    print("apk: " + APK + " (" + str(size // 1024) + " kb)")

    if install_phone:
        install()


if __name__ == "__main__":
    main()
