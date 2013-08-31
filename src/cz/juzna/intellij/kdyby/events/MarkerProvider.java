package cz.juzna.intellij.kdyby.events;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import icons.PhpIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;


/**
 * Pro eventy (fieldy) ukaze v gutteru rozklikavatelne pripojene handlery (metody)
 */
public class MarkerProvider extends RelatedItemLineMarkerProvider
{

	@Override
	protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
		if (element instanceof Field) {
			Field field = (Field) element;
			List<Method> methods = EventsUtil.findEventHandlers(element.getProject(), field);

			if (methods != null && methods.size() > 0) {
				int x = 1;
				NavigationGutterIconBuilder<PsiElement> builder =
						NavigationGutterIconBuilder.create(PhpIcons.TwigFileIcon).
								setTargets(methods).
								setTooltipText("Navigate to a attached event");
				result.add(builder.createLineMarkerInfo(element));
			}
		}
	}

}
