package cz.juzna.intellij.kdyby.events;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;


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
		Event event = EventFactory.create(eventName);
		if (event instanceof NetteEvent) {
			if (!(NetteEventUtils.getFieldByEvent((NetteEvent) event, psiElement.getProject()) != null)) {
				annotationHolder.createWarningAnnotation(psiElement, "Event " + event.getIdentifier() + " not found");
			}
		}
	}

}
