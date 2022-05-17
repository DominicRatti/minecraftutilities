package net.mcreator.minecraftutilities.procedures;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Collections;

import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedWriter;

public class CreateCuriosProcedure {
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	private static class GlobalTrigger {
		@SubscribeEvent
		public static void init(FMLCommonSetupEvent event) {
			executeProcedure(Collections.emptyMap());
		}
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		File file = new File("");
		file = (File) new File((FMLPaths.GAMEDIR.get().toString() + "\\defaultconfigs\\"), File.separator + "curios-server.toml");
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		if (file.exists()) {
			try {
				FileWriter filewriter = new FileWriter(file);
				BufferedWriter filebw = new BufferedWriter(filewriter);

				{
					filebw.write("#List of curio slot type settings");
					filebw.newLine();
				}
				{
					filebw.write("[[curiosSettings]]");
					filebw.newLine();
				}
				{
					filebw.write("identifier = \"back\"");
					filebw.newLine();
				}
				filebw.close();
				filewriter.close();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}
}
