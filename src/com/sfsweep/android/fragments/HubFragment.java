package com.sfsweep.android.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sfsweep.android.R;

public abstract class HubFragment extends Fragment {
	
	private View                   mInflatedView; 
	private ImageView              mIvItemImage;
	private TextView               mTvItemText; 
	private OnHubItemClickListener mListener; 
	
	private String   mFont = "Roboto-Light.ttf";
	private Typeface mTypeface;
	
	public interface OnHubItemClickListener {
		public void onHubItemClick(String hubFragmentType); 
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity); 
		
		if (activity instanceof OnHubItemClickListener) {
			mListener = (OnHubItemClickListener) activity;
		} else {
			throw new ClassCastException(activity.toString() + " must implement OnHubItemClickListener"); 
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState); 
		mInflatedView = inflater.inflate(R.layout.fragment_hub, parent, false);  
		
		setupWidgets(mInflatedView);
		return mInflatedView;
	}
	
	private void setupWidgets(View v) {
		mIvItemImage = (ImageView) v.findViewById(R.id.ivItemImage);
		mIvItemImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onHubItemClick(getHubFragmentType()); 
			}
		}); 
		
		mTvItemText = (TextView) v.findViewById(R.id.tvItemText); 
		mTvItemText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onHubItemClick(getHubFragmentType());
			}
		}); 
		
		mTypeface = Typeface.createFromAsset(getActivity().getAssets(), mFont); 
		mTvItemText.setTypeface(mTypeface); 
		mTvItemText.setTextColor(getResources().getColor(R.color.sfsweep_icon_gray));
	}

	public View getInflatedView() {
		return mInflatedView; 
	}
	
	public ImageView getItemImage() {
		return mIvItemImage;
	}
	
	public TextView getItemText() {
		return mTvItemText;
	}
	
	/*
	 * Subclass to implement so as to return a String representation of the subclass type
	 * (e.g., "AddressHubFragment")  
	 */
	public abstract String getHubFragmentType();
	
}
