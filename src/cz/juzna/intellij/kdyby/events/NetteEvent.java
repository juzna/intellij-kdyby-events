package cz.juzna.intellij.kdyby.events;


import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

public class NetteEvent extends Event {

	protected String className;

	protected String eventName;

	public NetteEvent(String className, String eventName) {
		super(className + "::" + eventName);
		this.className = className;
		this.eventName = eventName;
	}

	public String getClassName() {
		return className;
	}

	public String getEventName() {
		return eventName;
	}

	@Override
	public PsiElement resolveDeclaration(Project project) {
		return NetteEventUtils.getFieldByEvent(this, project);
	}
}
