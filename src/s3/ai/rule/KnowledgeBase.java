package s3.ai.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class KnowledgeBase {
	private final Map<String, List<Term>> facts;

	public KnowledgeBase() {
		facts = new HashMap<>();
	}

	public void addTerm(Term t) {
		if (!facts.containsKey(t.functor)) {
			facts.put(t.functor, new ArrayList<>());
		}
		facts.get(t.functor).add(t);
	}

	public List<Term> getTerms(String functor) {
		List<Term> ts = facts.get(functor);
		return ts != null ? ts : new ArrayList<>();
	}

	public void clear() {
		facts.clear();
	}

	public void remove(Term t) {
		facts.get(t.functor).removeIf(fact -> fact.equals(t));
	}

}
