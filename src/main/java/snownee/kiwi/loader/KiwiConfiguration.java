package snownee.kiwi.loader;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import snownee.kiwi.KiwiAnnotationData;

public class KiwiConfiguration {

	@SerializedName("Optional")
	public List<KiwiAnnotationData> optionals = List.of();

	@SerializedName("LoadingCondition")
	public List<KiwiAnnotationData> conditions = List.of();

	@SerializedName("KiwiModule")
	public List<KiwiAnnotationData> modules = List.of();

	@SerializedName("KiwiPacket")
	public List<KiwiAnnotationData> packets = List.of();

	@SerializedName("KiwiConfig")
	public List<KiwiAnnotationData> configs = List.of();

}
