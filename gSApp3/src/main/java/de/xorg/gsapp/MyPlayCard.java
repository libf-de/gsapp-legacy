package de.xorg.gsapp;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fima.cardsui.objects.RecyclableCard;

public class MyPlayCard extends RecyclableCard {

	Boolean isDark;
	
	public MyPlayCard(String titlePlay, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable, Boolean isDarkCard) {
		super(titlePlay, description, color, titleColor, hasOverflow,
				isClickable);
		isDark = isDarkCard;
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.playc;
	}

	@Override
	protected void applyTo(View convertView) {
		((TextView) convertView.findViewById(R.id.title)).setText(titlePlay);
		((TextView) convertView.findViewById(R.id.title)).setTextColor(Color
				.parseColor(titleColor));
		((TextView) convertView.findViewById(R.id.description))
				.setText(description);
		((ImageView) convertView.findViewById(R.id.stripe))
				.setBackgroundColor(Color.parseColor(color));
		
		if (isDark) {
			// TODO Wird implementiert.
			//convertView.setBackgroundResource(R.drawable.darkshadow);
			//((LinearLayout) convertView.findViewById(R.id.backLayout)).setBackgroundResource(R.drawable.darkshadow);
			//((TextView) convertView.findViewById(R.id.description)).setTextColor(Color.WHITE);
		}
		

		if (isClickable == true)
			((LinearLayout) convertView.findViewById(R.id.contentLayout))
					.setBackgroundResource(R.drawable.selectable_background_cardbank);

		if (hasOverflow == true)
			((ImageView) convertView.findViewById(R.id.overflow))
					.setVisibility(View.VISIBLE);
		else
			((ImageView) convertView.findViewById(R.id.overflow))
					.setVisibility(View.GONE);
	}
}
