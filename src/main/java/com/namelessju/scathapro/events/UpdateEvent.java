package com.namelessju.scathapro.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class UpdateEvent extends Event {
	public final String previousVersion;
	public final String newVersion;
	
	public UpdateEvent(String previousVersion, String newVersion) {
		this.previousVersion = previousVersion;
		this.newVersion = newVersion;
	}
}
