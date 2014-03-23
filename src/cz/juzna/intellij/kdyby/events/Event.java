package cz.juzna.intellij.kdyby.events;


import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

public class Event {

	protected String identifier;

	public Event(String identifier) {
		super();
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public PsiElement resolveDeclaration(Project project) {
		return null;
	}
}
