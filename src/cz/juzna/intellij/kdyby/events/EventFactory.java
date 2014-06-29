package cz.juzna.intellij.kdyby.events;


import com.jetbrains.php.lang.psi.elements.Field;

public class EventFactory {

	public static Event create(String eventName) {
		if (!eventName.contains("::")) {
			return new Event(eventName);
		}
		String[] tmp = eventName.split("::");
		if (tmp.length != 2) {
			return null;
		}
		String className = tmp[0].replace("\\\\", "\\"), fieldName = tmp[1];
		if (className.startsWith("\\")) {
			className = className.substring(1);
		}
		return new NetteEvent(className, fieldName);
	}

	public static Event create(Field field) {
		return create(field.getContainingClass().getFQN() + "::" + field.getName());
	}
}
