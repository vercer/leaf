package com.vercer.leaf;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

import lombok.Getter;

import com.google.common.collect.Sets;

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

	//TODO remove this - belongs with application code
	@Getter private
	Collection<Locale> locales = Sets.newTreeSet(new Comparator<Locale>()
	{
		@Override
		public int compare(Locale o1, Locale o2)
		{
			return o1.getDisplayName(o1).compareTo(o2.getDisplayName(o2));
		}
	});
	
	@Getter private
	Locale defaultLocale = Locale.ENGLISH;

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
		
		public SettingsBuilder addAllowedLocale(Locale locale)
		{
			locales.add(locale);
			return this;
		}
		
		public SettingsBuilder defaultLocale(Locale locale)
		{
			Settings.this.defaultLocale = locale;
			return this;
		}

		Settings build()
		{
			return Settings.this;
		}
	}
}