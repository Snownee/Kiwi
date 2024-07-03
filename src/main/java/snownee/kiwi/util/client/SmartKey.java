package snownee.kiwi.util.client;

import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;

public class SmartKey extends KeyMapping {
	//	private static final Logger LOGGER = LogUtils.getLogger();
	private static final long SHORT_PRESS_MAX_MS = 200;
	private static final long DOUBLE_PRESS_INTERVAL_MS = 200;
	private static final long LONG_PRESS_MIN_MS = 400;
	protected long pressSince = -1;
	protected long lastShortPress = -1;
	protected State state = State.Idle;
	private final BooleanSupplier onShortPress;
	private final BooleanSupplier onLongPress;
	private final BooleanSupplier onDoublePress;
	private final BooleanSupplier hasDoublePress;
	private final long longPressMinMs;

	private SmartKey(Builder builder) {
		super(builder.name, builder.type, builder.keyCode, builder.category);
		this.onShortPress = builder.onShortPress;
		this.onLongPress = builder.onLongPress;
		this.onDoublePress = builder.onDoublePress;
		this.hasDoublePress = builder.hasDoublePress;
		this.longPressMinMs = builder.longPressMinMs;
	}

	public void tick() {
		if (isUnbound()) {
			return;
		}
		long time = Util.getMillis();
		if (isDown()) {
			if (state != State.LongPress && pressSince != -1 && time - pressSince > longPressMinMs) {
				state = State.LongPress;
				onLongPress();
			}
		} else if (state == State.WaitingForDoublePress && time - lastShortPress > DOUBLE_PRESS_INTERVAL_MS) {
			onShortPress();
			pressSince = -1;
			state = State.Idle;
		}
	}

	@Override
	public void setDown(boolean isDown) {
		// use Forge's event to make it work when there is a GUI open
		setDownWithResult(isDown);
	}

	public boolean setDownWithResult(boolean isDown) {
		// use Forge's event to make it work when there is a GUI open
		if (isDown() == isDown) {
			return false;
		}
		super.setDown(isDown);
		long time = Util.getMillis();
		boolean result = false;
		if (isDown) { // on key down
			if (state == State.WaitingForDoublePress && time - lastShortPress < DOUBLE_PRESS_INTERVAL_MS) { // double press
				lastShortPress = -1;
				result = onDoublePress();
				state = State.Idle;
			} else {
				pressSince = time;
				state = State.ShortPress;
				return false;
			}
		} else { // on key up
			if (state == State.ShortPress && time - pressSince < SHORT_PRESS_MAX_MS) {
				lastShortPress = time;
				if (hasDoublePress()) {
					state = State.WaitingForDoublePress;
				} else {
					result = onShortPress();
					state = State.Idle;
				}
			} else {
				state = State.Idle;
			}
		}
		pressSince = -1;
		return result;
	}

	protected boolean hasDoublePress() {
		return onDoublePress != null && (hasDoublePress == null || hasDoublePress.getAsBoolean());
	}

	protected boolean onShortPress() {
		if (onShortPress != null) {
			return onShortPress.getAsBoolean();
		}
		return false;
	}

	@SuppressWarnings("UnusedReturnValue")
	protected boolean onLongPress() {
		if (onLongPress != null) {
			return onLongPress.getAsBoolean();
		}
		return false;
	}

	protected boolean onDoublePress() {
		if (onDoublePress != null) {
			return onDoublePress.getAsBoolean();
		}
		return false;
	}

	public enum State {
		Idle, ShortPress, WaitingForDoublePress, LongPress
	}

	public static class Builder {
		private final String name;
		private final String category;
		private InputConstants.Type type = InputConstants.Type.KEYSYM;
		private int keyCode = -1; // unbound
		private BooleanSupplier onShortPress;
		private BooleanSupplier onLongPress;
		private BooleanSupplier onDoublePress;
		private BooleanSupplier hasDoublePress;
		private long longPressMinMs = LONG_PRESS_MIN_MS;

		public Builder(String name, String category) {
			this.name = name;
			this.category = category;
		}

		public Builder key(InputConstants.Key key) {
			this.type = key.getType();
			this.keyCode = key.getValue();
			return this;
		}

		public Builder onShortPress(BooleanSupplier onShortPress) {
			this.onShortPress = onShortPress;
			return this;
		}

		public Builder onLongPress(BooleanSupplier onLongPress) {
			this.onLongPress = onLongPress;
			return this;
		}

		public Builder onDoublePress(BooleanSupplier onDoublePress) {
			this.onDoublePress = onDoublePress;
			return this;
		}

		public Builder hasDoublePress(BooleanSupplier hasDoublePress) {
			this.hasDoublePress = hasDoublePress;
			return this;
		}

		public SmartKey build() {
			return new SmartKey(this);
		}

		public Builder longPressMinMs(long longPressMinMs) {
			this.longPressMinMs = longPressMinMs;
			return this;
		}
	}
}