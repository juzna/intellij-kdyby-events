package cz.juzna.intellij.kdyby.events;


import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Field;
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

		extend(CompletionType.BASIC,
				PlatformPatterns.psiElement().inside(ArrayCreationExpression.class)
						.andNot(PlatformPatterns.psiElement().withParent(StringLiteralExpression.class)),
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
			PsiElement position = completionParameters.getOriginalPosition();
			if (position == null) {
				return;
			}
			Project project = position.getProject();
			PhpLanguageLevel languageLevel = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();
			if (!languageLevel.hasFeature(PhpLanguageFeature.CLASS_NAME_CONST)) {
				return;
			}
			if (!EventsUtil.isInGetSubscribedEvents(position)) {
				return;
			}
			PhpIndex index = PhpIndex.getInstance(project);
			Collection<PhpClass> classes;
			classes = new ArrayList<PhpClass>();
			for (String className : index.getAllClassNames(resultSet.getPrefixMatcher())) {
				classes.addAll(index.getClassesByName(className));
			}

			for (NetteEvent event : NetteEventUtil.findEvents(classes)) {
				Field field = event.resolveDeclaration(project);
				if (field.getContainingClass() == null) {
					continue;
				}
				LookupElement lookupElement = createLookupElement(event, field.getContainingClass());
				resultSet.addElement(lookupElement);
			}
		}
	}

	private LookupElement createLookupElement(@NotNull NetteEvent event, @NotNull PhpClass phpClass) {
		String shortName = event.className.substring(event.className.lastIndexOf("\\") + 1);
		String lookupString = shortName + "::class . '::" + event.getShortName() + "'";
		return LookupElementBuilder.create(phpClass, lookupString)
				.withInsertHandler(PhpReferenceInsertHandler.getInstance())
				.withPresentableText(event.getIdentifier())
				.withIcon(Icons.EVENT_ICON)
				.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE);
	}


}
