package snownee.kiwi.util.resource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

public class RequiredFolderRepositorySource extends FolderRepositorySource {
	private static final Logger LOGGER = LogUtils.getLogger();

	public RequiredFolderRepositorySource(
			Path pFolder,
			PackType pPackType,
			PackSource pPackSource) {
		super(pFolder, pPackType, pPackSource);
	}

	@Override
	public void loadPacks(Consumer<Pack> pOnLoad) {
		try {
			FileUtil.createDirectoriesSafe(this.folder);
			discoverPacks(this.folder, false, (path, resourcesSupplier) -> {
				String s = nameFromPath(path);
				Pack pack = Pack.readMetaAndCreate(
						"file/" + s,
						Component.literal(s),
						true,
						resourcesSupplier,
						this.packType,
						Pack.Position.TOP,
						this.packSource);
				if (pack != null) {
					pOnLoad.accept(pack);
				}
			});
		} catch (IOException ioexception) {
			LOGGER.warn("Failed to list packs in {}", this.folder, ioexception);
		}
	}
}
