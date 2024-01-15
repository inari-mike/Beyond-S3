package s3.ai.rule;

import static java.lang.Character.isUpperCase;

import java.util.Objects;

public class Symbol {
	private final boolean isVariable;
	private boolean isBound;
	private String value;
	private String bindValue;

	public Symbol(String value) {
		this.value = value;
		this.isVariable = value.length() == 1 && isUpperCase(value.charAt(0));
		isBound = false;
	}

	public boolean isVariable() {
		return isVariable;
	}

	public boolean isBound() {
		return isBound;
	}

	public void bind(String bindValue) {
		if (isVariable && bindValue != null) {
			this.bindValue = bindValue;
			isBound = true;
		}
	}

	@Override
	@SuppressWarnings("MethodDoesntCallSuperMethod")
	public Symbol clone() {
		Symbol clonedSymbol = new Symbol(this.value);
		clonedSymbol.bind(this.bindValue);
		return clonedSymbol;
	}

	public String getValue() {
		if (isBound) { // use the bindValue instead of variable name
			return bindValue;
		} else {
			return value;
		}
	}

	public void setValue(String newValue) {
		value = newValue;
	}

	public void unbind() {
		isBound = false;
		bindValue = null;
	}

	public void applyBindings(Bindings bindings) {
		if (isVariable && bindings.contains(value)) {
			bindValue = bindings.get(value);
			isBound = true;
		}
	}

	public boolean equals(Symbol another) {
		return Objects.equals(getValue(), another.getValue());
	}

}
