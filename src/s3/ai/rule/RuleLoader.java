package s3.ai.rule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RuleLoader {
	private final String filePath;
	private final List<Rule> rules;
	private final boolean loaded;

	public RuleLoader(String filePath) {
		this.filePath = filePath;
		rules = new ArrayList<>();
		loaded = false;
	}

	public List<Rule> getRules() {
		if (!loaded) {
			load();
		}
		return rules;
	}

	public void load() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();

			while (line != null) {
				if (!line.equals("") && '#' != line.charAt(0)) {
					buildRule(line.substring(0, line.length() - 1));
				}

				line = reader.readLine();
			}

			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void buildRule(String line) {
		String[] effectsAndPatterns = line.replaceAll("\\s+", "").split(":-");
		List<Term> effects = buildTerms(splitTerms(effectsAndPatterns[0]));
		List<Term> patterns = buildTerms(splitTerms(effectsAndPatterns[1]));
		rules.add(new Rule(patterns, effects));
	}

	private List<String> splitTerms(String termsStr) {
		List<String> terms = new ArrayList<>();
		StringBuilder newTerm = new StringBuilder();
		int stacksOfBracket = 0;
		for (char c : termsStr.toCharArray()) {
			if (c != ',') {
				newTerm.append(c);
				if (c == '(') {
					stacksOfBracket++;
				} else if (c == ')') {
					stacksOfBracket--;
				}
			} else {
				if (stacksOfBracket > 0) {
					newTerm.append(c);
				} else {
					terms.add(newTerm.toString());
					newTerm = new StringBuilder();
				}
			}
		}
		terms.add(newTerm.toString());
		return terms;
	}

	private List<Term> buildTerms(List<String> termsStrings) {
		List<Term> terms = new ArrayList<>();
		for (String term : termsStrings) {

			if (term.contains("(")) {
				String[] functorAndArguments = term.substring(0, term.length() - 1).split(Pattern.quote("("));
				String functor = functorAndArguments[0];
				String[] arguments = functorAndArguments.length == 1 ? new String[] {}
						: functorAndArguments[1].split(Pattern.quote(","));
				for (int i = 0; i < arguments.length; i++) {
					String arg = arguments[i];
					arguments[i] = '"' != arg.charAt(0) ? arg : arg.substring(1, arg.length() - 1);
				}
				terms.add(new Term(functor, arguments));

			} else {
				for (String operator : new String[] { ">", "<", "=" }) {
					if (term.contains(operator)) {
						String[] functorAndArguments = term.split(operator);
						String leftOperand = functorAndArguments[0];
						String rightOperand = functorAndArguments[1];
						terms.add(new Term(operator, leftOperand, rightOperand));
					}
				}
			}

		}
		return terms;
	}
}
