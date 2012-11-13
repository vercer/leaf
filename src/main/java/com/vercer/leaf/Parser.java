package com.vercer.leaf;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vercer.leaf.Markup.Builder;

/**
 * Parse an html file into a {@link Markup} tree. Line terminators are standardised
 * to a single new line character. Only tags containing
 * the specified name attribute will create a Markup instance.
 *
 * @author John Patterson (john@vercer.com)
 */
public class Parser
{
	public Markup parse(String template)
	{
		State state = new State(template);
		try
		{
			Builder builder = Markup.builder().special(true);
			state.tag(builder, true);
			return builder.build();
		}
		catch (Throwable t)
		{
			if (t instanceof ParseException)
			{
				throw (ParseException) t;
			}
			else
			{
				throw new ParseException("Problem parsing template", state, t);
			}
		}
	}

	protected boolean isSpecialTag(Markup markup)
	{
		if (markup.getTag().startsWith(Leaf.get().getSettings().getPrefix())) return true;

		Map<String, String> attributes = markup.getAttributes();
		for (String name : attributes.keySet())
		{
			if (name.startsWith(Leaf.get().getSettings().getPrefix() + ":"))
			{
				return true;
			}
		}
		return false;
	}

	// stateful implementation which allows shared configuration
	private class State
	{
		private int marker;
		private int position;
		private final String template;

		public State(String template)
		{
			this.template = template;
		}

		private void tag(Builder parent, boolean root)
		{
			Builder current;
			if (root)
			{
				// fill the passed in builder for root
				current = parent;
			}
			else
			{
				// create a new markup for every tag under the root
				current = Markup.builder();
			}

			prelude(current);
			open(current);
			attributes(current);

			// see if we have an open-close tag
			boolean openClose = current() == '/';

			if (openClose)
			{
				position++;
			}

			// move past the closing >
			position++;

			// get the markup to read even though it is not complete
			Markup markup = current.build();

			if (isSpecialTag(markup))
			{
				current.special(true);
			}

			// root markup is also special
			if (markup.isSpecial())
			{
				if (!root)
				{
					// add this markup as a child - we can still modify it
					parent.child(markup);
				}

				// add children to this markup not the ancestors
				parent = current;

				assert position > marker;
				marker = position;

				if (Leaf.get().getSettings().isRemoveComments())
				{
					current.prelude(commentPattern.matcher(markup.getPrelude()).replaceAll(""));
				}

				if (Leaf.get().getSettings().isRemoveWhiteSpace())
				{
					current.prelude(compact(markup.getPrelude()));
				}
			}

			if (openClose)
			{
				current.open(false);
				return;
			}

			// this is an open tag
			current.open(true);
			children(parent);
			content(current);
			close(current);

			if (markup.isSpecial())
			{
				assert position > marker;
				marker = position;
				if (Leaf.get().getSettings().isRemoveComments())
				{
					current.content(commentPattern.matcher(markup.getContent()).replaceAll(""));
				}

				if (Leaf.get().getSettings().isRemoveWhiteSpace())
				{
					current.content(compact(markup.getContent()));
				}
			}


		}

		// reduce all whitespace to at most one char
		Pattern adjacentWhitSpace = Pattern.compile("\\s{2,}", Pattern.DOTALL);
		
		// do not match ie conditional comments
		Pattern commentPattern = Pattern.compile("<!--[^\\[].*-->", Pattern.DOTALL);

		private String compact(String content)
		{
			// reduce all white space to at most one char
			Matcher matcher = adjacentWhitSpace.matcher(content);
			StringBuffer sb = new StringBuffer();
			while (matcher.find())
			{
				if (matcher.group().contains("\n"))
				{
					matcher.appendReplacement(sb, "\n");
				}
				else
				{
					matcher.appendReplacement(sb, " ");
				}
			}
			matcher.appendTail(sb);
			return sb.toString();
		}

		private void prelude(Builder builder)
		{
			moveToTag();
			if (position >= 0)
			{
				builder.prelude(template.substring(marker, position));
			}
		}

		private void open(Builder builder)
		{
			assert current() == '<' : "Expected <";
			position++;
			builder.tag(name());
			moveToNonWhiteSpace();
		}

		private void attributes(Builder builder)
		{
			// all attribute names start with a name character
			while (isNameCharacter())
			{
				// read the name
				String name = name();
				moveToNonWhiteSpace();

				// eat the = and "
				assert current() == '=' : "Expected =";
				position++;
				moveToNonWhiteSpace();
				assert current() == '"' : "Expected \"";
				position++;

				// read the quoted value
				int start = position;
				position = template.indexOf('"', position);
				String value = template.substring(start, position);

				builder.attribute(name, value);

				// skip the last quote
				position++;

				// move to next thing
				moveToNonWhiteSpace();
			}
		}

		private void children(Builder builder)
		{
			while (hasMoreElements())
			{
				tag(builder, false);
			}
		}

		private boolean hasMoreElements()
		{
			// check that the next tag is not a close tag
			return template.charAt(indexOfTag() + 1) != '/';
		}

		private void content(Builder builder)
		{
			moveToTag();
			builder.content(template.substring(marker, position));
		}

		private void close(Builder builder)
		{
			assert current() == '<' : "Expected <";
			position++;
			assert current() == '/' : "Expected /";
			position++;

			String close = name();
			String open = builder.build().getTag();
			if (!open.equals(close))
			{
				throw new IllegalStateException("Open tag " + open + " and close tag " + close + " do not match");
			}
			position++;
		}

		private char current()
		{
			return template.charAt(position);
		}

		private String name()
		{
			int start = position;
			moveToNameEnd();
			return template.substring(start, position);
		}

		private void moveToTag()
		{
			position = indexOfTag();
		}

		// finds the next < that is not followed by a ! or ?
		private int indexOfTag()
		{
			int index = this.position;

			// move to the next < but skip <! and <?
			while (true)
			{
				index = template.indexOf('<', index);
				if (template.charAt(index + 1) == '!' ||
					template.charAt(index + 1) == '?')
				{
					index++;

					// check to see if this is a comment starting
					if (template.charAt(index + 1) == '-' && template.charAt(index + 2) == '-')
					{
						// move to the end of the comment
						index = template.indexOf("-->", index) +  3;
					}
				}
				else
				{
					break;
				}
			}

			return index;
		}

		private void moveToNameEnd()
		{
			while (isNameCharacter()) position++;
		}

		private void moveToNonWhiteSpace()
		{
			while (Character.isWhitespace(current())) position++;
		}

		private boolean isNameCharacter()
		{
			return Character.isLetterOrDigit(current()) ||
				current() == '-' ||
				current() == '_' ||
				current() == ':';
		}

	}

	public static class ParseException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public ParseException(String message, State state)
		{
			this(message, state, null);
		}

		private static String buildMessage(String message, State state)
		{
			int beginIndex = Math.max(state.position - 50, 0);
			int endIndex = Math.min(state.position + 50, state.template.length());
			return message += "\n\nProblem near " + state.template.substring(beginIndex, endIndex);
		}

		public ParseException(String message, State state, Throwable cause)
		{
			super(buildMessage(message, state), cause);
		}
	}
}
