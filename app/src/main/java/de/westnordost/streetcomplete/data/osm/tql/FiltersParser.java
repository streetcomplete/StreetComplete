package de.westnordost.streetcomplete.data.osm.tql;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Compiles a string in filter syntax into a TagFilterExpression. A string in filter syntax is
 * something like this:
 *
 * <tt>"ways with (highway = residential or highway = tertiary) and !name"</tt> (finds all
 * residential and tertiary roads that have no name)
 */
public class FiltersParser
{
	private static final char[] QUOTATION_MARKS = {'"', '\''};
	private static final String[] OPERATORS = {"=", "!=", "~", "!~"};

	private static final String WITH = "with";
	private static final String OR = "or";
	private static final String AND = "and";
	private static final String AROUND = "around";

	private static final String[] RESERVED_WORDS = {WITH, OR, AND, AROUND};

	private StringWithCursor input;

	public TagFilterExpression parse(String input)
	{
		try
		{
			// convert all white-spacey things to whitespaces so we do not have to deal with them later
			this.input = new StringWithCursor(input.replaceAll("\\s", " "), Locale.US);

			List<ElementsTypeFilter> elementsTypeFilters = parseElementsDeclaration();
			BooleanExpression<OQLExpressionValue> tagExprRoot = parseTags();

			return new TagFilterExpression(elementsTypeFilters, tagExprRoot);
		}
		catch(ParseException e)
		{
			throw new RuntimeException(e);
		}
	}

	private int expectAnyNumberOfSpaces()
	{
		int count = 0;
		while(input.nextIsAndAdvance(' ')) count++;
		return count;
	}

	private int expectOneOrMoreSpaces() throws ParseException
	{
		if(!input.nextIsAndAdvance(' '))
			throw new ParseException("Expected a whitespace", input.getCursorPos());
		return expectAnyNumberOfSpaces() + 1;
	}

	private List<ElementsTypeFilter> parseElementsDeclaration() throws ParseException
	{
		List<ElementsTypeFilter> result = new ArrayList<>();
		result.add(parseElementDeclaration());
		while(input.nextIsAndAdvance(','))
		{
			ElementsTypeFilter element = parseElementDeclaration();
			if(result.contains(element))
			{
				throw new ParseException("Mentioned the same element type " + element + " twice",
						input.getCursorPos());
			}
			result.add(element);
		}
		return result;
	}

	private ElementsTypeFilter parseElementDeclaration() throws ParseException
	{
		expectAnyNumberOfSpaces();
		for(ElementsTypeFilter t : ElementsTypeFilter.values())
		{
			if(input.nextIsAndAdvanceIgnoreCase(t.name))
			{
				expectAnyNumberOfSpaces();
				return t;
			}
		}
		throw new ParseException("Expected element types." +
				"Any of: nodes, ways or relations, separated by ','", input.getCursorPos());
	}

	private BooleanExpression<OQLExpressionValue> parseTags() throws ParseException
	{
		// tags are optional...
		if(!input.nextIsAndAdvanceIgnoreCase(WITH))
		{
			if(!input.isAtEnd())
			{
				throw new ParseException("Expected end of string or 'with' keyword",
						input.getCursorPos());
			}
			return new BooleanExpression<>();
		}

		BooleanExpressionBuilder<OQLExpressionValue> builder = new BooleanExpressionBuilder<>();

		do
		{
			if(!parseBrackets('(', builder))
				// if it has no bracket, there must be at least one whitespace
				throw new ParseException("Expected a whitespace or bracket before the tag",
						input.getCursorPos());

			builder.addValue(parseTag());

			// parseTag() might have "eaten up" a whitespace after the key in expectation of an
			// operator.
			boolean separated = input.previousIs(' ');
			separated |= parseBrackets(')', builder);

			if(input.isAtEnd()) break;

			if(!separated)
				// same as with the opening bracket, only that if the string is over, its okay
				throw new ParseException("Expected a whitespace or bracket after the tag",
						input.getCursorPos());

			if (input.nextIsAndAdvanceIgnoreCase(OR))
			{
				builder.addOr();
			}
			else if (input.nextIsAndAdvanceIgnoreCase(AND))
			{
				builder.addAnd();
			}
			else
				throw new ParseException("Expected end of string, 'and' or 'or'",
						input.getCursorPos());

		} while(true);

		try
		{
			return builder.getResult();
		}
		catch(IllegalStateException e)
		{
			throw new ParseException(e.getMessage(), input.getCursorPos());
		}
	}

