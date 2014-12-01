package cz.juzna.intellij.kdyby.events;

import com.jetbrains.php.lang.psi.elements.Method;
import org.jetbrains.annotations.Nullable;


public class EventListener {

	private Event event;

	private Method method;

	public EventListener(Event event, @Nullable Method method) {
		this.event = event;
		this.method = method;
	}

	@Nullable
	public Method getMethod() {
		return method;
	}

	public Event getEvent() {
		return event;
	}
}
