package cz.juzna.intellij.kdyby.events.actions;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.ide.util.ChooseElementsDialog;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import cz.juzna.intellij.kdyby.events.EventListener;
import cz.juzna.intellij.kdyby.events.EventsUtil;
import cz.juzna.intellij.kdyby.events.NetteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class GenerateEventListenersAction extends CodeInsightAction {

	private static LanguageCodeInsightActionHandler HANDLER = new GenerateEventListenersHandler();

	protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
		return file.getLanguage().is(PhpLanguage.INSTANCE) && HANDLER.isValidFor(editor, file);
	}


	@NotNull
	@Override
	protected CodeInsightActionHandler getHandler() {
		return HANDLER;
	}

	private static class GenerateEventListenersHandler implements LanguageCodeInsightActionHandler {


		public boolean isValidFor(Editor editor, PsiFile file) {
			PhpClass phpClass = PhpCodeEditUtil.findClassAtCaret(editor, file);

			return phpClass != null ? EventsUtil.isClassSuitable(phpClass) : false;
		}

		@Override
		public void invoke(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile psiFile) {
			final PhpClass phpClass = PhpCodeEditUtil.findClassAtCaret(editor, psiFile);
			if (phpClass == null) {
				return;
			}
			List<EventListener> listeners = new ArrayList<EventListener>();
			for (EventListener listener : EventsUtil.findListeners(phpClass)) {
				if (listener.getMethod() == null && listener.getEvent() instanceof NetteEvent) {
					listeners.add(listener);
				}
			}

			final ChooseEventDialog dialog = new ChooseEventDialog(project, listeners, "FOo", "Bar");
			dialog.show();
			if (dialog.getExitCode() != 0) {
				return;
			}
			ApplicationManager.getApplication().runWriteAction(new Runnable() {
				@Override
				public void run() {

					for (EventListener listener : dialog.getChosenElements()) {
						Field field = ((NetteEvent) listener.getEvent()).resolveDeclaration(project);
						if (field == null) {
							continue;
						}
						String listenerTemplate = GeneratorUtil.createListenerTemplate(field, phpClass, listener.getMethodName());
						Method method = PhpCodeUtil.createMethodFromTemplate(phpClass, phpClass.getProject(), listenerTemplate);
						Method el = (Method) PhpCodeEditUtil.insertClassMember(phpClass, method);
					}
				}
			});

		}

		@Override
		public boolean startInWriteAction() {
			return false;
		}
	}

	private static class ChooseEventDialog extends ChooseElementsDialog<EventListener> {
		public ChooseEventDialog(Project project, List<? extends EventListener> list, String s, String s1) {
			super(project, list, s, s1);
		}

		@Override
		protected String getItemText(EventListener t) {
			return t.getEvent().getIdentifier();
		}

		@Nullable
		@Override
		protected Icon getItemIcon(EventListener t) {
			return null;
		}
	}


}
