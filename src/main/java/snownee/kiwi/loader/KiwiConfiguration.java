package snownee.kiwi.loader;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import snownee.kiwi.KiwiAnnotationData;

public class KiwiConfiguration {

	@SerializedName("Optional")
	public List<KiwiAnnotationData> optionals = Collections.EMPTY_LIST;

	@SerializedName("LoadingCondition")
	public List<KiwiAnnotationData> conditions = Collections.EMPTY_LIST;

	@SerializedName("KiwiModule")
	public List<KiwiAnnotationData> modules = Collections.EMPTY_LIST;

	@SerializedName("KiwiPacket")
	public List<KiwiAnnotationData> packets = Collections.EMPTY_LIST;

	@SerializedName("KiwiConfig")
	public List<KiwiAnnotationData> configs = Collections.EMPTY_LIST;

}
