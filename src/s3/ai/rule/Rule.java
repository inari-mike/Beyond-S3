package s3.ai.rule;

import java.util.List;

public record Rule(List<Term> patterns, List<Term> effects) {
	public List<Term> getPatterns() {
		return patterns;
	}

	public List<Term> getEffects() {
		return effects;
	}

	public void reset() {
		for (Term pattern : patterns) {
			pattern.reset();
		}
		for (Term effect : effects) {
			effect.reset();
		}
	}
}
