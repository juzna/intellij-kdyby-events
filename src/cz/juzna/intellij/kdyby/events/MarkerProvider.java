package cz.juzna.intellij.kdyby.events;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Pro eventy (fieldy) ukaze v gutteru rozklikavatelne pripojene handlery (metody)
 */
public class MarkerProvider extends RelatedItemLineMarkerProvider {

	PhpType doctrineEvm = new PhpType().add("Doctrine\\Common\\EventManager");
	PhpType symfonyEvm = new PhpType().add("Symfony\\Component\\EventDispatcher\\EventDispatcherInterface");


	@Override
	protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
		Event event = null;
		// event declaration
		if (element instanceof Field) {
			Field field = (Field) element;
			if (field.getName().startsWith("on")) {
				event = EventFactory.create(field);
			}
		}
		// event invocation
		if (element instanceof MethodReference) {
			event = getEventFromInvocation(element, (MethodReference) element);
		}
		if (event != null) {
			addMarkers(element, result, event);
		}
	}


	private Event getEventFromInvocation(PsiElement element, MethodReference method) {
		PhpExpression classReference = method.getClassReference();
		Event event = null;
		if (method.getName().startsWith("on")) {
			for (PhpClass clazz : PhpIndexUtil.getClasses(classReference, element.getProject())) {
				Field field = clazz.findFieldByName(method.getName(), false);
				if (field != null) {
					event = EventFactory.create(field);
				}
			}
		} else if ((method.getName().equals("dispatchEvent")
				&& doctrineEvm.isConvertibleFrom(classReference.getType(), PhpIndex.getInstance(element.getProject())))
				|| (method.getName().equals("dispatch")
				&& symfonyEvm.isConvertibleFrom(classReference.getType(), PhpIndex.getInstance(element.getProject())))) {

			String eventName = ElementValueResolver.resolve(method.getParameterList().getFirstChild());
			if (eventName != null) {
				event = EventFactory.create(eventName);
			}
		}
		return event;
	}

	private void addMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result, Event event) {
		final List<Method> methods = new ArrayList<Method>();
		for (EventListener eventListener : EventsUtil.findListeners(event, element.getProject())) {
			if(eventListener.getMethod() != null) {
				methods.add(eventListener.getMethod());
			}
		}
		if (methods.size() > 0) {
			NavigationGutterIconBuilder<PsiElement> builder =
					NavigationGutterIconBuilder.create(Icons.EVENT_ICON).
							setTargets(methods).
							setTooltipText("Navigate to attached event");
			result.add(builder.createLineMarkerInfo(element));
		}
	}

}
