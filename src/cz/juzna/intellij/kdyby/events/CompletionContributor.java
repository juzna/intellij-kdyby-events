package cz.juzna.intellij.kdyby.events;


import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;


public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
	public CompletionContributor() {
		extend(CompletionType.BASIC,
				PlatformPatterns.psiElement().withParent(StringLiteralExpression.class),
				new EventsCompletionProvider()
		);
		extend(CompletionType.BASIC,
				PlatformPatterns.psiElement(),
				new Php55EventsCompletionProvider()
				);
	}

	private class EventsCompletionProvider extends CompletionProvider {
		@Override
		protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {
			PsiElement originalPosition = completionParameters.getOriginalPosition().getParent();
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

			for (NetteEvent event : NetteEventUtil.findEvents(classes)) {
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

	private class Php55EventsCompletionProvider extends CompletionProvider {
		@Override
		protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {
			PsiElement originalPosition = completionParameters.getOriginalPosition();
			PhpIndex index = PhpIndex.getInstance(completionParameters.getPosition().getProject());
			Collection<PhpClass> classes;
			classes = new ArrayList<PhpClass>();
			for (String className : index.getAllClassNames(new CamelHumpMatcher(""))) {
				classes.addAll(index.getClassesByName(className));
			}

			for (NetteEvent event : NetteEventUtil.findEvents(classes)) {
				LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(event.className + "::class . '::" + event.getShortName() + "'")
						.withPresentableText(event.getIdentifier());
				resultSet.addElement(lookupElementBuilder.withIcon(Icons.EVENT_ICON));
			}
		}
	}
}
