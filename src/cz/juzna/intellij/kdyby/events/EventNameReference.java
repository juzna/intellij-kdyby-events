package cz.juzna.intellij.kdyby.events;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Reference to an event name (i.e. to field declaration)
 */
public class EventNameReference extends PsiReferenceBase<PsiElement> {
	private Event event;

	public EventNameReference(@NotNull PsiElement element, @NotNull Event event) {
		super(element);
		this.event = event;
	}

	@Override
	public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {

		getElement().accept(new PsiRecursiveElementVisitor() {
			@Override
			public void visitElement(PsiElement element) {
				if (element instanceof StringLiteralExpression
						&& ((StringLiteralExpression) element).getContents().contains("::")) {
					StringLiteralExpression expr = (StringLiteralExpression) element;
					String[] parts = expr.getContents().split("::");
					String className = parts[0].replace("\\\\", "\\");
					PsiElement arrayValue = getArrayValueAncestor(expr);
					if (arrayValue != null) {
						String code = "array('' => '" + parts[1] + "')";
						ArrayCreationExpression arr = (ArrayCreationExpression) PhpPsiElementFactory.createPhpPsiFromText(
								getElement().getProject(),
								PhpElementTypes.ARRAY_CREATION_EXPRESSION,
								code);
						ArrayHashElement el = arr.getHashElements().iterator().next();
						expr.updateText(className + "::" + newElementName);
						el.getKey().replace(arrayValue.getFirstChild());
						arrayValue.replace(el);
					} else {
						expr.updateText(className + "::" + newElementName);
					}
				} else {
					super.visitElement(element);
				}
			}
		});

		return this.getElement();
	}


	@Nullable
	@Override
	public PsiElement resolve() {
		return event.resolveDeclaration(getElement().getProject());
	}


	@NotNull
	@Override
	public Object[] getVariants() {
		return PsiElement.EMPTY_ARRAY;
	}

	@Override
	public TextRange getRangeInElement() {
		try {
			return super.getRangeInElement();
		} catch (Exception e) {
			return ApplicationManager.getApplication().runReadAction(new Computable<TextRange>() {
				@Override
				public TextRange compute() {
					return new TextRange(0, getElement().getText().length() - 1);
				}
			});
		}
	}

	public Event getEvent() {
		return event;
	}

	@Nullable
	private static PsiElement getArrayValueAncestor(StringLiteralExpression expr) {
		return PhpPsiUtil.getParentByCondition(expr, true, new Condition<PsiElement>() {
			@Override
			public boolean value(PsiElement element) {
				return element.getNode().getElementType().equals(PhpElementTypes.ARRAY_VALUE);
			}
		}, new Condition<PsiElement>() {
			@Override
			public boolean value(PsiElement element) {
				return element instanceof ArrayCreationExpression;
			}
		});
	}
}
