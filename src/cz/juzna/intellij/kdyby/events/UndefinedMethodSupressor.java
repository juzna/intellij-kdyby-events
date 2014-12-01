package cz.juzna.intellij.kdyby.events;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.LanguageInspectionSuppressors;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.inspections.suppression.PhpInspectionSuppressor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.MethodReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matej21 on 9.11.14.
 */
public class UndefinedMethodSupressor implements InspectionSuppressor {

	InspectionSuppressor parentSuppressor;

	public UndefinedMethodSupressor() {
		List<InspectionSuppressor> suppressors = LanguageInspectionSuppressors.INSTANCE.forKey(PhpLanguage.INSTANCE);
		if(suppressors.size() > 1) {
			parentSuppressor = suppressors.get(1);
		}
	}

	@Override
	public boolean isSuppressedFor(PsiElement psiElement, String s) {
		if(s.equals("PhpUndefinedMethodInspection") && psiElement.getParent() instanceof MethodReference && psiElement.getText().startsWith("on")) {
			return true;
		} else if(parentSuppressor != null) {
			return parentSuppressor.isSuppressedFor(psiElement, s);
		}
		return false;
	}

	@Override
	public SuppressQuickFix[] getSuppressActions(PsiElement psiElement, String s) {
		if(parentSuppressor != null) {
			return parentSuppressor.getSuppressActions(psiElement, s);
		}
		return new SuppressQuickFix[0];
	}
}
