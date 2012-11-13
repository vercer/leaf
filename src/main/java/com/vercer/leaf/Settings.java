package com.vercer.leaf;

import lombok.Getter;

public class Settings
{
	@Getter private
	boolean reload;

	@Getter private
	String prefix = "leaf";

	@Getter	private
	boolean removeSpecialAttributes;

	@Getter	private
	boolean removeSpecialTags;

	@Getter	private
	boolean removeWhiteSpace;

	@Getter	private
	boolean removeComments;

	@Getter private
	boolean sessionEncoded;

	@Getter private
	boolean development;

	private Settings()
	{
	}

	public static SettingsBuilder builder()
	{
		Settings settings = new Settings();
		return settings.new SettingsBuilder();
	}

	public class SettingsBuilder
	{
		public SettingsBuilder reloadTemplates(boolean reload)
		{
			Settings.this.reload = reload;
			return this;
		}

		public SettingsBuilder prefix(String prefix)
		{
			Settings.this.prefix = prefix;
			return this;
		}

		public SettingsBuilder removeSpecialAttributes(boolean value)
		{
			Settings.this.removeSpecialAttributes = value;
			return this;
		}

		public SettingsBuilder removeSpecialTags(boolean value)
		{
			Settings.this.removeSpecialTags = value;
			return this;
		}

		public SettingsBuilder removeWhiteSpace(boolean value)
		{
			Settings.this.removeWhiteSpace = value;
			return this;
		}

		public SettingsBuilder removeComments(boolean value)
		{
			Settings.this.removeComments = value;
			return this;
		}

		public SettingsBuilder encodeSession(boolean value)
		{
			Settings.this.sessionEncoded = value;
			return this;
		}

		public SettingsBuilder development(boolean development)
		{
			Settings.this.development = development;

			reloadTemplates(development);
			removeSpecialAttributes(!development);
			removeWhiteSpace(!development);
			removeSpecialTags(true);
			return this;
		}

		Settings build()
		{
			return Settings.this;
		}
	}
}