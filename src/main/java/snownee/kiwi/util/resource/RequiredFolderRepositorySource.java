package snownee.kiwi.util.resource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.FileUtil;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;

public class RequiredFolderRepositorySource extends FolderRepositorySource {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final PackSelectionConfig DISCOVERED_PACK_SELECTION_CONFIG = new PackSelectionConfig(true, Pack.Position.TOP, false);

	public RequiredFolderRepositorySource(
			Path pFolder,
			PackType pPackType,
			PackSource pPackSource,
			DirectoryValidator directoryValidator) {
		super(pFolder, pPackType, pPackSource, directoryValidator);
	}

	@Override
	public void loadPacks(Consumer<Pack> pOnLoad) {
		try {
			FileUtil.createDirectoriesSafe(this.folder);
			discoverPacks(this.folder, this.validator, (path, resourcesSupplier) -> {
				PackLocationInfo packLocationInfo = this.createDiscoveredFilePackInfo(path);
				Pack pack = Pack.readMetaAndCreate(packLocationInfo, resourcesSupplier, this.packType, DISCOVERED_PACK_SELECTION_CONFIG);
				if (pack != null) {
					pOnLoad.accept(pack);
				}
			});
		} catch (IOException ioexception) {
			LOGGER.warn("Failed to list packs in {}", this.folder, ioexception);
		}
	}
}