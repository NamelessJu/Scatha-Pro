package com.namelessju.scathapro.alertmodes;

import com.namelessju.scathapro.util.MessageUtil;

public class AlertTitle {
	
	public String title;
	public String subtitle;
	private final String titleFormatting;
	private final String subtitleFormatting;
	private final int fadeInTicks;
	private final int stayTicks;
	private final int fadeOutTicks;
	
	public AlertTitle(String title, String subtitle) {
		this(title, subtitle, null, null, 0, 0, 0);
	}

	public AlertTitle(String title, String subtitle, String titleFormatting, String subtitleFormatting, int fadeInTicks, int stayTicks, int fadeOutTicks) {
		this.title = title;
		this.subtitle = subtitle;
		this.titleFormatting = titleFormatting;
		this.subtitleFormatting = subtitleFormatting;
		this.fadeInTicks = fadeInTicks;
		this.stayTicks = stayTicks;
		this.fadeOutTicks = fadeOutTicks;
	}
	
	public void display() {
		if (fadeInTicks <= 0 && stayTicks <= 0 && fadeOutTicks <= 0) return;
		MessageUtil.displayTitle(nullableString(titleFormatting) + nullableString(title), nullableString(subtitleFormatting) + nullableString(subtitle), fadeInTicks, stayTicks, fadeOutTicks);
	}
	
	public AlertTitle apply(AlertTitle other, String details) {
		String title = this.title;
		if (title != null && other != null && other.title != null && !other.title.trim().isEmpty()) {
			title = other.title;
		}
		
		String subtitle = this.subtitle;
		if (subtitle != null && other != null && other.subtitle != null && !other.subtitle.trim().isEmpty()) {
			subtitle = other.subtitle;
		}
		else if (subtitle == null && details != null) subtitle = details;
		
		return new AlertTitle(title, subtitle, titleFormatting, subtitleFormatting, fadeInTicks, stayTicks, fadeOutTicks);
	}
	
	
	private String nullableString(String str) {
		return str == null ? "" : str;
	}
}
