package third_party.com.facebook.yoga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import third_party.com.facebook.yoga.YogaDelegates.YogaBaseline;
import third_party.com.facebook.yoga.YogaDelegates.YogaMeasure;
import third_party.com.facebook.yoga.YogaDelegates.YogaNodeCloned;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public class YogaNode implements Iterable<YogaNode> {
	public static final float DefaultFlexGrow = 0.0f;
	public static final float DefaultFlexShrink = 0.0f;
	public static final float WebDefaultFlexShrink = 1.0f;

	private static final AtomicInteger _instanceCount = new AtomicInteger();

	public Consumer<YogaNode> _print;
	public boolean _hasNewLayout;
	public YogaNodeType _nodeType;
	private YogaDelegates.YogaMeasure _measure;
	public YogaDelegates.YogaBaseline _baseline;
	private boolean _isReferenceBaseline;
	public Consumer<YogaNode> _dirtied;
	private YogaStyle _style;
	private YogaLayout _layout;
	public int _lineIndex;
	public YogaNode _owner;
	private List<YogaNode> _children;
	public YogaNode _nextChild;
	public YogaConfig _config;
	private boolean _isDirty;
	private YogaValue[] _resolvedDimensions; // [2]

	public static final YogaConfig defaultConfig = new YogaConfig();

	public YogaNode() {
		_print = null;
		_hasNewLayout = true;
		_nodeType = YogaNodeType.Default;
		_measure = null;
		_baseline = null;
		_dirtied = null;
		_style = new YogaStyle();
		_layout = new YogaLayout();
		_lineIndex = 0;
		_owner = null;
		_children = new ArrayList<>();
		_nextChild = null;
		_config = defaultConfig;
		_isDirty = false;
		_resolvedDimensions = new YogaValue[] { YogaValue.UNDEFINED, YogaValue.UNDEFINED };

		_instanceCount.incrementAndGet();
	}

	public YogaNode(YogaNode node) {
		_print = node._print;
		_hasNewLayout = node._hasNewLayout;
		_nodeType = node._nodeType;
		_measure = node._measure;
		_baseline = node._baseline;
		_dirtied = node._dirtied;
		_style = node._style;
		_layout = node._layout;
		_lineIndex = node._lineIndex;
		_owner = node._owner;
		_children = new ArrayList<>(node._children);
		_nextChild = node._nextChild;
		_config = node._config;
		_isDirty = node._isDirty;
		_resolvedDimensions = Arrays.copyOf(node._resolvedDimensions, 2);

		_instanceCount.incrementAndGet();
	}

	public YogaNode(YogaNode node, YogaNode owner) {
		this(node);
		_owner = owner;
	}

	public YogaNode(Consumer<YogaNode> print, boolean hasNewLayout, YogaNodeType nodeType, YogaMeasure measure, YogaBaseline baseline, Consumer<YogaNode> dirtied, YogaStyle style, YogaLayout layout, int lineIndex, YogaNode owner, List<YogaNode> children, YogaNode nextChild, YogaConfig config, boolean isDirty, YogaValue[] resolvedDimensions) {
		_print = print;
		_hasNewLayout = hasNewLayout;
		_nodeType = nodeType;
		_measure = measure;
		_baseline = baseline;
		_dirtied = dirtied;
		_style = style;
		_layout = layout;
		_lineIndex = lineIndex;
		_owner = owner;
		_children = new ArrayList<>(children);
		_nextChild = nextChild;
		_config = config;
		_isDirty = isDirty;
		_resolvedDimensions = Arrays.copyOf(resolvedDimensions, 2);

		_instanceCount.incrementAndGet();
	}

	public YogaNode(YogaConfig config) {
		this();
		_config = config != null ? config : defaultConfig;

		if (_config.UseWebDefaults) {
			_style.FlexDirection = YogaFlexDirection.Row;
			_style.AlignContent = YogaAlign.Stretch;
		}
	}

	//FIXME
//	@Override
//	protected void finalize() throws Throwable {
//		_instanceCount.decrementAndGet();
//	}

	// Getters

	public static int GetInstanceCount() {
		return _instanceCount.get();
	}

	public YogaMeasure GetMeasure() {
		return _measure;
	}

	public void SetMeasure(YogaMeasure value) {
		if (!_children.isEmpty())
			throw new UnsupportedOperationException("Cannot set measure function: Nodes with measure functions cannot have children.");

		if (value == null) {
			_measure = null;
			// TODO: t18095186 Move nodeType to opt-in function and mark appropriate
			// places in Litho
			_nodeType = YogaNodeType.Default;
		} else {
			_measure = value;
			// TODO: t18095186 Move nodeType to opt-in function and mark appropriate
			// places in Litho
			_nodeType = YogaNodeType.Text;
		}

		_measure = value;
	}

	public YogaStyle GetStyle() {
		return _style;
	}

	public void SetStyle(YogaStyle value) {
		_style.CopyFrom(value);
	}

	public YogaLayout GetLayout() {
		return _layout;
	}

	public void SetLayout(YogaLayout value) {
		_layout.CopyFrom(value);
	}

	public List<YogaNode> GetChildren() {
		return _children;
	}

	public void SetChildren(List<YogaNode> value) {
		if (value != null) {
			_children = new ArrayList<>();
		} else {
			_children = new ArrayList<>(value);
		}
	}

	public YogaNode GetChild(int index) {
		return _children.get(index);
	}

	public boolean GetIsDirty() {
		return _isDirty;
	}

	public void SetIsDirty(boolean value) {
		if (value == _isDirty)
			return;

		_isDirty = value;
		if (value && _dirtied != null)
			_dirtied.accept(this);
	}

	public YogaValue[] GetResolvedDimensions() {
		return _resolvedDimensions;
	}

	public YogaValue GetResolvedDimension(YogaDimension index) {
		return _resolvedDimensions[index.ordinal()];
	}

	// Methods related to positions, margin, padding and border
	public float GetLeadingPosition(YogaFlexDirection axis, float axisSize) {
		YogaValue leadingPosition = YogaValue.UNDEFINED;
		if (axis.IsRow()) {
			leadingPosition = ComputedEdgeValue(_style.Position, YogaEdge.Start, YogaValue.UNDEFINED);
			if (leadingPosition.Unit != YogaUnit.Undefined)
				return leadingPosition.Resolve(axisSize);
		}

		leadingPosition = ComputedEdgeValue(_style.Position, Leading[axis.ordinal()], YogaValue.UNDEFINED);
		return leadingPosition.Unit == YogaUnit.Undefined ? 0.0f : leadingPosition.Resolve(axisSize);
	}

	public float

			GetTrailingPosition(YogaFlexDirection axis, float axisSize) {
		YogaValue trailingPosition = YogaValue.UNDEFINED;
		if (axis.IsRow()) {
			trailingPosition = ComputedEdgeValue(_style.Position, YogaEdge.End, YogaValue.UNDEFINED);
			if (trailingPosition.Unit != YogaUnit.Undefined)
				return trailingPosition.Resolve(axisSize);
		}

		trailingPosition = ComputedEdgeValue(_style.Position, Trailing[axis.ordinal()], YogaValue.UNDEFINED);
		return trailingPosition.Unit == YogaUnit.Undefined ? 0.0f : trailingPosition.Resolve(axisSize);
	}

	public float GetRelativePosition(YogaFlexDirection axis, float axisSize) {
		return IsLeadingPositionDefined(axis) ? GetLeadingPosition(axis, axisSize) : -GetTrailingPosition(axis, axisSize);
	}

	public boolean IsLeadingPositionDefined(YogaFlexDirection axis) {
		return (axis.IsRow() && ComputedEdgeValue(_style.Position, YogaEdge.Start, YogaValue.UNDEFINED).Unit != YogaUnit.Undefined) || ComputedEdgeValue(_style.Position, Leading[axis.ordinal()], YogaValue.UNDEFINED).Unit != YogaUnit.Undefined;
	}

	public boolean IsTrailingPositionDefined(YogaFlexDirection axis) {
		return (axis.IsRow() && ComputedEdgeValue(_style.Position, YogaEdge.End, YogaValue.UNDEFINED).Unit != YogaUnit.Undefined) || ComputedEdgeValue(_style.Position, Trailing[axis.ordinal()], YogaValue.UNDEFINED).Unit != YogaUnit.Undefined;
	}

	public float GetLeadingMargin(YogaFlexDirection axis, float widthSize) {
		if (axis.IsRow() && _style.Margin[YogaEdge.Start.ordinal()].Unit != YogaUnit.Undefined)
			return ResolveValueMargin(_style.Margin[YogaEdge.Start.ordinal()], widthSize);

		return ResolveValueMargin(ComputedEdgeValue(_style.Margin, Leading[axis.ordinal()], YogaValue.ZERO), widthSize);
	}

	public float GetTrailingMargin(YogaFlexDirection axis, float widthSize) {
		if (axis.IsRow() && _style.Margin[YogaEdge.End.ordinal()].Unit != YogaUnit.Undefined)
			return ResolveValueMargin(_style.Margin[YogaEdge.End.ordinal()], widthSize);

		return ResolveValueMargin(ComputedEdgeValue(_style.Margin, Trailing[axis.ordinal()], YogaValue.ZERO), widthSize);
	}

	public float GetMarginForAxis(YogaFlexDirection axis, float widthSize) {
		return GetLeadingMargin(axis, widthSize) + GetTrailingMargin(axis, widthSize);
	}

	public YogaValue GetMarginLeadingValue(YogaFlexDirection axis) {
		if (axis.IsRow() && _style.Margin[YogaEdge.Start.ordinal()].Unit != YogaUnit.Undefined)
			return _style.Margin[YogaEdge.Start.ordinal()];

		return _style.Margin[Leading[axis.ordinal()].ordinal()];
	}

	public YogaValue GetMarginTrailingValue(YogaFlexDirection axis) {
		if (axis.IsRow() && _style.Margin[YogaEdge.End.ordinal()].Unit != YogaUnit.Undefined)
			return _style.Margin[YogaEdge.End.ordinal()];

		return _style.Margin[Trailing[axis.ordinal()].ordinal()];
	}

	public float GetLeadingBorder(YogaFlexDirection axis) {
		if (axis.IsRow() && _style.Border[YogaEdge.Start.ordinal()].Unit != YogaUnit.Undefined && !Float.isNaN(_style.Border[YogaEdge.Start.ordinal()].Value) && _style.Border[YogaEdge.Start.ordinal()].Value >= 0.0f) {
			return _style.Border[YogaEdge.Start.ordinal()].Value;
		}

		float computedEdgeValue = ComputedEdgeValue(_style.Border, Leading[axis.ordinal()], YogaValue.ZERO).Value;
		return YogaMath.Max(computedEdgeValue, 0.0F);
	}

	public float GetTrailingBorder(YogaFlexDirection flexDirection) {
		if (flexDirection.IsRow() && _style.Border[YogaEdge.End.ordinal()].Unit != YogaUnit.Undefined && !Float.isNaN(_style.Border[YogaEdge.End.ordinal()].Value) && _style.Border[YogaEdge.End.ordinal()].Value >= 0.0f) {
			return _style.Border[YogaEdge.End.ordinal()].Value;
		}

		float computedEdgeValue = ComputedEdgeValue(_style.Border, Trailing[flexDirection.ordinal()], YogaValue.ZERO).Value;
		return YogaMath.Max(computedEdgeValue, 0.0f);
	}

	public float GetLeadingPadding(YogaFlexDirection axis, float widthSize) {
		float paddingEdgeStart = _style.Padding[YogaEdge.Start.ordinal()].Resolve(widthSize);
		if (axis.IsRow() && _style.Padding[YogaEdge.Start.ordinal()].Unit != YogaUnit.Undefined && !Float.isNaN(paddingEdgeStart) && paddingEdgeStart >= 0.0f) {
			return paddingEdgeStart;
		}

		float resolvedValue = ComputedEdgeValue(_style.Padding, Leading[axis.ordinal()], YogaValue.ZERO).Resolve(widthSize);
		return YogaMath.Max(resolvedValue, 0.0f);
	}

	public float GetTrailingPadding(YogaFlexDirection axis, float widthSize) {
		float paddingEdgeEnd = _style.Padding[YogaEdge.End.ordinal()].Resolve(widthSize);
		if (axis.IsRow() && _style.Padding[YogaEdge.End.ordinal()].Unit != YogaUnit.Undefined && !Float.isNaN(paddingEdgeEnd) && paddingEdgeEnd >= 0.0f) {
			return paddingEdgeEnd;
		}

		float resolvedValue = ComputedEdgeValue(_style.Padding, Trailing[axis.ordinal()], YogaValue.ZERO).Resolve(widthSize);
		return YogaMath.Max(resolvedValue, 0.0f);
	}

	public float GetLeadingPaddingAndBorder(YogaFlexDirection axis, float widthSize) {
		return GetLeadingPadding(axis, widthSize) + GetLeadingBorder(axis);
	}

	public float GetTrailingPaddingAndBorder(YogaFlexDirection axis, float widthSize) {
		return GetTrailingPadding(axis, widthSize) + GetTrailingBorder(axis);
	}

	public float ResolveFlexGrow() {
		// Root nodes flexGrow should always be 0
		if (_owner == null)
			return 0.0f;

		if (!Float.isNaN(_style.FlexGrow))
			return _style.FlexGrow;

		if (!Float.isNaN(_style.Flex) && _style.Flex > 0.0f)
			return _style.Flex;

		return DefaultFlexGrow;
	}

	public float ResolveFlexShrink() {
		if (_owner == null)
			return 0.0f;

		if (!Float.isNaN(_style.FlexShrink))
			return _style.FlexShrink;

		if (!_config.UseWebDefaults && !Float.isNaN(_style.Flex) && _style.Flex < 0.0f)
			return -_style.Flex;

		return _config.UseWebDefaults ? WebDefaultFlexShrink : DefaultFlexShrink;
	}

	public YogaValue ResolveFlexBasis() {
		YogaValue flexBasis = _style.FlexBasis;
		if (flexBasis.Unit != YogaUnit.Auto && flexBasis.Unit != YogaUnit.Undefined)
			return flexBasis;

		if (!Float.isNaN(_style.Flex) && _style.Flex > 0.0f)
			return _config.UseWebDefaults ? YogaValue.AUTO : YogaValue.ZERO;

		return YogaValue.AUTO;
	}

	public void ResolveDimension() {
		for (int dim = YogaDimension.Width.ordinal(); dim < 2; dim++) {
			if (_style.MaxDimensions[dim].Unit != YogaUnit.Undefined && _style.MaxDimensions[dim].equals(_style.MinDimensions[dim]))
				_resolvedDimensions[dim] = _style.MaxDimensions[dim];
			else
				_resolvedDimensions[dim] = _style.Dimensions[dim];
		}
	}

	public YogaDirection ResolveDirection(YogaDirection ownerDirection) {
		if (_style.Direction == YogaDirection.Inherit)
			return ownerDirection.ordinal() > YogaDirection.Inherit.ordinal() ? ownerDirection : YogaDirection.LeftToRight;

		return _style.Direction;
	}

	public boolean IsNodeFlexible() {
		return ((_style.PositionType == YogaPositionType.Relative) && (ResolveFlexGrow() != 0 || ResolveFlexShrink() != 0));
	}

	public boolean DidUseLegacyFlag() {
		boolean didUseLegacyFlag = _layout.DidUseLegacyFlag;
		if (didUseLegacyFlag)
			return true;

		for (YogaNode child : _children) {
			if (child._layout.DidUseLegacyFlag)
				return true;
		}

		return false;
	}

	// setters
	public void SetLayoutMargin(float margin, YogaEdge index) {
		_layout.Margin[index.ordinal()] = margin;
	}

	public void SetLayoutBorder(float border, YogaEdge index) {
		_layout.Border[index.ordinal()] = border;
	}

	public void SetLayoutPadding(float padding, YogaEdge index) {
		_layout.Padding[index.ordinal()] = padding;
	}

	public void SetLayoutPosition(float position, YogaEdge index) {
		_layout.Position[index.ordinal()] = position;
	}

	public void SetLayoutMeasuredDimension(float measuredDimension, YogaDimension index) {
		_layout.MeasuredDimensions[index.ordinal()] = measuredDimension;
	}

	public void SetLayoutDimension(float dimension, YogaDimension index) {
		_layout.Dimensions[index.ordinal()] = dimension;
	}

	public void SetPosition(YogaDirection direction, float mainSize, float crossSize, float ownerWidth) {
		/* Root nodes should be always layouted as LTR, so we don't return negative
             * values. */
		YogaDirection directionRespectingRoot = _owner != null ? direction : YogaDirection.LeftToRight;
		YogaFlexDirection mainAxis = _style.FlexDirection.ResolveFlexDirection(directionRespectingRoot);
		YogaFlexDirection crossAxis = mainAxis.FlexDirectionCross(directionRespectingRoot);

		float relativePositionMain = GetRelativePosition(mainAxis, mainSize);
		float relativePositionCross = GetRelativePosition(crossAxis, crossSize);

		SetLayoutPosition((GetLeadingMargin(mainAxis, ownerWidth) + relativePositionMain), Leading[mainAxis.ordinal()]);
		SetLayoutPosition((GetTrailingMargin(mainAxis, ownerWidth) + relativePositionMain), Trailing[mainAxis.ordinal()]);
		SetLayoutPosition((GetLeadingMargin(crossAxis, ownerWidth) + relativePositionCross), Leading[crossAxis.ordinal()]);
		SetLayoutPosition((GetTrailingMargin(crossAxis, ownerWidth) + relativePositionCross), Trailing[crossAxis.ordinal()]);
	}

	public void SetAndPropogateUseLegacyFlag(boolean useLegacyFlag) {
		_config.UseLegacyStretchBehaviour = useLegacyFlag;
		for (YogaNode item : _children)
			item._config.UseLegacyStretchBehaviour = useLegacyFlag;
	}

	// Other methods

	public int IndexOf(YogaNode node) {
		return _children.indexOf(node);
	}

	public void Clear() {
		for (YogaNode item : _children)
			item._owner = null;

		_children.clear();
		_isDirty = true;
	}

	public void ReplaceChild(YogaNode oldChild, YogaNode newChild) {
		int index = _children.indexOf(oldChild);
		if (index < 0)
			return;

		newChild._owner = this;
		_children.set(index, newChild);

		MarkDirty();
	}

	public void ReplaceChild(YogaNode child, int index) {
		child._owner = this;
		_children.set(index, child);

		MarkDirty();
	}

	public void Insert(int index, YogaNode child) {
		if (child._owner != null)
			throw new UnsupportedOperationException("Child already has a owner, it must be removed first.");

		if (_measure != null)
			throw new UnsupportedOperationException("Cannot add child: Nodes with measure functions cannot have children.");

		child._owner = this;
		_children.add(index, child);

		MarkDirty();
	}

	public boolean Remove(YogaNode child) {
		if (child._owner == this)
			child._owner = null;

		boolean result = _children.remove(child);
		if (result)
			MarkDirty();

		return result;
	}

	public void RemoveAt(int index) {
		YogaNode child = _children.get(index);
		child._owner = null;
		_children.remove(index);

		MarkDirty();
	}

	public void MarkDirty() {
		if (!_isDirty) {
			_isDirty = true;

			_layout.ComputedFlexBasis = Float.NaN;
			if (_owner != null)
				_owner.MarkDirty();
		}
	}

	public void MarkDirtyAndPropogateDownwards() {
		_isDirty = true;
		for (YogaNode item : _children)
			item.MarkDirtyAndPropogateDownwards();
	}

	public boolean IsLayoutTreeEqualToNode(YogaNode node) {
		if (_children.size() != node._children.size())
			return false;

		if (_layout != node._layout)
			return false;

		if (_children.size() == 0)
			return true;

		boolean isLayoutTreeEqual = true;
		YogaNode otherNodeChildren = null;
		for (int i = 0; i < _children.size(); ++i) {
			otherNodeChildren = node._children.get(i);
			isLayoutTreeEqual = _children.get(i).IsLayoutTreeEqualToNode(otherNodeChildren);
			if (!isLayoutTreeEqual)
				return false;
		}

		return isLayoutTreeEqual;
	}

	private void SetChildTrailingPosition(YogaNode child, YogaFlexDirection axis) {
		float size = child._layout.MeasuredDimensions[Dimension[axis.ordinal()].ordinal()];
		child.SetLayoutPosition(_layout.MeasuredDimensions[Dimension[axis.ordinal()].ordinal()] - size - child._layout.Position[Position[axis.ordinal()].ordinal()], Trailing[axis.ordinal()]);
	}

	private void CloneChildrenIfNeeded() {
		int childCount = _children.size();
		if (childCount == 0)
			return;

		YogaNode firstChild = _children.get(0);
		if (firstChild._owner == this) {
			// If the first child has this node as its owner, we assume that it is
			// already unique. We can do this because if we have it as a child, that
			// means that its owner was at some point cloned which made that subtree
			// immutable. We also assume that all its sibling are cloned as well.
			return;
		}

		YogaNodeCloned cloneNodeCallback = _config.OnNodeCloned;
		for (int i = 0; i < childCount; ++i) {
			YogaNode oldChild = _children.get(i);
			YogaNode newChild = new YogaNode(oldChild);
			newChild._owner = null;

			ReplaceChild(newChild, i);
			newChild._owner = this;
			if (cloneNodeCallback != null)
				cloneNodeCallback.apply(oldChild, newChild, this, i);
		}
	}

	private static float ResolveValueMargin(YogaValue value, float ownerSize) {
		return value.Unit == YogaUnit.Auto ? 0F : value.Resolve(ownerSize);
	}

	private static YogaValue ComputedEdgeValue(YogaValue[] edges, YogaEdge edge, YogaValue defaultValue) {
		if (edges[edge.ordinal()].Unit != YogaUnit.Undefined)
			return edges[edge.ordinal()];

		if ((edge == YogaEdge.Top || edge == YogaEdge.Bottom) && edges[YogaEdge.Vertical.ordinal()].Unit != YogaUnit.Undefined)
			return edges[YogaEdge.Vertical.ordinal()];

		if ((edge == YogaEdge.Left || edge == YogaEdge.Right || edge == YogaEdge.Start || edge == YogaEdge.End) && edges[YogaEdge.Horizontal.ordinal()].Unit != YogaUnit.Undefined)
			return edges[YogaEdge.Horizontal.ordinal()];

		if (edges[YogaEdge.All.ordinal()].Unit != YogaUnit.Undefined)
			return edges[YogaEdge.All.ordinal()];

		if (edge == YogaEdge.Start || edge == YogaEdge.End)
			return YogaValue.UNDEFINED;

		return defaultValue;
	}

	// Layout
	private static final YogaEdge[] Leading = new YogaEdge[] { YogaEdge.Top, YogaEdge.Bottom, YogaEdge.Left, YogaEdge.Right };
	private static final YogaEdge[] Trailing = new YogaEdge[] { YogaEdge.Bottom, YogaEdge.Top, YogaEdge.Right, YogaEdge.Left };
	private static final YogaEdge[] Position = new YogaEdge[] { YogaEdge.Top, YogaEdge.Bottom, YogaEdge.Left, YogaEdge.Right };
	private static final YogaDimension[] Dimension = new YogaDimension[] { YogaDimension.Height, YogaDimension.Height, YogaDimension.Width, YogaDimension.Width };

	@SuppressWarnings("unused")
	private static int gDepth = 0;
	private static int gCurrentGenerationCount = 0;

	public void CalculateLayout() {
		CalculateLayout(Float.NaN, Float.NaN, GetStyleDirection());
	}

	public void CalculateLayout(float ownerWidth, float ownerHeight, YogaDirection ownerDirection) {
		// Increment the generation count. This will force the recursive routine to
		// visit
		// all dirty nodes at least once. Subsequent visits will be skipped if the
		// input
		// parameters don't change.
		gCurrentGenerationCount++;
		ResolveDimension();

		float width = Float.NaN;
		YogaMeasureMode widthMeasureMode = YogaMeasureMode.Undefined;
		if (IsStyleDimensionDefined(YogaFlexDirection.Row, ownerWidth)) {
			width = GetResolvedDimension(Dimension[YogaFlexDirection.Row.ordinal()]).Resolve(ownerWidth) + GetMarginForAxis(YogaFlexDirection.Row, ownerWidth);
			widthMeasureMode = YogaMeasureMode.Exactly;
		} else if (!Float.isNaN(_style.MaxDimensions[YogaDimension.Width.ordinal()].Resolve(ownerWidth))) {
			width = _style.MaxDimensions[YogaDimension.Width.ordinal()].Resolve(ownerWidth);
			widthMeasureMode = YogaMeasureMode.AtMost;
		} else {
			width = ownerWidth;
			widthMeasureMode = Float.isNaN(width) ? YogaMeasureMode.Undefined : YogaMeasureMode.Exactly;
		}

		float height = Float.NaN;
		YogaMeasureMode heightMeasureMode = YogaMeasureMode.Undefined;
		if (IsStyleDimensionDefined(YogaFlexDirection.Column, ownerHeight)) {
			height = GetResolvedDimension(Dimension[YogaFlexDirection.Column.ordinal()]).Resolve(ownerHeight) + GetMarginForAxis(YogaFlexDirection.Column, ownerWidth);
			heightMeasureMode = YogaMeasureMode.Exactly;
		} else if (!Float.isNaN(_style.MaxDimensions[YogaDimension.Height.ordinal()].Resolve(ownerHeight))) {
			height = _style.MaxDimensions[YogaDimension.Height.ordinal()].Resolve(ownerHeight);
			heightMeasureMode = YogaMeasureMode.AtMost;
		} else {
			height = ownerHeight;
			heightMeasureMode = Float.isNaN(height) ? YogaMeasureMode.Undefined : YogaMeasureMode.Exactly;
		}

		if (LayoutNode(width, height, ownerDirection, widthMeasureMode, heightMeasureMode, ownerWidth, ownerHeight, true, "initial", _config)) {
			SetPosition(_layout.Direction, ownerWidth, ownerHeight, ownerWidth);
			RoundToPixelGrid(_config.PointScaleFactor, 0.0f, 0.0f);
		}

		// We want to get rid off `useLegacyStretchBehaviour` from YGConfig. But we
		// aren't sure whether client's of yoga have gotten rid off this flag or not.
        // So logging this in YGLayout would help to find out the call sites depending
        // on this flag. This check would be removed once we are sure no one is
		// dependent on this flag anymore. The flag
		// `shouldDiffLayoutWithoutLegacyStretchBehaviour` in YGConfig will help to
		// run experiments.
		if (_config.ShouldDiffLayoutWithoutLegacyStretchBehaviour && DidUseLegacyFlag()) {
			YogaNode originalNode = DeepClone();
			originalNode.ResolveDimension();
			// Recursively mark nodes as dirty
			originalNode.MarkDirtyAndPropogateDownwards();
			gCurrentGenerationCount++;
			// Rerun the layout, and calculate the diff
			originalNode.SetAndPropogateUseLegacyFlag(false);
			if (originalNode.LayoutNode(width, height, ownerDirection, widthMeasureMode, heightMeasureMode, ownerWidth, ownerHeight, true, "initial", originalNode._config)) {
				originalNode.SetPosition(originalNode._layout.Direction, ownerWidth, ownerHeight, ownerWidth);
				originalNode.RoundToPixelGrid(originalNode._config.PointScaleFactor, 0.0f, 0.0f);

				// Set whether the two layouts are different or not.
				_layout.DoesLegacyStretchFlagAffectsLayout = !originalNode.IsLayoutTreeEqualToNode(this);
			}
		}
	}

	//
	// This is a wrapper around the YGNodelayoutImpl function. It determines
	// whether the layout request is redundant and can be skipped.
	//
	// Parameters:
	//  Input parameters are the same as YGNodelayoutImpl (see above)
	//  Return parameter is true if layout was performed, false if skipped
	//
	private boolean LayoutNode(float availableWidth, float availableHeight, YogaDirection ownerDirection, YogaMeasureMode widthMeasureMode, YogaMeasureMode heightMeasureMode, float ownerWidth, float ownerHeight, boolean performLayout, String reason, YogaConfig config) {
		YogaLayout layout = _layout;

		gDepth++;

		boolean needToVisitNode = (_isDirty && layout.GenerationCount != gCurrentGenerationCount) || layout.LastOwnerDirection != ownerDirection;

		if (needToVisitNode) {
			// Invalidate the cached results.
			layout.NextCachedMeasurementsIndex = 0;
			layout.CachedLayout.WidthMeasureMode = null;
			layout.CachedLayout.HeightMeasureMode = null;
			layout.CachedLayout.ComputedWidth = -1;
			layout.CachedLayout.ComputedHeight = -1;
		}

		YogaCachedMeasurement cachedResults = null;

		// Determine whether the results are already cached. We maintain a separate
		// cache for layouts and measurements. A layout operation modifies the
		// positions
		// and dimensions for nodes in the subtree. The algorithm assumes that each
		// node
		// gets layed out a maximum of one time per tree layout, but multiple
		// measurements
		// may be required to resolve all of the flex dimensions.
		// We handle nodes with measure functions specially here because they are the
		// most
		// expensive to measure, so it's worth avoiding redundant measurements if at
		// all possible.
		if (_measure != null) {
			float marginAxisRow = GetMarginForAxis(YogaFlexDirection.Row, ownerWidth);
			float marginAxisColumn = GetMarginForAxis(YogaFlexDirection.Column, ownerWidth);

			// First, try to use the layout cache.
			if (CanUseCachedMeasurement(widthMeasureMode, availableWidth, heightMeasureMode, availableHeight, layout.CachedLayout.WidthMeasureMode, layout.CachedLayout.AvailableWidth, layout.CachedLayout.HeightMeasureMode, layout.CachedLayout.AvailableHeight, layout.CachedLayout.ComputedWidth, layout.CachedLayout.ComputedHeight, marginAxisRow, marginAxisColumn, config)) {
				cachedResults = layout.CachedLayout;
			} else {
				// Try to use the measurement cache.
				for (int i = 0; i < layout.NextCachedMeasurementsIndex; i++) {
					if (CanUseCachedMeasurement(widthMeasureMode, availableWidth, heightMeasureMode, availableHeight, layout.CachedMeasurements[i].WidthMeasureMode, layout.CachedMeasurements[i].AvailableWidth, layout.CachedMeasurements[i].HeightMeasureMode, layout.CachedMeasurements[i].AvailableHeight, layout.CachedMeasurements[i].ComputedWidth, layout.CachedMeasurements[i].ComputedHeight, marginAxisRow, marginAxisColumn, config)) {
						cachedResults = layout.CachedMeasurements[i];
						break;
					}
				}
			}
		} else if (performLayout) {
			if (YogaMath.FloatsEqual(layout.CachedLayout.AvailableWidth, availableWidth) && YogaMath.FloatsEqual(layout.CachedLayout.AvailableHeight, availableHeight) && layout.CachedLayout.WidthMeasureMode == widthMeasureMode && layout.CachedLayout.HeightMeasureMode == heightMeasureMode) {
				cachedResults = layout.CachedLayout;
			}
		} else {
			for (int i = 0; i < layout.NextCachedMeasurementsIndex; i++) {
				if (YogaMath.FloatsEqual(layout.CachedMeasurements[i].AvailableWidth, availableWidth) && YogaMath.FloatsEqual(layout.CachedMeasurements[i].AvailableHeight, availableHeight) && layout.CachedMeasurements[i].WidthMeasureMode == widthMeasureMode && layout.CachedMeasurements[i].HeightMeasureMode == heightMeasureMode) {
					cachedResults = layout.CachedMeasurements[i];
					break;
				}
			}
		}

		if (!needToVisitNode && cachedResults != null) {
			layout.MeasuredDimensions[YogaDimension.Width.ordinal()] = cachedResults.ComputedWidth;
			layout.MeasuredDimensions[YogaDimension.Height.ordinal()] = cachedResults.ComputedHeight;
		} else {
			LayoutNode(availableWidth, availableHeight, ownerDirection, widthMeasureMode, heightMeasureMode, ownerWidth, ownerHeight, performLayout, config);

			layout.LastOwnerDirection = ownerDirection;

			if (cachedResults == null) {
				if (layout.NextCachedMeasurementsIndex == YogaLayout.MaxCachedResultCount)
					layout.NextCachedMeasurementsIndex = 0;

				YogaCachedMeasurement newCacheEntry = new YogaCachedMeasurement();
				if (performLayout) {
					// Use the single layout cache entry.
					newCacheEntry = layout.CachedLayout;
				} else {
					// Allocate a new measurement cache entry.
					newCacheEntry = layout.CachedMeasurements[layout.NextCachedMeasurementsIndex];
					layout.NextCachedMeasurementsIndex++;
				}

				newCacheEntry.AvailableWidth = availableWidth;
				newCacheEntry.AvailableHeight = availableHeight;
				newCacheEntry.WidthMeasureMode = widthMeasureMode;
				newCacheEntry.HeightMeasureMode = heightMeasureMode;
				newCacheEntry.ComputedWidth = layout.MeasuredDimensions[YogaDimension.Width.ordinal()];
				newCacheEntry.ComputedHeight = layout.MeasuredDimensions[YogaDimension.Height.ordinal()];
			}
		}

		if (performLayout) {
			SetLayoutDimension(_layout.MeasuredDimensions[YogaDimension.Width.ordinal()], YogaDimension.Width);
			SetLayoutDimension(_layout.MeasuredDimensions[YogaDimension.Height.ordinal()], YogaDimension.Height);

			_hasNewLayout = true;
			_isDirty = false;
		}

		gDepth--;
		layout.GenerationCount = gCurrentGenerationCount;
		return (needToVisitNode || cachedResults == null);
	}

	//
	// This is the main routine that implements a subset of the flexbox layout
	// algorithm
	// described in the W3C YG documentation: https://www.w3.org/TR/YG3-flexbox/.
	//
	// Limitations of this algorithm, compared to the full standard:
	//  * Display property is always assumed to be 'flex' except for Text nodes,
	//  which
	//    are assumed to be 'inline-flex'.
	//  * The 'zIndex' property (or any form of z ordering) is not supported. Nodes
	//  are
	//    stacked in document order.
	//  * The 'order' property is not supported. The order of flex items is always
	//  defined
	//    by document order.
	//  * The 'visibility' property is always assumed to be 'visible'. Values of
	//  'collapse'
	//    and 'hidden' are not supported.
	//  * There is no support for forced breaks.
	//  * It does not support vertical inline directions (top-to-bottom or
	//  bottom-to-top text).
	//
	// Deviations from standard:
	//  * Section 4.5 of the spec indicates that all flex items have a default
	//  minimum
	//    main size. For text blocks, for example, this is the width of the widest
	//    word.
	//    Calculating the minimum width is expensive, so we forego it and assume a
	//    default
	//    minimum main size of 0.
	//  * Min/Max sizes in the main axis are not honored when resolving flexible
	//  lengths.
	//  * The spec indicates that the default value for 'flexDirection' is 'row',
	//  but
	//    the algorithm below assumes a default of 'column'.
	//
	// Input parameters:
	//    - node: current node to be sized and layed out
	//    - availableWidth & availableHeight: available size to be used for sizing
	//    the node
	//      or YGUndefined if the size is not available; interpretation depends on
	//      layout
	//      flags
	//    - ownerDirection: the inline (text) direction within the owner
	//    (left-to-right or
	//      right-to-left)
	//    - widthMeasureMode: indicates the sizing rules for the width (see below
	//    for explanation)
	//    - heightMeasureMode: indicates the sizing rules for the height (see below
	//    for explanation)
	//    - performLayout: specifies whether the caller is interested in just the
	//    dimensions
	//      of the node or it requires the entire node and its subtree to be layed
	//      out
	//      (with final positions)
	//
	// Details:
	//    This routine is called recursively to lay out subtrees of flexbox
	//    elements. It uses the
	//    information in node.style, which is treated as a read-only input. It is
	//    responsible for
	//    setting the layout.direction and layout.measuredDimensions fields for the
	//    input node as well
	//    as the layout.position and layout.lineIndex fields for its child nodes.
	//    The
	//    layout.measuredDimensions field includes any border or padding for the
	//    node but does
	//    not include margins.
	//
	//    The spec describes four different layout modes: "fill available", "max
	//    content", "min
	//    content",
	//    and "fit content". Of these, we don't use "min content" because we don't
	//    support default
	//    minimum main sizes (see above for details). Each of our measure modes maps
	//    to a layout mode
	//    from the spec (https://www.w3.org/TR/YG3-sizing/#terms):
	//      - YGMeasureModeUndefined: max content
	//      - YGMeasureModeExactly: fill available
	//      - YGMeasureModeAtMost: fit content
	//
	//    When calling YGNodelayoutImpl and YGLayoutNodeInternal, if the caller passes
	//    an available size of
	//    undefined then it must also pass a measure mode of YGMeasureModeUndefined
	//    in that dimension.
	//
	private void LayoutNode(float availableWidth, float availableHeight, YogaDirection ownerDirection, YogaMeasureMode widthMeasureMode, YogaMeasureMode heightMeasureMode, float ownerWidth, float ownerHeight, boolean performLayout, YogaConfig config) {

		// Set the resolved resolution in the node's layout.
		YogaDirection direction = ResolveDirection(ownerDirection);
		_layout.Direction = direction;

		YogaFlexDirection flexRowDirection = YogaFlexDirection.Row.ResolveFlexDirection(direction);
		YogaFlexDirection flexColumnDirection = YogaFlexDirection.Column.ResolveFlexDirection(direction);

		SetLayoutMargin(GetLeadingMargin(flexRowDirection, ownerWidth), YogaEdge.Start);
		SetLayoutMargin(GetTrailingMargin(flexRowDirection, ownerWidth), YogaEdge.End);
		SetLayoutMargin(GetLeadingMargin(flexColumnDirection, ownerWidth), YogaEdge.Top);
		SetLayoutMargin(GetTrailingMargin(flexColumnDirection, ownerWidth), YogaEdge.Bottom);

		SetLayoutBorder(GetLeadingBorder(flexRowDirection), YogaEdge.Start);
		SetLayoutBorder(GetTrailingBorder(flexRowDirection), YogaEdge.End);
		SetLayoutBorder(GetLeadingBorder(flexColumnDirection), YogaEdge.Top);
		SetLayoutBorder(GetTrailingBorder(flexColumnDirection), YogaEdge.Bottom);

		SetLayoutPadding(GetLeadingPadding(flexRowDirection, ownerWidth), YogaEdge.Start);
		SetLayoutPadding(GetTrailingPadding(flexRowDirection, ownerWidth), YogaEdge.End);
		SetLayoutPadding(GetLeadingPadding(flexColumnDirection, ownerWidth), YogaEdge.Top);
		SetLayoutPadding(GetTrailingPadding(flexColumnDirection, ownerWidth), YogaEdge.Bottom);

		if (_measure != null) {
			WithMeasureFuncSetMeasuredDimensions(availableWidth, availableHeight, widthMeasureMode, heightMeasureMode, ownerWidth, ownerHeight);
			return;
		}

		int childCount = GetChildCount();
		if (childCount == 0) {
			EmptyContainerSetMeasuredDimensions(availableWidth, availableHeight, widthMeasureMode, heightMeasureMode, ownerWidth, ownerHeight);
			return;
		}

		// If we're not being asked to perform a full layout we can skip the algorithm if we already know
		// the size
		if (!performLayout && FixedSizeSetMeasuredDimensions(availableWidth, availableHeight, widthMeasureMode, heightMeasureMode, ownerWidth, ownerHeight)) {
			return;
		}

		// At this point we know we're going to perform work. Ensure that each child has a mutable copy.
		CloneChildrenIfNeeded();
		// Reset layout flags, as they could have changed.
		_layout.HadOverflow = false;

		// STEP 1: CALCULATE VALUES FOR REMAINDER OF ALGORITHM
		YogaFlexDirection mainAxis = _style.FlexDirection.ResolveFlexDirection(direction);
		YogaFlexDirection crossAxis = mainAxis.FlexDirectionCross(direction);
		boolean isMainAxisRow = mainAxis.IsRow();
		boolean isNodeFlexWrap = _style.FlexWrap != YogaWrap.NoWrap;

		float mainAxisOwnerSize = isMainAxisRow ? ownerWidth : ownerHeight;
		float crossAxisOwnerSize = isMainAxisRow ? ownerHeight : ownerWidth;

		float leadingPaddingAndBorderCross = GetLeadingPaddingAndBorder(crossAxis, ownerWidth);
		float paddingAndBorderAxisMain = GetPaddingAndBorderForAxis(mainAxis, ownerWidth);
		float paddingAndBorderAxisCross = GetPaddingAndBorderForAxis(crossAxis, ownerWidth);

		YogaMeasureMode measureModeMainDim = isMainAxisRow ? widthMeasureMode : heightMeasureMode;
		YogaMeasureMode measureModeCrossDim = isMainAxisRow ? heightMeasureMode : widthMeasureMode;

		float paddingAndBorderAxisRow = isMainAxisRow ? paddingAndBorderAxisMain : paddingAndBorderAxisCross;
		float paddingAndBorderAxisColumn = isMainAxisRow ? paddingAndBorderAxisCross : paddingAndBorderAxisMain;

		float marginAxisRow = GetMarginForAxis(YogaFlexDirection.Row, ownerWidth);
		float marginAxisColumn = GetMarginForAxis(YogaFlexDirection.Column, ownerWidth);

		float minInnerWidth = _style.MinDimensions[YogaDimension.Width.ordinal()].Resolve(ownerWidth) - paddingAndBorderAxisRow;
		float maxInnerWidth = _style.MaxDimensions[YogaDimension.Width.ordinal()].Resolve(ownerWidth) - paddingAndBorderAxisRow;
		float minInnerHeight = _style.MinDimensions[YogaDimension.Height.ordinal()].Resolve(ownerHeight) - paddingAndBorderAxisColumn;
		float maxInnerHeight = _style.MaxDimensions[YogaDimension.Height.ordinal()].Resolve(ownerHeight) - paddingAndBorderAxisColumn;

		float minInnerMainDim = isMainAxisRow ? minInnerWidth : minInnerHeight;
		float maxInnerMainDim = isMainAxisRow ? maxInnerWidth : maxInnerHeight;

		// STEP 2: DETERMINE AVAILABLE SIZE IN MAIN AND CROSS DIRECTIONS

		float availableInnerWidth = CalculateAvailableInnerDim(YogaFlexDirection.Row, availableWidth, ownerWidth);
		float availableInnerHeight = CalculateAvailableInnerDim(YogaFlexDirection.Column, availableHeight, ownerHeight);

		float availableInnerMainDim = isMainAxisRow ? availableInnerWidth : availableInnerHeight;
		float availableInnerCrossDim = isMainAxisRow ? availableInnerHeight : availableInnerWidth;

		float totalOuterFlexBasis = 0F;

		// STEP 3: DETERMINE FLEX BASIS FOR EACH ITEM

		totalOuterFlexBasis = ComputeFlexBasisForChildren(availableInnerWidth, availableInnerHeight, widthMeasureMode, heightMeasureMode, direction, mainAxis, config, performLayout, /*ref */ totalOuterFlexBasis);

		boolean flexBasisOverflows = measureModeMainDim == YogaMeasureMode.Undefined ? false : totalOuterFlexBasis > availableInnerMainDim;
		if (isNodeFlexWrap && flexBasisOverflows && measureModeMainDim == YogaMeasureMode.AtMost)
			measureModeMainDim = YogaMeasureMode.Exactly;

		// STEP 4: COLLECT FLEX ITEMS INTO FLEX LINES

		// Indexes of children that represent the first and last items in the line.
		int startOfLineIndex = 0;
		int endOfLineIndex = 0;

		// Number of lines.
		int lineCount = 0;

		// Accumulated cross dimensions of all lines so far.
		float totalLineCrossDim = 0F;

		// Max main dimension of all the lines.
		float maxLineMainDim = 0F;

		YogaCollectFlexItemsRowValues collectedFlexItemsValues = new YogaCollectFlexItemsRowValues();
		for (; endOfLineIndex < childCount; lineCount++, startOfLineIndex = endOfLineIndex) {
			collectedFlexItemsValues = CalculateCollectFlexItemsRowValues(ownerDirection, mainAxisOwnerSize, availableInnerWidth, availableInnerMainDim, startOfLineIndex, lineCount);

			endOfLineIndex = collectedFlexItemsValues.EndOfLineIndex;

			// If we don't need to measure the cross axis, we can skip the entire flex
			// step.
			boolean canSkipFlex = !performLayout && measureModeCrossDim == YogaMeasureMode.Exactly;

			// STEP 5: RESOLVING FLEXIBLE LENGTHS ON MAIN AXIS
			// Calculate the remaining available space that needs to be allocated.
			// If the main dimension size isn't known, it is computed based on
			// the line length, so there's no more space left to distribute.

			boolean sizeBasedOnContent = false;
			// If we don't measure with exact main dimension we want to ensure we don't violate min and max
			if (measureModeMainDim != YogaMeasureMode.Exactly) {
				if (!Float.isNaN(minInnerMainDim) && collectedFlexItemsValues.SizeConsumedOnCurrentLine < minInnerMainDim) {
					availableInnerMainDim = minInnerMainDim;
				} else if (!Float.isNaN(maxInnerMainDim) && collectedFlexItemsValues.SizeConsumedOnCurrentLine > maxInnerMainDim) {
					availableInnerMainDim = maxInnerMainDim;
				} else {
					if (!_config.UseLegacyStretchBehaviour && ((!Float.isNaN(collectedFlexItemsValues.TotalFlexGrowFactors) && collectedFlexItemsValues.TotalFlexGrowFactors == 0) || (ResolveFlexGrow() == 0))) {
						// If we don't have any children to flex or we can't flex the node
						// itself, space we've used is all space we need. Root node also
						// should be shrunk to minimum
						availableInnerMainDim = collectedFlexItemsValues.SizeConsumedOnCurrentLine;
					}

					if (_config.UseLegacyStretchBehaviour)
						_layout.DidUseLegacyFlag = true;

					sizeBasedOnContent = !_config.UseLegacyStretchBehaviour;
				}
			}

			if (!sizeBasedOnContent && !Float.isNaN(availableInnerMainDim)) {
				collectedFlexItemsValues.RemainingFreeSpace = availableInnerMainDim - collectedFlexItemsValues.SizeConsumedOnCurrentLine;
			} else if (collectedFlexItemsValues.SizeConsumedOnCurrentLine < 0) {
				// availableInnerMainDim is indefinite which means the node is being sized based on its
				// content.
				// sizeConsumedOnCurrentLine is negative which means the node will allocate 0 points for
				// its content. Consequently, remainingFreeSpace is 0 - sizeConsumedOnCurrentLine.
				collectedFlexItemsValues.RemainingFreeSpace = -collectedFlexItemsValues.SizeConsumedOnCurrentLine;
			}

			if (!canSkipFlex) {

				ResolveFlexibleLength(/*ref */ collectedFlexItemsValues, mainAxis, crossAxis, mainAxisOwnerSize, availableInnerMainDim, availableInnerCrossDim, availableInnerWidth, availableInnerHeight, flexBasisOverflows, measureModeCrossDim, performLayout, config);
			}

			_layout.HadOverflow = _layout.HadOverflow || (collectedFlexItemsValues.RemainingFreeSpace < 0);

			// STEP 6: MAIN-AXIS JUSTIFICATION & CROSS-AXIS SIZE DETERMINATION

			// At this point, all the children have their dimensions set in the main
			// axis.
			// Their dimensions are also set in the cross axis with the exception of
			// items
			// that are aligned "stretch". We need to compute these stretch values and
			// set the final positions.

			JustifyMainAxisReturn ret = JustifyMainAxis(/*ref */ collectedFlexItemsValues, /*ref */ startOfLineIndex, /*ref */ mainAxis, /*ref */ crossAxis, /*ref */ measureModeMainDim, /*ref */ measureModeCrossDim, /*ref */ mainAxisOwnerSize, /*ref */ ownerWidth, /*ref */ availableInnerMainDim, /*ref */ availableInnerCrossDim, /*ref */ availableInnerWidth, /*ref */ performLayout);
			ret.collectedFlexItemsValues = collectedFlexItemsValues;
			ret.startOfLineIndex = startOfLineIndex;
			ret.mainAxis = mainAxis;
			ret.crossAxis = crossAxis;
			ret.measureModeMainDim = measureModeMainDim;
			ret.measureModeCrossDim = measureModeCrossDim;
			ret.mainAxisOwnerSize = mainAxisOwnerSize;
			ret.ownerWidth = ownerWidth;
			ret.availableInnerMainDim = availableInnerMainDim;
			ret.availableInnerCrossDim = availableInnerCrossDim;
			ret.availableInnerWidth = availableInnerWidth;
			ret.performLayout = performLayout;

			float containerCrossAxis = availableInnerCrossDim;
			if (measureModeCrossDim == YogaMeasureMode.Undefined || measureModeCrossDim == YogaMeasureMode.AtMost) {
				// Compute the cross axis from the max cross dimension of the children.
				containerCrossAxis = BoundAxis(crossAxis, collectedFlexItemsValues.CrossDimension + paddingAndBorderAxisCross, crossAxisOwnerSize, ownerWidth) - paddingAndBorderAxisCross;
			}

			// If there's no flex wrap, the cross dimension is defined by the container.
			if (!isNodeFlexWrap && measureModeCrossDim == YogaMeasureMode.Exactly) {
				collectedFlexItemsValues.CrossDimension = availableInnerCrossDim;
			}

			// Clamp to the min/max size specified on the container.
			collectedFlexItemsValues.CrossDimension =

					BoundAxis(crossAxis, collectedFlexItemsValues.CrossDimension + paddingAndBorderAxisCross, crossAxisOwnerSize, ownerWidth) - paddingAndBorderAxisCross;

			// STEP 7: CROSS-AXIS ALIGNMENT
			// We can skip child alignment if we're just measuring the container.
			if (performLayout) {
				for (int i = startOfLineIndex; i < endOfLineIndex; i++) {
					YogaNode child = GetChild(i);
					if (child._style.Display == YogaDisplay.None)
						continue;

					if (child._style.PositionType == YogaPositionType.Absolute) {
						// If the child is absolutely positioned and has a
						// top/left/bottom/right set, override
						// all the previously computed positions to set it correctly.
						boolean isChildLeadingPosDefined = child.IsLeadingPositionDefined(crossAxis);
						if (isChildLeadingPosDefined)
							child.SetLayoutPosition(child.GetLeadingPosition(crossAxis, availableInnerCrossDim) + GetLeadingBorder(crossAxis) + child.GetLeadingMargin(crossAxis, availableInnerWidth), Position[crossAxis.ordinal()]);

						// If leading position is not defined or calculations result in Nan, default to border + margin
						if (!isChildLeadingPosDefined || Float.isNaN(child._layout.Position[Position[crossAxis.ordinal()].ordinal()]))
							child.SetLayoutPosition(GetLeadingBorder(crossAxis) + (child.GetLeadingMargin(crossAxis, availableInnerWidth)), Position[crossAxis.ordinal()]);
					} else {
						float leadingCrossDim = leadingPaddingAndBorderCross;

						// For a relative children, we're either using alignItems (owner) or
						// alignSelf (child) in order to determine the position in the cross
						// axis
						YogaAlign alignItem = GetAlign(child);

						// If the child uses align stretch, we need to lay it out one more
						// time, this time
						// forcing the cross-axis size to be the computed cross size for the
						// current line.
						if (alignItem == YogaAlign.Stretch && child.GetMarginLeadingValue(crossAxis).Unit != YogaUnit.Auto && child.GetMarginTrailingValue(crossAxis).Unit != YogaUnit.Auto) {
							// If the child defines a definite size for its cross axis, there's
							// no need to stretch.
							if (!child.IsStyleDimensionDefined(crossAxis, availableInnerCrossDim)) {
								float childMainSize = child._layout.MeasuredDimensions[Dimension[mainAxis.ordinal()].ordinal()];
								float childCrossSize = !Float.isNaN(child._style.AspectRatio) ? ((child.GetMarginForAxis(crossAxis, availableInnerWidth) + (isMainAxisRow ? childMainSize / child._style.AspectRatio : childMainSize * child._style.AspectRatio))) : collectedFlexItemsValues.CrossDimension;

								childMainSize += child.GetMarginForAxis(mainAxis, availableInnerWidth);

								YogaMeasureMode childMainMeasureMode = YogaMeasureMode.Exactly;
								YogaMeasureMode childCrossMeasureMode = YogaMeasureMode.Exactly;

								AtomicReference<YogaMeasureMode> childMainMeasureModeRef = new AtomicReference<>(childMainMeasureMode);
								AtomicReference<YogaMeasureMode> childCrossMeasureModeRef = new AtomicReference<>(childCrossMeasureMode);
								AtomicReference<Float> childMainSizeRef = new AtomicReference<>(childMainSize);
								AtomicReference<Float> childCrossSizeRef = new AtomicReference<>(childCrossSize);
								child.ConstrainMaxSizeForMode(mainAxis, availableInnerMainDim, availableInnerWidth, /*ref */ childMainMeasureModeRef, /*ref */ childMainSizeRef);
								child.ConstrainMaxSizeForMode(crossAxis, availableInnerCrossDim, availableInnerWidth, /*ref */ childCrossMeasureModeRef, /*ref */ childCrossSizeRef);
								//childMainMeasureMode = childMainMeasureModeRef.get();
								//childCrossMeasureMode = childCrossMeasureModeRef.get();
								childMainSize = childMainSizeRef.get();
								childCrossSize = childCrossSizeRef.get();

								float childWidth = isMainAxisRow ? childMainSize : childCrossSize;
								float childHeight = !isMainAxisRow ? childMainSize : childCrossSize;

								YogaMeasureMode childWidthMeasureMode = Float.isNaN(childWidth) ? YogaMeasureMode.Undefined : YogaMeasureMode.Exactly;
								YogaMeasureMode childHeightMeasureMode = Float.isNaN(childHeight) ? YogaMeasureMode.Undefined : YogaMeasureMode.Exactly;

								child.LayoutNode(childWidth, childHeight, direction, childWidthMeasureMode, childHeightMeasureMode, availableInnerWidth, availableInnerHeight, true, "stretch", config);
							}
						} else {
							float remainingCrossDim = containerCrossAxis - child.GetDimensionWithMargin(crossAxis, availableInnerWidth);

							if (child.GetMarginLeadingValue(crossAxis).Unit == YogaUnit.Auto && child.GetMarginTrailingValue(crossAxis).Unit == YogaUnit.Auto) {
								leadingCrossDim += YogaMath.Max(0.0f, remainingCrossDim / 2);
							} else if (child.GetMarginTrailingValue(crossAxis).Unit == YogaUnit.Auto) {
								// No-Op
							} else if (child.GetMarginLeadingValue(crossAxis).Unit == YogaUnit.Auto) {
								leadingCrossDim += YogaMath.Max(0.0f, remainingCrossDim);
							} else if (alignItem == YogaAlign.FlexStart) {
								// No-Op
							} else if (alignItem == YogaAlign.Center) {
								if (!Float.isNaN(remainingCrossDim))
									leadingCrossDim += remainingCrossDim / 2;

							} else {
								if (!Float.isNaN(remainingCrossDim))
									leadingCrossDim += remainingCrossDim;

							}
						}

						// And we apply the position
						child.SetLayoutPosition(child._layout.Position[Position[crossAxis.ordinal()].ordinal()] + totalLineCrossDim + leadingCrossDim, Position[crossAxis.ordinal()]);
					}
				}
			}

			totalLineCrossDim += collectedFlexItemsValues.CrossDimension;
			maxLineMainDim = YogaMath.Max(maxLineMainDim, collectedFlexItemsValues.MainDimension);
		}

		// STEP 8: MULTI-LINE CONTENT ALIGNMENT
		if (performLayout && (lineCount > 1 || IsBaselineLayout())) {
			float crossDimLead = 0F;
			float currentLead = leadingPaddingAndBorderCross;

			if (!Float.isNaN(availableInnerCrossDim)) {
				float remainingAlignContentDim = availableInnerCrossDim - totalLineCrossDim;
				switch (_style.AlignContent) {
				case FlexEnd:
					currentLead += remainingAlignContentDim;
					break;
				case Center:
					currentLead += remainingAlignContentDim / 2;
					break;
				case Stretch:
					if (availableInnerCrossDim > totalLineCrossDim) {
						crossDimLead = remainingAlignContentDim / lineCount;
					}
					break;
				case SpaceAround:
					if (availableInnerCrossDim > totalLineCrossDim) {
						currentLead += remainingAlignContentDim / (2 * lineCount);
						if (lineCount > 1) {
							crossDimLead = remainingAlignContentDim / lineCount;
						}
					} else {
						currentLead += remainingAlignContentDim / 2;
					}
					break;
				case SpaceBetween:
					if (availableInnerCrossDim > totalLineCrossDim && lineCount > 1) {
						crossDimLead = remainingAlignContentDim / (lineCount - 1);
					}
					break;
				case Auto:
				case FlexStart:
				case Baseline:
					break;
				}
			}

			int endIndex = 0;
			for (int i = 0; i < lineCount; i++) {
				int startIndex = endIndex;
				int ii;

				// compute the line's height and find the endIndex
				float lineHeight = 0F;
				float maxAscentForCurrentLine = 0F;
				float maxDescentForCurrentLine = 0F;
				for (ii = startIndex; ii < childCount; ii++) {
					YogaNode child = GetChild(ii);
					if (child._style.Display == YogaDisplay.None)
						continue;

					if (child._style.PositionType == YogaPositionType.Relative) {
						if (child._lineIndex != i)
							break;

						if (child.IsLayoutDimensionDefined(crossAxis)) {
							lineHeight = YogaMath.Max(lineHeight, child._layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()] + child.GetMarginForAxis(crossAxis, availableInnerWidth));
						}

						if (GetAlign(child) == YogaAlign.Baseline) {
							float ascent = CalculateBaseline(child) + child.GetLeadingMargin(YogaFlexDirection.Column, availableInnerWidth);
							float descent = child._layout.MeasuredDimensions[YogaDimension.Height.ordinal()] + child.GetMarginForAxis(YogaFlexDirection.Column, availableInnerWidth) - ascent;

							maxAscentForCurrentLine = YogaMath.Max(maxAscentForCurrentLine, ascent);
							maxDescentForCurrentLine = YogaMath.Max(maxDescentForCurrentLine, descent);
							lineHeight = YogaMath.Max(lineHeight, maxAscentForCurrentLine + maxDescentForCurrentLine);
						}
					}
				}
				endIndex = ii;
				lineHeight += crossDimLead;

				if (performLayout) {
					for (ii = startIndex; ii < endIndex; ii++) {
						YogaNode child = GetChild(ii);
						if (child._style.Display == YogaDisplay.None)
							continue;

						if (child._style.PositionType == YogaPositionType.Relative) {
							switch (GetAlign(child)) {
							case FlexStart: {
								child.SetLayoutPosition(currentLead + child.GetLeadingMargin(crossAxis, availableInnerWidth), Position[crossAxis.ordinal()]);
								break;
							}
							case FlexEnd: {
								child.SetLayoutPosition(currentLead + lineHeight - child.GetTrailingMargin(crossAxis, availableInnerWidth) - child._layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()], Position[crossAxis.ordinal()]);
								break;
							}
							case Center: {
								float childHeight = child._layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()];
								child.SetLayoutPosition(currentLead + (lineHeight - childHeight) / 2, Position[crossAxis.ordinal()]);
								break;
							}
							case Stretch: {
								child.SetLayoutPosition(currentLead + child.GetLeadingMargin(crossAxis, availableInnerWidth), Position[crossAxis.ordinal()]);

								// Remeasure child with the line height as it as been only measured with the
								// owners height yet.
								if (!child.IsStyleDimensionDefined(crossAxis, availableInnerCrossDim)) {
									float childWidth = isMainAxisRow ? (child._layout.MeasuredDimensions[YogaDimension.Width.ordinal()] + child.GetMarginForAxis(mainAxis, availableInnerWidth)) : lineHeight;

									float childHeight = !isMainAxisRow ? (child._layout.MeasuredDimensions[YogaDimension.Height.ordinal()] + child.GetMarginForAxis(crossAxis, availableInnerWidth)) : lineHeight;

									if ((!YogaMath.FloatsEqual(childWidth, child._layout.MeasuredDimensions[YogaDimension.Width.ordinal()]) || !YogaMath.FloatsEqual(childHeight, child._layout.MeasuredDimensions[YogaDimension.Height.ordinal()]))) {
										child.LayoutNode(childWidth, childHeight, direction, YogaMeasureMode.Exactly, YogaMeasureMode.Exactly, availableInnerWidth, availableInnerHeight, true, "multiline-stretch", config);
									}
								}
								break;
							}
							case Baseline: {
								child.SetLayoutPosition(currentLead + maxAscentForCurrentLine - CalculateBaseline(child) + child.GetLeadingPosition(YogaFlexDirection.Column, availableInnerCrossDim), YogaEdge.Top);

								break;
							}
							case Auto:
							case SpaceBetween:
							case SpaceAround:
								break;
							}
						}
					}
				}

				currentLead += lineHeight;
			}
		}

		// STEP 9: COMPUTING FINAL DIMENSIONS

		SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Row, availableWidth - marginAxisRow, ownerWidth, ownerWidth), YogaDimension.Width);

		SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Column, availableHeight - marginAxisColumn, ownerHeight, ownerWidth), YogaDimension.Height);

		// If the user didn't specify a width or height for the node, set the
		// dimensions based on the children.
		if (measureModeMainDim == YogaMeasureMode.Undefined || (_style.Overflow != YogaOverflow.Scroll && measureModeMainDim == YogaMeasureMode.AtMost)) {
			// Clamp the size to the min/max size, if specified, and make sure it
			// doesn't go below the padding and border amount.
			SetLayoutMeasuredDimension(BoundAxis(mainAxis, maxLineMainDim, mainAxisOwnerSize, ownerWidth), Dimension[mainAxis.ordinal()]);

		} else if (measureModeMainDim == YogaMeasureMode.AtMost && _style.Overflow == YogaOverflow.Scroll) {
			SetLayoutMeasuredDimension(YogaMath.Max(YogaMath.Min(availableInnerMainDim + paddingAndBorderAxisMain, BoundAxisWithinMinAndMax(mainAxis, maxLineMainDim, mainAxisOwnerSize)), paddingAndBorderAxisMain), Dimension[mainAxis.ordinal()]);
		}

		if (measureModeCrossDim == YogaMeasureMode.Undefined || (_style.Overflow != YogaOverflow.Scroll && measureModeCrossDim == YogaMeasureMode.AtMost)) {
			// Clamp the size to the min/max size, if specified, and make sure it
			// doesn't go below the padding and border amount.

			SetLayoutMeasuredDimension(BoundAxis(crossAxis, totalLineCrossDim + paddingAndBorderAxisCross, crossAxisOwnerSize, ownerWidth), Dimension[crossAxis.ordinal()]);

		} else if (measureModeCrossDim == YogaMeasureMode.AtMost && _style.Overflow == YogaOverflow.Scroll) {
			SetLayoutMeasuredDimension(YogaMath.Max(YogaMath.Min(availableInnerCrossDim + paddingAndBorderAxisCross, BoundAxisWithinMinAndMax(crossAxis, totalLineCrossDim + paddingAndBorderAxisCross, crossAxisOwnerSize)), paddingAndBorderAxisCross), Dimension[crossAxis.ordinal()]);
		}

		// As we only wrapped in normal direction yet, we need to reverse the positions on wrap-reverse.
		if (performLayout && _style.FlexWrap == YogaWrap.WrapReverse) {
			for (int i = 0; i < childCount; i++) {
				YogaNode child = GetChild(i);
				if (child._style.PositionType == YogaPositionType.Relative) {
					child.SetLayoutPosition(_layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()] - child._layout.Position[Position[crossAxis.ordinal()].ordinal()] - child._layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()], Position[crossAxis.ordinal()]);
				}
			}
		}

		if (performLayout) {
			// STEP 10: SIZING AND POSITIONING ABSOLUTE CHILDREN
			for (YogaNode child : _children) {
				if (child._style.PositionType != YogaPositionType.Absolute)
					continue;

				AbsoluteLayoutChild(child, availableInnerWidth, isMainAxisRow ? measureModeMainDim : measureModeCrossDim, availableInnerHeight, direction, config);
			}

			// STEP 11: SETTING TRAILING POSITIONS FOR CHILDREN
			boolean needsMainTrailingPos = mainAxis == YogaFlexDirection.RowReverse || mainAxis == YogaFlexDirection.ColumnReverse;
			boolean needsCrossTrailingPos = crossAxis == YogaFlexDirection.RowReverse || crossAxis == YogaFlexDirection.ColumnReverse;

			// Set trailing position if necessary.
			if (needsMainTrailingPos || needsCrossTrailingPos) {
				for (int i = 0; i < childCount; i++) {
					YogaNode child = GetChild(i);
					if (child._style.Display == YogaDisplay.None)
						continue;

					if (needsMainTrailingPos)
						SetChildTrailingPosition(child, mainAxis);

					if (needsCrossTrailingPos)
						SetChildTrailingPosition(child, crossAxis);
				}
			}
		}

	}

	private void WithMeasureFuncSetMeasuredDimensions(float availableWidth, float availableHeight, YogaMeasureMode widthMeasureMode, YogaMeasureMode heightMeasureMode, float ownerWidth, float ownerHeight) {
		if (_measure == null)
			throw new UnsupportedOperationException("Measure must not be null");

		float paddingAndBorderAxisRow = GetPaddingAndBorderForAxis(YogaFlexDirection.Row, availableWidth);
		float paddingAndBorderAxisColumn = GetPaddingAndBorderForAxis(YogaFlexDirection.Column, availableWidth);
		float marginAxisRow = GetMarginForAxis(YogaFlexDirection.Row, availableWidth);
		float marginAxisColumn = GetMarginForAxis(YogaFlexDirection.Column, availableWidth);

		// We want to make sure we don't call measure with negative size
		float innerWidth = Float.isNaN(availableWidth) ? availableWidth : YogaMath.Max(0, availableWidth - marginAxisRow - paddingAndBorderAxisRow);
		float innerHeight = Float.isNaN(availableHeight) ? availableHeight : YogaMath.Max(0, availableHeight - marginAxisColumn - paddingAndBorderAxisColumn);

		if (widthMeasureMode == YogaMeasureMode.Exactly && heightMeasureMode == YogaMeasureMode.Exactly) {
			// Don't bother sizing the text if both dimensions are already defined.
			SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Row, availableWidth - marginAxisRow, ownerWidth, ownerWidth), YogaDimension.Width);

			SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Column, availableHeight - marginAxisColumn, ownerHeight, ownerWidth), YogaDimension.Height);
		} else {
			// Measure the text under the current raints.
			YogaSize measuredSize = _measure.apply(this, innerWidth, widthMeasureMode, innerHeight, heightMeasureMode);

			SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Row, (widthMeasureMode == YogaMeasureMode.Undefined || widthMeasureMode == YogaMeasureMode.AtMost) ? measuredSize.Width + paddingAndBorderAxisRow : availableWidth - marginAxisRow, ownerWidth, ownerWidth), YogaDimension.Width);

			SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Column, (heightMeasureMode == YogaMeasureMode.Undefined || heightMeasureMode == YogaMeasureMode.AtMost) ? measuredSize.Height + paddingAndBorderAxisColumn : availableHeight - marginAxisColumn, ownerHeight, ownerWidth), YogaDimension.Height);
		}
	}

	// For nodes with no children, use the available values if they were provided,
	// or the minimum size as indicated by the padding and border sizes.
	private void EmptyContainerSetMeasuredDimensions(float availableWidth, float availableHeight, YogaMeasureMode widthMeasureMode, YogaMeasureMode heightMeasureMode, float ownerWidth, float ownerHeight) {
		float paddingAndBorderAxisRow = GetPaddingAndBorderForAxis(YogaFlexDirection.Row, ownerWidth);
		float paddingAndBorderAxisColumn = GetPaddingAndBorderForAxis(YogaFlexDirection.Column, ownerWidth);
		float marginAxisRow = GetMarginForAxis(YogaFlexDirection.Row, ownerWidth);
		float marginAxisColumn = GetMarginForAxis(YogaFlexDirection.Column, ownerWidth);

		SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Row, (widthMeasureMode == YogaMeasureMode.Undefined || widthMeasureMode == YogaMeasureMode.AtMost) ? paddingAndBorderAxisRow : availableWidth - marginAxisRow, ownerWidth, ownerWidth), YogaDimension.Width);

		SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Column, (heightMeasureMode == YogaMeasureMode.Undefined || heightMeasureMode == YogaMeasureMode.AtMost) ? paddingAndBorderAxisColumn : availableHeight - marginAxisColumn, ownerHeight, ownerWidth), YogaDimension.Height);
	}

	private boolean FixedSizeSetMeasuredDimensions(float availableWidth, float availableHeight, YogaMeasureMode widthMeasureMode, YogaMeasureMode heightMeasureMode, float ownerWidth, float ownerHeight) {
		if ((!Float.isNaN(availableWidth) && widthMeasureMode == YogaMeasureMode.AtMost && availableWidth <= 0.0f) || (!Float.isNaN(availableHeight) && heightMeasureMode == YogaMeasureMode.AtMost && availableHeight <= 0.0f) || (widthMeasureMode == YogaMeasureMode.Exactly && heightMeasureMode == YogaMeasureMode.Exactly)) {
			float marginAxisColumn = GetMarginForAxis(YogaFlexDirection.Column, ownerWidth);
			float marginAxisRow = GetMarginForAxis(YogaFlexDirection.Row, ownerWidth);

			SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Row, Float.isNaN(availableWidth) || (widthMeasureMode == YogaMeasureMode.AtMost && availableWidth < 0.0f) ? 0.0f : availableWidth - marginAxisRow, ownerWidth, ownerWidth), YogaDimension.Width);

			SetLayoutMeasuredDimension(BoundAxis(YogaFlexDirection.Column, Float.isNaN(availableHeight) || (heightMeasureMode == YogaMeasureMode.AtMost && availableHeight < 0.0f) ? 0.0f : availableHeight - marginAxisColumn, ownerHeight, ownerWidth), YogaDimension.Height);
			return true;
		}

		return false;
	}

	private void ZeroOutLayoutRecursively() {
		_layout.Clear();
		_hasNewLayout = true;
		CloneChildrenIfNeeded();
		int childCount = GetChildCount();
		for (int i = 0; i < childCount; i++) {
			YogaNode child = GetChild(i);
			child.ZeroOutLayoutRecursively();
		}
	}

	private float CalculateAvailableInnerDim(YogaFlexDirection axis, float availableDim, float ownerDim) {
		YogaFlexDirection direction = axis.IsRow() ? YogaFlexDirection.Row : YogaFlexDirection.Column;
		YogaDimension dimension = axis.IsRow() ? YogaDimension.Width : YogaDimension.Height;

		float margin = GetMarginForAxis(direction, ownerDim);
		float paddingAndBorder = GetPaddingAndBorderForAxis(direction, ownerDim);

		float availableInnerDim = availableDim - margin - paddingAndBorder;
		// Max dimension overrides predefined dimension value; Min dimension in turn
		// overrides both of the above
		if (!Float.isNaN(availableInnerDim)) {
			// We want to make sure our available height does not violate min and max
			// constraints
			float minInnerResolved = _style.MinDimensions[dimension.ordinal()].Resolve(ownerDim);
			float minInnerDim = Float.isNaN(minInnerResolved) ? 0.0F : minInnerResolved - paddingAndBorder;

			float maxInnerResolved = _style.MaxDimensions[dimension.ordinal()].Resolve(ownerDim);
			float maxInnerDim = Float.isNaN(maxInnerResolved) ? Float.MAX_VALUE : maxInnerResolved - paddingAndBorder;

			availableInnerDim = YogaMath.Max(YogaMath.Min(availableInnerDim, maxInnerDim), minInnerDim);
		}

		return availableInnerDim;
	}

	private float ComputeFlexBasisForChildren(float availableInnerWidth, float availableInnerHeight, YogaMeasureMode widthMeasureMode, YogaMeasureMode heightMeasureMode, YogaDirection direction, YogaFlexDirection mainAxis, YogaConfig config, boolean performLayout, /*ref */ float totalOuterFlexBasis) {
		YogaNode singleFlexChild = null;
		List<YogaNode> children = _children;
		YogaMeasureMode measureModeMainDim = mainAxis.IsRow() ? widthMeasureMode : heightMeasureMode;

		// If there is only one child with flexGrow + flexShrink it means we can set
		// the computedFlexBasis to 0 instead of measuring and shrinking / flexing the
		// child to exactly match the remaining space

		if (measureModeMainDim == YogaMeasureMode.Exactly) {

			for (YogaNode child : children) {
				if (child.IsNodeFlexible()) {
					if (singleFlexChild != null || YogaMath.FloatsEqual(child.ResolveFlexGrow(), 0.0f) || YogaMath.FloatsEqual(child.ResolveFlexShrink(), 0.0f)) {
						// There is already a flexible child, or this flexible child doesn't
						// have flexGrow and flexShrink, abort
						singleFlexChild = null;
						break;
					} else {
						singleFlexChild = child;
					}
				}
			}
		}

		for (YogaNode child : children) {
			child.ResolveDimension();
			if (child._style.Display == YogaDisplay.None) {
				child.ZeroOutLayoutRecursively();
				child._hasNewLayout = true;
				child._isDirty = false;
				continue;
			}

			if (performLayout) {
				// Set the initial position (relative to the owner).
				YogaDirection childDirection = child.ResolveDirection(direction);
				float mainDim = mainAxis.IsRow() ? availableInnerWidth : availableInnerHeight;
				float crossDim = mainAxis.IsRow() ? availableInnerHeight : availableInnerWidth;

				child.SetPosition(childDirection, mainDim, crossDim, availableInnerWidth);
			}

			if (child._style.PositionType == YogaPositionType.Absolute)
				continue;

			if (child == singleFlexChild) {
				child._layout.ComputedFlexBasisGeneration = gCurrentGenerationCount;
				child._layout.ComputedFlexBasis = 0;
			} else {
				ComputeFlexBasisForChild(child, availableInnerWidth, widthMeasureMode, availableInnerHeight, availableInnerWidth, availableInnerHeight, heightMeasureMode, direction, config);
			}

			totalOuterFlexBasis += child._layout.ComputedFlexBasis + child.GetMarginForAxis(mainAxis, availableInnerWidth);
		}
		return totalOuterFlexBasis;
	}

	private float BoundAxisWithinMinAndMax(YogaFlexDirection axis, float value, float axisSize) {
		float min = Float.NaN;
		float max = Float.NaN;

		if (axis.IsColumn()) {
			min = _style.MinDimensions[YogaDimension.Height.ordinal()].Resolve(axisSize);
			max = _style.MaxDimensions[YogaDimension.Height.ordinal()].Resolve(axisSize);
		} else if (axis.IsRow()) {
			min = _style.MinDimensions[YogaDimension.Width.ordinal()].Resolve(axisSize);
			max = _style.MaxDimensions[YogaDimension.Width.ordinal()].Resolve(axisSize);
		}

		float boundValue = value;
		if (!Float.isNaN(max) && max >= 0.0f && boundValue > max)
			boundValue = max;

		if (!Float.isNaN(min) && min >= 0.0f && boundValue < min)
			boundValue = min;

		return boundValue;
	}

	// Like YGNodeBoundAxisWithinMinAndMax but also ensures that the value doesn't go
	// below the
	// padding and border amount.
	private float BoundAxis(YogaFlexDirection axis, float value, float axisSize, float widthSize) {
		return YogaMath.Max(BoundAxisWithinMinAndMax(axis, value, axisSize), GetPaddingAndBorderForAxis(axis, widthSize));
	}

	private void ConstrainMaxSizeForMode(YogaFlexDirection axis, float ownerAxisSize, float ownerWidth, /*ref */ AtomicReference<YogaMeasureMode> mode, /*ref */ AtomicReference<Float> size) {
		float maxSize = _style.MaxDimensions[Dimension[axis.ordinal()].ordinal()].Resolve(ownerAxisSize) + GetMarginForAxis(axis, ownerWidth);
		switch (mode.get()) {
		case Exactly:
		case AtMost:
			float sizeIn = size.get();
			size.set((Float.isNaN(maxSize) || sizeIn < maxSize) ? sizeIn : maxSize);
			break;
		case Undefined:
			if (!Float.isNaN(maxSize)) {
				mode.set(YogaMeasureMode.AtMost);
				size.set(maxSize);
			}
			break;
		}
	}

	private void ComputeFlexBasisForChild(YogaNode child, float width, YogaMeasureMode widthMode, float height, float ownerWidth, float ownerHeight, YogaMeasureMode heightMode, YogaDirection direction, YogaConfig config) {
		YogaFlexDirection mainAxis = _style.FlexDirection.ResolveFlexDirection(direction);
		boolean isMainAxisRow = mainAxis.IsRow();
		float mainAxisSize = isMainAxisRow ? width : height;
		float mainAxisOwnerSize = isMainAxisRow ? ownerWidth : ownerHeight;

		float childWidth = Float.NaN;
		float childHeight = Float.NaN;
		YogaMeasureMode childWidthMeasureMode;
		YogaMeasureMode childHeightMeasureMode;

		float resolvedFlexBasis = child.ResolveFlexBasis().Resolve(mainAxisOwnerSize);
		boolean isRowStyleDimDefined = child.IsStyleDimensionDefined(YogaFlexDirection.Row, ownerWidth);
		boolean isColumnStyleDimDefined = child.IsStyleDimensionDefined(YogaFlexDirection.Column, ownerHeight);

		if (!Float.isNaN(resolvedFlexBasis) && !Float.isNaN(mainAxisSize)) {
			if (Float.isNaN(child._layout.ComputedFlexBasis) || (child._config.IsExperimentalFeatureEnabled(YogaExperimentalFeature.WebFlexBasis) && child._layout.ComputedFlexBasisGeneration != gCurrentGenerationCount)) {
				float paddingAndBorder = child.GetPaddingAndBorderForAxis(mainAxis, ownerWidth);
				child._layout.ComputedFlexBasis = YogaMath.Max(resolvedFlexBasis, paddingAndBorder);
			}
		} else if (isMainAxisRow && isRowStyleDimDefined) {
			// The width is definite, so use that as the flex basis.
			child._layout.ComputedFlexBasis = (YogaMath.Max(child.GetResolvedDimension(YogaDimension.Width).Resolve(ownerWidth), child.GetPaddingAndBorderForAxis(YogaFlexDirection.Row, ownerWidth)));
		} else if (!isMainAxisRow && isColumnStyleDimDefined) {
			// The height is definite, so use that as the flex basis.
			child._layout.ComputedFlexBasis = (YogaMath.Max(child.GetResolvedDimension(YogaDimension.Height).Resolve(ownerHeight), child.GetPaddingAndBorderForAxis(YogaFlexDirection.Column, ownerWidth)));
		} else {
			// Compute the flex basis and hypothetical main size (i.e. the clamped
			// flex basis).
			childWidth = Float.NaN;
			childHeight = Float.NaN;
			childWidthMeasureMode = YogaMeasureMode.Undefined;
			childHeightMeasureMode = YogaMeasureMode.Undefined;

			float marginRow = child.GetMarginForAxis(YogaFlexDirection.Row, ownerWidth);
			float marginColumn = child.GetMarginForAxis(YogaFlexDirection.Column, ownerWidth);

			if (isRowStyleDimDefined) {
				childWidth = (child.GetResolvedDimension(YogaDimension.Width).Resolve(ownerWidth) + marginRow);
				childWidthMeasureMode = YogaMeasureMode.Exactly;
			}
			if (isColumnStyleDimDefined) {
				childHeight = (child.GetResolvedDimension(YogaDimension.Height).Resolve(ownerHeight) + marginColumn);
				childHeightMeasureMode = YogaMeasureMode.Exactly;
			}

			// The W3C spec doesn't say anything about the 'overflow' property,
			// but all major browsers appear to implement the following logic.
			if ((!isMainAxisRow && _style.Overflow == YogaOverflow.Scroll) || _style.Overflow != YogaOverflow.Scroll) {
				if (Float.isNaN(childWidth) && !Float.isNaN(width)) {
					childWidth = width;
					childWidthMeasureMode = YogaMeasureMode.AtMost;
				}
			}

			if ((isMainAxisRow && _style.Overflow == YogaOverflow.Scroll) || _style.Overflow != YogaOverflow.Scroll) {
				if (Float.isNaN(childHeight) && !Float.isNaN(height)) {
					childHeight = height;
					childHeightMeasureMode = YogaMeasureMode.AtMost;
				}
			}

			if (!Float.isNaN(child._style.AspectRatio)) {
				if (!isMainAxisRow && childWidthMeasureMode == YogaMeasureMode.Exactly) {
					childHeight = marginColumn + (childWidth - marginRow) / child._style.AspectRatio;
					childHeightMeasureMode = YogaMeasureMode.Exactly;
				} else if (isMainAxisRow && childHeightMeasureMode == YogaMeasureMode.Exactly) {
					childWidth = marginRow + (childHeight - marginColumn) * child._style.AspectRatio;
					childWidthMeasureMode = YogaMeasureMode.Exactly;
				}
			}

			// If child has no defined size in the cross axis and is set to stretch,
			// set the cross
			// axis to be measured exactly with the available inner width

			boolean hasExactWidth = !Float.isNaN(width) && widthMode == YogaMeasureMode.Exactly;
			boolean childWidthStretch = GetAlign(child) == YogaAlign.Stretch && childWidthMeasureMode != YogaMeasureMode.Exactly;
			if (!isMainAxisRow && !isRowStyleDimDefined && hasExactWidth && childWidthStretch) {
				childWidth = width;
				childWidthMeasureMode = YogaMeasureMode.Exactly;
				if (!Float.isNaN(child._style.AspectRatio)) {
					childHeight = (childWidth - marginRow) / child._style.AspectRatio;
					childHeightMeasureMode = YogaMeasureMode.Exactly;
				}
			}

			boolean hasExactHeight = !Float.isNaN(height) && heightMode == YogaMeasureMode.Exactly;
			boolean childHeightStretch = GetAlign(child) == YogaAlign.Stretch && childHeightMeasureMode != YogaMeasureMode.Exactly;
			if (isMainAxisRow && !isColumnStyleDimDefined && hasExactHeight && childHeightStretch) {
				childHeight = height;
				childHeightMeasureMode = YogaMeasureMode.Exactly;

				if (!Float.isNaN(child._style.AspectRatio)) {
					childWidth = (childHeight - marginColumn) * child._style.AspectRatio;
					childWidthMeasureMode = YogaMeasureMode.Exactly;
				}
			}

			AtomicReference<YogaMeasureMode> childWidthMeasureModeRef = new AtomicReference<>(childWidthMeasureMode);
			AtomicReference<YogaMeasureMode> childHeightMeasureModeRef = new AtomicReference<>(childHeightMeasureMode);
			AtomicReference<Float> childWidthRef = new AtomicReference<>(childWidth);
			AtomicReference<Float> childHeightRef = new AtomicReference<>(childHeight);
			child.ConstrainMaxSizeForMode(YogaFlexDirection.Row, ownerWidth, ownerWidth, /*ref */ childWidthMeasureModeRef, /*ref */ childWidthRef);
			child.ConstrainMaxSizeForMode(YogaFlexDirection.Column, ownerHeight, ownerWidth, /*ref */ childHeightMeasureModeRef, /*ref */ childHeightRef);
			childWidthMeasureMode = childWidthMeasureModeRef.get();
			childHeightMeasureMode = childHeightMeasureModeRef.get();
			childWidth = childWidthRef.get();
			childHeight = childHeightRef.get();

			// Measure the child
			child.LayoutNode(childWidth, childHeight, direction, childWidthMeasureMode, childHeightMeasureMode, ownerWidth, ownerHeight, false, "measure", config);

			child._layout.ComputedFlexBasis = (YogaMath.Max(child._layout.MeasuredDimensions[Dimension[mainAxis.ordinal()].ordinal()], child.GetPaddingAndBorderForAxis(mainAxis, ownerWidth)));
		}

		child._layout.ComputedFlexBasisGeneration = gCurrentGenerationCount;
	}

	private static float CalculateBaseline(YogaNode node) {
		float baseline;
		if (node._baseline != null) {
			return node._baseline.apply(node, node._layout.MeasuredDimensions[YogaDimension.Width.ordinal()], node._layout.MeasuredDimensions[YogaDimension.Height.ordinal()]);
		}

		YogaNode baselineChild = null;
		int childCount = node.GetChildCount();
		for (int i = 0; i < childCount; i++) {
			YogaNode child = node.GetChild(i);
			if (child._lineIndex > 0)
				break;

			if (child._style.PositionType == YogaPositionType.Absolute)
				continue;

			if (node.GetAlign(child) == YogaAlign.Baseline || child.GetIsReferenceBaseline()) {
				baselineChild = child;
				break;
			}

			if (baselineChild == null)
				baselineChild = child;
		}

		if (baselineChild == null)
			return node._layout.MeasuredDimensions[YogaDimension.Height.ordinal()];

		baseline = CalculateBaseline(baselineChild);
		return baseline + baselineChild._layout.Position[YogaEdge.Top.ordinal()];
	}

	// This function assumes that all the children of node have their
	// computedFlexBasis properly computed(To do this use
	// YGNodeComputeFlexBasisForChildren function).
	// This function calculates YGCollectFlexItemsRowMeasurement
	private YogaCollectFlexItemsRowValues CalculateCollectFlexItemsRowValues(YogaDirection ownerDirection, float mainAxisOwnerSize, float availableInnerWidth, float availableInnerMainDim, int startOfLineIndex, int lineCount) {
		YogaCollectFlexItemsRowValues flexAlgoRowMeasurement = new YogaCollectFlexItemsRowValues();
		//flexAlgoRowMeasurement.RelativeChildren.Capacity = GetChildCount();

		float sizeConsumedOnCurrentLineIncludingMinConstraint = 0F;
		YogaFlexDirection mainAxis = _style.FlexDirection.ResolveFlexDirection(ResolveDirection(ownerDirection));
		boolean isNodeFlexWrap = _style.FlexWrap != YogaWrap.NoWrap;

		// Add items to the current line until it's full or we run out of items.
		int endOfLineIndex = startOfLineIndex;
		for (; endOfLineIndex < GetChildCount(); endOfLineIndex++) {
			YogaNode child = GetChild(endOfLineIndex);
			if (child._style.Display == YogaDisplay.None || child._style.PositionType == YogaPositionType.Absolute)
				continue;

			child._lineIndex = lineCount;
			float childMarginMainAxis = child.GetMarginForAxis(mainAxis, availableInnerWidth);
			float flexBasisWithMinAndMaxConstraints = child.BoundAxisWithinMinAndMax(mainAxis, child._layout.ComputedFlexBasis, mainAxisOwnerSize);

			// If this is a multi-line flow and this item pushes us over the
			// available size, we've
			// hit the end of the current line. Break out of the loop and lay out
			// the current line.
			if (sizeConsumedOnCurrentLineIncludingMinConstraint + flexBasisWithMinAndMaxConstraints + childMarginMainAxis > availableInnerMainDim && isNodeFlexWrap && flexAlgoRowMeasurement.ItemsOnLine > 0) {
				break;
			}

			sizeConsumedOnCurrentLineIncludingMinConstraint += flexBasisWithMinAndMaxConstraints + childMarginMainAxis;
			flexAlgoRowMeasurement.SizeConsumedOnCurrentLine += flexBasisWithMinAndMaxConstraints + childMarginMainAxis;
			flexAlgoRowMeasurement.ItemsOnLine++;

			if (child.IsNodeFlexible()) {
				flexAlgoRowMeasurement.TotalFlexGrowFactors += child.ResolveFlexGrow();

				// Unlike the grow factor, the shrink factor is scaled relative to the
				// child dimension.
				flexAlgoRowMeasurement.TotalFlexShrinkScaledFactors += -child.ResolveFlexShrink() * (child._layout.ComputedFlexBasis);
			}

			flexAlgoRowMeasurement.RelativeChildren.add(child);
		}

		// The total flex factor needs to be floored to 1.
		if (flexAlgoRowMeasurement.TotalFlexGrowFactors > 0 && flexAlgoRowMeasurement.TotalFlexGrowFactors < 1)
			flexAlgoRowMeasurement.TotalFlexGrowFactors = 1;

		// The total flex shrink factor needs to be floored to 1.
		if (flexAlgoRowMeasurement.TotalFlexShrinkScaledFactors > 0 && flexAlgoRowMeasurement.TotalFlexShrinkScaledFactors < 1)
			flexAlgoRowMeasurement.TotalFlexShrinkScaledFactors = 1;

		flexAlgoRowMeasurement.EndOfLineIndex = endOfLineIndex;
		return flexAlgoRowMeasurement;
	}

	// Do two passes over the flex items to figure out how to distribute the
	// remaining space.
	// The first pass finds the items whose min/max constraints trigger,
	// freezes them at those
	// sizes, and excludes those sizes from the remaining space. The second
	// pass sets the size
	// of each flexible item. It distributes the remaining space amongst the
	// items whose min/max
	// constraints didn't trigger in pass 1. For the other items, it sets
	// their sizes by forcing
	// their min/max constraints to trigger again.
	//
	// This two pass approach for resolving min/max constraints deviates from
	// the spec. The
	// spec (https://www.w3.org/TR/YG-flexbox-1/#resolve-flexible-lengths)
	// describes a process
	// that needs to be repeated a variable number of times. The algorithm
	// implemented here
	// won't handle all cases but it was simpler to implement and it mitigates
	// performance
	// concerns because we know exactly how many passes it'll do.
	//
	// At the end of this function the child nodes would have the proper size
	// assigned to them.
	//
	private YogaCollectFlexItemsRowValues ResolveFlexibleLength(/*ref */ YogaCollectFlexItemsRowValues collectedFlexItemsValues, YogaFlexDirection mainAxis, YogaFlexDirection crossAxis, float mainAxisOwnerSize, float availableInnerMainDim, float availableInnerCrossDim, float availableInnerWidth, float availableInnerHeight, boolean flexBasisOverflows, YogaMeasureMode measureModeCrossDim, boolean performLayout, YogaConfig config) {
		float originalFreeSpace = collectedFlexItemsValues.RemainingFreeSpace;
		// First pass: detect the flex items whose min/max constraints trigger
		collectedFlexItemsValues = DistributeFreeSpaceFirstPass(/*ref */ collectedFlexItemsValues, mainAxis, mainAxisOwnerSize, availableInnerMainDim, availableInnerWidth);

		// Second pass: resolve the sizes of the flexible items
		float distributedFreeSpace = DistributeFreeSpaceSecondPass(/*ref */ collectedFlexItemsValues, mainAxis, crossAxis, mainAxisOwnerSize, availableInnerMainDim, availableInnerCrossDim, availableInnerWidth, availableInnerHeight, flexBasisOverflows, measureModeCrossDim, performLayout, config);

		collectedFlexItemsValues.RemainingFreeSpace = originalFreeSpace - distributedFreeSpace;
		return collectedFlexItemsValues;
	}

	// It distributes the free space to the flexible items.For those flexible items
	// whose min and max constraints are triggered, those flex item's clamped size
	// is removed from the remaingfreespace.
	private static YogaCollectFlexItemsRowValues DistributeFreeSpaceFirstPass(/*ref */ YogaCollectFlexItemsRowValues collectedFlexItemsValues, YogaFlexDirection mainAxis, float mainAxisOwnerSize, float availableInnerMainDim, float availableInnerWidth) {
		float flexShrinkScaledFactor = 0F;
		float flexGrowFactor = 0F;
		float baseMainSize = 0F;
		float boundMainSize = 0F;
		float deltaFreeSpace = 0F;

		for (YogaNode currentRelativeChild : collectedFlexItemsValues.RelativeChildren) {
			float childFlexBasis = currentRelativeChild.BoundAxisWithinMinAndMax(mainAxis, currentRelativeChild._layout.ComputedFlexBasis, mainAxisOwnerSize);

			if (collectedFlexItemsValues.RemainingFreeSpace < 0) {
				flexShrinkScaledFactor = -currentRelativeChild.ResolveFlexShrink() * childFlexBasis;

				// Is this child able to shrink?
				if (!Float.isNaN(flexShrinkScaledFactor) && flexShrinkScaledFactor != 0) {
					baseMainSize = childFlexBasis + collectedFlexItemsValues.RemainingFreeSpace / collectedFlexItemsValues.TotalFlexShrinkScaledFactors * flexShrinkScaledFactor;
					boundMainSize = currentRelativeChild.BoundAxis(mainAxis, baseMainSize, availableInnerMainDim, availableInnerWidth);

					if (!Float.isNaN(baseMainSize) && !Float.isNaN(boundMainSize) && baseMainSize != boundMainSize) {
						// By excluding this item's size and flex factor from remaining,
						// this item's
						// min/max constraints should also trigger in the second pass
						// resulting in the
						// item's size calculation being identical in the first and second
						// passes.
						deltaFreeSpace += boundMainSize - childFlexBasis;
						collectedFlexItemsValues.TotalFlexShrinkScaledFactors -= flexShrinkScaledFactor;
					}
				}
			} else if (!Float.isNaN(collectedFlexItemsValues.RemainingFreeSpace) && collectedFlexItemsValues.RemainingFreeSpace > 0) {
				flexGrowFactor = currentRelativeChild.ResolveFlexGrow();

				// Is this child able to grow?
				if (flexGrowFactor != 0) {
					baseMainSize = childFlexBasis + collectedFlexItemsValues.RemainingFreeSpace / collectedFlexItemsValues.TotalFlexGrowFactors * flexGrowFactor;
					boundMainSize = currentRelativeChild.BoundAxis(mainAxis, baseMainSize, availableInnerMainDim, availableInnerWidth);

					if (!Float.isNaN(baseMainSize) && !Float.isNaN(boundMainSize) && baseMainSize != boundMainSize) {
						// By excluding this item's size and flex factor from remaining,
						// this item's
						// min/max constraints should also trigger in the second pass
						// resulting in the
						// item's size calculation being identical in the first and second
						// passes.
						deltaFreeSpace += boundMainSize - childFlexBasis;
						collectedFlexItemsValues.TotalFlexGrowFactors -= flexGrowFactor;
					}
				}
			}
		}

		collectedFlexItemsValues.RemainingFreeSpace -= deltaFreeSpace;
		return collectedFlexItemsValues;
	}

	// It distributes the free space to the flexible items and ensures that the size
	// of the flex items abide the min and max constraints. At the end of this
	// function the child nodes would have proper size. Prior using this function
	// please ensure that YGDistributeFreeSpaceFirstPass is called.
	private float DistributeFreeSpaceSecondPass(/*ref */ YogaCollectFlexItemsRowValues collectedFlexItemsValues, YogaFlexDirection mainAxis, YogaFlexDirection crossAxis, float mainAxisOwnerSize, float availableInnerMainDim, float availableInnerCrossDim, float availableInnerWidth, float availableInnerHeight, boolean flexBasisOverflows, YogaMeasureMode measureModeCrossDim, boolean performLayout, YogaConfig config) {
		float childFlexBasis = 0F;
		float flexShrinkScaledFactor = 0F;
		float flexGrowFactor = 0F;
		float deltaFreeSpace = 0F;
		boolean isMainAxisRow = mainAxis.IsRow();
		boolean isNodeFlexWrap = _style.FlexWrap != YogaWrap.NoWrap;

		for (YogaNode currentRelativeChild : collectedFlexItemsValues.RelativeChildren) {
			childFlexBasis = currentRelativeChild.BoundAxisWithinMinAndMax(mainAxis, currentRelativeChild._layout.ComputedFlexBasis, mainAxisOwnerSize);

			float updatedMainSize = childFlexBasis;
			if (!Float.isNaN(collectedFlexItemsValues.RemainingFreeSpace) && collectedFlexItemsValues.RemainingFreeSpace < 0) {
				flexShrinkScaledFactor = -currentRelativeChild.ResolveFlexShrink() * childFlexBasis;
				// Is this child able to shrink?
				if (flexShrinkScaledFactor != 0) {
					float childSize = 0F;
					if (!Float.isNaN(collectedFlexItemsValues.TotalFlexShrinkScaledFactors) && collectedFlexItemsValues.TotalFlexShrinkScaledFactors == 0) {
						childSize = childFlexBasis + flexShrinkScaledFactor;
					} else {
						childSize = childFlexBasis + (collectedFlexItemsValues.RemainingFreeSpace / collectedFlexItemsValues.TotalFlexShrinkScaledFactors) * flexShrinkScaledFactor;
					}

					updatedMainSize = currentRelativeChild.BoundAxis(mainAxis, childSize, availableInnerMainDim, availableInnerWidth);
				}
			} else if (!Float.isNaN(collectedFlexItemsValues.RemainingFreeSpace) && collectedFlexItemsValues.RemainingFreeSpace > 0) {
				flexGrowFactor = currentRelativeChild.ResolveFlexGrow();

				// Is this child able to grow?
				if (flexGrowFactor != 0) {
					updatedMainSize = currentRelativeChild.BoundAxis(mainAxis, childFlexBasis + collectedFlexItemsValues.RemainingFreeSpace / collectedFlexItemsValues.TotalFlexGrowFactors * flexGrowFactor, availableInnerMainDim, availableInnerWidth);
				}
			}

			deltaFreeSpace += updatedMainSize - childFlexBasis;

			float marginMain = currentRelativeChild.GetMarginForAxis(mainAxis, availableInnerWidth);
			float marginCross = currentRelativeChild.GetMarginForAxis(crossAxis, availableInnerWidth);

			float childCrossSize = Float.NaN;
			float childMainSize = updatedMainSize + marginMain;
			YogaMeasureMode childCrossMeasureMode;
			YogaMeasureMode childMainMeasureMode = YogaMeasureMode.Exactly;

			if (!Float.isNaN(currentRelativeChild._style.AspectRatio)) {
				childCrossSize = isMainAxisRow ? (childMainSize - marginMain) / currentRelativeChild._style.AspectRatio : (childMainSize - marginMain) * currentRelativeChild._style.AspectRatio;

				childCrossMeasureMode = YogaMeasureMode.Exactly;
				childCrossSize += marginCross;
			} else if (!Float.isNaN(availableInnerCrossDim) && !currentRelativeChild.IsStyleDimensionDefined(crossAxis, availableInnerCrossDim) && measureModeCrossDim == YogaMeasureMode.Exactly && (!isNodeFlexWrap || !flexBasisOverflows) && GetAlign(currentRelativeChild) == YogaAlign.Stretch && currentRelativeChild.GetMarginLeadingValue(crossAxis).Unit != YogaUnit.Auto && currentRelativeChild.GetMarginTrailingValue(crossAxis).Unit != YogaUnit.Auto) {
				childCrossSize = availableInnerCrossDim;
				childCrossMeasureMode = YogaMeasureMode.Exactly;
			} else if (!currentRelativeChild.IsStyleDimensionDefined(crossAxis, availableInnerCrossDim)) {
				childCrossSize = availableInnerCrossDim;
				childCrossMeasureMode = Float.isNaN(childCrossSize) ? YogaMeasureMode.Undefined : YogaMeasureMode.AtMost;
			} else {
				childCrossSize = (currentRelativeChild.GetResolvedDimension(Dimension[crossAxis.ordinal()]).Resolve(availableInnerCrossDim)) + marginCross;
				boolean isLoosePercentageMeasurement = currentRelativeChild.GetResolvedDimension(Dimension[crossAxis.ordinal()]).Unit == YogaUnit.Percent && measureModeCrossDim != YogaMeasureMode.Exactly;

				childCrossMeasureMode = Float.isNaN(childCrossSize) || isLoosePercentageMeasurement ? YogaMeasureMode.Undefined : YogaMeasureMode.Exactly;
			}

			AtomicReference<YogaMeasureMode> childMainMeasureModeRef = new AtomicReference<>(childMainMeasureMode);
			AtomicReference<YogaMeasureMode> childCrossMeasureModeRef = new AtomicReference<>(childCrossMeasureMode);
			AtomicReference<Float> childMainSizeRef = new AtomicReference<>(childMainSize);
			AtomicReference<Float> childCrossSizeRef = new AtomicReference<>(childCrossSize);
			currentRelativeChild.ConstrainMaxSizeForMode(mainAxis, availableInnerMainDim, availableInnerWidth, /*ref */ childMainMeasureModeRef, /*ref */ childMainSizeRef);
			currentRelativeChild.ConstrainMaxSizeForMode(crossAxis, availableInnerCrossDim, availableInnerWidth, /*ref */ childCrossMeasureModeRef, /*ref */ childCrossSizeRef);
			childMainMeasureMode = childMainMeasureModeRef.get();
			childCrossMeasureMode = childCrossMeasureModeRef.get();
			childMainSize = childMainSizeRef.get();
			childCrossSize = childCrossSizeRef.get();

			boolean requiresStretchLayout = !currentRelativeChild.IsStyleDimensionDefined(crossAxis, availableInnerCrossDim) && GetAlign(currentRelativeChild) == YogaAlign.Stretch && currentRelativeChild.GetMarginLeadingValue(crossAxis).Unit != YogaUnit.Auto && currentRelativeChild.GetMarginTrailingValue(crossAxis).Unit != YogaUnit.Auto;

			float childWidth = isMainAxisRow ? childMainSize : childCrossSize;
			float childHeight = !isMainAxisRow ? childMainSize : childCrossSize;

			YogaMeasureMode childWidthMeasureMode = isMainAxisRow ? childMainMeasureMode : childCrossMeasureMode;
			YogaMeasureMode childHeightMeasureMode = !isMainAxisRow ? childMainMeasureMode : childCrossMeasureMode;

			// Recursively call the layout algorithm for this child with the updated
			// main size.
			currentRelativeChild.LayoutNode(childWidth, childHeight, _layout.Direction, childWidthMeasureMode, childHeightMeasureMode, availableInnerWidth, availableInnerHeight, performLayout && !requiresStretchLayout, "flex", config);

			_layout.HadOverflow = _layout.HadOverflow || currentRelativeChild._layout.HadOverflow;
		}

		return deltaFreeSpace;
	}

	@SuppressWarnings("unused")
	private static final class JustifyMainAxisReturn {
		YogaCollectFlexItemsRowValues collectedFlexItemsValues;
		int startOfLineIndex;
		YogaFlexDirection mainAxis;
		YogaFlexDirection crossAxis;
		YogaMeasureMode measureModeMainDim;
		YogaMeasureMode measureModeCrossDim;
		float mainAxisOwnerSize;
		float ownerWidth;
		float availableInnerMainDim;
		float availableInnerCrossDim;
		float availableInnerWidth;
		boolean performLayout;
	}

	private JustifyMainAxisReturn JustifyMainAxis(/*ref */ YogaCollectFlexItemsRowValues collectedFlexItemsValues, /*ref */ int startOfLineIndex, /*ref */ YogaFlexDirection mainAxis, /*ref */ YogaFlexDirection crossAxis, /*ref */ YogaMeasureMode measureModeMainDim, /*ref */ YogaMeasureMode measureModeCrossDim, /*ref */ float mainAxisOwnerSize, /*ref */ float ownerWidth, /*ref */ float availableInnerMainDim, /*ref */ float availableInnerCrossDim, /*ref */ float availableInnerWidth, /*ref */ boolean performLayout) {
		YogaStyle style = _style;
		float leadingPaddingAndBorderMain = GetLeadingPaddingAndBorder(mainAxis, ownerWidth);
		float trailingPaddingAndBorderMain = GetTrailingPaddingAndBorder(mainAxis, ownerWidth);

		// If we are using "at most" rules in the main axis. Calculate the remaining
		// space when constraint by the min size defined for the main axis.
		if (measureModeMainDim == YogaMeasureMode.AtMost && collectedFlexItemsValues.RemainingFreeSpace > 0) {
			if (style.MinDimensions[Dimension[mainAxis.ordinal()].ordinal()].Unit != YogaUnit.Undefined && !Float.isNaN(style.MinDimensions[Dimension[mainAxis.ordinal()].ordinal()].Resolve(mainAxisOwnerSize))) {
				// This condition makes sure that if the size of main dimension(after
				// considering child nodes main dim, leading and trailing padding etc)
				// falls below min dimension, then the remainingFreeSpace is reassigned
				// considering the min dimension

				// `minAvailableMainDim` denotes minimum available space in which child
				// can be laid out, it will exclude space consumed by padding and border.

				float minAvailableMainDim = style.MinDimensions[Dimension[mainAxis.ordinal()].ordinal()].Resolve(mainAxisOwnerSize) - leadingPaddingAndBorderMain - trailingPaddingAndBorderMain;

				float occupiedSpaceByChildNodes = availableInnerMainDim - collectedFlexItemsValues.RemainingFreeSpace;
				collectedFlexItemsValues.RemainingFreeSpace = YogaMath.Max(0, minAvailableMainDim - occupiedSpaceByChildNodes);
			} else {
				collectedFlexItemsValues.RemainingFreeSpace = 0;
			}
		}

		int numberOfAutoMarginsOnCurrentLine = 0;
		for (int i = startOfLineIndex; i < collectedFlexItemsValues.EndOfLineIndex; i++) {
			YogaNode child = GetChild(i);
			if (child._style.PositionType == YogaPositionType.Relative) {
				if (child.GetMarginLeadingValue(mainAxis).Unit == YogaUnit.Auto)
					numberOfAutoMarginsOnCurrentLine++;

				if (child.GetMarginTrailingValue(mainAxis).Unit == YogaUnit.Auto)
					numberOfAutoMarginsOnCurrentLine++;
			}
		}

		// In order to position the elements in the main axis, we have two
		// controls. The space between the beginning and the first element
		// and the space between each two elements.
		float leadingMainDim = 0F;
		float betweenMainDim = 0F;
		YogaJustify justifyContent = _style.JustifyContent;

		if (numberOfAutoMarginsOnCurrentLine == 0) {
			switch (justifyContent) {
			case Center:
				leadingMainDim = collectedFlexItemsValues.RemainingFreeSpace / 2;
				break;
			case FlexEnd:
				leadingMainDim = collectedFlexItemsValues.RemainingFreeSpace;
				break;
			case SpaceBetween:
				if (collectedFlexItemsValues.ItemsOnLine > 1) {
					betweenMainDim = YogaMath.Max(collectedFlexItemsValues.RemainingFreeSpace, 0) / (collectedFlexItemsValues.ItemsOnLine - 1);
				} else {
					betweenMainDim = 0;
				}
				break;
			case SpaceEvenly:
				// Space is distributed evenly across all elements
				betweenMainDim = collectedFlexItemsValues.RemainingFreeSpace / (collectedFlexItemsValues.ItemsOnLine + 1);
				leadingMainDim = betweenMainDim;
				break;
			case SpaceAround:
				// Space on the edges is half of the space between elements
				betweenMainDim = collectedFlexItemsValues.RemainingFreeSpace / collectedFlexItemsValues.ItemsOnLine;
				leadingMainDim = betweenMainDim / 2;
				break;
			case FlexStart:
				break;
			}
		}

		collectedFlexItemsValues.MainDimension = leadingPaddingAndBorderMain + leadingMainDim;
		collectedFlexItemsValues.CrossDimension = 0;

		float maxAscentForCurrentLine = 0F;
		float maxDescentForCurrentLine = 0F;
		boolean isNodeBaselineLayout = IsBaselineLayout();
		for (int i = startOfLineIndex; i < collectedFlexItemsValues.EndOfLineIndex; i++) {
			YogaNode child = GetChild(i);
			YogaStyle childStyle = child._style;
			YogaLayout childLayout = child._layout;
			if (childStyle.Display == YogaDisplay.None)
				continue;

			if (childStyle.PositionType == YogaPositionType.Absolute && child.IsLeadingPositionDefined(mainAxis)) {
				if (performLayout) {
					// In case the child is position absolute and has left/top being
					// defined, we override the position to whatever the user said
					// (and margin/border).
					child.SetLayoutPosition(child.GetLeadingPosition(mainAxis, availableInnerMainDim) + GetLeadingBorder(mainAxis) + child.GetLeadingMargin(mainAxis, availableInnerWidth), Position[mainAxis.ordinal()]);
				}
			} else {
				// Now that we placed the element, we need to update the variables.
				// We need to do that only for relative elements. Absolute elements
				// do not take part in that phase.
				if (childStyle.PositionType == YogaPositionType.Relative) {
					if (child.GetMarginLeadingValue(mainAxis).Unit == YogaUnit.Auto) {
						collectedFlexItemsValues.MainDimension += collectedFlexItemsValues.RemainingFreeSpace / numberOfAutoMarginsOnCurrentLine;
					}

					if (performLayout) {
						child.SetLayoutPosition(childLayout.Position[Position[mainAxis.ordinal()].ordinal()] + collectedFlexItemsValues.MainDimension, Position[mainAxis.ordinal()]);
					}

					if (child.GetMarginTrailingValue(mainAxis).Unit == YogaUnit.Auto) {
						collectedFlexItemsValues.MainDimension += collectedFlexItemsValues.RemainingFreeSpace / numberOfAutoMarginsOnCurrentLine;
					}

					boolean canSkipFlex = !performLayout && measureModeCrossDim == YogaMeasureMode.Exactly;
					if (canSkipFlex) {
						// If we skipped the flex step, then we can't rely on the
						// measuredDims because
						// they weren't computed. This means we can't call
						// YGNodeDimWithMargin.
						collectedFlexItemsValues.MainDimension += betweenMainDim + child.GetMarginForAxis(mainAxis, availableInnerWidth) + (childLayout.ComputedFlexBasis);
						collectedFlexItemsValues.CrossDimension = availableInnerCrossDim;
					} else {
						// The main dimension is the sum of all the elements dimension plus
						// the spacing.
						collectedFlexItemsValues.MainDimension += betweenMainDim + child.GetDimensionWithMargin(mainAxis, availableInnerWidth);

						if (isNodeBaselineLayout) {
							// If the child is baseline aligned then the cross dimension is
							// calculated by adding maxAscent and maxDescent from the baseline.

							float ascent = CalculateBaseline(child) + child.GetLeadingMargin(YogaFlexDirection.Column, availableInnerWidth);
							float descent = child._layout.MeasuredDimensions[YogaDimension.Height.ordinal()] + child.GetMarginForAxis(YogaFlexDirection.Column, availableInnerWidth) - ascent;

							maxAscentForCurrentLine = YogaMath.Max(maxAscentForCurrentLine, ascent);
							maxDescentForCurrentLine = YogaMath.Max(maxDescentForCurrentLine, descent);
						} else {
							collectedFlexItemsValues.CrossDimension = YogaMath.Max(collectedFlexItemsValues.CrossDimension, child.GetDimensionWithMargin(crossAxis, availableInnerWidth));

						}
					}
				} else if (performLayout) {
					child.SetLayoutPosition(childLayout.Position[Position[mainAxis.ordinal()].ordinal()] + GetLeadingBorder(mainAxis) + leadingMainDim, Position[mainAxis.ordinal()]);
				}
			}
		}

		collectedFlexItemsValues.MainDimension += trailingPaddingAndBorderMain;

		if (isNodeBaselineLayout)
			collectedFlexItemsValues.CrossDimension = maxAscentForCurrentLine + maxDescentForCurrentLine;

		JustifyMainAxisReturn ret = new JustifyMainAxisReturn();
		ret.collectedFlexItemsValues = collectedFlexItemsValues;
		ret.startOfLineIndex = startOfLineIndex;
		ret.mainAxis = mainAxis;
		ret.crossAxis = crossAxis;
		ret.measureModeMainDim = measureModeMainDim;
		ret.measureModeCrossDim = measureModeCrossDim;
		ret.mainAxisOwnerSize = mainAxisOwnerSize;
		ret.ownerWidth = ownerWidth;
		ret.availableInnerMainDim = availableInnerMainDim;
		ret.availableInnerCrossDim = availableInnerCrossDim;
		ret.availableInnerWidth = availableInnerWidth;
		ret.performLayout = performLayout;
		return ret;
	}

	private void AbsoluteLayoutChild(YogaNode child, float width, YogaMeasureMode widthMode, float height, YogaDirection direction, YogaConfig config) {
		YogaFlexDirection mainAxis = _style.FlexDirection.ResolveFlexDirection(direction);
		YogaFlexDirection crossAxis = mainAxis.FlexDirectionCross(direction);
		boolean isMainAxisRow = mainAxis.IsRow();

		float childWidth = Float.NaN;
		float childHeight = Float.NaN;
		YogaMeasureMode childWidthMeasureMode = YogaMeasureMode.Undefined;
		YogaMeasureMode childHeightMeasureMode = YogaMeasureMode.Undefined;

		float marginRow = child.GetMarginForAxis(YogaFlexDirection.Row, width);
		float marginColumn = child.GetMarginForAxis(YogaFlexDirection.Column, width);

		if (child.IsStyleDimensionDefined(YogaFlexDirection.Row, width)) {
			childWidth = (child.GetResolvedDimension(YogaDimension.Width).Resolve(width)) + marginRow;
		} else {
			// If the child doesn't have a specified width, compute the width based
			// on the left/right
		// offsets if they're defined.
            if (child.IsLeadingPositionDefined(YogaFlexDirection.Row) && child.IsTrailingPositionDefined(YogaFlexDirection.Row))
            {
                childWidth = _layout.MeasuredDimensions[YogaDimension.Width.ordinal()] -
                    (GetLeadingBorder(YogaFlexDirection.Row) +
                     GetTrailingBorder(YogaFlexDirection.Row)) -
                    (child.GetLeadingPosition(YogaFlexDirection.Row, width) +
                     child.GetTrailingPosition(YogaFlexDirection.Row, width));
                childWidth = child.BoundAxis(YogaFlexDirection.Row, childWidth, width, width);
            }
        }

        if (child.IsStyleDimensionDefined(YogaFlexDirection.Column, height))
        {
            childHeight = (child.GetResolvedDimension(YogaDimension.Height).Resolve(height)) + marginColumn;
        }
        else
        {
            // If the child doesn't have a specified height, compute the height
            // based on the top/bottom
		// offsets if they're defined.
            if (child.IsLeadingPositionDefined(YogaFlexDirection.Column) && child.IsTrailingPositionDefined(YogaFlexDirection.Column))
            {
                childHeight = _layout.MeasuredDimensions[YogaDimension.Height.ordinal()] -
                    (GetLeadingBorder(YogaFlexDirection.Column) +
                     GetTrailingBorder(YogaFlexDirection.Column)) -
                    (child.GetLeadingPosition(YogaFlexDirection.Column, height) +
                     child.GetTrailingPosition(YogaFlexDirection.Column, height));
                childHeight = child.BoundAxis(YogaFlexDirection.Column, childHeight, height, width);
            }
        }

        // Exactly one dimension needs to be defined for us to be able to do aspect ratio
		// calculation. One dimension being the anchor and the other being flexible.
		if (Float.isNaN(childWidth) ^ Float.isNaN(childHeight)) {
			if (!Float.isNaN(child._style.AspectRatio)) {
				if (Float.isNaN(childWidth))
					childWidth = marginRow + (childHeight - marginColumn) * child._style.AspectRatio;
				else if (Float.isNaN(childHeight))
					childHeight = marginColumn + (childWidth - marginRow) / child._style.AspectRatio;
			}
		}

		// If we're still missing one or the other dimension, measure the content.
		if (Float.isNaN(childWidth) || Float.isNaN(childHeight)) {
			childWidthMeasureMode = Float.isNaN(childWidth) ? YogaMeasureMode.Undefined : YogaMeasureMode.Exactly;
			childHeightMeasureMode = Float.isNaN(childHeight) ? YogaMeasureMode.Undefined : YogaMeasureMode.Exactly;

			// If the size of the owner is defined then try to constrain the absolute child to that size
			// as well. This allows text within the absolute child to wrap to the size of its owner.
			// This is the same behavior as many browsers implement.
			if (!isMainAxisRow && Float.isNaN(childWidth) && widthMode != YogaMeasureMode.Undefined && !Float.isNaN(width) && width > 0) {
				childWidth = width;
				childWidthMeasureMode = YogaMeasureMode.AtMost;
			}

			child.LayoutNode(childWidth, childHeight, direction, childWidthMeasureMode, childHeightMeasureMode, childWidth, childHeight, false, "abs-measure", config);

			childWidth = child._layout.MeasuredDimensions[YogaDimension.Width.ordinal()] + child.GetMarginForAxis(YogaFlexDirection.Row, width);
			childHeight = child._layout.MeasuredDimensions[YogaDimension.Height.ordinal()] + child.GetMarginForAxis(YogaFlexDirection.Column, width);
		}

		child.LayoutNode(childWidth, childHeight, direction, YogaMeasureMode.Exactly, YogaMeasureMode.Exactly, childWidth, childHeight, true, "abs-layout", config);

		if (child.IsTrailingPositionDefined(mainAxis) && !child.IsLeadingPositionDefined(mainAxis)) {
			child.SetLayoutPosition(_layout.MeasuredDimensions[Dimension[mainAxis.ordinal()].ordinal()] - child._layout.MeasuredDimensions[Dimension[mainAxis.ordinal()].ordinal()] - GetTrailingBorder(mainAxis) - child.GetTrailingMargin(mainAxis, width) - child.GetTrailingPosition(mainAxis, isMainAxisRow ? width : height), Leading[mainAxis.ordinal()]);
		} else if (!child.IsLeadingPositionDefined(mainAxis) && _style.JustifyContent == YogaJustify.Center) {
			child.SetLayoutPosition((_layout.MeasuredDimensions[Dimension[mainAxis.ordinal()].ordinal()] - child._layout.MeasuredDimensions[Dimension[mainAxis.ordinal()].ordinal()]) / 2.0f, Leading[mainAxis.ordinal()]);
		} else if (!child.IsLeadingPositionDefined(mainAxis) && _style.JustifyContent == YogaJustify.FlexEnd) {
			child.SetLayoutPosition((_layout.MeasuredDimensions[Dimension[mainAxis.ordinal()].ordinal()] - child._layout.MeasuredDimensions[Dimension[mainAxis.ordinal()].ordinal()]), Leading[mainAxis.ordinal()]);
		}

		if (child.IsTrailingPositionDefined(crossAxis) && !child.IsLeadingPositionDefined(crossAxis)) {
			child.SetLayoutPosition(_layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()] - child._layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()] - GetTrailingBorder(crossAxis) - child.GetTrailingMargin(crossAxis, width) - child.GetTrailingPosition(crossAxis, isMainAxisRow ? height : width), Leading[crossAxis.ordinal()]);

		} else if (!child.IsLeadingPositionDefined(crossAxis) && GetAlign(child) == YogaAlign.Center) {
			child.SetLayoutPosition((_layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()] - child._layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()]) / 2.0f, Leading[crossAxis.ordinal()]);
		} else if (!child.IsLeadingPositionDefined(crossAxis) && ((GetAlign(child) == YogaAlign.FlexEnd) ^ (_style.FlexWrap == YogaWrap.WrapReverse))) {
			child.SetLayoutPosition((_layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()] - child._layout.MeasuredDimensions[Dimension[crossAxis.ordinal()].ordinal()]), Leading[crossAxis.ordinal()]);
		}
	}

	private float GetDimensionWithMargin(YogaFlexDirection axis, float widthSize) {
		return _layout.MeasuredDimensions[Dimension[axis.ordinal()].ordinal()] + GetLeadingMargin(axis, widthSize) + GetTrailingMargin(axis, widthSize);
	}

	private boolean IsStyleDimensionDefined(YogaFlexDirection axis, float ownerSize) {
		boolean isUndefined = Float.isNaN(GetResolvedDimension(Dimension[axis.ordinal()]).Value);
		return ((GetResolvedDimension(Dimension[axis.ordinal()]).Unit != YogaUnit.Auto) && (GetResolvedDimension(Dimension[axis.ordinal()]).Unit != YogaUnit.Undefined) && ((GetResolvedDimension(Dimension[axis.ordinal()]).Unit != YogaUnit.Point) || isUndefined || (GetResolvedDimension(Dimension[axis.ordinal()]).Value >= 0.0f)) && ((GetResolvedDimension(Dimension[axis.ordinal()]).Unit != YogaUnit.Percent) || isUndefined || ((GetResolvedDimension(Dimension[axis.ordinal()]).Value >= 0.0f) && !Float.isNaN(ownerSize))));
	}

	private boolean IsLayoutDimensionDefined(YogaFlexDirection axis) {
		float value = _layout.MeasuredDimensions[Dimension[axis.ordinal()].ordinal()];
		return !Float.isNaN(value) && value >= 0.0f;
	}

	private boolean IsBaselineLayout() {
		if (_style.FlexDirection.IsColumn())
			return false;

		if (_style.AlignItems == YogaAlign.Baseline)
			return true;

		int childCount = GetChildCount();
		for (int i = 0; i < childCount; i++) {
			YogaNode child = GetChild(i);
			if (child._style.PositionType == YogaPositionType.Relative && child._style.AlignSelf == YogaAlign.Baseline) {
				return true;
			}
		}

		return false;
	}

	private float GetPaddingAndBorderForAxis(YogaFlexDirection axis, float widthSize) {
		return GetLeadingPaddingAndBorder(axis, widthSize) + GetTrailingPaddingAndBorder(axis, widthSize);
	}

	private YogaAlign GetAlign(YogaNode child) {
		YogaAlign align = child._style.AlignSelf == YogaAlign.Auto ? _style.AlignItems : child._style.AlignSelf;

		if (align == YogaAlign.Baseline && _style.FlexDirection.IsColumn())
			return YogaAlign.FlexStart;

		return align;
	}

	private void RoundToPixelGrid(float pointScaleFactor, float absoluteLeft, float absoluteTop) {
		if (pointScaleFactor == 0.0f)
			return;

		float nodeLeft = _layout.Position[YogaEdge.Left.ordinal()];
		float nodeTop = _layout.Position[YogaEdge.Top.ordinal()];

		float nodeWidth = _layout.Dimensions[YogaDimension.Width.ordinal()];
		float nodeHeight = _layout.Dimensions[YogaDimension.Height.ordinal()];

		float absoluteNodeLeft = absoluteLeft + nodeLeft;
		float absoluteNodeTop = absoluteTop + nodeTop;

		float absoluteNodeRight = absoluteNodeLeft + nodeWidth;
		float absoluteNodeBottom = absoluteNodeTop + nodeHeight;

		// If a this has a custom measure function we never want to round down its size as this could
		// lead to unwanted text truncation.
		boolean textRounding = _nodeType == YogaNodeType.Text;

		SetLayoutPosition(YogaMath.RoundValueToPixelGrid(nodeLeft, pointScaleFactor, false, textRounding), YogaEdge.Left);
		SetLayoutPosition(YogaMath.RoundValueToPixelGrid(nodeTop, pointScaleFactor, false, textRounding), YogaEdge.Top);

		// We multiply dimension by scale factor and if the result is close to the whole number, we don't
		// have any fraction
		// To verify if the result is close to whole number we want to check both floor and ceil numbers
		boolean hasFractionalWidth = !YogaMath.FloatsEqual(((nodeWidth * pointScaleFactor) % 1.0F), 0) && !YogaMath.FloatsEqual(((nodeWidth * pointScaleFactor) % 1.0F), 1.0F);
		boolean hasFractionalHeight = !YogaMath.FloatsEqual(((nodeHeight * pointScaleFactor) % 1.0F), 0) && !YogaMath.FloatsEqual(((nodeHeight * pointScaleFactor) % 1.0F), 1.0F);

		SetLayoutDimension(YogaMath.RoundValueToPixelGrid(absoluteNodeRight, pointScaleFactor, (textRounding && hasFractionalWidth), (textRounding && !hasFractionalWidth)) - YogaMath.RoundValueToPixelGrid(absoluteNodeLeft, pointScaleFactor, false, textRounding), YogaDimension.Width);

		SetLayoutDimension(YogaMath.RoundValueToPixelGrid(absoluteNodeBottom, pointScaleFactor, (textRounding && hasFractionalHeight), (textRounding && !hasFractionalHeight)) - YogaMath.RoundValueToPixelGrid(absoluteNodeTop, pointScaleFactor, false, textRounding), YogaDimension.Height);

		int childCount = GetChildCount();
		for (int i = 0; i < childCount; i++) {
			YogaNode child = GetChild(i);
			child.RoundToPixelGrid(pointScaleFactor, absoluteNodeLeft, absoluteNodeTop);
		}
	}

	private YogaNode DeepClone() {
		YogaNode node = new YogaNode(this, null);
		List<YogaNode> vec = new ArrayList<>(GetChildCount());

		YogaNode childNode;
		for (YogaNode item : _children) {
			childNode = item.DeepClone();
			childNode._owner = node;
			vec.add(childNode);
		}

		node.SetChildren(vec);

		if (_config != null)
			node._config = _config.DeepClone();

		if (_nextChild != null)
			node._nextChild = _nextChild.DeepClone();

		return node;
	}

	private static boolean MeasureModeSizeIsExactAndMatchesOldMeasuredSize(YogaMeasureMode sizeMode, float size, float lastComputedSize) {
		return sizeMode == YogaMeasureMode.Exactly && YogaMath.FloatsEqual(size, lastComputedSize);
	}

	private static boolean MeasureModeOldSizeIsUnspecifiedAndStillFits(YogaMeasureMode sizeMode, float size, YogaMeasureMode lastSizeMode, float lastComputedSize) {
		return sizeMode == YogaMeasureMode.AtMost && lastSizeMode == YogaMeasureMode.Undefined && ((size > lastComputedSize || size == lastComputedSize) || YogaMath.FloatsEqual(size, lastComputedSize));
	}

	private static boolean MeasureModeNewMeasureSizeIsStricterAndStillValid(YogaMeasureMode sizeMode, float size, YogaMeasureMode lastSizeMode, float lastSize, float lastComputedSize) {
		return lastSizeMode == YogaMeasureMode.AtMost && sizeMode == YogaMeasureMode.AtMost && !Float.isNaN(size) && !Float.isNaN(lastSize) && !Float.isNaN(lastComputedSize) && lastSize > size && (lastComputedSize < size || lastComputedSize == size || YogaMath.FloatsEqual(size, lastComputedSize));
	}

	@SuppressWarnings("null")
	private static boolean CanUseCachedMeasurement(YogaMeasureMode widthMode, float width, YogaMeasureMode heightMode, float height, YogaMeasureMode lastWidthMode, float lastWidth, YogaMeasureMode lastHeightMode, float lastHeight, float lastComputedWidth, float lastComputedHeight, float marginRow, float marginColumn, YogaConfig config) {
		if ((!Float.isNaN(lastComputedHeight) && lastComputedHeight < 0) || (!Float.isNaN(lastComputedWidth) && lastComputedWidth < 0))
			return false;

		boolean useRoundedComparison = config != null && config.PointScaleFactor != 0;
		float effectiveWidth = useRoundedComparison ? YogaMath.RoundValueToPixelGrid(width, config.PointScaleFactor, false, false) : width;
		float effectiveHeight = useRoundedComparison ? YogaMath.RoundValueToPixelGrid(height, config.PointScaleFactor, false, false) : height;
		float effectiveLastWidth = useRoundedComparison ? YogaMath.RoundValueToPixelGrid(lastWidth, config.PointScaleFactor, false, false) : lastWidth;
		float effectiveLastHeight = useRoundedComparison ? YogaMath.RoundValueToPixelGrid(lastHeight, config.PointScaleFactor, false, false) : lastHeight;

		boolean hasSameWidthSpec = lastWidthMode == widthMode && YogaMath.FloatsEqual(effectiveLastWidth, effectiveWidth);
		boolean hasSameHeightSpec = lastHeightMode == heightMode && YogaMath.FloatsEqual(effectiveLastHeight, effectiveHeight);

		boolean widthIsCompatible = hasSameWidthSpec || MeasureModeSizeIsExactAndMatchesOldMeasuredSize(widthMode, width - marginRow, lastComputedWidth) || MeasureModeOldSizeIsUnspecifiedAndStillFits(widthMode, width - marginRow, lastWidthMode, lastComputedWidth) || MeasureModeNewMeasureSizeIsStricterAndStillValid(widthMode, width - marginRow, lastWidthMode, lastWidth, lastComputedWidth);

		boolean heightIsCompatible = hasSameHeightSpec || MeasureModeSizeIsExactAndMatchesOldMeasuredSize(heightMode, height - marginColumn, lastComputedHeight) || MeasureModeOldSizeIsUnspecifiedAndStillFits(heightMode, height - marginColumn, lastHeightMode, lastComputedHeight) || MeasureModeNewMeasureSizeIsStricterAndStillValid(heightMode, height - marginColumn, lastHeightMode, lastHeight, lastComputedHeight);

		return widthIsCompatible && heightIsCompatible;
	}

	// Public
	public Object _data;

	public boolean IsMeasureDefined() {
		return _measure != null;
	}

	public boolean IsBaselineDefined() {
		return _baseline != null;
	}

	public boolean GetIsReferenceBaseline() {
		return _isReferenceBaseline;
	}

	public void SetIsReferenceBaseline(boolean value) {
		if (_isReferenceBaseline != value) {
			_isReferenceBaseline = value;
			MarkDirty();
		}
	}

	public void CopyStyle(YogaNode srcNode) {
		_style.CopyFrom(srcNode._style);
	}

	public YogaDirection GetStyleDirection() {
		return _style.Direction;
	}

	public void SetStyleDirection(YogaDirection value) {
		if (_style.Direction != value) {
			_style.Direction = value;
			MarkDirty();
		}
	}

	public YogaFlexDirection GetFlexDirection() {
		return _style.FlexDirection;
	}

	public void SetFlexDirection(YogaFlexDirection value) {
		if (_style.FlexDirection != value) {
			_style.FlexDirection = value;
			MarkDirty();
		}
	}

	public YogaJustify GetJustifyContent() {
		return _style.JustifyContent;
	}

	public void SetJustifyContent(YogaJustify value) {
		if (_style.JustifyContent != value) {
			_style.JustifyContent = value;
			MarkDirty();
		}
	}

	public YogaDisplay GetDisplay() {
		return _style.Display;
	}

	public void SetDisplay(YogaDisplay value) {
		if (_style.Display != value) {
			_style.Display = value;
			MarkDirty();
		}
	}

	public YogaAlign GetAlignItems() {
		return _style.AlignItems;
	}

	public void SetAlignItems(YogaAlign value) {
		if (_style.AlignItems != value) {
			_style.AlignItems = value;
			MarkDirty();
		}
	}

	public YogaAlign GetAlignSelf() {
		return _style.AlignSelf;
	}

	public void SetAlignSelf(YogaAlign value) {
		if (_style.AlignSelf != value) {
			_style.AlignSelf = value;
			MarkDirty();
		}
	}

	public YogaAlign GetAlignContent() {
		return _style.AlignContent;
	}

	public void SetAlignContent(YogaAlign value) {
		if (_style.AlignContent != value) {
			_style.AlignContent = value;
			MarkDirty();
		}
	}

	public YogaPositionType GetPositionType() {
		return _style.PositionType;
	}

	public void SetPositionType(YogaPositionType value) {
		if (_style.PositionType != value) {
			_style.PositionType = value;
			MarkDirty();
		}
	}

	public YogaWrap GetWrap() {
		return _style.FlexWrap;
	}

	public void SetWrap(YogaWrap value) {
		if (_style.FlexWrap != value) {
			_style.FlexWrap = value;
			MarkDirty();
		}
	}

	public void SetFlex(float value) {
		if (_style.Flex != value) {
			_style.Flex = value;
			MarkDirty();
		}
	}

	public float GetFlexGrow() {
		return _style.FlexGrow;
	}

	public void SetFlexGrow(float value) {
		if (_style.FlexGrow != value) {
			_style.FlexGrow = value;
			MarkDirty();
		}
	}

	public float GetFlexShrink() {
		return _style.FlexShrink;
	}

	public void SetFlexShrink(float value) {
		if (_style.FlexShrink != value) {
			_style.FlexShrink = value;
			MarkDirty();
		}
	}

	public YogaValue GetFlexBasis() {
		return _style.FlexBasis;
	}

	public void SetFlexBasis(YogaValue value) {
		YogaValue current = _style.FlexBasis;
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.FlexBasis = value;
			MarkDirty();
		}
	}

	public YogaValue GetWidth() {
		return _style.Dimensions[YogaDimension.Width.ordinal()];
	}

	public void SetWidth(YogaValue value) {
		YogaValue current = _style.Dimensions[YogaDimension.Width.ordinal()];
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.Dimensions[YogaDimension.Width.ordinal()] = value;
			MarkDirty();
		}
	}

	public YogaValue GetHeight() {
		return _style.Dimensions[YogaDimension.Height.ordinal()];
	}

	public void SetHeight(YogaValue value) {
		YogaValue current = _style.Dimensions[YogaDimension.Height.ordinal()];
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.Dimensions[YogaDimension.Height.ordinal()] = value;
			MarkDirty();
		}
	}

	public YogaValue GetMaxWidth() {
		return _style.MaxDimensions[YogaDimension.Width.ordinal()];
	}

	public void SetMaxWidth(YogaValue value) {
		YogaValue current = _style.MaxDimensions[YogaDimension.Width.ordinal()];
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.MaxDimensions[YogaDimension.Width.ordinal()] = value;
			MarkDirty();
		}
	}

	public YogaValue GetMaxHeight() {
		return _style.MaxDimensions[YogaDimension.Height.ordinal()];
	}

	public void SetMaxHeight(YogaValue value) {
		YogaValue current = _style.MaxDimensions[YogaDimension.Height.ordinal()];
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.MaxDimensions[YogaDimension.Height.ordinal()] = value;
			MarkDirty();
		}
	}

	public YogaValue GetMinWidth() {
		return _style.MinDimensions[YogaDimension.Width.ordinal()];
	}

	public void SetMinWidth(YogaValue value) {
		YogaValue current = _style.MinDimensions[YogaDimension.Width.ordinal()];
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.MinDimensions[YogaDimension.Width.ordinal()] = value;
			MarkDirty();
		}
	}

	public YogaValue GetMinHeight() {
		return _style.MinDimensions[YogaDimension.Height.ordinal()];
	}

	public void SetMinHeight(YogaValue value) {
		YogaValue current = _style.MinDimensions[YogaDimension.Height.ordinal()];
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.MinDimensions[YogaDimension.Height.ordinal()] = value;
			MarkDirty();
		}
	}

	public float GetAspectRatio() {
		return _style.AspectRatio;
	}

	public void SetAspectRatio(float value) {
		if (_style.AspectRatio != value) {
			_style.AspectRatio = value;
			MarkDirty();
		}
	}

	public float GetLayoutX() {
		float value = _layout.Position[YogaEdge.Left.ordinal()];
		return Float.isNaN(value) ? 0 : value;
	}

	public float GetLayoutY() {
		float value = _layout.Position[YogaEdge.Top.ordinal()];
		return Float.isNaN(value) ? 0 : value;
	}

	public float GetLayoutWidth() {
		float value = _layout.Dimensions[YogaDimension.Width.ordinal()];
		return Float.isNaN(value) ? 0 : value;
	}

	public float GetLayoutHeight() {
		float value = _layout.Dimensions[YogaDimension.Height.ordinal()];
		return Float.isNaN(value) ? 0 : value;
	}

	public YogaDirection GetLayoutDirection() {
		return _layout.Direction;
	}

	public YogaOverflow GetOverflow() {
		return _style.Overflow;
	}

	public void SetOverflow(YogaOverflow value) {
		if (_style.Overflow != value) {
			_style.Overflow = value;
			MarkDirty();
		}
	}

	public int GetChildCount() {
		return _children.size();
	}

	public void MarkLayoutSeen() {
		_hasNewLayout = false;
	}

	public void Reset() {
		if (_children.size() > 0)
			throw new UnsupportedOperationException("Cannot reset a node which still has children attached");

		if (_owner != null)
			throw new UnsupportedOperationException("Cannot reset a node still attached to a owner");

		Clear();

		_print = null;
		_hasNewLayout = true;
		_nodeType = YogaNodeType.Default;
		_measure = null;
		_baseline = null;
		_dirtied = null;
		_style = new YogaStyle();
		_layout = new YogaLayout();
		_lineIndex = 0;
		_owner = null;
		_children = new ArrayList<>();
		_nextChild = null;
		_isDirty = false;
		_resolvedDimensions = new YogaValue[] { YogaValue.UNDEFINED, YogaValue.UNDEFINED };

		if (_config.UseWebDefaults) {
			_style.FlexDirection = YogaFlexDirection.Row;
			_style.AlignContent = YogaAlign.Stretch;
		}
	}

	@Override
	public Iterator<YogaNode> iterator() {
		return _children.iterator();
	}

	// Spacing
	public YogaValue GetLeft() {
		return _style.Position[YogaEdge.Left.ordinal()];
	}

	public void SetLeft(YogaValue value) {
		SetStylePosition(YogaEdge.Left, value);
	}

	public YogaValue GetTop() {
		return _style.Position[YogaEdge.Top.ordinal()];
	}

	public void SetTop(YogaValue value) {
		SetStylePosition(YogaEdge.Top, value);
	}

	public YogaValue GetRight() {
		return _style.Position[YogaEdge.Right.ordinal()];
	}

	public void SetRight(YogaValue value) {
		SetStylePosition(YogaEdge.Right, value);
	}

	public YogaValue GetBottom() {
		return _style.Position[YogaEdge.Bottom.ordinal()];
	}

	public void SetBottom(YogaValue value) {
		SetStylePosition(YogaEdge.Bottom, value);
	}

	public YogaValue GetStart() {
		return _style.Position[YogaEdge.Start.ordinal()];
	}

	public void SetStart(YogaValue value) {
		SetStylePosition(YogaEdge.Start, value);
	}

	public YogaValue GetEnd() {
		return _style.Position[YogaEdge.End.ordinal()];
	}

	public void SetEnd(YogaValue value) {
		SetStylePosition(YogaEdge.End, value);
	}

	public YogaValue GetMarginLeft() {
		return _style.Margin[YogaEdge.Left.ordinal()];
	}

	public void SetMarginLeft(YogaValue value) {
		SetStyleMargin(YogaEdge.Left, value);
	}

	public YogaValue GetMarginTop() {
		return _style.Margin[YogaEdge.Top.ordinal()];
	}

	public void SetMarginTop(YogaValue value) {
		SetStyleMargin(YogaEdge.Top, value);
	}

	public YogaValue GetMarginRight() {
		return _style.Margin[YogaEdge.Right.ordinal()];
	}

	public void SetMarginRight(YogaValue value) {
		SetStyleMargin(YogaEdge.Right, value);
	}

	public YogaValue GetMarginBottom() {
		return _style.Margin[YogaEdge.Bottom.ordinal()];
	}

	public void SetMarginBottom(YogaValue value) {
		SetStyleMargin(YogaEdge.Bottom, value);
	}

	public YogaValue GetMarginStart() {
		return _style.Margin[YogaEdge.Start.ordinal()];
	}

	public void SetMarginStart(YogaValue value) {
		SetStyleMargin(YogaEdge.Start, value);
	}

	public YogaValue GetMarginEnd() {
		return _style.Margin[YogaEdge.End.ordinal()];
	}

	public void SetMarginEnd(YogaValue value) {
		SetStyleMargin(YogaEdge.End, value);
	}

	public YogaValue GetMarginHorizontal() {
		return _style.Margin[YogaEdge.Horizontal.ordinal()];
	}

	public void SetMarginHorizontal(YogaValue value) {
		SetStyleMargin(YogaEdge.Horizontal, value);
	}

	public YogaValue GetMarginVertical() {
		return _style.Margin[YogaEdge.Vertical.ordinal()];
	}

	public void SetMarginVertical(YogaValue value) {
		SetStyleMargin(YogaEdge.Vertical, value);
	}

	public YogaValue GetMargin() {
		return _style.Margin[YogaEdge.All.ordinal()];
	}

	public void SetMargin(YogaValue value) {
		SetStyleMargin(YogaEdge.All, value);
	}

	public YogaValue GetPaddingLeft() {
		return _style.Padding[YogaEdge.Left.ordinal()];
	}

	public void SetPaddingLeft(YogaValue value) {
		SetStylePadding(YogaEdge.Left, value);
	}

	public YogaValue GetPaddingTop() {
		return _style.Padding[YogaEdge.Top.ordinal()];
	}

	public void SetPaddingTop(YogaValue value) {
		SetStylePadding(YogaEdge.Top, value);
	}

	public YogaValue GetPaddingRight() {
		return _style.Padding[YogaEdge.Right.ordinal()];
	}

	public void SetPaddingRight(YogaValue value) {
		SetStylePadding(YogaEdge.Right, value);
	}

	public YogaValue GetPaddingBottom() {
		return _style.Padding[YogaEdge.Bottom.ordinal()];
	}

	public void SetPaddingBottom(YogaValue value) {
		SetStylePadding(YogaEdge.Bottom, value);
	}

	public YogaValue GetPaddingStart() {
		return _style.Padding[YogaEdge.Start.ordinal()];
	}

	public void SetPaddingStart(YogaValue value) {
		SetStylePadding(YogaEdge.Start, value);
	}

	public YogaValue GetPaddingEnd() {
		return _style.Padding[YogaEdge.End.ordinal()];
	}

	public void SetPaddingEnd(YogaValue value) {
		SetStylePadding(YogaEdge.End, value);
	}

	public YogaValue GetPaddingHorizontal() {
		return _style.Padding[YogaEdge.Horizontal.ordinal()];
	}

	public void SetPaddingHorizontal(YogaValue value) {
		SetStylePadding(YogaEdge.Horizontal, value);
	}

	public YogaValue GetPaddingVertical() {
		return _style.Padding[YogaEdge.Vertical.ordinal()];
	}

	public void SetPaddingVertical(YogaValue value) {
		SetStylePadding(YogaEdge.Vertical, value);
	}

	public YogaValue GetPadding() {
		return _style.Padding[YogaEdge.All.ordinal()];
	}

	public void SetPadding(YogaValue value) {
		SetStylePadding(YogaEdge.All, value);
	}

	public float GetBorderLeftWidth() {
		YogaValue value = _style.Border[YogaEdge.Left.ordinal()];
		if (value.Unit == YogaUnit.Auto || value.Unit == YogaUnit.Undefined)
			return Float.NaN;

		return value.Value;
	}

	public void SetBorderLeftWidth(float value) {
		YogaEdge edge = YogaEdge.Left;
		YogaValue current = _style.Border[edge.ordinal()];
		YogaValue next = new YogaValue(value, Float.isNaN(value) ? YogaUnit.Undefined : YogaUnit.Point);

		if (current.Unit != next.Unit || (next.Unit != YogaUnit.Undefined && current.Value != next.Value)) {
			_style.Border[edge.ordinal()] = next;
			MarkDirty();
		}
	}

	public float GetBorderTopWidth() {
		YogaValue value = _style.Border[YogaEdge.Top.ordinal()];
		if (value.Unit == YogaUnit.Auto || value.Unit == YogaUnit.Undefined)
			return Float.NaN;

		return value.Value;
	}

	public void SetBorderTopWidth(float value) {
		YogaEdge edge = YogaEdge.Top;
		YogaValue current = _style.Border[edge.ordinal()];
		YogaValue next = new YogaValue(value, Float.isNaN(value) ? YogaUnit.Undefined : YogaUnit.Point);

		if (current.Unit != next.Unit || (next.Unit != YogaUnit.Undefined && current.Value != next.Value)) {
			_style.Border[edge.ordinal()] = next;
			MarkDirty();
		}
	}

	public float GetBorderRightWidth() {
		YogaValue value = _style.Border[YogaEdge.Right.ordinal()];
		if (value.Unit == YogaUnit.Auto || value.Unit == YogaUnit.Undefined)
			return Float.NaN;

		return value.Value;
	}

	public void SetBorderRightWidth(float value) {
		YogaEdge edge = YogaEdge.Right;
		YogaValue current = _style.Border[edge.ordinal()];
		YogaValue next = new YogaValue(value, Float.isNaN(value) ? YogaUnit.Undefined : YogaUnit.Point);

		if (current.Unit != next.Unit || (next.Unit != YogaUnit.Undefined && current.Value != next.Value)) {
			_style.Border[edge.ordinal()] = next;
			MarkDirty();
		}
	}

	public float GetBorderBottomWidth() {
		YogaValue value = _style.Border[YogaEdge.Bottom.ordinal()];
		if (value.Unit == YogaUnit.Auto || value.Unit == YogaUnit.Undefined)
			return Float.NaN;

		return value.Value;
	}

	public void SetBorderBottomWidth(float value) {
		YogaEdge edge = YogaEdge.Bottom;
		YogaValue current = _style.Border[edge.ordinal()];
		YogaValue next = new YogaValue(value, Float.isNaN(value) ? YogaUnit.Undefined : YogaUnit.Point);

		if (current.Unit != next.Unit || (next.Unit != YogaUnit.Undefined && current.Value != next.Value)) {
			_style.Border[edge.ordinal()] = next;
			MarkDirty();
		}
	}

	public float GetBorderStartWidth() {
		YogaValue value = _style.Border[YogaEdge.Start.ordinal()];
		if (value.Unit == YogaUnit.Auto || value.Unit == YogaUnit.Undefined)
			return Float.NaN;

		return value.Value;
	}

	public void SetBorderStartWidth(float value) {
		YogaEdge edge = YogaEdge.Start;
		YogaValue current = _style.Border[edge.ordinal()];
		YogaValue next = new YogaValue(value, Float.isNaN(value) ? YogaUnit.Undefined : YogaUnit.Point);

		if (current.Unit != next.Unit || (next.Unit != YogaUnit.Undefined && current.Value != next.Value)) {
			_style.Border[edge.ordinal()] = next;
			MarkDirty();
		}
	}

	public float GetBorderEndWidth() {
		YogaValue value = _style.Border[YogaEdge.End.ordinal()];
		if (value.Unit == YogaUnit.Auto || value.Unit == YogaUnit.Undefined)
			return Float.NaN;

		return value.Value;
	}

	public void SetBorderEndWidth(float value) {
		YogaEdge edge = YogaEdge.End;
		YogaValue current = _style.Border[edge.ordinal()];
		YogaValue next = new YogaValue(value, Float.isNaN(value) ? YogaUnit.Undefined : YogaUnit.Point);

		if (current.Unit != next.Unit || (next.Unit != YogaUnit.Undefined && current.Value != next.Value)) {
			_style.Border[edge.ordinal()] = next;
			MarkDirty();
		}
	}

	public float GetBorderWidth() {
		YogaValue value = _style.Border[YogaEdge.All.ordinal()];
		if (value.Unit == YogaUnit.Auto || value.Unit == YogaUnit.Undefined)
			return Float.NaN;

		return value.Value;
	}

	public void SetBorderWidth(float value) {
		YogaEdge edge = YogaEdge.All;
		YogaValue current = _style.Border[edge.ordinal()];
		YogaValue next = new YogaValue(value, Float.isNaN(value) ? YogaUnit.Undefined : YogaUnit.Point);

		if (current.Unit != next.Unit || (next.Unit != YogaUnit.Undefined && current.Value != next.Value)) {
			_style.Border[edge.ordinal()] = next;
			MarkDirty();
		}
	}

	public float GetLayoutMarginLeft() {
		return GetLayoutMargin(YogaEdge.Left);
	}

	public float GetLayoutMarginTop() {
		return GetLayoutMargin(YogaEdge.Top);
	}

	public float GetLayoutMarginRight() {
		return GetLayoutMargin(YogaEdge.Right);
	}

	public float GetLayoutMarginBottom() {
		return GetLayoutMargin(YogaEdge.Bottom);
	}

	public float GetLayoutMarginStart() {
		return GetLayoutMargin(YogaEdge.Start);
	}

	public float GetLayoutMarginEnd() {
		return GetLayoutMargin(YogaEdge.End);
	}

	public float GetLayoutPaddingLeft() {
		return GetLayoutPadding(YogaEdge.Left);
	}

	public float GetLayoutPaddingTop() {
		return GetLayoutPadding(YogaEdge.Top);
	}

	public float GetLayoutPaddingRight() {
		return GetLayoutPadding(YogaEdge.Right);
	}

	public float GetLayoutPaddingBottom() {
		return GetLayoutPadding(YogaEdge.Bottom);
	}

	public float GetLayoutPaddingStart() {
		return GetLayoutPadding(YogaEdge.Start);
	}

	public float GetLayoutPaddingEnd() {
		return GetLayoutPadding(YogaEdge.End);
	}

	private void SetStylePosition(YogaEdge edge, YogaValue value) {
		YogaValue current = _style.Position[edge.ordinal()];
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.Position[edge.ordinal()] = value;
			MarkDirty();
		}
	}

	private void SetStyleMargin(YogaEdge edge, YogaValue value) {
		YogaValue current = _style.Margin[edge.ordinal()];
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.Margin[edge.ordinal()] = value;
			MarkDirty();
		}
	}

	private void SetStylePadding(YogaEdge edge, YogaValue value) {
		YogaValue current = _style.Padding[edge.ordinal()];
		if (current.Unit != value.Unit || (value.Unit != YogaUnit.Undefined && current.Value != value.Value)) {
			_style.Padding[edge.ordinal()] = value;
			MarkDirty();
		}
	}

	public float GetLayoutMargin(YogaEdge edge) {
		if (edge == YogaEdge.Left) {
			if (_layout.Direction == YogaDirection.RightToLeft)
				return _layout.Margin[YogaEdge.End.ordinal()];
			return _layout.Margin[YogaEdge.Start.ordinal()];
		}

		if (edge == YogaEdge.Right) {
			if (_layout.Direction == YogaDirection.RightToLeft)
				return _layout.Margin[YogaEdge.Start.ordinal()];
			return _layout.Margin[YogaEdge.End.ordinal()];
		}

		return _layout.Margin[edge.ordinal()];
	}

	public float GetLayoutPadding(YogaEdge edge) {
		if (edge == YogaEdge.Left) {
			if (_layout.Direction == YogaDirection.RightToLeft)
				return _layout.Padding[YogaEdge.End.ordinal()];
			return _layout.Padding[YogaEdge.Start.ordinal()];
		}

		if (edge == YogaEdge.Right) {
			if (_layout.Direction == YogaDirection.RightToLeft)
				return _layout.Padding[YogaEdge.Start.ordinal()];
			return _layout.Padding[YogaEdge.End.ordinal()];
		}

		return _layout.Padding[edge.ordinal()];
	}

}
