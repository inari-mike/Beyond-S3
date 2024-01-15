package s3.ai.rule;

import java.util.HashMap;

public class Bindings {
	private final HashMap<String, String> bindingMap;
	protected boolean satisfiable;

	public Bindings() {
		bindingMap = new HashMap<>();
		satisfiable = true;
	}

	public boolean isSatisfiable() {
		return satisfiable;
	}

	public void add(String variable, String value) {
		bindingMap.put(variable, value);
	}

	public String get(String variable) {
		return bindingMap.get(variable);
	}

	public boolean contains(String key) {
		return bindingMap.containsKey(key);
	}

	public Bindings update(Bindings another) {
		Bindings clonedBindings = new Bindings();
		clonedBindings.bindingMap.putAll(this.bindingMap);
		clonedBindings.bindingMap.putAll(another.bindingMap);
		return clonedBindings;
	}
}
