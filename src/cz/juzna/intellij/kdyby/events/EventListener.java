package cz.juzna.intellij.kdyby.events;

import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

public class EventListener {

	private Event event;

	private PhpClass containingClass;

	private String methodName;

	public EventListener(Event event, PhpClass clazz, String methodName)
	{
		this.event = event;
		this.containingClass = clazz;
		this.methodName = methodName;
	}

	@Nullable
	public Method getMethod() {
		return containingClass.findMethodByName(methodName);
	}

	public boolean hasListener()
	{
		return getMethod() != null;
	}

	public String getMethodName() {
		return methodName;
	}

	public PhpClass getContainingClass() {
		return containingClass;
	}

	public Event getEvent() {
		return event;
	}
}
