package cz.juzna.intellij.kdyby.events;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;


public class SubscriberAnnotator implements Annotator {

	@Override
	public void annotate(PsiElement psiElement, AnnotationHolder annotationHolder) {
		if (psiElement.getParent() == null || !(psiElement.getParent() instanceof ArrayCreationExpression)) {
			return;
		}
		PsiElement el = psiElement instanceof ArrayHashElement ? ((ArrayHashElement) psiElement).getKey() : psiElement.getFirstChild();
		String eventName = ElementValueResolver.resolve(el);
		if (eventName == null) {
			return;
		}
		Event event = EventsUtil.createEvent(eventName);
		if (event instanceof NetteEvent) {
			if (!eventExists((NetteEvent) event, PhpIndex.getInstance(psiElement.getProject()))) {
				annotationHolder.createWarningAnnotation(psiElement, "Event " + event.getIdentifier() + " not found");
			}

		}

	}

	private static boolean eventExists(NetteEvent event, PhpIndex index) {
		for (PhpClass phpClass : index.getClassesByFQN(event.getClassName())) {
			if (phpClass.findFieldByName(event.getEventName(), false) != null) {
				return true;
			}
		}
		return false;
	}

}
