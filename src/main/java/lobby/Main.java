package lobby;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Scanner; 

public class Main {

	// Stolen from https://github.com/Minestom/Minestom/discussions/1082
	public static void sendPlayerToServer(Player player, String server) {
		final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		final DataOutputStream dataStream = new DataOutputStream(byteStream);
	
		try {
			dataStream.writeUTF("Connect");
			dataStream.writeUTF(server);
			byteStream.close();
			dataStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		player.getPlayerConnection().sendPacket(new PluginMessagePacket("bungeecord:main", byteStream.toByteArray()));
	}

	public static void main(String[] args) {
		// Initialization
		MinecraftServer minecraftServer = MinecraftServer.init();
		InstanceManager instanceManager = MinecraftServer.getInstanceManager();

		// Create the instance & Set time
		InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
		instanceContainer.setChunkLoader(new AnvilLoader("world"));
		instanceContainer.setTimeRate(0);
		instanceContainer.setTime(6000);

		// Add an event callback to specify the spawning instance (and the spawn position)
		GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
		globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
			final Player player = event.getPlayer();
			event.setSpawningInstance(instanceContainer);
			player.setRespawnPoint(new Pos(0.5, 70, 0.5));
		});

		globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
			final Player player = event.getPlayer();
			final Vec facing = new Vec(15.5, 76, 0.5);
			player.facePosition(Player.FacePoint.EYE, facing);
			player.setGameMode(GameMode.ADVENTURE);
			player.setAllowFlying(true);
			
			if (player.getUsername().equals("Legit_Magic")) player.setGameMode(GameMode.CREATIVE);
			//if (player.getUuid() == UUID.fromString("3732411a-84d8-481c-9920-14d5caf196c6")) player.setGameMode(GameMode.CREATIVE);

		});

		// Make blocks only placeable by me
		globalEventHandler.addListener(PlayerBlockPlaceEvent.class, event -> {
			final Player player = event.getPlayer();

			if (player.getUsername().equals("Legit_Magic") && event.getBlock() == Block.BEDROCK) {
				instanceContainer.saveChunksToStorage();
			}
		});

		globalEventHandler.addListener(PlayerMoveEvent.class , event -> {
			final Player player = event.getPlayer();
			final Pos position = event.getNewPosition();
			final Block belowBlock = instanceContainer.getBlock((int) Math.floor(position.x()), (int) position.y() - 1, (int) Math.floor(position.z()));

			// Bounce on Void
			if (position.y() < 70 && belowBlock.isAir()) {
				Vec oldVelocity = player.getVelocity();
				Vec newVelocity = oldVelocity.withY(15);
				player.setVelocity(newVelocity);
			}

			// Speed platform
			if (belowBlock.compare(Block.MAGENTA_GLAZED_TERRACOTTA)) {
				String direction = belowBlock.getProperty("facing");
				Vec oldVelocity = player.getVelocity();
				Vec newVelocity = new Vec(0);
				int speed = 15;
				switch(direction) {
					case "north":
						newVelocity = oldVelocity.withZ(-speed);
						break;
					case "south":
						newVelocity = oldVelocity.withZ(speed);
						break;
					case "west":
						newVelocity = oldVelocity.withX(speed);
						break;
					case "east":
						newVelocity = oldVelocity.withX(-speed);
						break;
				}
				player.setVelocity(newVelocity);
			}

			// Launch pad
			else if (belowBlock.compare(Block.SLIME_BLOCK)) {
				Block sign = instanceContainer.getBlock((int) Math.floor(position.x()), (int) Math.ceil(position.y()) - 3, (int) Math.floor(position.z())); // Ceil for them slimeblock sounds
				if (sign.compare(Block.OAK_SIGN)) {
					Vec oldVelocity = player.getVelocity();
					Vec newVelocity = new Vec(0);
					int speedHorizontal = 46;
					int speedVertical = 23;
					String direction = sign.getProperty("rotation");
					switch(direction) {
						case "8": // South
							newVelocity = new Vec(oldVelocity.x(), speedVertical, speedHorizontal);
							break;
						case "0": // North
							newVelocity = new Vec(oldVelocity.x(), speedVertical, -speedHorizontal);
							break;
						case "3": // Just bounce up
							newVelocity = oldVelocity.withY(speedVertical*2);
							break;
					}
					player.setVelocity(newVelocity);
				}
			}

			// This needs to be changed once there are more than just two portals
			else if (belowBlock.compare(Block.OBSIDIAN)) {
				sendPlayerToServer(player, "survival");
				player.teleport(new Pos(15.5, 70, -34.5));
				player.facePosition(Player.FacePoint.EYE, new Vec(15.5, 71.5, -44));
			}
			else if (belowBlock.compare(Block.END_PORTAL)) {
				sendPlayerToServer(player, "creative");
				player.teleport(new Pos(15.5, 70, 35.5));
				player.facePosition(Player.FacePoint.EYE, new Vec(15.5, 71.5, 44));
			}

		});

		// Start the server on port 25565
		String forwardingSecret = new String();
		
		try {
			File secretFile = new File("secret.txt");
			Scanner secretReader = new Scanner(secretFile);
			forwardingSecret = secretReader.nextLine();
			secretReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("Set your velocity forwarding secret in secret.txt");
			e.printStackTrace();
		}

		VelocityProxy.enable(forwardingSecret);
		minecraftServer.start("0.0.0.0", 25565);
	}
}
