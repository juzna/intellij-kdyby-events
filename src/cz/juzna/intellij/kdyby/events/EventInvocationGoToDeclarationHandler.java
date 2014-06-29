package cz.juzna.intellij.kdyby.events;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;


public class EventInvocationGoToDeclarationHandler extends GotoDeclarationHandlerBase {
	@Nullable
	@Override
	public PsiElement getGotoDeclarationTarget(PsiElement psiElement, Editor editor) {
		if (!(psiElement instanceof LeafPsiElement) || !(psiElement.getParent() instanceof MethodReference)) {
			return null;
		}
		String eventName = psiElement.getText();
		if (!eventName.startsWith("on")) {
			return null;
		}

		MethodReference methodReference = (MethodReference) psiElement.getParent();
		PhpIndex index = PhpIndex.getInstance(psiElement.getProject());
		Field field = null;
		for (String fqn : methodReference.getClassReference().getType().getTypes()) {
			for (PhpClass cls : index.getClassesByFQN(fqn)) {
				if (cls.findMethodByName(eventName) != null) {
					return null;
				}
				field = cls.findFieldByName(eventName, false);
			}
		}

		return field;
	}
}
