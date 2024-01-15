package s3.ai.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class InferenceEngine {
	private final KnowledgeBase knowledgeBase;
	private final List<Rule> rules;

	public InferenceEngine(KnowledgeBase knowledgeBase, List<Rule> rules) {
		this.knowledgeBase = knowledgeBase;
		this.rules = rules;
	}

	public List<Term> inference() {
		List<Term> firedActions = new ArrayList<>();

		for (Rule rule : rules) {
			solve(rule, firedActions);
		}

		return firedActions;
	}

	private void solve(Rule rule, List<Term> firedActions) {
		Bindings solution = unification(rule.getPatterns());
		if (solution.isSatisfiable()) {
			triggerEffects(rule, solution, firedActions);
		}

	}

	private Bindings unification(List<Term> patterns) {
		Bindings bindings = new Bindings();
		return unification(patterns, 0, bindings);
	}

	private Bindings unification(List<Term> patterns, Integer patternIndex, Bindings bindings) {
		if (patternIndex == patterns.size()) {
			return bindings;

		} else {
			Term pattern = patterns.get(patternIndex).applyBindings(bindings);

			if (pattern.isMathExpression) {
				return solveMathExpression(pattern, patterns, patternIndex, bindings);

			} else if (pattern.isNegation) {
				return solveNegation(pattern, patterns, patternIndex, bindings);

			} else if (pattern.canUnite) {
				return solveRegularPattern(pattern, patterns, patternIndex, bindings);

			} else { // Unknown pattern type, this case should never be reach
				return new NoSolution();
			}
		}
	}

	private Bindings solveMathExpression(Term pattern, List<Term> patterns, int patternIndex, Bindings bindings) {
		if (pattern.mathExpressionHolds()) {
			// find solution for the rest patterns
			return unification(patterns, patternIndex + 1, bindings);

		} else {
			return new NoSolution();
		}
	}

	private Bindings solveNegation(Term pattern, List<Term> patterns, int patternIndex, Bindings bindings) {
		Term reversedPattern = pattern.reverse();

		boolean satisfiable = false;
		outerLoop: for (Term t : knowledgeBase.getTerms(reversedPattern.functor)) {

			for (int i = 0; i < reversedPattern.argSize(); i++) {
				Symbol argP = reversedPattern.getArg(i);
				Symbol argT = t.getArg(i);
				if ((!argP.isVariable() || argP.isBound()) && !argP.equals(argT)) {
					continue outerLoop;
				}
			}

			satisfiable = true;
			break;
		}

		if (satisfiable) { // existed at least one fact can make the reversed pattern be satisfied
			return new NoSolution();

		} else {
			// find solution for the rest patterns
			return unification(patterns, patternIndex + 1, bindings);
		}
	}

	private Bindings solveRegularPattern(Term pattern, List<Term> patterns, int patternIndex, Bindings bindings) {
		outerLoop: for (Term t : knowledgeBase.getTerms(pattern.functor)) {

			Bindings newBindings = new Bindings();

			for (int i = 0; i < pattern.argSize(); i++) {
				Symbol argP = pattern.getArg(i);
				Symbol argT = t.getArg(i);
				if (argP.isVariable() && !argP.isBound()) {
					newBindings.add(argP.getValue(), argT.getValue());
				} else if (!argP.equals(argT)) {
					continue outerLoop;
				}
			}

			Bindings updatedBindings = bindings.update(newBindings);
			// find solution for the rest patterns
			Bindings solution = unification(patterns, patternIndex + 1, updatedBindings);

			if (solution.isSatisfiable()) {
				updateIdleFacts(pattern, updatedBindings);
				return solution;
			} // else, current solution bindings fail in the rest patterns, continue outerLoop

		}
		return new NoSolution();
	}

	private void updateIdleFacts(Term pattern, Bindings updatedBindings) {
		// update idle units, one unit can't be assigned 2 tasks
		if (pattern.functor.contains("idle")) {
			knowledgeBase.remove(pattern.applyBindings(updatedBindings));
		}
	}

	private void triggerEffects(Rule rule, Bindings solution, List<Term> firedActions) {
		for (Term effect : rule.getEffects()) {
			Term updatedEffect = effect.applyBindings(solution);

			if (!updatedEffect.isAction) {
				knowledgeBase.addTerm(updatedEffect);

			} else {
				firedActions.add(updatedEffect);
				updatedResourceAvailability(rule.getPatterns(), solution);
			}
		}
	}

	private void updatedResourceAvailability(List<Term> patterns, Bindings solution) {
		// update resource availability,
		// avoid conflict between actions for snatching limited resources
		for (Term pattern : patterns) {
			for (String resource : new String[] { "wood", "gold" }) {
				if (Objects.equals(pattern.functor, resource + "NeededFor")) {
					int resourceConsumed = Integer.parseInt(pattern.applyBindings(solution).getArg(1).getValue());
					Symbol resourceFact = knowledgeBase.getTerms("woodAvailable").get(0).getArg(0);
					int resourceAvailable = Integer.parseInt(resourceFact.getValue());
					int resourceRemain = resourceAvailable - resourceConsumed;
					resourceFact.setValue(String.valueOf(resourceRemain));
				}
			}
		}
	}

}
