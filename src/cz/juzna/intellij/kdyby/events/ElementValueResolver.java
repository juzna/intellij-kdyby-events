package cz.juzna.intellij.kdyby.events;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;


public class ElementValueResolver {

	private PsiElement element;

	public ElementValueResolver(PsiElement element) {
		super();
		this.element = element;
	}

	public String resolve() {
		try {
			return doResolve(this.element);
		} catch (UnresolvableValueException e) {
			return null;
		}
	}

	public static String resolve(PsiElement element) {
		return (new ElementValueResolver(element)).resolve();
	}


	private String doResolve(PsiElement element) throws UnresolvableValueException {
		if (element instanceof StringLiteralExpression) {
			return ((StringLiteralExpression) element).getContents();
		} else if (element instanceof BinaryExpression && element.getNode().getElementType().equals(PhpElementTypes.CONCATENATION_EXPRESSION)) {
			BinaryExpression binaryExpression = (BinaryExpression) element;

			return doResolve(binaryExpression.getLeftOperand()) + doResolve(binaryExpression.getRightOperand());
		} else if (element instanceof ClassConstantReference) {
			ClassConstantReference constantReference = (ClassConstantReference) element;
			ClassReference classReference = (ClassReference) constantReference.getClassReference();
			if (constantReference.getLastChild() instanceof LeafPsiElement) {
				String constantName = constantReference.getLastChild().getText();
				if (constantName.equals("class")) {
					return classReference.getFQN();
				}
				for (PhpClass phpClass : PhpIndexUtils.getClasses(classReference, element.getProject())) {
					Field constant = phpClass.findFieldByName(constantName, true);
					if (constant != null && constant.isConstant()) {
						try {
							return doResolve(constant.getDefaultValue());
						} catch (UnresolvableValueException e) {
						}
					}
				}
			}
		}
		throw new UnresolvableValueException();

	}

	private class UnresolvableValueException extends Exception {
	}
}

