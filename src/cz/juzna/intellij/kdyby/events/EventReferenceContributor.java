package cz.juzna.intellij.kdyby.events;


import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;



/**
 * Click thru event subscriber to event definition
 */
public class EventReferenceContributor extends PsiReferenceContributor
{

	@Override
	public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
		registrar.registerReferenceProvider(PlatformPatterns.psiElement(PhpElementTypes.STRING), new PsiReferenceProvider() {
			@NotNull
			@Override
			public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
				// if i'm within method getSubscribedEvents of a Subscriber?
				PsiElement el = element;
				boolean isInSubscriber = false;
				while (el != null) {
					if (el instanceof Method) {
						if (((Method) el).getName().equals("getSubscribedEvents")) {
							isInSubscriber = true;
						}
					}

					el = el.getParent();
				}

				if (element instanceof StringLiteralExpression && isInSubscriber) {
					String contents = ((StringLiteralExpression) element).getContents();
					if (contents.contains("::")) {
						String[] tmp = contents.split("::");
						String className = tmp[0].replace("\\\\", "\\"), fieldName = tmp[1];

						return new PsiReference[] { new EventNameReference(element, className, fieldName) };
					}
				}

				return new PsiReference[0];
			}
		});
	}

}
