package s3.ai.rule;

import java.util.ArrayList;
import java.util.Objects;

class Term {
	public boolean isAction;
	public boolean isNegation;
	public boolean isMathExpression;
	public boolean canUnite;

	public String functor;
	private ArrayList<Symbol> arguments;

	public Term() {
		this.arguments = new ArrayList<>();
	}

	public Term(String functor, String... arguments) {
		isMathExpression = functor.contains("<") || functor.contains(">") || functor.contains("=");
		isNegation = !isMathExpression && '~' == functor.charAt(0);
		isAction = !isMathExpression && !isNegation && "do".equals(functor.substring(0, 2));
		canUnite = !isAction && !isNegation && !isMathExpression;

		this.functor = functor;
		this.arguments = new ArrayList<>();
		for (String arg : arguments) {
			this.arguments.add(new Symbol(arg));
		}
	}

	@Override
	@SuppressWarnings("MethodDoesntCallSuperMethod")
	public Term clone() { // only expected to be called by term.reverse() and term.applyBindings()
		Term clonedTerm = new Term();
		clonedTerm.isMathExpression = isMathExpression;
		clonedTerm.isNegation = isNegation;
		clonedTerm.isAction = isAction;
		clonedTerm.canUnite = canUnite;
		clonedTerm.functor = functor;
		clonedTerm.arguments = new ArrayList<>();
		for (Symbol arg : this.arguments) {
			clonedTerm.arguments.add(arg.clone());
		}
		return clonedTerm;
	}

	public Term reverse() {
		Term reversed = this.clone();
		reversed.isNegation = !isNegation;
		reversed.functor = isNegation ? functor.substring(1) : "~" + functor;
		return reversed;
	}

	public int argSize() {
		return arguments.size();
	}

	public Symbol getArg(int index) {
		return arguments.get(index);
	}

	public Term applyBindings(Bindings bindings) {
		Term clonedTerm = this.clone();
		for (Symbol arg : clonedTerm.arguments) {
			if (arg.isVariable() && !arg.isBound()) {
				arg.applyBindings(bindings);
			}
		}
		return clonedTerm;
	}

	public boolean mathExpressionHolds() {
		int leftOperand = Integer.parseInt(arguments.get(0).getValue());
		int rightOperand = Integer.parseInt(arguments.get(1).getValue());
		return switch (functor) {
		case ">" -> leftOperand > rightOperand;
		case "<" -> leftOperand < rightOperand;
		case "=" -> leftOperand == rightOperand;
		default -> false;
		};
	}

	public void reset() {
		for (Symbol arg : arguments) {
			if (arg.isVariable()) {
				arg.unbind();
			}
		}
	}

	public boolean equals(Term another) {
		if (!Objects.equals(this.functor, another.functor)) {
			return false;
		} else {
			if (this.argSize() == another.argSize()) {
				for (int i = 0; i < this.argSize(); i++) {
					if (!this.getArg(i).equals(another.getArg(i))) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
	}
}
