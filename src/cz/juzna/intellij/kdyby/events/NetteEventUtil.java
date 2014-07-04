package cz.juzna.intellij.kdyby.events;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class NetteEventUtil {

	public static Field getFieldByEvent(NetteEvent event, Project project) {
		for (PhpClass clazz : PhpIndex.getInstance(project).getClassesByFQN(event.getClassName())) {
			Field field = clazz.findFieldByName(event.getEventName(), false);
			if (field != null) {
				return field;
			}
		}

		return null;
	}

	public static Collection<NetteEvent> findEvents(Collection<PhpClass> classes) {
		List<NetteEvent> events = new ArrayList<NetteEvent>();
		for (PhpClass phpClass : classes) {
			events.addAll(findEvents(phpClass));
		}

		return events;

	}

	public static Collection<NetteEvent> findEvents(PhpClass phpClass) {
		List<NetteEvent> events = new ArrayList<NetteEvent>();
		for (Field field : phpClass.getOwnFields()) {
			if (!field.getName().startsWith("on")) {
				continue;
			}
			events.add(new NetteEvent(phpClass.getFQN().substring(1), field.getName()));
		}

		return events;
	}
}
