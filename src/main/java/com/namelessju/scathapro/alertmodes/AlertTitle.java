package com.namelessju.scathapro.alertmodes;

import com.namelessju.scathapro.util.MessageUtil;

public class AlertTitle {
	
	public String title;
	public String subtitle;
	
	public AlertTitle(String title, String subtitle) {
		this.title = title;
		this.subtitle = subtitle;
	}
	
	public void display(int fadeInTicks, int stayTicks, int fadeOutTicks) {
		String title = this.title;
		if (title == null) title = "";
		String subtitle = this.subtitle;
		if (subtitle == null) subtitle = "";
		
		MessageUtil.displayTitle(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
	}
}
