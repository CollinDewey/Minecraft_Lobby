# Minecraft_Lobby
The lobby for the UofL Esports Minecraft Server

To build, install graalvm17 and gradle

nix-shell -p gradle graalvm17-ce

Then build

gradle shadowJar

After then, you can run

java -jar build/libs/Minecraft_Lobby-1.0-SNAPSHOT-all.jar

OR

native-image -jar build/libs/Minecraft_Lobby-1.0-SNAPSHOT-all.jar

to get a native executable, Minecraft_Lobby-1.0-SNAPSHOT


Do note: apparently the native executable has issues with lots of players on minestom but it really shouldn't matter for our case.
