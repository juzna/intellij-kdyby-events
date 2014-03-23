package cz.juzna.intellij.kdyby.events;

import com.jetbrains.php.lang.psi.elements.Method;


public class EventListener {

	private Event event;

	private Method method;

	public EventListener(Event event, Method method) {
		this.event = event;
		this.method = method;
	}

	public Method getMethod() {
		return method;
	}

	public Event getEvent() {
		return event;
	}
}
