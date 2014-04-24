package com.sfsweep.android.helpers;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class HeightAnimation extends Animation {
	private final int targetHeight;
	private final int originalHeight;
	private final View view;

	public HeightAnimation(View view, int targetHeight) {
		this.view = view;
		this.targetHeight = targetHeight;
		this.originalHeight = view.getLayoutParams().height;
	}

	@Override
	protected void applyTransformation(float interpolatedTime,
			Transformation t) {
		int diff = targetHeight - originalHeight;
		int newHeight = Math.round(interpolatedTime * diff)
				+ originalHeight;
		view.getLayoutParams().height = newHeight;
		view.requestLayout();
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
	}

	@Override
	public boolean willChangeBounds() {
		return true;
	}
}
