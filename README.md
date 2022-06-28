# UofL Esports Minecraft Lobby
The lobby for the UofL Esports Minecraft Server

To build, you need gradle and Java JDK 17. `nix-shell -p gradle graalvm17-ce`

Then build using gradle `gradle shadowJar`

After then, you can run the generated jar with `java -jar build/libs/Minecraft_Lobby-1.0-SNAPSHOT-all.jar`

OR you can generate a native image using graalvm (if installed) `native-image -jar build/libs/Minecraft_Lobby-1.0-SNAPSHOT-all.jar`, then you can run the newly generated executable, `Minecraft_Lobby-1.0-SNAPSHOT`

Note: The native executable is actually slower due to slow record equals/hashcode
