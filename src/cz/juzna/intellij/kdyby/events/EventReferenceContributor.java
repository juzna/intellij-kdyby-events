package cz.juzna.intellij.kdyby.events;


import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;


/**
 * Click thru event subscriber to event definition
 */
public class EventReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
		registrar.registerReferenceProvider(PlatformPatterns.psiElement(PhpElementTypes.STRING), new PsiReferenceProvider() {
			@NotNull
			@Override
			public PsiReference[] getReferencesByElement(@NotNull PsiElement originalElement, @NotNull ProcessingContext processingContext) {
				if (!(originalElement instanceof StringLiteralExpression)) {
					return new PsiReference[0];
				}
				PsiElement element = EventsUtil.getEventIdentifierInArray(originalElement);
				if (element == null) {
					return new PsiReference[0];
				}

				String content = ElementValueResolver.resolve(element);
				if (content == null) {
					return new PsiReference[0];
				}
				Event event = EventsUtil.createEvent(content);
				if (event == null) {
					return new PsiReference[0];
				}

				return new PsiReference[]{new EventNameReference(originalElement, event)};


			}
		});
	}

}
