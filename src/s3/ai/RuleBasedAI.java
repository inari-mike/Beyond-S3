package s3.ai;

import java.util.ArrayList;
import java.util.List;

class Term {
	String functor;
	List<String> parameters;

	public Term(String f, List<String> p) {
		functor = f;
		parameters = p;
	}
}

class KnowledgeBase {
	List<Term> facts;

	void addTerm(Term t) {
		facts.add(t);
	}

	void clear() {
		facts.clear();
	}

}

class InferenceEngine {

	private List<Rule> rules;

	public InferenceEngine() {
		loadRules("src/s3/ai/rules-S3.txt");
	}

	private void loadRules(String filePath) {
		// load rules from the give file
	}

	public List<Term> getFiredRules(KnowledgeBase kb) {
		List<Term> firedRules = new ArrayList<>();
		firedRules.add(new Term("ownBase", new ArrayList<>()));
		return null;
//		return firedRules;
	}

	private static class Rule {
		Term[] patterns;
		Term[] effects;
		int effectType; // 0: add new fact, 1: do action

		public Rule(Term[] p, Term[] e, int type) {
			patterns = p;
			effects = e;
			effectType = type;
		}
	}

}
