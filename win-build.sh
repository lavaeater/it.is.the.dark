#!/bin/bash
rm -rf ../turbo-build/out-win

java -jar ../turbo-build/packr-all-4.0.0.jar \
     --platform windows64 \
     --jdk ../turbo-build/windows.zip \
     --useZgcIfSupportedOs \
     --executable it-is-the-dark \
     --classpath ./lwjgl3/build/lib/ItIsTheDark-1.0.0.jar \
     --mainclass dark.core.lwjgl3.Lwjgl3Launcher \
     --vmargs Xmx1G XstartOnFirstThread \
     --resources assets/* \
     --output ../turbo-build/out-win

butler push ../turbo-build/out-win lavaeater/it-is-the-dark:win
