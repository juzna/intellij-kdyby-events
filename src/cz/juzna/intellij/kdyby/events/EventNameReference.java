package cz.juzna.intellij.kdyby.events;


import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Reference to an event name (i.e. to field declaration)
 */
public class EventNameReference extends PsiReferenceBase<PsiElement>
{

	private String className;
	private String fieldName;


	public EventNameReference(@NotNull PsiElement element, @NotNull String className, @NotNull String fieldName) {
		super(element);
		this.className = className;
		this.fieldName = fieldName;
	}


	@Nullable
	@Override
	public PsiElement resolve() {
		for (PhpClass clazz : PhpIndex.getInstance(myElement.getProject()).getClassesByFQN(className)) {
			Field field = clazz.findFieldByName(fieldName, false);
			if (field != null) {
				return field;
			}
		}

		return null;
	}


	@NotNull
	@Override
	public Object[] getVariants() {
		return PsiElement.EMPTY_ARRAY;
	}

}
