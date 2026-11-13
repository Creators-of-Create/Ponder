package net.createmod.ponder.testmod.neoforge;

import net.createmod.ponder.testmod.Testmod;
import net.createmod.ponder.testmod.client.TestmodClient;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Testmod.MOD_ID, dist = Dist.CLIENT)
public class TestmodClientNeoForge {
	public TestmodClientNeoForge() {
		TestmodClient.init();
	}
}
