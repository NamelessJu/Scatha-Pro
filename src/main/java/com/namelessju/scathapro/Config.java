package com.namelessju.scathapro;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Config {
	
	private static Config instance = new Config();
	
	private Configuration config;
	
	public enum Key {
		
		overlayX("overlay", "x", -1D), overlayY("overlay", "y", -1D),
		chatCopy("other", "chatCopy", false),
		memeMode("other", "memeMode", false),
		devMode("other", "devMode", false);
		
		String category;
		String key;
		Object defaultValue;
		
		Key(String category, String key, Object defaultValue) {
			this.category = category;
			this.key = key;
			this.defaultValue = defaultValue;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}
	}
	
	private Config() {
        File configFile = new File(Loader.instance().getConfigDir(), ScathaPro.MODID + ".cfg");
        config = new Configuration(configFile);
        config.load();
	}
	
	public static Config getInstance() {
		return instance;
	}
	
	
	private Property getIntProperty(Key key) {
		return config.get(key.category, key.key, (Integer) key.getDefaultValue());
	}
	private Property getDoubleProperty(Key key) {
		return config.get(key.category, key.key, (Double) key.getDefaultValue());
	}
	private Property getStringProperty(Key key) {
		return config.get(key.category, key.key, (String) key.getDefaultValue());
	}
	private Property getBooleanProperty(Key key) {
		return config.get(key.category, key.key, (Boolean) key.getDefaultValue());
	}
	
	
	public int getInt(Key key) {
		return getIntProperty(key).getInt();
	}
	public double getDouble(Key key) {
		return getDoubleProperty(key).getDouble();
	}
	public String getString(Key key) {
		return getStringProperty(key).getString();
	}
	public boolean getBoolean(Key key) {
		return getBooleanProperty(key).getBoolean();
	}
	
	public void set(Key key, int value) {
		getIntProperty(key).set(value);
	}
	public void set(Key key, double value) {
		getDoubleProperty(key).set(value);
	}
	public void set(Key key, String value) {
		getStringProperty(key).set(value);
	}
	public void set(Key key, boolean value) {
		getBooleanProperty(key).set(value);
	}
	
	public void reset(Key key) {
		if (key.getDefaultValue() instanceof Integer) set(key, (Integer) key.getDefaultValue());
		else if (key.getDefaultValue() instanceof Double) set(key, (Double) key.getDefaultValue());
		else if (key.getDefaultValue() instanceof String) set(key, (String) key.getDefaultValue());
		else if (key.getDefaultValue() instanceof Boolean) set(key, (Boolean) key.getDefaultValue());
	}
	
	public void save() {
		config.save();
	}
}
