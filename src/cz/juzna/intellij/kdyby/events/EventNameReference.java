package cz.juzna.intellij.kdyby.events;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
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
}
