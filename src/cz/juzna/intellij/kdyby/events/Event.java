package cz.juzna.intellij.kdyby.events;


import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Event {

	protected String identifier;

	public Event(String identifier) {
		super();
		this.identifier = identifier;
	}

	public String getShortName() {
		Matcher matcher = Pattern.compile("^([^\\w]?(?<namespace>.*\\w+)[^\\w]{1,2})?(?<name>[a-z]\\w+)$").matcher(identifier);
		if (matcher.matches()) {
			return matcher.group("name");
		}
		return identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public PsiElement resolveDeclaration(Project project) {
		return null;
	}

	public boolean equals(Event obj) {
		return this.identifier.equals(obj.getIdentifier());
	}
}
