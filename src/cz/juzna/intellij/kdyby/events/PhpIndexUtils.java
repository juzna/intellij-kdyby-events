package cz.juzna.intellij.kdyby.events;


import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import java.util.ArrayList;
import java.util.Collection;

public class PhpIndexUtils {

	public static Collection<PhpClass> getClasses(PhpTypedElement element, Project project)
	{
		PhpIndex phpIndex = PhpIndex.getInstance(project);
		Collection<PhpClass> classes = new ArrayList<PhpClass>();
		for(String className : element.getType().getTypes()) {
			classes.addAll(phpIndex.getClassesByFQN(className));
		}

		return classes;
	}
}
