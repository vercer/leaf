package com.vercer.jet;

import lombok.Getter;

public class Settings
{
	@Getter private
	boolean reload;

	@Getter private
	String prefix = "jet";

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

	public static Builder builder()
	{
		Settings settings = new Settings();
		return settings.new Builder();
	}

	public class Builder
	{
		public Builder reloadTemplates(boolean reload)
		{
			Settings.this.reload = reload;
			return this;
		}

		public Builder prefix(String prefix)
		{
			Settings.this.prefix = prefix;
			return this;
		}

		public Builder removeSpecialAttributes(boolean value)
		{
			Settings.this.removeSpecialAttributes = value;
			return this;
		}

		public Builder removeSpecialTags(boolean value)
		{
			Settings.this.removeSpecialTags = value;
			return this;
		}

		public Builder removeWhiteSpace(boolean value)
		{
			Settings.this.removeWhiteSpace = value;
			return this;
		}

		public Builder removeComments(boolean value)
		{
			Settings.this.removeComments = value;
			return this;
		}

		public Builder encodeSession(boolean value)
		{
			Settings.this.sessionEncoded = value;
			return this;
		}

		public Builder development(boolean development)
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