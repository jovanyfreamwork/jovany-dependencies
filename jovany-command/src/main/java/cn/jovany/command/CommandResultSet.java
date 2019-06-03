package cn.jovany.command;

import java.util.function.Function;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class CommandResultSet {

	private final String result;

	public CommandResultSet(String result) {
		this.result = result;
	}

	public <R, T> R on(T t, Function<T, R> error) {
		return error.apply(t);
	}
	
	public MatchResultSet regex(String regexDuration, int count) throws MalformedPatternException {
		PatternCompiler compiler = new Perl5Compiler();
		Pattern patternDuration = compiler.compile(regexDuration, Perl5Compiler.CASE_INSENSITIVE_MASK);
		PatternMatcher matcherDuration = new Perl5Matcher();
		if (matcherDuration.contains(result, patternDuration)) {
			return new MatchResultSet(matcherDuration.getMatch());
		}
		return new MatchResultSet(null);
	}
	
	@Override
	public String toString() {
		return result;
	}

}
