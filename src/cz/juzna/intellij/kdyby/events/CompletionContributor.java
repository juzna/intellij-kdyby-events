package cz.juzna.intellij.kdyby.events;


import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;


public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
	public CompletionContributor() {
		extend(CompletionType.BASIC,
				PlatformPatterns.psiElement().withParent(StringLiteralExpression.class),
				new EventsCompletionProvider()
		);
	}

	private class EventsCompletionProvider extends CompletionProvider {
		@Override
		protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {
			PsiElement originalPosition = completionParameters.getOriginalPosition().getParent();
			if (!(originalPosition instanceof StringLiteralExpression)) {
				return;
			}
			PsiElement element = EventsUtil.getEventIdentifierInArray(originalPosition);
			if (element == null) {
				return;
			}
			String eventName = ElementValueResolver.resolve(element);
			eventName = eventName.substring(eventName.startsWith("\\") ? 1 : 0);
			PhpIndex index = PhpIndex.getInstance(completionParameters.getPosition().getProject());
			Collection<PhpClass> classes;
			if (eventName.contains("::")) {
				classes = index.getClassesByFQN(eventName.split("::")[0]);
			} else {
				PrefixMatcher prefixMatcher = new CamelHumpMatcher(eventName);
				classes = new ArrayList<PhpClass>();
				for (String className : index.getAllClassNames(prefixMatcher)) {
					classes.addAll(index.getClassesByName(className));
				}
			}

			for (NetteEvent event : NetteEventUtils.findEvents(classes)) {
				LookupElementBuilder lookupElementBuilder = null;
				if (element instanceof StringLiteralExpression) {
					lookupElementBuilder = LookupElementBuilder.create(event.getClassName() + "::" + event.getEventName());
				} else if (((StringLiteralExpression) originalPosition).getContents().startsWith("::")
						&& eventName.startsWith(event.className + "::")) {
					lookupElementBuilder = LookupElementBuilder.create("::" + event.getEventName())
							.withPresentableText(event.getEventName());
				}
				if (lookupElementBuilder != null) {
					resultSet.addElement(lookupElementBuilder.withIcon(Icons.EVENT_ICON));
				}
			}
		}
	}

}