	private boolean parseBrackets(char bracket, BooleanExpressionBuilder expr) throws ParseException
	{
		int characterCount = expectAnyNumberOfSpaces();
		int previousCharacterCount;
		do
		{
			previousCharacterCount = characterCount;
			if (input.nextIsAndAdvance(bracket))
			{
				try
				{
					if (bracket == '(') expr.addOpenBracket();
					else if (bracket == ')') expr.addCloseBracket();
				}
				catch(IllegalStateException e)
				{
					throw new ParseException(e.getMessage(), input.getCursorPos());
				}
				characterCount++;
			}
			characterCount += expectAnyNumberOfSpaces();
		}
		while(characterCount > previousCharacterCount);

		return characterCount > 0;
	}

	private TagFilterValue parseTag() throws ParseException
	{
		String operator = null;
		String value = null;

		// !key at the start is translated to key!~.*
		if(input.nextIsAndAdvance('!'))
		{
			// Overpass understands "." to mean "any string". For a Java regex matcher, that would
			// be ".*"
			operator = "!~";
			value = ".*";
		}

		String key = parseKey();

		expectAnyNumberOfSpaces();

		String binaryOperator = parseOperator();
		if(binaryOperator != null)
		{
			if(operator == null)
				operator = binaryOperator;
			else
				throw new ParseException("Tried to use the '"+binaryOperator+"' operator while " +
						"already using the unary negation operator", input.getCursorPos());
		}

		// without operator, we do not expect a value
		if(operator != null && value == null)
		{
			expectAnyNumberOfSpaces();
			value = parseValue();
		}

		return new TagFilterValue(key, operator, value);
	}

	private String parseKey() throws ParseException
	{
		String reserved = nextIsReservedWord();
		if(reserved != null)
			throw new ParseException("A key cannot be named like the reserved word '" +
					reserved + "', you must surround the key with quotation marks",
					input.getCursorPos());

		int length = findKeyLength();
		if(length == 0)
		{
			throw new ParseException("Missing key (dangling boolean operator)", input.getCursorPos());
		}
		return input.advanceBy(length);
	}

	private String parseOperator()
	{
		for(String o : OPERATORS)
		{
			if(input.nextIsAndAdvance(o)) return o;
		}
		return null;
	}

	private String parseValue() throws ParseException
	{
		int length = findValueLength();
		if(length == 0)
		{
			throw new ParseException("Missing value (dangling operator)", input.getCursorPos());
		}
		return input.advanceBy(length);
	}

	private int findKeyLength() throws ParseException
	{
		Integer length = findQuotationLength();
		if(length != null) return length;

		length = Math.min(input.findNext(' '), input.findNext(')'));
		for(String o : OPERATORS)
		{
			int opLen = input.findNext(o);
			if(opLen < length) length = opLen;
		}
		return length;
	}

	private int findValueLength() throws ParseException
	{
		Integer length = findQuotationLength();
		if(length != null) return length;
		return Math.min(input.findNext(' '), input.findNext(')'));
	}

	private Integer findQuotationLength() throws ParseException
	{
		for (char quot : QUOTATION_MARKS)
		{
			if (input.nextIs(quot))
			{
				int length = input.findNext(quot,1);
				if(input.isAtEnd(length))
					throw new ParseException("Did not close quotation marks",input.getCursorPos()-1);
				// +1 because we want to include teh closing quotation mark
				return length+1;
			}
		}
		return null;
	}

	private String nextIsReservedWord()
	{
		for (String reserved : RESERVED_WORDS)
		{
			if(input.nextIsIgnoreCase(reserved)) return reserved;
		}
		return null;
	}
}
