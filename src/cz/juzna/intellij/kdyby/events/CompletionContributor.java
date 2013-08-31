package cz.juzna.intellij.kdyby.events;


import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;


/**
 * Auto completion for event names, works only within Subscriber::getSubscribedEvents
 */
public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor
{
	public CompletionContributor() {
		extend(CompletionType.BASIC,
				PlatformPatterns.psiElement().withParent(StringLiteralExpression.class),
				new CompletionProvider<CompletionParameters>() {
					@Override
					protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {
						// if i'm within method getSubscribedEvents of a Subscriber?
						PsiElement el = completionParameters.getPosition();
						boolean isInSubscriber = false;
						while (el != null) {
							if (el instanceof Method) {
								if (((Method) el).getName().equals("getSubscribedEvents")) {
									isInSubscriber = true;
								}
							}

							el = el.getParent();
						}

						if (isInSubscriber) {
							PrefixMatcher prefixMatcher = resultSet.getPrefixMatcher();
							if (prefixMatcher.getPrefix().contains("::")) { // already after ::, must remove it for class name search
								prefixMatcher = prefixMatcher.cloneWithPrefix(prefixMatcher.getPrefix().split("::")[0]);
							}

							PhpIndex index = PhpIndex.getInstance(completionParameters.getPosition().getProject());
							for (String className : index.getAllClassNames(prefixMatcher)) {
								for (PhpClass clazz : index.getClassesByFQN(className)) {
									for (Field field : clazz.getOwnFields()) {
										if (field.getName().startsWith("on")) {
											resultSet.addElement(LookupElementBuilder.create(className + "::" + field.getName()));
										}
									}
								}
							}
						}
					}
				}
		);
	}

}
