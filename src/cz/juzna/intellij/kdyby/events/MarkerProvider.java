package cz.juzna.intellij.kdyby.events;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Pro eventy (fieldy) ukaze v gutteru rozklikavatelne pripojene handlery (metody)
 */
public class MarkerProvider extends RelatedItemLineMarkerProvider {

	@Override
	protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
		// event declaration
		if (element instanceof Field) {
			Field field = (Field) element;
			addMarkers(element, result, field);
		}

		// event invocation
		if (element instanceof MethodReference) {
			MethodReference method = (MethodReference) element;

			for (String className : method.getClassReference().getType().getTypes()) {
				for (PhpClass clazz : PhpIndex.getInstance(element.getProject()).getClassesByFQN(className)) {
					Field field = clazz.findFieldByName(method.getName(), false);
					if (field != null) {
						addMarkers(element, result, field);
					}
				}
			}
		}
	}

	private void addMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result, Field field) {
		final List<Method> methods = new ArrayList<Method>();
		for (EventListener eventListener : EventsUtil.findListeners(field)) {
			methods.add(eventListener.getMethod());
		}
		if (methods != null && methods.size() > 0) {
			NavigationGutterIconBuilder<PsiElement> builder =
					NavigationGutterIconBuilder.create(Icons.EVENT_ICON).
							setTargets(methods).
							setTooltipText("Navigate to attached event");
			result.add(builder.createLineMarkerInfo(element));
		}
	}

}
